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
 
 * SimpleCrossRef.java
 
 *
 
 * Created on June 15, 2005, 5:32 PM
 
 */



package org.biojavax;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.biojava.bio.Annotatable;
import org.biojava.bio.Annotation;

import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeForwarder;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.bio.BioEntryAnnotation;
import org.biojavax.bio.SimpleBioEntryAnnotation;
import org.biojavax.ontology.ComparableTerm;




/**
 *
 * A basic CrossRef implementation.
 *
 *
 *
 * Equality is the dbname, accession and version combination.
 *
 *
 *
 * @author Richard Holland
 *
 * @author Mark Schreiber
 *
 */

public class SimpleCrossRef extends AbstractChangeable implements CrossRef {
    
    
    
    /**
     *
     * The terms associated with this cross reference.
     *
     */
    
    private BioEntryAnnotation ann = new SimpleBioEntryAnnotation();
    private ChangeForwarder annFor;
    
    /**
     *
     * The accession for this cross reference.
     *
     */
    
    private String accession;
    
    /**
     *
     * The dbname for this cross reference.
     *
     */
    
    private String dbname;
    
    /**
     *
     * The version for this cross reference.
     *
     */
    
    private int version;
    
    
    
    /**
     *
     * Creates a new instance of SimpleCrossRef
     *
     * @param dbname the dbname for this crossref.
     *
     * @param accession the accession for this crossref.
     *
     * @param version the version for this crossref.
     *
     */
    
    public SimpleCrossRef(String dbname, String accession, int version) {
        
        if (accession==null) throw new IllegalArgumentException("Accession cannot be null");
        
        if (dbname==null) throw new IllegalArgumentException("DBName cannot be null");
        
        this.accession = accession;
        
        this.dbname = dbname;
        
        this.version = version;
        
        // construct the forwarder so that it emits Annotatable.ANNOTATION ChangeEvents
        
        // for the Annotation.PROPERTY events it will listen for
        
        this.annFor = new ChangeForwarder.Retyper(this, this.getChangeSupport(Annotatable.ANNOTATION), Annotatable.ANNOTATION);
        
        // connect the forwarder so it listens for Annotation.PROPERTY events
        
        this.ann.addChangeListener(this.annFor, Annotation.PROPERTY);
        
    }
    
    // Hibernate requirement - not for public use.
    private SimpleCrossRef() {}
    
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
        private CrossRef crossref;
        private Note() {}
        private Note(ComparableTerm term, String value, int rank, CrossRef crossref) {
            this.term = term;
            this.value = value;
            this.rank = rank;
            this.crossref = crossref;            
        }
        private void setTerm(ComparableTerm term) { this.term = term; }
        private ComparableTerm getTerm() { return this.term; }
        private void setValue(String value) { this.value = value; }
        private String getValue() { return this.value; }
        private void setRank(int rank) { this.rank = rank; }
        private int getRank() { return this.rank; }
        private void setCrossRef(CrossRef crossref) { this.crossref = crossref; }
        private CrossRef getCrossRef() { return this.crossref; }
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
    
    // Hibernate requirement - not for public use.
    private void setAccession(String accession) { this.accession = accession; }
    
    /**
     *
     * Getter for property dbname.
     *
     * @return Value of property dbname.
     *
     */
    
    public String getDbname() {
        
        return this.dbname;
        
    }
    
    // Hibernate requirement - not for public use.
    private void setDbname(String dbname) { this.dbname = dbname; }
    
    
    
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
    
    // Hibernate requirement - not for public use.
    private void setVersion(int version) { this.version = version; }
    
    
    /**
     *
     * Indicates whether some other object is "equal to" this one.
     *
     * @return a negative integer, zero, or a positive integer as this object
     *
     * 		is less than, equal to, or greater than the specified object.
     *
     * @param o the Object to be compared.
     *
     */
    
    public int compareTo(Object o) {
        
        CrossRef them = (CrossRef)o;
        
        if (!this.getDbname().equals(them.getDbname())) return this.getDbname().compareTo(them.getDbname());
        
        if (!this.getAccession().equals(them.getAccession())) return this.getAccession().compareTo(them.getAccession());
        
        return this.getVersion()-them.getVersion();
        
    }
    
    
    
    /**
     *
     * Indicates whether some other object is "equal to" this one.
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
        
        if(this == obj) return true;
        
        if (obj==null || !(obj instanceof CrossRef)) return false;
        
        else {
            
            CrossRef them = (CrossRef)obj;
            
            return (this.getDbname().equals(them.getDbname()) &&
                    
                    this.getAccession().equals(them.getAccession()) &&
                    
                    this.getVersion()==them.getVersion()
                    
                    );
            
        }
        
    }
    
    
    
    /**
     *
     * Returns a hash code value for the object. This method is
     *
     * supported for the benefit of hashtables such as those provided by
     *
     * <code>java.util.Hashtable</code>.
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
        
        code = 37*code + this.getDbname().hashCode();
        
        code = 37*code + this.getAccession().hashCode();
        
        code = 37*code + this.getVersion();
        
        return code;
        
    }
    
    
    
    /**
     *
     * Returns a string representation of the object of the form <code>
     *
     * this.getDbname()+":"+this.getAccession()+", v."+this.getVersion();</code>
     *
     * @return  a string representation of the object.
     *
     */
    
    public String toString() {
        
        return this.getDbname()+":"+this.getAccession()+", v."+this.getVersion();
        
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

