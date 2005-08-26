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
 * Represents a documentary reference. 
 * @author Richard Holland
 */
public class SimpleRankedDocRef implements RankedDocRef {
    
    private DocRef docref;
    private Integer start;
    private Integer end;
    private int rank;
    
    /**
     * Constructs a new docref for a given location.
     * @param docref the document reference. Must not be null.
     * @param start the start position of the location
     * @param end the end position of the location
     */
    public SimpleRankedDocRef(DocRef docref, Integer start, Integer end, int rank) {
        if (docref==null) throw new IllegalArgumentException("Document reference cannot be null");
        this.docref = docref;
        this.start = start;
        this.end = end;
        this.rank = rank;
    }
    
    // Hibernate requirement - not for public use.
    private SimpleRankedDocRef() {}
    
    // Hibernate requirement - not for public use.
    private void setRank(int rank) { this.rank = rank; }
    
    /**
     * {@inheritDoc}
     */
    public int getRank() { return this.rank; }
    
    /**
     * {@inheritDoc}
     */
    public DocRef getDocumentReference() { return this.docref; }
    
    /**
     * {@inheritDoc}
     */
    public Integer getStart() { return this.start; }
    
    /**
     * {@inheritDoc}
     */
    public Integer getEnd() { return this.end; }
    
    // Hibernate requirement - not for public use.
    private void setDocumentReference(DocRef docref) { this.docref = docref; }
    
    // Hibernate requirement - not for public use.
    private void setStart(Integer start) { this.start = start; }
    
    // Hibernate requirement - not for public use.
    private void setEnd(Integer end) { this.end = end; }
    
    /**
     * {@inheritDoc}
     * Two ranked document references are equal if they have the same rank 
     * and refer to the same document reference.
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj==null || !(obj instanceof RankedDocRef)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.docref==null) return false;
        // Normal comparison
        RankedDocRef them = (RankedDocRef)obj;
        return (this.rank==them.getRank() &&
                this.docref.equals(them.getDocumentReference()));
    }
    
    /**
     * {@inheritDoc}
     * Ranked document references are sorted first by rank then by actual
     * document reference.
     */
    public int compareTo(Object o) {
        // Hibernate comparison - we haven't been populated yet
        if (this.docref==null) return -1;
        // Normal comparison
        RankedDocRef them = (RankedDocRef)o;
        if (this.rank!=them.getRank()) return this.rank - them.getRank();
        return this.docref.compareTo(them.getDocumentReference());
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.docref==null) return code;
        // Normal comparison
        code = 37*code + this.docref.hashCode();
        code = 37*code + this.rank;
        return code;
    }
        
    /**
     * {@inheritDoc}
     * Form: "(#rank) docref"
     */
    public String toString() {
        return "(#"+this.rank+") "+this.docref;
    }
}



