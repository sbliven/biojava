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

/**
 * Annotation keyed off a BioSQL comment table
 *
 * @author Thomas Down
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

	    PreparedStatement get_annotations = conn.prepareStatement("select comment_text from comment where bioentry_id = ?");
	    get_annotations.setInt(1, bioentry_id);
	    ResultSet rs = get_annotations.executeQuery();
	    
	    underlyingAnnotation = new SmallAnnotation();
	    while (rs.next()) {
		String value = rs.getString(1);
		String key = "comment";
		if (value.startsWith("(")) {
		    int closeBracket = value.indexOf(')');
		    if (closeBracket > 0) {
			key = value.substring(1, closeBracket);
			value = value.substring(closeBracket + 1);
		    }
		}
		try {
		    underlyingAnnotation.setProperty(key, value);
		} catch (ChangeVetoException ex) {
		    throw new BioError(ex);
		}
	    }

	    get_annotations.close();
	    seqDB.getPool().putConnection(conn);
	} catch (SQLException ex) {
	    throw new BioRuntimeException(ex, "Error fetching annotations");
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
	if (underlyingAnnotation != null) {
	    underlyingAnnotation.setProperty(key, value);
	}
	persistProperty(key, value);
    }

    private void persistProperty(Object key, Object value)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("FIXME");
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
