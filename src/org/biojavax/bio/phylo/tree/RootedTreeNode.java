package org.biojavax.bio.phylo.tree;

import java.util.Collection;

/**
 * @author Tobias Thierer
 * @version $Id$
 *          <p/>
 *          created on 12.12.2006 13:28:46
 */
public interface RootedTreeNode extends Node {
    RootedTreeNode getParent();
    void setParentBranch(Branch parentBranch);
    Branch getParentBranch();
    Collection getChildren();
    void addChild(Node node);
    boolean isLeaf();
    int getSubtreeSize();
}
