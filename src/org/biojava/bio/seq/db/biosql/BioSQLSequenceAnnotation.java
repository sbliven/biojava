/* -*- c-basic-offset: 4; indent-tabs-mode: nil -*- */
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


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.biojava.bio.Annotation;
import org.biojava.bio.BioError;
import org.biojava.bio.BioRuntimeException;
import org.biojava.bio.SmallAnnotation;
import org.biojava.bio.seq.io.OrganismParser;
import org.biojava.bio.taxa.CircularReferenceException;
import org.biojava.bio.taxa.EbiFormat;
import org.biojava.bio.taxa.Taxon;
import org.biojava.bio.taxa.TaxonFactory;
import org.biojava.bio.taxa.WeakTaxonFactory;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;

/**
 * Annotation keyed off a BioSQL comment table
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @author Len Trigg
 * @since 1.3
 */
class BioSQLSequenceAnnotation implements Annotation {

    private BioSQLSequenceDB seqDB;
    private int bioentry_id;
    private Annotation underlyingAnnotation;

    int getBioentryID() {
	return bioentry_id;
    }

    BioSQLSequenceAnnotation(BioSQLSequenceDB seqDB,
			     int bioentry_id)
    {
	this.seqDB = seqDB;
	this.bioentry_id = bioentry_id;
    }

    /** Attempt to close the Statement. Continue on if there is a problem during the close. */
    static void attemptClose(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    /** Attempt to close the ResultSet. Continue on if there is a problem during the close. */
    static void attemptClose(ResultSet resultset) {
        if (resultset != null) {
            try {
                resultset.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }

    private void initAnnotations() {
	try {
	    Connection conn = seqDB.getPool().takeConnection();
	    underlyingAnnotation = new SmallAnnotation();
            underlyingAnnotation.setProperty("bioentry_id", new Integer(bioentry_id));

	    //
	    // Handle all the hacky special cases first
	    //
            initTaxon(conn);

	    //
	    // General-purpose tagvalue data.
	    //
	    if (seqDB.isBioentryPropertySupported()) {
		PreparedStatement get_properties = conn.prepareStatement(
			"select term.name as qn, bioentry_qualifier_value.value " + /*, bioentry_property.property_rank as rank " + */
			"  from bioentry_qualifier_value, term " +
			" where bioentry_qualifier_value.bioentry_id = ? " +
			"   and term.term_id = bioentry_qualifier_value.term_id " /* + */
			/* " order by qn, rank" */);
		get_properties.setInt(1, bioentry_id);
		ResultSet rs = get_properties.executeQuery();
		while (rs.next()) {
		    String key = rs.getString(1).trim();   // HACK due to stupid schema change
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
                rs.close();
                get_properties.close();
	    }
	    
	    seqDB.getPool().putConnection(conn);
	} catch (SQLException ex) {
	    throw new BioRuntimeException("Error fetching annotations", ex);
	} catch (ChangeVetoException ex) {
	    throw new BioError(ex);
	}
    }


    /**
     * Initialize the Taxon annotation. Reads from the taxon and
     * taxon_name table and stuffs the results into the annotation
     * object under key OrganismParser.PROPERTY_ORGANISM.
     *
     * @param conn the current <code>Connection</code>.
     */
    private void initTaxon(Connection conn) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            int taxon_id = 0;
            statement = conn.prepareStatement("select taxon_id " 
                                              + "from bioentry " 
                                              + "where bioentry_id = ? ");
            statement.setInt(1, bioentry_id);
            rs = statement.executeQuery();
            if (rs.next()) {
                taxon_id = rs.getInt(1);
                if (rs.wasNull()) {
                    taxon_id = 0;
                }
            }
            rs.close();
            statement.close();

            if (taxon_id != 0) {
                // We are expected to get a taxon structure

                // Constants for our wee id array
                final int NCBI_ID = 1;
                final int TAXON_ID = 0;

                // First, get the taxon ids up to the root.
                statement = conn.prepareStatement("select ncbi_taxon_id, parent_taxon_id " 
                                                  + "from taxon " 
                                                  + "where taxon_id = ? ");

                ArrayList path = new ArrayList();
                while (taxon_id != 0) {
                    statement.setInt(1, taxon_id);
                    rs = statement.executeQuery();
                    if (rs.next()) {
                        path.add(new int [] {taxon_id, rs.getInt(1)});
                        taxon_id = rs.getInt(2);
                        if (rs.wasNull()) {
                            taxon_id = 0;
                        }
                    } else {
                        throw new BioRuntimeException("Error fetching taxonomy structure. No taxon with taxon_id=" + taxon_id);
                    }
                    rs.close();
                }
                statement.close();

                // Traverse from the root down as far has has been created previously...
                TaxonFactory factory = WeakTaxonFactory.GLOBAL;
                Taxon taxon = factory.getRoot();
                int pos = path.size() - 1;
                int []ids = (int[]) path.get(pos--);
                Map names = getTaxonNames(conn, ids[TAXON_ID]);
                taxon.getAnnotation().setProperty(EbiFormat.PROPERTY_NCBI_TAXON, "" + ids[NCBI_ID]);
                taxon.getAnnotation().setProperty(EbiFormat.PROPERTY_TAXON_NAMES, names);
                for (; pos >= 0; pos--) {
                    // Who's the next id down the path?
                    ids = (int[]) path.get(pos);
                    String nextID = "" + ids[NCBI_ID];
                    // Now look among the children for the next child.
                    Set children = taxon.getChildren();
                    for (Iterator it = children.iterator(); it.hasNext(); ) {
                        Taxon child = (Taxon) it.next();
                        Annotation anno = child.getAnnotation();
                        if (anno.containsProperty(EbiFormat.PROPERTY_NCBI_TAXON)) {
                            String childID = (String) anno.getProperty(EbiFormat.PROPERTY_NCBI_TAXON);
                            if (childID.equals(nextID)) {
                                taxon = child;
                                continue;
                            }
                        } else {
                            throw new BioRuntimeException("Taxon has not been annotated with NCBI taxon ids.");
                        }
                    }
                    // No child with desired ncbi_id has been found.
                    break;
                }

                // Now create taxa from here on down.
                try {
                    for (; pos >= 0; pos--) {
                        // Now look for the next child.
                        ids = (int[]) path.get(pos);
                        String nextID = "" + ids[NCBI_ID];
                        names = getTaxonNames(conn, ids[TAXON_ID]);
                        String sciName = (String) names.get("scientific name");
                        if (sciName == null) {
                            throw new BioRuntimeException("No scientific name for taxon_id=" + ids[TAXON_ID]);
                        }
                        String commonName = (String) names.get("common name");
                        taxon = factory.addChild(taxon, factory.createTaxon(sciName, commonName));
                        taxon.getAnnotation().setProperty(EbiFormat.PROPERTY_NCBI_TAXON, nextID);
                        taxon.getAnnotation().setProperty(EbiFormat.PROPERTY_TAXON_NAMES, names);
                    }           
                } catch (CircularReferenceException ex) {
                    throw new BioRuntimeException("Circular references in taxon table. taxon_id=" + ids[TAXON_ID]);
                }
                underlyingAnnotation.setProperty(OrganismParser.PROPERTY_ORGANISM, taxon);
            }
	} catch (ChangeVetoException ex) {
	    throw new BioRuntimeException("Couldn't manipulate in-memory taxonomy", ex);
	} catch (SQLException ex) {
	    throw new BioRuntimeException("Error fetching taxonomy annotations", ex);
        } finally {
            attemptClose(rs);
            attemptClose(statement);
        }
    }


    /**
     * Look up all the names associated with a taxon_id.
     *
     * @param conn the current <code>Connection</code>.
     * @param taxon_id the NCBI taxon id for the taxon of interest.
     * @return a <code>Map</code> from name_class (e.g.: "scientific
     * name") to name.
     */
    private Map getTaxonNames(Connection conn, int taxon_id) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = conn.prepareStatement("select name_class, name " 
                                              + "from taxon_name " 
                                              + "where taxon_id = ? ");
            statement.setInt(1, taxon_id);
            rs = statement.executeQuery();

            Map names = new HashMap();
            while (rs.next()) {
                String name_class = rs.getString(1);
                String name = rs.getString(2);
                //System.err.println("Got " + name_class + "=" + name + " for taxon_id=" + taxon_id); 
                names.put(name_class, name);
            }

            return names;
	} catch (SQLException ex) {
	    throw new BioRuntimeException("Error fetching taxonomy annotations", ex);
        } finally {
            attemptClose(rs);
            attemptClose(statement);
        }
    }


