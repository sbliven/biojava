package org.biojavax.bio.phylo.tree;

import junit.framework.TestCase;

import java.util.Iterator;

/**
 * @author Tobias Thierer
 * @version $Id$
 *          <p/>
 *          created on 13.12.2006 12:26:32
 */
public class DefaultRootedTreeTest extends TestCase {

    private static class StringNode extends DefaultRootedTreeNode {
        private String value;
        StringNode(String s) {
            this.value = s;
        }
        public String getValue() {
            return value;
        }
        public String toString() {
            return "stringNode[" + value + "]";
        }
    }

    public void testWeightedTree() {
        RootedTreeNode root = new StringNode("root");
        RootedTree t = new DefaultRootedTree(root);
        t.addBranch(new DefaultBranch(root, new StringNode("child")));
        Iterator itNodes = t.postOrderIterator();
        String result = "";
        while (itNodes.hasNext()) {
            StringNode node = (StringNode) itNodes.next();
            result += node.getValue();
        }
        assertEquals(result, "childroot");
    }
}
