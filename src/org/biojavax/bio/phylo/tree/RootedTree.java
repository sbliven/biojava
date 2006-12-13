package org.biojavax.bio.phylo.tree;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Tobias Thierer
 * @version $Id$
 *          <p/>
 *          created on 12.12.2006 13:24:10
 */
public interface RootedTree extends Tree {    
    RootedTreeNode getRoot();
    boolean isRoot(RootedTreeNode node);
    Collection getNodes();
    Tree asUnrooted();

    Iterator postOrderIterator();
    //Iterator preOrderIterator();
}
