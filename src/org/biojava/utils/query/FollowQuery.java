package org.biojava.utils.query;

import java.util.*;

/**
 * This implementation of Follow allows a sub-query to be executed for each
 * element in a Queryable and the result of the sub-query is then returned.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class FollowQuery extends Follow {
  private final Query query;
  private final Node startNode;
  private final Node endNode;
  
  /**
   * Create a new follow that will evaluate <code>query</code> starting from
   * <code>startNode</code> for each item and then return the result set.
   *
   * @param query  the Query to evaluate
   * @param startNode  the Node to start evaluation from
   * @param endNode  the Node that will select items
   */
  public FollowQuery(
    Query query,
    Node startNode,
    Node endNode
  ) {
    this.query = query;
    this.startNode = startNode;
    this.endNode = endNode;
  }
  
  /**
   * Retrieve the query performed.
   *
   * @return  the Query
   */
  public Query getQuery() {
    return query;
  }
  
  /**
   * Retrieve the starting node for the query execution.
   *
   * @return the starting node
   */
  public Node getStartNode() {
    return startNode;
  }
  
  /**
   * Retrieve the ending node for the query execution.
   *
   * @return the ending node
   */
  public Node getEndNode() {
    return endNode;
  }
  
  public Queryable follow(Object item)
  throws OperationException {
    return QueryTools.select(
      query,
      startNode,
      endNode,
      QueryTools.createQueryable(Collections.singleton(item),
                                 JavaType.getType(Object.class))
    );
  }
  
  public Type getInputType() {
    return startNode.getInputType();
  }
  
  public Type getOutputType() {
    return endNode.getOutputType();
  }
}
