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
import java.util.HashMap;
import java.util.Map;
import org.biojava.utils.Unchangeable;
import org.biojavax.ontology.ComparableTerm;

/**
 * Represents a relationship between two bioentries that is described by a term.
 * 
 * Equality is the combination of unique subject, object and term.
 *
 * @author Richard Holland
 * @author Mark Schreiber
 */
public class SimpleBioEntryRelationship extends Unchangeable implements BioEntryRelationship {
    
    /**
     * The object bioentry.
     */
    private BioEntry object;
    /**
     * The subject bioentry.
     */
    private BioEntry subject;
    /**
     * The relationship term.
     */
    private ComparableTerm term;
    
    /**
     * Creates a new instance of SimpleBioEntryRelationship
     * @param object The object bioentry.
     * @param subject The subject bioentry.
     * @param term The relationship term.
     */
    public SimpleBioEntryRelationship(BioEntry object, BioEntry subject, ComparableTerm term) {
        if (object==null) throw new IllegalArgumentException("Object cannot be null");
        if (subject==null) throw new IllegalArgumentException("Subject cannot be null");
        if (term==null) throw new IllegalArgumentException("Term cannot be null");
        if (object.equals(subject)) throw new IllegalArgumentException("Object cannot be the same as the subject");
        this.object = object;
        this.subject = subject;
        this.term = term;
    }
    
    /**
     * Getter for property object.
     * @return Value of property object.
     */
    public BioEntry getObject() {
        return this.object;
    }
    
    /**
     * Getter for property subject.
     * @return Value of property subject.
     */
    public BioEntry getSubject() {
        return this.subject;
    }
        
    /**
     * Getter for property term.
     * @return Value of property term.
     */
    public ComparableTerm getTerm() {
        return this.term;
    }
    
    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * @return a negative integer, zero, or a positive integer as this object
     * 		is less than, equal to, or greater than the specified object.
     * @param o the Object to be compared.
     */
    public int compareTo(Object o) {
        BioEntryRelationship them = (BioEntryRelationship)o;
        if (!this.getObject().equals(them.getObject())) return this.getObject().compareTo(them.getObject());
        if (!this.getSubject().equals(them.getSubject())) return this.getSubject().compareTo(them.getSubject());
        return this.getTerm().compareTo(them.getTerm());
    }
    
    /**
     * Indicates whether some other object is "equal to" this one.
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     * @see     #hashCode()
     * @see     java.util.Hashtable
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
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>.
     * @return  a hash code value for this object.
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see     java.util.Hashtable
     */
    public int hashCode() {
        int code = 17;
        code = code*37 + this.getObject().hashCode();
        code = code*37 + this.getSubject().hashCode();
        code = code*37 + this.getTerm().hashCode();
        return code;
    }
    
    /**
     * Returns a string representation of the object of the form <code>
     * this.getTerm()+"("+this.getSubject()+","+this.getObject()+")";<code>
     * @return  a string representation of the object.
     */
    public String toString() {
        return this.getTerm()+"("+this.getSubject()+","+this.getObject()+")";
    }
}