    public Object getProperty(Object key) {
	if (underlyingAnnotation == null) {
	    initAnnotations();
	}

	return underlyingAnnotation.getProperty(key);
    }

    public void setProperty(Object key, Object value)
        throws ChangeVetoException
    {
        BioSQLEntryAnnotationChangeHub entryAnnotationHub = seqDB.getEntryAnnotationChangeHub();
        synchronized (entryAnnotationHub) {
            ChangeEvent cev = new ChangeEvent(this, Annotation.PROPERTY, key);
            entryAnnotationHub.firePreChange(cev);
            _setProperty(key, value);
            entryAnnotationHub.firePostChange(cev);
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
    
    public void removeProperty(Object key)
        throws ChangeVetoException
    {
        if (underlyingAnnotation == null) {
            initAnnotations();
        }
        if (!underlyingAnnotation.containsProperty(key)) {
            throw new NoSuchElementException("Annotation doesn't contain property " + key.toString());
        }
        
        BioSQLEntryAnnotationChangeHub entryAnnotationHub = seqDB.getEntryAnnotationChangeHub();
        synchronized (entryAnnotationHub) {
            ChangeEvent cev = new ChangeEvent(this, Annotation.PROPERTY, key);
            entryAnnotationHub.firePreChange(cev);
            underlyingAnnotation.removeProperty(key);
            persistProperty(key, null);
            entryAnnotationHub.firePostChange(cev);
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
	    throw new BioRuntimeException("Error adding BioSQL tables" + 
					(rolledback ? " (rolled back successfully)" : ""), ex);
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

    
    public void addChangeListener(ChangeListener cl) {
	addChangeListener(cl, ChangeType.UNKNOWN);
    }
    
    public void addChangeListener(ChangeListener cl, ChangeType ct) {
	seqDB.getEntryAnnotationChangeHub().addListener(new Integer(bioentry_id), cl, ct);
    }

    public void removeChangeListener(ChangeListener cl) {
	removeChangeListener(cl, ChangeType.UNKNOWN);
    }

    public void removeChangeListener(ChangeListener cl, ChangeType ct) {
	seqDB.getEntryAnnotationChangeHub().removeListener(new Integer(bioentry_id), cl, ct);
    }

    public boolean isUnchanging(ChangeType ct) {
	return false;
    }
}
