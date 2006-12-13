package org.biojavax.bio.phylo;

import java.util.Iterator;

/**
 * @author Tobias Thierer
 * @version $Id$
 *          <p/>
 *          created on 12.12.2006 13:23:41
 */
public interface Tree extends Iterable {
    /**
     * Adds a branch to the tree. branch.getNodeA() must
     * already be in this tree; branch.getNodeB()
     * must not yet be in this tree, but it may already
     * be in another tree.
     * @param branch
     */
    void addBranch(Branch branch);

    /**
     * An iterator over the nodes of this tree.
     */
    Iterator iterator();

    boolean isEmpty();
}
