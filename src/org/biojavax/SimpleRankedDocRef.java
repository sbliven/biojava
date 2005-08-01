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
 * SimpleRankedDocRef.java
 *
 * Created on July 12, 2005, 8:10 AM
 */

package org.biojavax;

/**
 * Represents a documentary reference, the bioentryreference table in BioSQL.
 * @author Richard Holland
 */
public class SimpleRankedDocRef implements RankedDocRef {
    
    private DocRef docref;
    private int start;
    private int end;
    private int rank;
    
    /**
     * Constructs a new docref for a given location.
     * @param docref the document reference
     * @param start the start position of the location
     * @param end the end position of the location
     */
    public SimpleRankedDocRef(int rank, DocRef docref, int start, int end) {
        if (docref==null) throw new IllegalArgumentException("Document reference cannot be null");
        this.docref = docref;
        this.start = start;
        this.end = end;
        this.rank = rank;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleRankedDocRef() {}
    
    // Hibernate requirement - not for public use.
    private void setRank(int rank) { this.rank = rank; }
    
    /**
     * {@inheritDocs}
     */
    public int getRank() { return this.rank; }
    
    /**
     * {@inheritDocs}
     */
    public DocRef getDocumentReference() { return this.docref; }
    
    /**
     * {@inheritDocs}
     */
    public int getStart() { return this.start; }
    
    /**
     * {@inheritDocs}
     */
    public int getEnd() { return this.end; }
    
    // Hibernate requirement - not for public use.
    private void setDocumentReference(DocRef docref) { this.docref = docref; }
    
    // Hibernate requirement - not for public use.
    private void setStart(int start) { this.start = start; }
    
    // Hibernate requirement - not for public use.
    private void setEnd(int end) { this.end = end; }
    
    /**
     * {@inheritDocs}
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj==null || !(obj instanceof RankedDocRef)) return false;
        else {
            RankedDocRef them = (RankedDocRef)obj;
            return (this.getRank()==them.getRank() &&
                    this.getDocumentReference().equals(them.getDocumentReference()) &&
                    this.getStart()==them.getStart() &&
                    this.getEnd()==them.getEnd());
        }
    }
    
    /**
     * {@inheritDocs}
     */
    public int compareTo(Object o) {
        RankedDocRef them = (RankedDocRef)o;
        if (!this.getDocumentReference().equals(them.getDocumentReference())) return this.getDocumentReference().compareTo(them.getDocumentReference());
        if (this.getRank()!=them.getRank()) return this.getRank()-them.getRank();
        if (this.getStart()!=them.getStart()) return this.getStart()-them.getStart();
        return this.getEnd()-them.getEnd();
    }
    
    /**
     * {@inheritDocs}
     */
    public int hashCode() {
        int code = 17;
        code = 37*code + this.getDocumentReference().hashCode();
        code = 37*code + this.getRank();
        code = 37*code + this.getStart();
        code = 37*code + this.getEnd();
        return code;
    }
    
    /**
     * {@inheritDocs}
     * Form: <code>this.getDocumentReference()+": "+this.getStart()+"-"+this.getEnd();</code>
     */
    public String toString() { return this.getDocumentReference()+": "+this.getStart()+"-"+this.getEnd(); }
    
}



