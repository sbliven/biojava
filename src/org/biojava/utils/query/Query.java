package org.biojava.utils.query;

import java.util.*;

/**
 * An entire query that can be used to navigate through a network of enteties.
 * By moving from a node in a graph to all reachable nodes and applying the
 * Operations as you go, you can build up a complete list of accepted Objects.
 * The query may be used to find individual objects (e.g. all males aged 19-25
 * with computer skills), or can be used to prune down a graph of objects (just
 * give me the sub-tree of features that overlap 35-36mb on Chr I).
 * <P>
 * If the graph branches (i.e. more than one node is reachable from another),
 * then the query interpreter should produce the result equivalent to following
 * both branches and then finding the union of their result sets (this is
 * equivalent to an <i>or</i> operation). Following a trail though the graph
 * should find the (order dependant) intersection of their operations
 * (equivalent to an <i>and</i>)
 * <P>
 * Queries are directed graphs. There is only ever one transition between two
 * nodes. Transitions are labeled with the operation to perform. Nodes and
 * Operations can be shared across multiple Query instances. The pair {Query
 *, Node} uniquely defines a place to start some processing.
 * <P>
 * Queries are read-only. If you need to produce a new Query by altering one or
 * more Queries, then build a new one. There should be tools available for all
 * the common tasks.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public interface Query {
  /**
   * Usefull instance of a Query that will always return everything that you put
   * into it.
   */
  public static IdentityQuery IDENTITY_QUERY = new IdentityQuery(Object.class);

  /**
   * The Query that return everything put in to it.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public class IdentityQuery implements Query {
    private final Node node;
    private final Set nodeSet;
    
    public IdentityQuery(Class clazz) {
      node = new SimpleNode("result", clazz);
      nodeSet = Collections.singleton(node);
    }
    
    public Node getNode() {
      return node;
    }
    
    public Set getNodes() {
      return nodeSet;
    }
    
    public Map getArcsToOperators() {
      return Collections.EMPTY_MAP;
    }
    
    public Set getOperations(Arc arc) {
      return Collections.EMPTY_SET;
    }
    
    public Set getArcsFrom(Node from) {
      return Collections.EMPTY_SET;
    }
    
    public Set getArcsTo(Node from) {
      return Collections.EMPTY_SET;
    }
  }
  
  /**
   * Return the nodes in this Query.
   *
   * @return Set <Node>
   */
  public Set getNodes();
  
  /**
   * Return the complete set of transitions within the model.
   * <P>
   * The Map is indexed by Arc instances and returns a Set of operators
   * (posibly empty) linking these two nodes.
   *
   * @return Map <Arc, Set<Operator>>
   *         desribing all arcs and all labelings for them
   */
  public Map getArcsToOperators();
  
  /**
   * Return the set of operators that link the two nodes in <code>arc</code>.
   * <P>
   * All operators should be followed in paralell when evaluating the query.
   *
   * @param arc  an Arc indicating the two nodes to evaluate
   * @return a Set <Operator> describing all Operators between the nodes of the
   *         arc
   */
  public Set getOperations(Arc arc);

  /**
   * Return all arcs that start from <code>from</code>.
   *
   * @param from  the Node to start from
   * @return a Set <Arc> of all Arcs starting from <code>from</code>
   */
  public Set getArcsFrom(Node from);
  
  /**
   * Return all arcs that ending at <code>to</code>.
   *
   * @param to  the Node to end at
   * @return a Set <Arc> of all Arcs end at <code>to</code>
   */
  public Set getArcsTo(Node to);
}
