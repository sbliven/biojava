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
 * SimpleRichFeatureRelationship.java
 *
 * Created on June 16, 2005, 2:07 PM
 */

package org.biojavax.bio.seq;

import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.ontology.ComparableTerm;

/**
 * Represents a relationship between two features that is described by a term.
 * Equality is the combination of unique subject, object and term.
 * @author Richard Holland
 * @author Mark Schreiber
 *
 */
public class SimpleRichFeatureRelationship extends AbstractChangeable implements RichFeatureRelationship {
    
    private RichFeature object;
    private RichFeature subject;
    private ComparableTerm term;
    private int rank;
    
    /**
     * Creates a new instance of SimpleRichFeatureRelationship
     * @param object The object RichFeature.
     * @param subject The subject RichFeature.
     * @param term The relationship term.
     * @param rank the rank of the relationship.
     */
    
    public SimpleRichFeatureRelationship(RichFeature object, RichFeature subject, ComparableTerm term, int rank) {
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
    protected SimpleRichFeatureRelationship() {}
    
    /**
     * {@inheritDoc}
     */
    public void setRank(int rank) throws ChangeVetoException {
        if(!this.hasListeners(RichFeatureRelationship.RANK)) {
            this.rank = rank;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichFeatureRelationship.RANK,
                    Integer.valueOf(rank),
                    Integer.valueOf(this.rank)
                    );
            ChangeSupport cs = this.getChangeSupport(RichFeatureRelationship.RANK);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.rank = rank;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int getRank() { return this.rank; }
    
    /**
     * Getter for property object.
     * @return Value of property object.
     */
    public RichFeature getObject() { return this.object; }
    
    // Hibernate requirement - not for public use.
    private void setObject(RichFeature object) { this.object = object; }
    
    /**
     * Getter for property subject.
     * @return Value of property subject.
     */
    public RichFeature getSubject() { return this.subject; }
    
    // Hibernate requirement - not for public use.
    private void setSubject(RichFeature subject) { this.subject = subject; }
    
    /**
     * Getter for property term.
     * @return Value of property term.
     */
    public ComparableTerm getTerm() { return this.term; }
    
    // Hibernate requirement - not for public use.
    private void setTerm(ComparableTerm term) { this.term = term; }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        // Hibernate comparison - we haven't been populated yet
        if (this.object==null) return -1;
        // Normal comparison
        RichFeatureRelationship them = (RichFeatureRelationship)o;
        if (!this.object.equals(them.getObject())) return -1; // Can't compare features :(
        else if (!this.subject.equals(them.getSubject())) return 1; // Can't compare features :(
        else return this.getTerm().compareTo(them.getTerm());
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj==null || !(obj instanceof RichFeatureRelationship)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.object==null) return false;
        // Normal comparison
        RichFeatureRelationship them = (RichFeatureRelationship)obj;
        return (this.object.equals(them.getObject()) &&
                this.subject.equals(them.getSubject()) &&
                this.term.equals(them.getTerm()));
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.object==null) return code;
        // Normal comparison
        code = code*37 + this.object.hashCode();
        code = code*37 + this.subject.hashCode();
        code = code*37 + this.term.hashCode();
        return code;
    }
    
    /**
     * {@inheritDoc}
     * In the form <code>this.getTerm()+"("+this.getSubject()+","+this.getObject()+")";<code>
     */
    public String toString() {
        return this.getTerm()+"("+this.getSubject()+","+this.getObject()+")";
    }
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    // Hibernate requirement - not for public use.
    private Long getId() { return this.id; }
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) { this.id = id; }
}

