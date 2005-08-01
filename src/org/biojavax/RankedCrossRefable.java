/*
 * RankedCrossRefable.java
 *
 * Created on July 29, 2005, 9:56 AM
 */

package org.biojavax;

import java.util.Set;
import org.biojava.utils.ChangeVetoException;

/**
 *
 * @author Richard Holland
 */
public interface RankedCrossRefable {
    
    
    /**
     *
     * Returns a list of all crossrefs associated with this bioentry. This
     *
     * list is not mutable. If no crossrefs are associated, you will get back an
     *
     * empty list. If the crossrefs have indexes that are not consecutive, then the
     *
     * list will contain nulls at the indexes corresponding to the gaps between
     *
     * the extant crossrefs. eg. If there are only two crossrefs A and B at positions 10
     *
     * and 20 respectively, then the List returned will be of size 20, with nulls
     *
     * at index positions 0-9 and 11-19.
     *
     * @return Value of property crossrefs.
     *
     */
    
    public Set getRankedCrossRefs();
    
    public void setRankedCrossRefs(Set crossrefs) throws ChangeVetoException;
    
    public void addRankedCrossRef(RankedCrossRef crossref) throws ChangeVetoException;
    
    public void removeRankedCrossRef(RankedCrossRef crossref) throws ChangeVetoException;
}
