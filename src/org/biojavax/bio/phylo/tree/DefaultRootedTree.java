package org.biojavax.bio.phylo.tree;

import java.util.Collection;
import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * @author Tobias Thierer
 * @version $Id$
 *          <p/>
 *          created on 12.12.2006 14:44:10
 */
public class DefaultRootedTree implements RootedTree {
    private RootedTreeNode rootNode;

    /**
     * Constructs an empty rooted tree
     */
    public DefaultRootedTree() {
        this(null);
    }

    public boolean isRoot(RootedTreeNode node) {
        return node.equals(rootNode);
    }

    /**
     * Constructs a rooted tree with the given root node.
     * @param root root node of the tree to construct. If this
     * node already has children, these automatically become part
     * of the tree, but they aren't copied. If root is another
     * tree's root, this will just create a new view on the other
     * tree, i.e. a new tree that shares all nodes with the old one.
     */
    public DefaultRootedTree(RootedTreeNode root) {
        this.rootNode = root;
    }

    public Collection getNodes() {
        final RootedTreeNode root = getRoot();
        return new AbstractCollection() {
            public Iterator iterator() {
                return DefaultRootedTree.this.iterator();
            }

            public int size() {
                return root.getSubtreeSize();
            }
        };
    }

    /**
     * @return The root node, or null if the tree is empty
     */
    public RootedTreeNode getRoot() {
        return rootNode;
    }

    /**
     * @return An unrooted view onto this tree. The unrooted view shares
     *         all nodes with this tree.
     */
    public Tree asUnrooted() {
        return new UnrootedTree() {
            public void addBranch(Branch branch) {
                DefaultRootedTree.this.addBranch(branch);
            }

            public Iterator iterator() {
                /**
                 * This works only because RootedTreeNode extends
                 * Node (hence RootedTreeNodes can occur in an unrooted
                 * tree). Is this ok?
                 */
                return getNodes().iterator();
            }

            public boolean isEmpty() {
                return DefaultRootedTree.this.isEmpty();
            }
        };
    }

    public final Iterator postOrderIterator() {
        return new PostOrderTreeIterator(this);
    }

    /**
     * @return An iterator over this tree's nodes. The default implementation
     *  returns {@link #postOrderIterator()}
     */
    public Iterator iterator() {
        return postOrderIterator();
    }

    public void addBranch(Branch branch) {
        branch.getNodeA().addBranch(branch);
    }

    public final boolean isEmpty() {
        return getRoot() == null;
    }
}
