/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.bio.seq.db.biosql;

import java.sql.*;
import java.util.*;

import org.biojava.utils.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.taxa.*;

/**
 * Annotation keyed off a BioSQL comment table
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.3
 */

class BioSQLSequenceAnnotation implements Annotation {
    private BioSQLSequenceDB seqDB;
    private int bioentry_id;
    private Annotation underlyingAnnotation;
    private ChangeSupport changeSupport;

    private void initChangeSupport() {
	changeSupport = new ChangeSupport();
    }

    BioSQLSequenceAnnotation(BioSQLSequenceDB seqDB,
			     int bioentry_id)
    {
	this.seqDB = seqDB;
	this.bioentry_id = bioentry_id;
    }

    private void initAnnotations() {
	try {
	    Connection conn = seqDB.getPool().takeConnection();
	    underlyingAnnotation = new SmallAnnotation();

	    //
	    // Handle all the hacky special cases first
	    //
      
	    PreparedStatement get_taxa = conn.prepareStatement(
			"select taxa.full_lineage, taxa.common_name, taxa.ncbi_taxa_id " +
			"from bioentry_taxa, taxa " +
			"where bioentry_taxa.bioentry_id = ? and " +
			"      bioentry_taxa.taxa_id = taxa.taxa_id "
			                                      );
	    get_taxa.setInt(1, bioentry_id);
	    ResultSet rs = get_taxa.executeQuery();
	    if (rs.next()) {
		Taxa taxa = EbiFormat.getInstance().parse(WeakTaxaFactory.GLOBAL, rs.getString(1));
		taxa.setCommonName(rs.getString(2));
		taxa.getAnnotation().setProperty(
						 EbiFormat.PROPERTY_NCBI_TAXA,
						 String.valueOf(rs.getInt(3))
						 );
		underlyingAnnotation.setProperty(OrganismParser.PROPERTY_ORGANISM, taxa);
	    }

	    //
	    // General-purpose tagvalue data.
	    //

	    if (seqDB.isBioentryPropertySupported()) {
		PreparedStatement get_properties = conn.prepareStatement(
			"select ontology_term.term_name as qn, bioentry_qualifier_value.qualifier_value " + /*, bioentry_property.property_rank as rank " + */
			"  from bioentry_qualifier_value, ontology_term " +
			" where bioentry_qualifier_value.bioentry_id = ? " +
			"   and ontology_term.ontology_term_id = bioentry_qualifier_value.ontology_term_id " /* + */
			/* " order by qn, rank" */);
		get_properties.setInt(1, bioentry_id);
		rs = get_properties.executeQuery();
		while (rs.next()) {
		    String key = rs.getString(1);
		    String value = rs.getString(2);
		    if (underlyingAnnotation.containsProperty(key)) {
			Object current = underlyingAnnotation.getProperty(key);
			Collection coll;
			if (! (current instanceof Collection)) {
			    coll = new ArrayList();
			    coll.add(current);
			    underlyingAnnotation.setProperty(key, coll);
			} else {
			    coll = (Collection) current;
			}
			coll.add(value);
		    } else {
			underlyingAnnotation.setProperty(key, value);
		    }
		}
	    }
	    
	    seqDB.getPool().putConnection(conn);
	} catch (SQLException ex) {
	    throw new BioRuntimeException(ex, "Error fetching annotations");
	} catch (ChangeVetoException ex) {
	    throw new BioError(ex);
	} catch (CircularReferenceException ex) {
	    throw new BioError(ex);
	}
    }

    public Object getProperty(Object key)
        throws NoSuchElementException
    {
	if (underlyingAnnotation == null) {
	    initAnnotations();
	}

	return underlyingAnnotation.getProperty(key);
    }

    public void setProperty(Object key, Object value)
        throws ChangeVetoException
    {
	if (changeSupport == null) {
	    _setProperty(key, value);
	} else {
	    synchronized (changeSupport) {
		ChangeEvent cev = new ChangeEvent(this, Annotation.PROPERTY, key);
		changeSupport.firePreChangeEvent(cev);
		_setProperty(key, value);
		changeSupport.firePostChangeEvent(cev);
	    }
	}
    }

    private void _setProperty(Object key, Object value) 
        throws ChangeVetoException
    {
	persistProperty(key, value);
	if (underlyingAnnotation != null) {
	    underlyingAnnotation.setProperty(key, value);
	}
    }

    private void persistProperty(Object key, Object value)
        throws ChangeVetoException
    {
	Connection conn = null;
	try {
	    conn = seqDB.getPool().takeConnection();
	    conn.setAutoCommit(false);

	    seqDB.persistBioentryProperty(conn, bioentry_id, key, value, true, false);
	    
	    conn.commit();
	    seqDB.getPool().putConnection(conn);
	} catch (SQLException ex) {
	    boolean rolledback = false;
	    if (conn != null) {
		try {
		    conn.rollback();
		    rolledback = true;
		} catch (SQLException ex2) {}
	    }
	    throw new BioRuntimeException(ex, "Error adding BioSQL tables" + (rolledback ? " (rolled back successfully)" : ""));
	}
    }

    public boolean containsProperty(Object key) {
	if (underlyingAnnotation == null) {
	    initAnnotations();
	}

	return underlyingAnnotation.containsProperty(key);
    }

    public Set keys() {
	if (underlyingAnnotation == null) {
	    initAnnotations();
	}

	return underlyingAnnotation.keys();
    }

    public Map asMap() {
	if (underlyingAnnotation == null) {
	    initAnnotations();
	}

	return Collections.unmodifiableMap(underlyingAnnotation.asMap());
    }

    // 
    // Changeable
    //

    public void addChangeListener(ChangeListener cl) {
	addChangeListener(cl, ChangeType.UNKNOWN);
    }
	
    public void addChangeListener(ChangeListener cl, ChangeType ct) {
	if (changeSupport == null) {
	    initChangeSupport();
	}

	changeSupport.addChangeListener(cl, ct);
    }

    public void removeChangeListener(ChangeListener cl) {
	removeChangeListener(cl, ChangeType.UNKNOWN);
    }

    public void removeChangeListener(ChangeListener cl, ChangeType ct) {
	if (changeSupport != null) {
	    changeSupport.removeChangeListener(cl, ct);
	}
    }
}
