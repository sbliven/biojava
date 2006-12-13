package org.biojavax.bio.phylo;

import java.util.Collection;

/**
 * @author Tobias Thierer
 * @version $Id$
 *          <p/>
 *          created on 12.12.2006 13:25:21
 */
public interface Node {
    void addBranch(Branch branch);

    /**
     * If this is a RootedTreeNode, then this Collection's iterator
     * is guaranteed to return the parent node first (if there is one).
     *
     * @return all branches adjacent to this node
     */
    Collection getBranches();
}
