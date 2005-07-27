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
import java.util.HashSet;

import java.util.Iterator;
import java.util.Set;

import org.biojava.bio.Annotatable;

import org.biojava.bio.Annotation;

import org.biojava.utils.AbstractChangeable;

import org.biojava.utils.ChangeEvent;

import org.biojava.utils.ChangeForwarder;

import org.biojava.utils.ChangeSupport;

import org.biojava.utils.ChangeVetoException;

import org.biojavax.Namespace;

import org.biojavax.bio.taxa.NCBITaxon;
import org.biojavax.ontology.ComparableTerm;



/**
 *
 * Reference implementation of a BioEntry object which has no features or sequence. *
 *
 *
 *
 * Equality is the combination of namespace, name, accession and version.
 *
 *
 *
 * @author Richard Holland
 *
 * @author Mark Schreiber
 *
 */

public class SimpleBioEntry extends AbstractChangeable implements BioEntry {
    
    
    
    /**
     *
     * The comments for this entry.
     *
     */
    
    private Set comments = new HashSet();
    
    /**
     *
     * The crossrefs for this entry.
     *
     */
    
    private Set crossrefs = new HashSet();
    
    /**
     *
     * The docrefs for this entry.
     *
     */
    
    private Set docrefs = new HashSet();
    
    /**
     *
     * The relationships for this entry.
     *
     */
    
    private Set relationships = new HashSet();
    
    /**
     *
     * The relationships for this entry.
     *
     */
    
    private String description;
    
    /**
     *
     * The division for this entry.
     *
     */
    
    private String division;
    
    /**
     *
     * The identifier for this entry.
     *
     */
    
    private String identifier;
    
    /**
     *
     * The name for this entry.
     *
     */
    
    private String name;
    
    /**
     *
     * The accession for this entry.
     *
     */
    
    private String accession;
    
    /**
     *
     * The version for this entry.
     *
     */
    
    private int version;
    
    /**
     *
     * The taxon for this entry.
     *
     */
    
    private NCBITaxon taxon;
    
    /**
     *
     * The namespace for this entry.
     *
     */
    
    private Namespace ns;
    
    /**
     *
     * The annotation for this entry.
     *
     */
    
    private BioEntryAnnotation ann = new SimpleBioEntryAnnotation();
    
    /**
     *
     * The event forwarder for this entry.
     *
     */
    
    private ChangeForwarder annFor;
    
    
    
    /**
     *
     * Creates a new feature holding bioentry.
     *
     * @param ns The namespace for this new bioentry.
     *
     * @param name The name for this new bioentry.
     *
     * @param accession The accession for this new bioentry.
     *
     * @param version The version for this new bioentry.
     *
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
        
        // construct the forwarder so that it emits Annotatable.ANNOTATION ChangeEvents
        
        // for the Annotation.PROPERTY events it will listen for
        
        this.annFor = new ChangeForwarder.Retyper(this, super.getChangeSupport(Annotatable.ANNOTATION), Annotatable.ANNOTATION);
        
        // connect the forwarder so it listens for Annotation.PROPERTY events
        
        this.ann.addChangeListener(this.annFor, Annotation.PROPERTY);
        
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleBioEntry() {}
    
    
    /**
     *
     * Should return the associated annotation object.
     *
     *
     *
     * @return an Annotation object, never null
     *
     */
    
    public Annotation getAnnotation() {
        
        return this.ann;
        
    }
    
    
    
