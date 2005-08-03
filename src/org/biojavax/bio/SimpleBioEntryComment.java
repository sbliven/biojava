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

package org.biojavax.bio;

/**
 * An implementaion of BioEntryComment.
 * @author Richard Holland
 */
public class SimpleBioEntryComment implements BioEntryComment {
    
    private String comment;
    private int rank;
    private BioEntry parent;
    
    /**
     * Constructs a new, immutable comment.
     * @param parent the entry the comment belongs to.
     * @param comment the text of the comment.
     * @param rank the rank of the comment.
     */
    public SimpleBioEntryComment(BioEntry parent, String comment, int rank) {
        if (parent==null) throw new IllegalArgumentException("Parent cannot be null");
        if (comment==null) throw new IllegalArgumentException("Comment cannot be null");
        this.parent = parent;
        this.comment = comment;
        this.rank = rank;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleBioEntryComment() {}
    
    // Hibernate requirement - not for public use.
    private void setComment(String comment) { this.comment = comment; }
    
    /**
     * {@inheritDoc}
     */
    public String getComment() { return this.comment; }
    
    // Hibernate requirement - not for public use.
    private void setRank(int rank) { this.rank = rank; }
    
    /**
     * {@inheritDoc}
     */
    public int getRank() { return this.rank; }
    
    // Hibernate requirement - not for public use.
    private void setParent(BioEntry parent) { this.parent = parent; }
    
    /**
     * {@inheritDoc}
     */
    public BioEntry getParent() { return this.parent; }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj==null || !(obj instanceof BioEntryComment)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.parent==null) return false;
        // Normal comparison
        BioEntryComment them = (BioEntryComment)obj;
        return (this.parent.equals(them.getParent()) &&
                this.rank==them.getRank() &&
                this.comment.equals(them.getComment()));
    }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        // Hibernate comparison - we haven't been populated yet
        if (this.parent==null) return -1;
        // Normal comparison
        BioEntryComment them = (BioEntryComment)o;
        if (!this.parent.equals(them.getParent())) return this.parent.compareTo(them.getParent());
        if (this.rank!=them.getRank()) return this.rank-them.getRank();
        return this.comment.compareTo(them.getComment());
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.parent==null) return code;
        // Normal comparison
        code = 37*code + this.parent.hashCode();
        code = 37*code + this.comment.hashCode();
        code = 37*code + this.rank;
        return code;
    }
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    // Hibernate requirement - not for public use.
    private Long getId() { return this.id; }
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) { this.id = id;}
}
