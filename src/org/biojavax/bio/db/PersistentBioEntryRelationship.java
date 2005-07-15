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

/*
 * PersistentBioEntryRelationship.java
 *
 * Created on June 16, 2005, 2:07 PM
 */

package org.biojavax.bio.db;
import java.sql.SQLException;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.BioEntryRelationship;
import org.biojavax.bio.SimpleBioEntryRelationship;
import org.biojavax.ontology.ComparableTerm;

/**
 * Represents a relationship between two bioentries that is described by a term.
 * 
 * Equality is the combination of unique subject, object and term.
 *
 * @author Richard Holland
 * @author Mark Schreiber
 */
public abstract class PersistentBioEntryRelationship extends SimpleBioEntryRelationship implements Persistent {
        
    private int status;
    private int uid;
    private PersistentBioDB db;
    
    private PersistentBioEntryRelationship(PersistentBioDB db, BioEntry object, BioEntry subject, ComparableTerm term) {
        super((PersistentBioEntry)db.convert(object), (PersistentBioEntry)db.convert(subject), (PersistentComparableTerm)db.convert(term));
        this.status = Persistent.UNMODIFIED;
        this.uid = Persistent.UID_UNKNOWN;
        this.db = db;
    }
    
    protected PersistentBioEntryRelationship(PersistentBioDB db, BioEntryRelationship br) {
        this(db, br.getObject(), br.getSubject(), br.getTerm());
    }
    
    public PersistentBioDB getDB() {
        return this.db;
    }
    
    public int getStatus() {
        return this.status;
    }
    
    public int getUid() {
        return this.uid;
    }
    
    public void setStatus(int status) throws IllegalArgumentException {
        if (status!=Persistent.UNMODIFIED && status!=Persistent.MODIFIED && status!=Persistent.DELETED)
            throw new IllegalArgumentException("Invalid status code");
        this.status = status;
    }
    
    public void setUid(int uid) {
        this.uid = uid;
    }
    
    public abstract Persistent load(Object[] vars) throws SQLException;
    
    public abstract boolean remove(Object[] vars) throws SQLException;
    
    public abstract Persistent store(Object[] vars) throws SQLException;
    
}