    // Hibernate requirement - not for public use.
    private class Note {
        private ComparableTerm term;
        private String value;
        private int rank;
        private BioEntry f;
        private Note() {}
        private Note(ComparableTerm term, String value, int rank, BioEntry f) {
            this.term = term;
            this.value = value;
            this.rank = rank;
            this.f = f;
        }
        private void setTerm(ComparableTerm term) { this.term = term; }
        private ComparableTerm getTerm() { return this.term; }
        private void setValue(String value) { this.value = value; }
        private String getValue() { return this.value; }
        private void setRank(int rank) { this.rank = rank; }
        private int getRank() { return this.rank; }
        private void setBioEntry(BioEntry f) { this.f = f; }
        private BioEntry getBioEntry() { return this.f; }
    }
    // Hibernate requirement - not for public use.
    private void setAnnotationSet(Set ann) throws ChangeVetoException {
        this.ann.clear();
        for (Iterator i = ann.iterator(); i.hasNext(); ) {
            Note n = (Note)i.next();
            this.ann.setProperty(new SimpleBioEntryAnnotation.RankedTerm(n.getTerm(),n.getRank()),n.getValue());
        }
    }
    // Hibernate requirement - not for public use.
    private Set getAnnotationSet() {
        Set ns = new HashSet();
        for (Iterator i = this.ann.keys().iterator(); i.hasNext(); ) {
            SimpleBioEntryAnnotation.RankedTerm rt = (SimpleBioEntryAnnotation.RankedTerm)i.next();
            ComparableTerm ct = rt.getTerm();
            int rank = rt.getRank();
            String v = (String)this.ann.getProperty(ct);
            Note n = new Note(ct,v,rank,this);
            ns.add(n);
        }
        return ns;
    }
    
    public Set getCrossRefs() { return this.crossrefs; }
    
    // Hibernate requirement - not for public use.
    private void setCrossRefs(Set crossRefs) { this.crossrefs = crossrefs; }
    
    
    /**
     *
     * Setter for property taxon.
     *
     * @param taxon New value of property taxon.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
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
    
    public Set getComments() { return this.comments; }
    
    // Hibernate requirement - not for public use.
    private void setComments(Set comments) { this.comments = comments; }
    
    public Set getDocRefs() { return this.docrefs; }
    
    // Hibernate requirement - not for public use.
    private void setDocRefs(Set docrefs) { this.docrefs = docrefs; }
    
    public Set getBioEntryRelationships() { return this.relationships; }
    
    // Hibernate requirement - not for public use.
    private void setBioEntryRelationships(Set relationships) { this.relationships = relationships; }
       
    
    // Hibernate requirement - not for public use.
    private void setNamespace(Namespace ns) { this.ns = ns; }

    // Hibernate requirement - not for public use.
    private void setName(String name) { this.name = name; }

    // Hibernate requirement - not for public use.
    private void setAccession(String acc) { this.accession = acc; }

    // Hibernate requirement - not for public use.
    private void setVersion(int v) { this.version = v; }

    /**
     *
     * Setter for property identifier.
     *
     * @param identifier New value of property identifier.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
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
     *
     * Setter for property division.
     *
     * @param division New value of property division.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
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
     *
     * Setter for property description.
     *
     * @param description New value of property description.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
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
     *
     * Getter for property accession.
     *
     * @return Value of property accession.
     *
     */
    
    public String getAccession() {
        
        return this.accession;
        
    }
    
        
    /**
     *
     * Getter for property description.
     *
     * @return Value of property description.
     *
     */
    
    public String getDescription() {
        
        return this.description;
        
    }
    
    
    
    /**
     *
     * Getter for property division.
     *
     * @return Value of property division.
     *
     */
    
    public String getDivision() {
        
        return this.division;
        
    }
    
    
    
    /**
     *
     * Getter for property identifier.
     *
     * @return Value of property identifier.
     *
     */
    
    public String getIdentifier() {
        
        return this.identifier;
        
    }
    
    
    
    /**
     *
     * Getter for property name.
     *
     * @return Value of property name.
     *
     */
    
    public String getName() {
        
        return this.name;
        
    }
    
    
    
    /**
     *
     * Getter for property namespace.
     *
     * @return Value of property namespace.
     *
     */
    
    public Namespace getNamespace() {
        
        return this.ns;
        
    }
    
    
    
    /**
     *
     * Getter for property taxon.
     *
     * @return Value of property taxon.
     *
     */
    
    public NCBITaxon getTaxon() {
        
        return this.taxon;
        
    }
    
    
    
    /**
     *
     * Getter for property version.
     *
     * @return Value of property version.
     *
     */
    
