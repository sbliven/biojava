package org.biojavax.bio.phylo.tree;

import java.util.Collection;

/**
 * @author Tobias Thierer
 * @version $Id$
 *          <p/>
 *          created on 12.12.2006 13:27:47
 */
public interface Branch {
    /**
     * @return The node of this branch that was part of the tree first.
     */
    Node getNodeA();

    /**
     * @return The node at the other end of this branch (not the one returned by {@link #getNodeA})
     */
    Node getNodeB();
    Node getOtherNode(Node node) throws IllegalArgumentException;

    /**
     * If this branch is part of a rooted tree, then it is guaranteed
     * that the iterator of this collection will return the parent node
     * first.
     * @return A collection with two nodes, namely the ones returned
     * by {@link #getNodeA} and {@link #getNodeB}. 
     */
    Collection getNodes();
}
