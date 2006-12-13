package org.biojavax.bio.phylo;

import java.util.*;

/**
 * @author Tobias Thierer
 * @version $Id$
 *          <p/>
 *          created on 12.12.2006 15:13:22
 */
public class AbstractRootedTreeNode implements RootedTreeNode {
    private Branch parentBranch = null;
    private List childBranches = new LinkedList();
    private int currentSubtreeSize = 0;

    private Collection children = new AbstractCollection() {
        public Iterator iterator() {
            final Iterator itChildBranches = childBranches.iterator();
            return new Iterator() {
                public boolean hasNext() {
                    return itChildBranches.hasNext();
                }

                public Object next() {
                    Branch nextBranch = (Branch) itChildBranches.next();
                    return nextBranch.getOtherNode(AbstractRootedTreeNode.this);
                }

                public void remove() {
                    itChildBranches.remove();
                }
            };
        }

        public int size() {
            return childBranches.size();
        }
    };

    /**
     * Calls addBranch(new SimpleBranch(this, node)).
     *
     * @param node New child node to add
     */
    public void addChild(Node node) {
        addBranch(new SimpleBranch(this, node));
    }

    public Collection getChildren() {
        // immutable, so we can return the same object every time
        return children;
    }

    public RootedTreeNode getParent() {
        return parentBranch == null
                ? null
                : (RootedTreeNode) parentBranch.getNodeA();
    }

    public int getSubtreeSize() {
        return currentSubtreeSize;
    }

    public void setParentBranch(Branch parentBranch) {
        if (!this.equals(parentBranch.getNodeB())) {
            throw new IllegalArgumentException();
        }
        this.parentBranch = parentBranch;
    }

    public Branch getParentBranch() {
        return parentBranch;
    }

    public void addBranch(Branch branch) {
        if (!this.equals(branch.getNodeA())) {
            throw new IllegalArgumentException();
        }
        childBranches.add(branch);
        currentSubtreeSize += ((RootedTreeNode) branch.getNodeB()).getSubtreeSize();
    }

    public Collection getBranches() {
        return childBranches;
    }

    public boolean isLeaf() {
        return childBranches.isEmpty();
    }
}
