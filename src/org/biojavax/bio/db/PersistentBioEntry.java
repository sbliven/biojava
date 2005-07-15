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
 * PersistentBioEntry.java
 *
 * Created on June 16, 2005, 10:29 AM
 */

package org.biojavax.bio.db;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.ontology.AlreadyExistsException;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.CrossRef;
import org.biojavax.LocatedDocumentReference;
import org.biojavax.Namespace;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.BioEntryRelationship;
import org.biojavax.bio.SimpleBioEntry;

/**
 * Reference implementation of a BioEntry object which has no features or sequence. *
 *
 * Equality is the combination of namespace, name, accession and version.
 *
 * @author Richard Holland
 * @author Mark Schreiber
 */
public abstract class PersistentBioEntry extends SimpleBioEntry implements Persistent {
    
    private int status;
    private int uid;
    private PersistentBioDB db;
    
    protected PersistentBioEntry(PersistentBioDB db, Namespace ns, String name, String accession, int version, SymbolList symList, double seqversion) {
        super((PersistentNamespace)db.convert(ns), name, accession, version, symList, seqversion);
        this.status = Persistent.UNMODIFIED;
        this.uid = Persistent.UID_UNKNOWN;
        this.db = db;
    }
    
    protected PersistentBioEntry(PersistentBioDB db, BioEntry b) {
        this(db, b.getNamespace(), b.getName(), b.getAccession(), b.getVersion(), b, b.getSeqVersion());
        try {
            this.setDescription(b.getDescription());
            this.setDivision(b.getDivision());
            this.setIdentifier(b.getIdentifier());
            this.setSeqVersion(b.getSeqVersion());
            this.setTaxon(b.getTaxon());
            List rels = b.getBioEntryRelationships();
            for (int i = 0; i < rels.size(); i++) this.setBioEntryRelationship((BioEntryRelationship)rels.get(i),i);
            List coms = b.getComments();
            for (int i = 0; i < coms.size(); i++) this.setComment((String)coms.get(i),i);
            List xrefs = b.getCrossRefs();
            for (int i = 0; i < xrefs.size(); i++) this.setCrossRef((CrossRef)xrefs.get(i),i);
            List drefs = b.getDocRefs();
            for (int i = 0; i < drefs.size(); i++) this.setDocRef((LocatedDocumentReference)drefs.get(i),i);
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
    }
    
    public void setUid(int uid) {
        this.uid = uid;
    }
    
    public abstract Persistent load(Object[] vars) throws SQLException;
    
    public abstract boolean remove(Object[] vars) throws SQLException;
    
    public abstract Persistent store(Object[] vars) throws SQLException;
    
    public void setSeqVersion(double seqVersion) throws ChangeVetoException {
        super.setSeqVersion(seqVersion);
        this.status = Persistent.MODIFIED;
    }
    
    public void setTaxon(org.biojavax.bio.taxa.NCBITaxon taxon) throws ChangeVetoException {
        super.setTaxon((PersistentNCBITaxon)this.db.convert(taxon));
        this.status = Persistent.MODIFIED;
    }
    
    public void setDescription(String description) throws ChangeVetoException {
        super.setDescription(description);
        this.status = Persistent.MODIFIED;
    }
    
    public void setDivision(String division) throws ChangeVetoException {
        super.setDivision(division);
        this.status = Persistent.MODIFIED;
    }
    
    public void setIdentifier(String identifier) throws ChangeVetoException {
        super.setIdentifier(identifier);
        this.status = Persistent.MODIFIED;
    }
    
    // Contains all names removed since the last commit, EXCEPT those which
    // were added AND removed since the last commit. It is up to the subclass
    // to reset this else it could get inconsistent.
    private Set alteredComments = new HashSet();
    protected Set getAlteredComments() { return Collections.unmodifiableSet(this.alteredComments); }
    protected void resetAlteredComments() { this.alteredComments.clear(); }
    
    public void setComment(String comment, int index) throws AlreadyExistsException, ChangeVetoException {
        super.setComment(comment, index);
        this.alteredComments.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
    }
    
    public boolean removeComment(int index) throws IndexOutOfBoundsException, ChangeVetoException {
        boolean retValue = super.removeComment(index);
        this.alteredComments.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
        return retValue;
    }
    
    public boolean removeComment(String comment) throws ChangeVetoException {
        int index = this.getComments().indexOf(comment);
        boolean retValue = super.removeComment(comment);
        this.alteredComments.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
        return retValue;
    }
    
    public int addComment(String comment) throws AlreadyExistsException, ChangeVetoException {
        int index = super.addComment(comment);
        this.alteredComments.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
        return index;
    }
    
    // Contains all names removed since the last commit, EXCEPT those which
    // were added AND removed since the last commit. It is up to the subclass
    // to reset this else it could get inconsistent.
    private Set alteredCrossrefs = new HashSet();
    protected Set getAlteredCrossrefs() { return Collections.unmodifiableSet(this.alteredCrossrefs); }
    protected void resetAlteredCrossrefs() { this.alteredCrossrefs.clear(); }
    
    public boolean removeCrossRef(CrossRef crossref) throws ChangeVetoException {
        int index = this.getCrossRefs().indexOf(crossref);
        boolean retValue = super.removeCrossRef(crossref);
        this.alteredCrossrefs.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
        return retValue;
    }
    
    public int addCrossRef(CrossRef crossref) throws AlreadyExistsException, ChangeVetoException {
        int index = super.addCrossRef(crossref);
        this.alteredCrossrefs.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
        return index;
    }
    
    public boolean removeCrossRef(int index) throws IndexOutOfBoundsException, ChangeVetoException {
        boolean retValue = super.removeCrossRef(index);
        this.alteredCrossrefs.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
        return retValue;
    }
    
    public void setCrossRef(CrossRef crossref, int index) throws AlreadyExistsException, ChangeVetoException {
        super.setCrossRef(crossref, index);
        this.alteredCrossrefs.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
    }  
    
    // Contains all names removed since the last commit, EXCEPT those which
    // were added AND removed since the last commit. It is up to the subclass
    // to reset this else it could get inconsistent.
    private Set alteredDocrefs = new HashSet();
    protected Set getAlteredDocrefs() { return Collections.unmodifiableSet(this.alteredDocrefs); }
    protected void resetAlteredDocrefs() { this.alteredDocrefs.clear(); }
    
    public void setDocRef(LocatedDocumentReference docref, int index) throws AlreadyExistsException, ChangeVetoException {
        super.setDocRef(docref, index);
        this.alteredDocrefs.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
    }
    
    public int addDocRef(LocatedDocumentReference docref) throws AlreadyExistsException, ChangeVetoException {
        int index = super.addDocRef(docref);
        this.alteredDocrefs.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
        return index;
    }
    
    public boolean removeDocRef(LocatedDocumentReference docref) throws ChangeVetoException {
        int index = this.getDocRefs().indexOf(docref);
        boolean retValue = super.removeDocRef(docref);
        this.alteredDocrefs.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
        return retValue;
    }
    
    public boolean removeDocRef(int index) throws IndexOutOfBoundsException, ChangeVetoException {
        boolean retValue = super.removeDocRef(index);
        this.alteredDocrefs.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
        return retValue;
    }
    
    // Contains all names removed since the last commit, EXCEPT those which
    // were added AND removed since the last commit. It is up to the subclass
    // to reset this else it could get inconsistent.
    private Set alteredRelations = new HashSet();
    protected Set getAlteredRelations() { return Collections.unmodifiableSet(this.alteredRelations); }
    protected void resetAlteredRelations() { this.alteredRelations.clear(); }
    
    public void setBioEntryRelationship(BioEntryRelationship relationship, int index) throws AlreadyExistsException, ChangeVetoException {
        super.setBioEntryRelationship(relationship, index); 
        this.alteredRelations.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
    }
    
    public boolean removeBioEntryRelationship(int index) throws IndexOutOfBoundsException, ChangeVetoException {  
        boolean retValue = super.removeBioEntryRelationship(index);
        this.alteredRelations.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
        return retValue;
    }
    
    public boolean removeBioEntryRelationship(BioEntryRelationship relationship) throws ChangeVetoException {
        int index = this.getBioEntryRelationships().indexOf(relationship);
        boolean retValue = super.removeBioEntryRelationship(relationship);
        this.alteredRelations.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
        return retValue;
    }
    
    public int addBioEntryRelationship(BioEntryRelationship relationship) throws AlreadyExistsException, ChangeVetoException {
        int index = super.addBioEntryRelationship(relationship);
        this.alteredRelations.add(Integer.valueOf(index));
        this.status = Persistent.MODIFIED;
        return index;
    }
    
    // Contains all names removed since the last commit, EXCEPT those which
    // were added AND removed since the last commit. It is up to the subclass
    // to reset this else it could get inconsistent.
    private Set addedFeatures = new HashSet();
    protected Set getAddedFeatures() { return Collections.unmodifiableSet(this.addedFeatures); }
    protected void resetAddedFeatures() { this.addedFeatures.clear(); }
    // Contains all names removed since the last commit, EXCEPT those which
    // were added AND removed since the last commit. It is up to the subclass
    // to reset this else it could get inconsistent.
    private Set removedFeatures = new HashSet();
    protected Set getRemovedFeatures() { return Collections.unmodifiableSet(this.removedFeatures); }
    protected void resetRemovedFeatures() { this.removedFeatures.clear(); }
    
    public void removeFeature(Feature f) throws ChangeVetoException, BioException {
        super.removeFeature(f);
        this.addedFeatures.remove(f);
        this.removedFeatures.add(f);
        this.status = Persistent.MODIFIED;
    }
    
    public Feature createFeature(Feature.Template ft) throws BioException, ChangeVetoException {
        Feature retValue = super.createFeature(ft);
        this.addedFeatures.add(retValue);
        this.removedFeatures.remove(retValue);
        this.status = Persistent.MODIFIED;
        return retValue;
    }
    
}
