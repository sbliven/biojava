package org.biojavax.bio.phylo;

import java.util.Collection;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Tobias Thierer
 * @version $Id$
 *          <p/>
 *          created on 12.12.2006 14:16:54
 */
public abstract class AbstractBranch implements Branch {
    public final Node getOtherNode(Node node) throws IllegalArgumentException {
        Node nodeA = getNodeA(), nodeB = getNodeB();
        if (nodeA.equals(node)) {
            return nodeA;
        } else if (nodeB.equals(node)) {
            return nodeB;
        } else {
            throw new IllegalArgumentException("Node " + node + " is not part of the branch " + this);
        }
    }

    public final Collection getNodes() {
        return new AbstractCollection() {
            public Iterator iterator() {
                return new Iterator() {
                    private int index = 0;

                    public boolean hasNext() {
                        return index < 2;
                    }

                    public Object next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        index++;
                        return (index == 1 ? getNodeA() : getNodeB());
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            public int size() {
                return 2;
            }
        };
    }

    public String toString() {
        return "branch(" + getNodeA() + "," + getNodeB() + ")";
    }
}
