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
 * ComparableTriple.java
 *
 * Created on July 11, 2005, 10:54 AM
 */

package org.biojavax.bio.db;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.biojava.bio.BioError;
import org.biojava.ontology.AlreadyExistsException;
import org.biojava.ontology.Ontology;
import org.biojava.ontology.Term;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.ontology.ComparableTerm;
import org.biojavax.ontology.ComparableTriple;
import org.biojavax.ontology.SimpleComparableTriple;


/**
 * Basic comparable triple, BioSQL style.
 *
 * Equality is a unique combination of ontology, predicate, object and subject.
 *
 * @author Richard Holland
 */
public abstract class PersistentComparableTriple extends SimpleComparableTriple implements Persistent {
    
    private int status;
    private int uid;
    private PersistentBioDB db;
    
    protected PersistentComparableTriple(PersistentBioDB db, Ontology onto, Term subject, Term object, Term predicate) {
        super((PersistentComparableOntology)db.convert(onto),
                (PersistentComparableTerm)db.convert(subject),
                (PersistentComparableTerm)db.convert(object),
                (PersistentComparableTerm)db.convert(predicate));
        this.status = Persistent.UNMODIFIED;
        this.uid = Persistent.UID_UNKNOWN;
        this.db = db;
    }
    
    protected PersistentComparableTriple(PersistentBioDB db, ComparableTriple t) {
        this(db, t.getOntology(), t.getSubject(), t.getObject(), t.getPredicate());
        try {
            for (Iterator i = t.getDescriptors().iterator(); i.hasNext(); ) this.addDescriptor((ComparableTerm)i.next());
        } catch (ChangeVetoException e) {
            throw new BioError("Whoops! Parent class does not understand its own data!");
        } catch (AlreadyExistsException e) {
            throw new BioError("Reality alert! Duplicates found in duplicate-free set!");
        }
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
            this.addedDescriptors.clear();
            this.removedDescriptors.clear();
        }
    }
    
    public void setUid(int uid) {
        this.uid = uid;
    }
    
    public abstract Persistent load(Object[] vars) throws Exception;
    
    public abstract boolean remove(Object[] vars) throws Exception;
    
    public abstract Persistent store(Object[] vars) throws Exception;
    
    public void removeSynonym(Object synonym) {
        super.removeSynonym(synonym);
        this.status = Persistent.MODIFIED;
    }
    
    public void addSynonym(Object synonym) {
        super.addSynonym(synonym);
        this.status = Persistent.MODIFIED;
    }
    
    // Contains all names removed since the last commit, EXCEPT those which
    // were added AND removed since the last commit. It is up to the subclass
    // to reset this else it could get inconsistent.
    private Set removedDescriptors = new HashSet();
    protected Set getRemovedDescriptors() { return Collections.unmodifiableSet(this.removedDescriptors); }
    // Contains all names added since the last commit, EXCEPT those which
    // were added AND removed since the last commit. It is up to the subclass
    // to reset this else it could get inconsistent.
    private Set addedDescriptors = new HashSet();
    protected Set getAddedDescriptors() { return Collections.unmodifiableSet(this.addedDescriptors); }
    
    public boolean removeDescriptor(ComparableTerm desc) throws IllegalArgumentException, ChangeVetoException {
        boolean retValue;
        retValue = super.removeDescriptor(desc);
        this.addedDescriptors.remove(desc);
        this.removedDescriptors.add(desc);
        this.status = Persistent.MODIFIED;
        return retValue;
    }
    
    public void addDescriptor(ComparableTerm desc) throws AlreadyExistsException, IllegalArgumentException, ChangeVetoException {
        PersistentComparableTerm pt = (PersistentComparableTerm)this.db.convert(desc);
        pt.setStatus(Persistent.MODIFIED);
        super.addDescriptor(pt);
        this.removedDescriptors.remove(pt);
        this.addedDescriptors.add(pt);
        this.status = Persistent.MODIFIED;
    }
    
}
