package org.biojavax.bio.phylo;

/**
 * @author Tobias Thierer
 * @version $Id$
 *          <p/>
 *          created on 12.12.2006 15:15:11
 */
public class SimpleBranch extends AbstractBranch {
    private final Node nodeA, nodeB;

    public SimpleBranch(Node nodeA, Node nodeB) {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
    }

    public Node getNodeA() {
        return nodeA;
    }

    public Node getNodeB() {
        return nodeB;
    }
}
