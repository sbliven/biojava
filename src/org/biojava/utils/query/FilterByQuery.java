package org.biojava.utils.query;

import java.util.*;

/**
 * This implementation of Filter allows a sub-query to be executed for each
 * element in a Queryable and the result of the sub-query is then evaluated with
 * another filter. This allows the equivalient funcitonality to SQL sub-selects.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class FilterByQuery extends Filter {
  private final Query query;
  private final Node startNode;
  private final Node endNode;
  private final Filter.CompareInteger.Comparison comparison;
  private final int value;
  
  /**
   * Create a new filter that will evaluate <code>query</code> starting from
   * <code>startNode</code> for each item and then return wether <code>filter
   * </code> accepts the result set.
   *
   * @param query  the Query to evaluate
   * @param startNode  the Node to start evaluation from
   * @param filter  the filter to check the result of the sub-query with
   */
  public FilterByQuery(
    Query query,
    Node startNode,
    Node endNode,
    Filter.CompareInteger.Comparison comparison,
    int value
  ) {
    this.query = query;
    this.startNode = startNode;
    this.endNode = endNode;
    this.comparison = comparison;
    this.value = value;
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
   * Retrieve the filter to apply to the return set.
   *
   * @return the Filter
   */
  public Filter.CompareInteger.Comparison getComparison() {
    return comparison;
  }
  
  public int getValue() {
    return value;
  }
  
  public boolean accept(Object object)
  throws OperationException {
    Queryable res = QueryTools.select(
      query,
      startNode,
      endNode,
      QueryTools.createQueryable(Collections.singleton(object), Object.class)
    );
    
    return comparison.compare(res.size(), value);
  }
  
  public Class getInputClass() {
    return startNode.getInputClass();
  }
  
  public Class getOutputClass() {
    return startNode.getInputClass();
  }
}
