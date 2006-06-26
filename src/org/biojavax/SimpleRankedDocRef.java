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

import org.biojavax.bio.seq.Position;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.SimplePosition;
import org.biojavax.bio.seq.SimpleRichLocation;

/**
 * Represents a documentary reference. 
 * @author Richard Holland
 * @since 1.5
 */
public class SimpleRankedDocRef implements RankedDocRef {
    
    private DocRef docref;
    private Integer start;
    private Integer end;
    private RichLocation location;
    private int rank;
    
    /**
     * Constructs a new docref for a given location. If one or the other
     * of start and end are null, only the non-null value is used. If both
     * are null, no value is used for the location. 
     * @param docref the document reference. Must not be null.
     * @param start the start position of the location. 
     * @param end the end position of the location.
     */
    public SimpleRankedDocRef(DocRef docref, Integer start, Integer end, int rank) {
        if (docref==null) throw new IllegalArgumentException("Document reference cannot be null");
        this.docref = docref;
        this.setStart(start);
        this.setEnd(end);
        this.rank = rank;
    }
    
    /**
     * Constructs a new docref for a given location.
     * @param docref the document reference. Must not be null.
     * @param location the position of the document reference. Must not be null.
     */
    public SimpleRankedDocRef(DocRef docref, RichLocation location, int rank) {
        if (docref==null) throw new IllegalArgumentException("Document reference cannot be null");
        if (location==null) throw new IllegalArgumentException("Document location cannot be null");
        this.docref = docref;
        this.setLocation(location);
        this.rank = rank;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleRankedDocRef() {}
    
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
    private void setStart(Integer start) { 
    	this.start = start; 
    	this.createLocation();
   	}
    
    // Hibernate requirement - not for public use.
    private void setEnd(Integer end) { 
    	this.end = end; 
    	this.createLocation();
	}
    
    // Internal use only.
    private void createLocation() {
//    	Position position;
//    	if (this.start==null && this.end==null) position = null;
//    	else if (this.start==null) position = new SimplePosition(this.end.intValue());
//    	else if (this.end==null) position = new SimplePosition(this.start.intValue());
//    	else position = new SimplePosition(this.start.intValue(), this.end.intValue());
//        this.location = position == null ? RichLocation.EMPTY_LOCATION : new SimpleRichLocation(position, 0);
    	if (this.start==null && this.end==null) location = RichLocation.EMPTY_LOCATION;
    	else if (this.start==null) location = new SimpleRichLocation(null, new SimplePosition(this.end.intValue()), 0);
    	else if (this.end==null) location = new SimpleRichLocation(new SimplePosition(this.start.intValue()), null, 0);
    	else location = new SimpleRichLocation(new SimplePosition(this.start.intValue()), new SimplePosition(this.end.intValue()), 0);
    }
    
    // Internal use only.
    private void setLocation(RichLocation location) {
    	this.location = location;
    	this.start = new Integer(location.getMin());
    	this.end = new Integer(location.getMax());
    }
    
    public RichLocation getLocation() {
    	return this.location;
    }
    
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



