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

import org.biojavax.CrossRef;

/**
 *
 * @author Richard Holland
 */
public class SimpleBioEntryCrossRef implements BioEntryCrossRef {
    

    private CrossRef crossref;
    private int rank;
    private BioEntry parent;
    
    public SimpleBioEntryCrossRef(BioEntry parent, CrossRef crossref, int rank) {
        this.parent = parent;
        this.crossref = crossref;
        this.rank = rank;
    }
    
    // Hibernate requirement - not for public use.
    private SimpleBioEntryCrossRef() {}
    
    // Hibernate requirement - not for public use.
    private void setCrossRef(CrossRef crossref) { this.crossref = crossref; }

    public CrossRef getCrossRef() {
        return this.crossref;
    }
    
    // Hibernate requirement - not for public use.
    private void setRank(int rank) { this.rank = rank; }

    public int getRank() {
        return this.rank;
    }
    
    // Hibernate requirement - not for public use.
    private void setParent(BioEntry parent) { this.parent = parent; }

    public BioEntry getParent() {
        return this.parent;
    }
    
        public boolean equals(Object obj) {

        if (this == obj) return true;

        if (obj==null || !(obj instanceof BioEntryCrossRef)) return false;

        else {

            BioEntryCrossRef them = (BioEntryCrossRef)obj;

            return (this.getParent().equals(them.getParent()) &&
                    this.getRank()==them.getRank() &&
                    this.getCrossRef().equals(them.getCrossRef()));

        }

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

        BioEntryCrossRef them = (BioEntryCrossRef)o;

        if (!this.getParent().equals(them.getParent())) return this.getParent().compareTo(them.getParent());

        if (this.getRank()!=them.getRank()) return this.getRank()-them.getRank();
        
        return this.getCrossRef().compareTo(them.getCrossRef());
    }

    

    /**

     * Returns a hash code value for the object. This method is

     * supported for the benefit of hashtables such as those provided by

     * <code>Hashtable</code>.

     * @return  a hash code value for this object.

     * @see     java.lang.Object#equals(java.lang.Object)

     * @see     java.util.Hashtable

     */

    public int hashCode() {

        int code = 17;

        code = 37*code + this.getParent().hashCode();

        code = 37*code + this.getCrossRef().hashCode();
        
        code = 37*code + this.getRank();

        return code;

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
