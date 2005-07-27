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
 
 * SimpleBioEntryRelationship.java
 
 *
 
 * Created on June 16, 2005, 2:07 PM
 
 */



package org.biojavax.bio;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;

import org.biojavax.ontology.ComparableTerm;



/**
 *
 * Represents a relationship between two bioentries that is described by a term.
 *
 *
 *
 * Equality is the combination of unique subject, object and term.
 *
 *
 *
 * @author Richard Holland
 *
 * @author Mark Schreiber
 *
 */

public class SimpleBioEntryRelationship extends AbstractChangeable implements BioEntryRelationship {
    
    
    
    /**
     *
     * The object bioentry.
     *
     */
    
    private BioEntry object;
    
    /**
     *
     * The subject bioentry.
     *
     */
    
    private BioEntry subject;
    
    /**
     *
     * The relationship term.
     *
     */
    
    private ComparableTerm term;
    
    
    
    private int rank;
    
    /**
     *
     * Creates a new instance of SimpleBioEntryRelationship
     *
     * @param object The object bioentry.
     *
     * @param subject The subject bioentry.
     *
     * @param term The relationship term.
     *
     */
    
    public SimpleBioEntryRelationship(BioEntry object, BioEntry subject, ComparableTerm term, int rank) {
        
        if (object==null) throw new IllegalArgumentException("Object cannot be null");
        
        if (subject==null) throw new IllegalArgumentException("Subject cannot be null");
        
        if (term==null) throw new IllegalArgumentException("Term cannot be null");
        
        if (object.equals(subject)) throw new IllegalArgumentException("Object cannot be the same as the subject");
        
        this.object = object;
        
        this.subject = subject;
        
        this.term = term;
        
        this.rank = rank;
        
    }
    
    // Hibernate requirement - not for public use.
    private SimpleBioEntryRelationship() {}
    
    public void setRank(int rank) throws ChangeVetoException {
        
        if(!this.hasListeners(BioEntryRelationship.RANK)) {
            
            this.rank = rank;
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    BioEntryRelationship.RANK,
                    
                    Integer.valueOf(rank),
                    
                    Integer.valueOf(this.rank)
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(BioEntryRelationship.RANK);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.rank = rank;
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
    }
    
    public int getRank() {
        return this.rank;
    }
    
    
    /**
     *
     * Getter for property object.
     *
     * @return Value of property object.
     *
     */
    
    public BioEntry getObject() {
        
        return this.object;
        
    }
    
    
    // Hibernate requirement - not for public use.
    private void setObject(BioEntry object) { this.object = object; }
    
    /**
     *
     * Getter for property subject.
     *
     * @return Value of property subject.
     *
     */
    
    public BioEntry getSubject() {
        
        return this.subject;
        
    }
    
    // Hibernate requirement - not for public use.
    private void setSubject(BioEntry subject) { this.subject = subject; }
    
    
    /**
     *
     * Getter for property term.
     *
     * @return Value of property term.
     *
     */
    
    public ComparableTerm getTerm() {
        
        return this.term;
        
    }
    
    
    // Hibernate requirement - not for public use.
    private void setTerm(ComparableTerm term) { this.term = term; }
    
    
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
        
        BioEntryRelationship them = (BioEntryRelationship)o;
        
        if (!this.getObject().equals(them.getObject())) return this.getObject().compareTo(them.getObject());
        
        if (!this.getSubject().equals(them.getSubject())) return this.getSubject().compareTo(them.getSubject());
        
        return this.getTerm().compareTo(them.getTerm());
        
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
        
        if (this == obj) return true;
        
        if (obj==null || !(obj instanceof BioEntryRelationship)) return false;
        
        else {
            
            BioEntryRelationship them = (BioEntryRelationship)obj;
            
            return (this.getObject().equals(them.getObject()) &&
                    
                    this.getSubject().equals(them.getSubject()) &&
                    
                    this.getTerm().equals(them.getTerm()));
            
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
        
        code = code*37 + this.getObject().hashCode();
        
        code = code*37 + this.getSubject().hashCode();
        
        code = code*37 + this.getTerm().hashCode();
        
        return code;
        
    }
    
    
    
    /**
     *
     * Returns a string representation of the object of the form <code>
     *
     * this.getTerm()+"("+this.getSubject()+","+this.getObject()+")";<code>
     *
     * @return  a string representation of the object.
     *
     */
    
    public String toString() {
        
        return this.getTerm()+"("+this.getSubject()+","+this.getObject()+")";
        
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

