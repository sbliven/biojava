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
 * PersistentComparableTerm.java
 *
 * Created on July 13, 2005, 10:22 AM
 */

package org.biojavax.bio.db;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.biojava.ontology.Ontology;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.ontology.ComparableTerm;
import org.biojavax.ontology.SimpleComparableTerm;

/**
 * A Term object that can be compared and thus sorted.
 *
 * Equality is inherited from Term.Impl.
 *
 * @author Richard Holland
 */
public abstract class PersistentComparableTerm extends SimpleComparableTerm implements Persistent {
    
    private int status;
    private int uid;
    private PersistentBioDB db;
    
    protected PersistentComparableTerm(PersistentBioDB db, Ontology o, String name, String description, Object[] synonyms) {
        super((PersistentComparableOntology)db.convert(o),name,description,synonyms);
        this.status = Persistent.UNMODIFIED;
        this.uid = Persistent.UID_UNKNOWN;
        this.db = db;
    }
    
    protected PersistentComparableTerm(PersistentBioDB db, ComparableTerm t) {
        this(db,t.getOntology(), t.getName(), t.getDescription(), t.getSynonyms());
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
        if (status==Persistent.UNMODIFIED) {
            this.addedSynonyms.clear();
            this.removedSynonyms.clear();
        }
    }
    
    public void setUid(int uid) {
        this.uid = uid;
    }    
    
    public void setIdentifier(String identifier) throws ChangeVetoException {
        super.setIdentifier(identifier);
        this.setStatus(Persistent.MODIFIED);
    }
    
    public void setObsolete(boolean obsolete) throws ChangeVetoException {
        super.setObsolete(obsolete);
        this.setStatus(Persistent.MODIFIED);
    }
    
    public abstract Persistent load(Object[] vars) throws Exception;
    
    public abstract boolean remove(Object[] vars) throws Exception;
    
    public abstract Persistent store(Object[] vars) throws Exception;
    
    // Contains all names removed since the last commit, EXCEPT those which
    // were added AND removed since the last commit. It is up to the subclass
    // to reset this else it could get inconsistent.
    private Set removedSynonyms = new HashSet();
    protected Set getRemovedSynonyms() { return Collections.unmodifiableSet(this.removedSynonyms); }
    // Contains all names added since the last commit, EXCEPT those which
    // were added AND removed since the last commit. It is up to the subclass
    // to reset this else it could get inconsistent.
    private Set addedSynonyms = new HashSet();
    protected Set getAddedSynonyms() { return Collections.unmodifiableSet(this.addedSynonyms); }
    
    public void removeSynonym(Object synonym) {
        super.removeSynonym(synonym);
        this.addedSynonyms.remove(synonym);
        this.removedSynonyms.add(synonym);
        this.status = Persistent.MODIFIED;
    }
    
    public void addSynonym(Object synonym) {
        super.addSynonym(synonym);
        this.removedSynonyms.remove(synonym);
        this.addedSynonyms.add(synonym);
        this.status = Persistent.MODIFIED;
    }
}