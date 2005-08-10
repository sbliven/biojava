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
 * SimpleBioEntry.java
 *
 * Created on June 16, 2005, 10:29 AM
 */

package org.biojavax.bio;

import java.util.Collections;
import java.util.TreeSet;
import java.util.Set;
import org.biojava.bio.Annotation;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeForwarder;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.Namespace;
import org.biojavax.RankedCrossRef;
import org.biojavax.RankedDocRef;
import org.biojavax.RichAnnotation;
import org.biojavax.SimpleRichAnnotation;
import org.biojavax.bio.taxa.NCBITaxon;
import org.biojavax.Comment;

/**
 * Reference implementation of a BioEntry object which has no features or sequence.
 * Equality is the combination of namespace, name, accession and version.
 * @author Richard Holland
 * @author Mark Schreiber
 */
public class SimpleBioEntry extends AbstractChangeable implements BioEntry {
    
    private Set comments = new TreeSet();
    private Set rankedcrossrefs = new TreeSet();
    private Set rankeddocrefs = new TreeSet();
    private Set relationships = new TreeSet();
    private String description;
    private String division;
    private String identifier;
    private String name;
    private String accession;
    private int version;
    private NCBITaxon taxon;
    private Namespace ns;
    private RichAnnotation notes = new SimpleRichAnnotation();
    private ChangeForwarder annFor;
    
    
    /**
     * Creates a new feature holding bioentry.
     * @param ns The namespace for this new bioentry.
     * @param name The name for this new bioentry.
     * @param accession The accession for this new bioentry.
     * @param version The version for this new bioentry.
     */
    public SimpleBioEntry(Namespace ns, String name, String accession, int version) {
        if (name==null) throw new IllegalArgumentException("Name cannot be null");
        if (accession==null) throw new IllegalArgumentException("Accession cannot be null");
        if (ns==null) throw new IllegalArgumentException("Namespace cannot be null");
        this.description = null;
        this.division = null;
        this.identifier = null;
        this.name = name;
        this.accession = accession;
        this.version = version;
        this.taxon = null;
        this.ns = ns;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleBioEntry() {}
    
    /**
     * {@inheritDoc}
     */
    public Set getRankedCrossRefs() { return Collections.unmodifiableSet(this.rankedcrossrefs); }
    
    /**
     * {@inheritDoc}
     */
    public void setTaxon(NCBITaxon taxon) throws ChangeVetoException {
        if(!this.hasListeners(BioEntry.TAXON)) {
            this.taxon = taxon;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    BioEntry.TAXON,
                    taxon,
                    this.taxon
                    );
            ChangeSupport cs = this.getChangeSupport(BioEntry.TAXON);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.taxon = taxon;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Annotation getAnnotation() { return this.notes; }
    
    /**
     * {@inheritDoc}
     */
    public Set getNoteSet() { return this.notes.getNoteSet(); }
    
    /**
     * {@inheritDoc}
     */
    public void setNoteSet(Set notes) throws ChangeVetoException { this.notes.setNoteSet(notes); }
    
    /**
     * {@inheritDoc}
     */
    public Set getComments() { return Collections.unmodifiableSet(this.comments); }
    
    /**
     * {@inheritDoc}
     */
    public Set getRankedDocRefs() { return Collections.unmodifiableSet(this.rankeddocrefs); }
    
    /**
     * {@inheritDoc}
     */
    public Set getRelationships() { return Collections.unmodifiableSet(this.relationships); }
    
    /**
     * {@inheritDoc}
     */
    public void setIdentifier(String identifier) throws ChangeVetoException {
        if(!this.hasListeners(BioEntry.IDENTIFIER)) {
            this.identifier = identifier;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    BioEntry.IDENTIFIER,
                    identifier,
                    this.identifier
                    );
            ChangeSupport cs = this.getChangeSupport(BioEntry.IDENTIFIER);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.identifier = identifier;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void setDivision(String division) throws ChangeVetoException {
        if(!this.hasListeners(BioEntry.DIVISION)) {
            this.division = division;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    BioEntry.DIVISION,
                    division,
                    this.division
                    );
            ChangeSupport cs = this.getChangeSupport(BioEntry.DIVISION);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.division = division;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void setDescription(String description) throws ChangeVetoException {
        if(!this.hasListeners(BioEntry.DESCRIPTION)) {
            this.description = description;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    BioEntry.DESCRIPTION,
                    description,
                    this.description
                    );
            ChangeSupport cs = this.getChangeSupport(BioEntry.DESCRIPTION);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.description = description;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String getAccession() { return this.accession; }
    
    /**
     * {@inheritDoc}
     */
    public String getDescription() { return this.description; }
    
    /**
     * {@inheritDoc}
     */
    public String getDivision() { return this.division; }
    
    /**
     * {@inheritDoc}
     */
    public String getIdentifier() { return this.identifier; }
    
    /**
     * {@inheritDoc}
     */
    public String getName() { return this.name; }
    
    /**
     * {@inheritDoc}
     */
    public Namespace getNamespace() { return this.ns; }
    
    /**
     * {@inheritDoc}
     */
    public NCBITaxon getTaxon() { return this.taxon; }
    
    /**
     * {@inheritDoc}
     */
    public int getVersion() { return this.version; }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj==null || !(obj instanceof BioEntry)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.ns==null) return false;
        // Normal comparison
            BioEntry them = (BioEntry)obj;
            return (this.ns.equals(them.getNamespace()) &&
                    this.name.equals(them.getName()) &&
                    this.accession.equals(them.getAccession()) &&
                    this.version==them.getVersion());
    }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        // Hibernate comparison - we haven't been populated yet
        if (this.ns==null) return -1;
        // Normal comparison
        BioEntry them = (BioEntry)o;
        if (!this.ns.equals(them.getNamespace())) return this.ns.compareTo(them.getNamespace());
        if (!this.name.equals(them.getName())) return this.name.compareTo(them.getName());
        if (!this.accession.equals(them.getAccession())) return this.accession.compareTo(them.getAccession());
        return this.version-them.getVersion();
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.ns==null) return code;
        // Normal comparison
        code = 37*code + this.ns.hashCode();
        code = 37*code + this.name.hashCode();
        code = 37*code + this.accession.hashCode();
        code = 37*code + this.version;
        return code;
    }
    
    /**
     * {@inheritDoc}
     * Form: <code>this.getNamespace()+": "+this.getName()+"/"+this.getAccession()+" v."+this.getVersion();</code>
     */
    public String toString() { return this.getNamespace()+": "+this.getName()+"/"+this.getAccession()+" v."+this.getVersion(); }
        
    /**
     * {@inheritDoc}
     */
    public void addRankedCrossRef(RankedCrossRef crossref) throws ChangeVetoException {
        if (crossref==null) throw new IllegalArgumentException("Crossref cannot be null");
        if(!this.hasListeners(BioEntry.RANKEDCROSSREF)) {
            this.rankedcrossrefs.add(crossref);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    BioEntry.RANKEDCROSSREF,
                    crossref,
                    null
                    );
            ChangeSupport cs = this.getChangeSupport(BioEntry.RANKEDCROSSREF);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.rankedcrossrefs.add(crossref);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeRankedCrossRef(RankedCrossRef crossref) throws ChangeVetoException {
        if (crossref==null) throw new IllegalArgumentException("Crossref cannot be null");
        if(!this.hasListeners(BioEntry.RANKEDCROSSREF)) {
            this.rankedcrossrefs.remove(crossref);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    BioEntry.RANKEDCROSSREF,
                    null,
                    crossref
                    );
            ChangeSupport cs = this.getChangeSupport(BioEntry.RANKEDCROSSREF);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.rankedcrossrefs.remove(crossref);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void addRankedDocRef(RankedDocRef docref) throws ChangeVetoException {
        if (docref==null) throw new IllegalArgumentException("Docref cannot be null");
        if(!this.hasListeners(BioEntry.RANKEDDOCREF)) {
            this.rankeddocrefs.add(docref);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    BioEntry.RANKEDDOCREF,
                    docref,
                    null
                    );
            ChangeSupport cs = this.getChangeSupport(BioEntry.RANKEDDOCREF);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.rankeddocrefs.add(docref);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeRankedDocRef(RankedDocRef docref) throws ChangeVetoException {
        if (docref==null) throw new IllegalArgumentException("Docref cannot be null");
        if(!this.hasListeners(BioEntry.RANKEDDOCREF)) {
            this.rankeddocrefs.remove(docref);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    BioEntry.RANKEDDOCREF,
                    null,
                    docref
                    );
            ChangeSupport cs = this.getChangeSupport(BioEntry.RANKEDDOCREF);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.rankeddocrefs.remove(docref);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void addComment(Comment comment) throws ChangeVetoException {
        if (comment==null) throw new IllegalArgumentException("Comment cannot be null");
        if(!this.hasListeners(BioEntry.COMMENT)) {
            this.comments.add(comment);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    BioEntry.COMMENT,
                    comment,
                    null
                    );
            ChangeSupport cs = this.getChangeSupport(BioEntry.COMMENT);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.comments.add(comment);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeComment(Comment comment) throws ChangeVetoException {
        if (comment==null) throw new IllegalArgumentException("Comment cannot be null");
        if(!this.hasListeners(BioEntry.COMMENT)) {
            this.comments.remove(comment);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    BioEntry.COMMENT,
                    null,
                    comment
                    );
            ChangeSupport cs = this.getChangeSupport(BioEntry.COMMENT);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.comments.remove(comment);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void addRelationship(BioEntryRelationship relation) throws ChangeVetoException {
        if (relation==null) throw new IllegalArgumentException("Relationship cannot be null");
        if(!this.hasListeners(BioEntry.RELATIONS)) {
            this.relationships.add(relation);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    BioEntry.RELATIONS,
                    relation,
                    null
                    );
            ChangeSupport cs = this.getChangeSupport(BioEntry.RELATIONS);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.relationships.add(relation);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeRelationship(BioEntryRelationship relation) throws ChangeVetoException {
        if (relation==null) throw new IllegalArgumentException("Relationship cannot be null");
        if(!this.hasListeners(BioEntry.RELATIONS)) {
            this.relationships.remove(relation);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    BioEntry.RELATIONS,
                    null,
                    relation
                    );
            ChangeSupport cs = this.getChangeSupport(BioEntry.RELATIONS);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.relationships.remove(relation);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    // Hibernate requirement - not for public use.
    private void setRelationships(Set relationships) { 
        this.relationships.clear();
        if (relationships!=null) this.relationships.addAll(relationships); 
    }
    
    // Hibernate requirement - not for public use.
    private void setNamespace(Namespace ns) { this.ns = ns; }
    
    // Hibernate requirement - not for public use.
    private void setName(String name) { this.name = name; }
    
    // Hibernate requirement - not for public use.
    private void setAccession(String acc) { this.accession = acc; }
    
    // Hibernate requirement - not for public use.
    private void setVersion(int v) { this.version = v; }
    
    // Hibernate requirement - not for public use.
    private void setRankedDocRefs(Set docrefs) { 
        this.rankeddocrefs.clear();
        if (docrefs!=null) this.rankeddocrefs.addAll(docrefs); 
    }
    
    // Hibernate requirement - not for public use.
    private void setComments(Set comments) { 
        this.comments.clear();
        if (comments!=null) this.comments.addAll(comments); 
    }
    
    // Hibernate requirement - not for public use.
    public void setRankedCrossRefs(Set rankedcrossrefs) { 
        this.rankedcrossrefs.clear();
        if (rankedcrossrefs!=null) this.comments.addAll(rankedcrossrefs); 
    }
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    // Hibernate requirement - not for public use.
    private Long getId() { return this.id; }
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) { this.id = id; }
    
}

