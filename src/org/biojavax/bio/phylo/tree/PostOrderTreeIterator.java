package org.biojavax.bio.phylo.tree;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;


/**
 * @author Tobias Thierer
 * @version $Id$
 *
 * An iterator over the nodes of a tree, in post order, which is a
 * reverse topological sort.
 *          <p/>
 *          created on 12.12.2006 16:09:23
 */

class PostOrderTreeIterator implements Iterator {
    // We always have nodeStack.size() == iteratorStack.size()
    // iteratorStack.get(i) iterates over the children of nodeStack.get(i)
    protected LinkedList iteratorStack = new LinkedList();
	protected LinkedList nodeStack = new LinkedList();

	/**
	 * iterates the nodes of a tree in reverse topological order (i.e.
	 * in the order in which they are visited in post order a depth
	 * first search); Uses only O(depth(tree)) memory.
     * @param tree Tree to iterate over 
	 */
	PostOrderTreeIterator(RootedTree tree) {
        RootedTreeNode root = tree.getRoot();
        if (root != null) {
			nodeStack.addFirst(root);
             // note: root may be a leaf
            iteratorStack.addFirst(root.getChildren().iterator());
		}
	}

    /**
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return (iteratorStack.size() > 0);
	}

	/**
	 * @see java.util.Iterator#next()
     * @throws NoSuchElementException If there is no further element, i.e. if {@link #hasNext()} is false
	 */
	public Object next() {
         // return type is always RootedTreeNode
		if (!hasNext()) {
			throw new NoSuchElementException();
		} else {
			Iterator currIterator = (Iterator) iteratorStack.getFirst();
			if (currIterator.hasNext()) {
				RootedTreeNode currNode = (RootedTreeNode) currIterator.next();
				while (!currNode.isLeaf()) {
					nodeStack.addFirst(currNode);
					currIterator = currNode.getChildren().iterator();
					iteratorStack.addFirst(currIterator);
					currNode = (RootedTreeNode) currIterator.next();
				}
				return currNode;
			} else {
				iteratorStack.removeFirst();
				return nodeStack.removeFirst();
			}
		}
	}

	/**
	 * Unsupported operation
     * @throws UnsupportedOperationException when called
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
