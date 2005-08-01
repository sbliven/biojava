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
        this.parent = parent;
        this.comment = comment;
        this.rank = rank;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleBioEntryComment() {}
    
    // Hibernate requirement - not for public use.
    private void setComment(String comment) { this.comment = comment; }
    
    /**
     * {@inheritDocs}
     */
    public String getComment() { return this.comment; }
    
    // Hibernate requirement - not for public use.
    private void setRank(int rank) { this.rank = rank; }
    
    /**
     * {@inheritDocs}
     */
    public int getRank() { return this.rank; }
    
    // Hibernate requirement - not for public use.
    private void setParent(BioEntry parent) { this.parent = parent; }
    
    /**
     * {@inheritDocs}
     */
    public BioEntry getParent() { return this.parent; }
    
    /**
     * {@inheritDocs}
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj==null || !(obj instanceof BioEntryComment)) return false;
        else {
            BioEntryComment them = (BioEntryComment)obj;
            return (this.getParent().equals(them.getParent()) &&
                    this.getRank()==them.getRank() &&
                    this.getComment().equals(them.getComment()));
        }
    }
    
    /**
     * {@inheritDocs}
     */
    public int compareTo(Object o) {
        BioEntryComment them = (BioEntryComment)o;
        if (!this.getParent().equals(them.getParent())) return this.getParent().compareTo(them.getParent());
        if (this.getRank()!=them.getRank()) return this.getRank()-them.getRank();
        return this.getComment().compareTo(them.getComment());
    }
    
    /**
     * {@inheritDocs}
     */
    public int hashCode() {
        int code = 17;
        code = 37*code + this.getParent().hashCode();
        code = 37*code + this.getComment().hashCode();
        code = 37*code + this.getRank();
        return code;
    }
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    // Hibernate requirement - not for public use.
    private Long getId() { return this.id; }
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) { this.id = id;}
}
