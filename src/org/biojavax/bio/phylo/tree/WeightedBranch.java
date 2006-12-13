package org.biojavax.bio.phylo.tree;

/**
 * @author Tobias Thierer
 * @version $Id$
 *          <p/>
 *          created on 13.12.2006 12:16:03
 */
public class WeightedBranch extends DefaultBranch implements Weighted {
    private double weight;

    WeightedBranch(Node nodeA, Node nodeB) {
        this(nodeA, nodeB, 1.0);
    }

    public WeightedBranch(Node nodeA, Node nodeB, double weight) {
        super(nodeA, nodeB);
        setWeight(weight);
    }

    public final double getWeight() {
        return weight;
    }

    public final void setWeight(double weight) {
        this.weight = weight;
    }
}
