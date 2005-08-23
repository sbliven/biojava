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
 * Represents a relationship between two bioentries that is described by a term.
 * Equality is the combination of unique subject and term.
 * @author Richard Holland
 * @author Mark Schreiber
 */
public class SimpleBioEntryRelationship extends AbstractChangeable implements BioEntryRelationship {
    
    private BioEntry subject;
    private ComparableTerm term;
    private Integer rank;
    
    /**
     * Creates a new instance of SimpleBioEntryRelationship
     * @param rank The rank of the relationship.
     * @param subject The subject bioentry.
     * @param term The relationship term.
     */
    
    public SimpleBioEntryRelationship(BioEntry subject, ComparableTerm term, Integer rank) {
        if (subject==null) throw new IllegalArgumentException("Subject cannot be null");
        if (term==null) throw new IllegalArgumentException("Term cannot be null");
        this.subject = subject;
        this.term = term;
        this.rank = rank;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleBioEntryRelationship() {}
    
    /**
     * {@inheritDoc}
     */
    public void setRank(Integer rank) throws ChangeVetoException {
        if(!this.hasListeners(BioEntryRelationship.RANK)) {
            this.rank = rank;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    BioEntryRelationship.RANK,
                    rank,
                    this.rank
                    );
            ChangeSupport cs = this.getChangeSupport(BioEntryRelationship.RANK);
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
    public Integer getRank() { return this.rank; }
        
    /**
     * {@inheritDoc}
     */
    public BioEntry getSubject() { return this.subject; }
    
    // Hibernate requirement - not for public use.
    private void setSubject(BioEntry subject) { this.subject = subject; }
    
    /**
     * {@inheritDoc}
     */
    public ComparableTerm getTerm() { return this.term; }
    
    // Hibernate requirement - not for public use.
    private void setTerm(ComparableTerm term) { this.term = term; }
    
    /**
     * Compares this object to another. The comparison is made based on the rank,
     * subject and term (in that order). The first of these to be unequal will
     * be used as the basis for the returned value. If all are equal the value
     * will be 0.
     * @return a positive int if <code>o</code> preceeds <code>this</code>, 0 if
     *  they are equal and a negative int if <code>this</code> preceeds <code>o</code>
     */
    public int compareTo(Object o) {
        // Hibernate comparison - we haven't been populated yet
        if (this.subject==null) return -1;
        // Normal comparison
        BioEntryRelationship them = (BioEntryRelationship)o;
        if (!this.rank.equals(them.getRank())) return this.rank.compareTo(them.getRank());
        if (!this.subject.equals(them.getSubject())) return this.subject.compareTo(them.getSubject());
        return this.term.compareTo(them.getTerm());
    }
    
    /**
     * {@inheritDoc} Equality is defined by a comparison of the subject and
     * term of the relationship.
     * return true if the subject and term of this object are equal to the 
     *   subject and term of <code>obj</code>. False in all other conditions.
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj==null || !(obj instanceof BioEntryRelationship)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.subject==null) return false;
        // Normal comparison
        BioEntryRelationship them = (BioEntryRelationship)obj;
        return (this.subject.equals(them.getSubject()) &&
                this.term.equals(them.getTerm()));
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.subject==null) return code;
        // Normal comparison
        code = code*37 + this.subject.hashCode();
        code = code*37 + this.term.hashCode();
        return code;
    }
    
    /**
     * {@inheritDoc}
     * Form is <code>this.getTerm()+"("+this.getSubject()+")";<code>
     */
    public String toString() { return this.getTerm()+"("+this.getSubject()+")"; }
    
    // Hibernate requirement - not for public use.
    private Integer id;
    
    // Hibernate requirement - not for public use.
    private Integer getId() { return this.id; }
    
    // Hibernate requirement - not for public use.
    private void setId(Integer id) { this.id = id; }
}

