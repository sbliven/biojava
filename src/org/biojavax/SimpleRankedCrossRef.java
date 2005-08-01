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

package org.biojavax;

/**
 * Simple implementation of RankedCrossRef.
 * @author Richard Holland
 */
public class SimpleRankedCrossRef implements RankedCrossRef {
    
    private CrossRef crossref;
    private int rank;
    
    /**
     * Constructs a new crossref with a rank.
     * @param crossref the crossref to rank.
     * @param rank the rank to give it.
     */
    public SimpleRankedCrossRef(CrossRef crossref, int rank) {
        if (crossref==null) throw new IllegalArgumentException("Crossref cannot be null");
        this.crossref = crossref;
        this.rank = rank;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleRankedCrossRef() {}
    
    // Hibernate requirement - not for public use.
    private void setCrossRef(CrossRef crossref) { this.crossref = crossref; }
    
    /**
     * {@inheritDocs}
     */
    public CrossRef getCrossRef() { return this.crossref; }
    
    // Hibernate requirement - not for public use.
    private void setRank(int rank) { this.rank = rank; }
    
    /**
     * {@inheritDocs}
     */
    public int getRank() { return this.rank; }
    
    /**
     * {@inheritDocs}
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj==null || !(obj instanceof RankedCrossRef)) return false;
        else {
            RankedCrossRef them = (RankedCrossRef)obj;
            return (this.getRank()==them.getRank() &&
                    this.getCrossRef().equals(them.getCrossRef()));
        }
    }
    
    /**
     * {@inheritDocs}
     */
    public int compareTo(Object o) {
        RankedCrossRef them = (RankedCrossRef)o;
        if (this.getRank()!=them.getRank()) return this.getRank()-them.getRank();
        return this.getCrossRef().compareTo(them.getCrossRef());
    }
    
    /**
     * {@inheritDocs}
     */
    public int hashCode() {
        int code = 17;
        code = 37*code + this.getCrossRef().hashCode();
        code = 37*code + this.getRank();
        return code;
    }
}