    public int getVersion() {
        
        return this.version;
        
    }
    
    
    
    /**
     *
     * Indicates whether some other object is "equal to" this one. Equality is
     *
     * the combination of namespace, name, accession and version.
     *
     * @param   obj   the reference object with which to compare.
     *
     * @return  <code>true</code> if this object is the same as the obj
     *
     *          argument; <code>false</code> otherwise.
     *
     * @see     #hashCode()
     *
     * @see     java.util.Hashtable
     *
     */
    
    public boolean equals(Object obj) {
        
        if (this == obj) return true;
        
        if (obj==null || !(obj instanceof BioEntry)) return false;
        
        else {
            
            BioEntry them = (BioEntry)obj;
            
            return (this.getNamespace().equals(them.getNamespace()) &&
                    
                    this.getName().equals(them.getName()) &&
                    
                    this.getAccession().equals(them.getAccession()) &&
                    
                    this.getVersion()==them.getVersion());
            
        }
        
    }
    
    
    
    /**
     *
     * Compares this object with the specified object for order.  Returns a
     *
     * negative integer, zero, or a positive integer as this object is less
     *
     * than, equal to, or greater than the specified object.
     *
     * @return a negative integer, zero, or a positive integer as this object
     *
     * 		is less than, equal to, or greater than the specified object.
     *
     * @param o the Object to be compared.
     *
     */
    
    public int compareTo(Object o) {
        
        BioEntry them = (BioEntry)o;
        
        if (!this.getNamespace().equals(them.getNamespace())) return this.getNamespace().compareTo(them.getNamespace());
        
        if (!this.getName().equals(them.getName())) return this.getName().compareTo(them.getName());
        
        if (!this.getAccession().equals(them.getAccession())) return this.getAccession().compareTo(them.getAccession());
        
        return this.getVersion()-them.getVersion();
        
    }
    
    
    
    /**
     *
     * Returns a hash code value for the object. This method is
     *
     * supported for the benefit of hashtables such as those provided by
     *
     * <code>Hashtable</code>.
     *
     * @return  a hash code value for this object.
     *
     * @see     java.lang.Object#equals(java.lang.Object)
     *
     * @see     java.util.Hashtable
     *
     */
    
    public int hashCode() {
        
        int code = 17;
        
        code = 37*code + this.getNamespace().hashCode();
        
        code = 37*code + this.getName().hashCode();
        
        code = 37*code + this.getAccession().hashCode();
        
        code = 37*code + this.getVersion();
        
        return code;
        
    }
    
    
    
    /**
     *
     * Returns a string representation of the object of the form
     *
     * <code>this.getNamespace()+": "+this.getName()+"/"+this.getAccession()+" v."+this.getVersion();</code>
     *
     * @return  a string representation of the object.
     *
     */
    
    public String toString() {
        
        return this.getNamespace()+": "+this.getName()+"/"+this.getAccession()+" v."+this.getVersion();
        
    }
    
        
    
    
    
    /**
     *
     * A <a href="http://www.rfc-editor.org/rfc/rfc2396.txt">Uniform
     *
     * Resource Identifier</a> (URI) which identifies the sequence
     *
     * represented by this object.  For sequences in well-known
     *
     * database, this may be a URN, e.g.
     *
     *
     *
     * <pre>
     *
     * urn:sequence/embl:AL121903
     *
     * </pre>
     *
     *
     *
     * It may also be a URL identifying a specific resource, either
     *
     * locally or over the network
     *
     *
     *
     * <pre>
     *
     * file:///home/thomas/myseq.fa|seq22
     *
     * http://www.mysequences.net/chr22.seq
     *
     * </pre>
     *
     *
     *
     * In this implementation, URN just equals Name.
     *
     *
     *
     * @return the URI as a String
     *
     */
    
    public String getURN() {
        
        return this.getName();
        
    }
    
       
    
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    
    // Hibernate requirement - not for public use.
    private Long getId() {
        
        return this.id;
    }
    
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) {
        
        this.id = id;
    }
    
}

