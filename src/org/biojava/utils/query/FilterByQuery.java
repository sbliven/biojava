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
  private final Filter filter;
  
  /**
   * Create a new filter that will evaluate <code>query</code> starting from
   * <code>startNode</code> for each item and then return wether <code>filter
   * </code> accepts the result set.
   *
   * @param query  the Query to evaluate
   * @param startNode  the Node to start evaluation from
   * @param filter  the filter to check the result of the sub-query with
   */
  public FilterByQuery(Query query, Node startNode, Filter filter) {
    this.query = query;
    this.startNode = startNode;
    this.filter = filter;
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
  public Filter getFilter() {
    return filter;
  }
  
  public boolean accept(Object object) {
    Queryable res = QueryTools.select(
      query,
      startNode,
      QueryTools.createQueryable(Collections.singleton(object), Object.class)
    );
    
    return filter.accept(res);
  }
  
  public Class getInputClass() {
    return Object.class;
  }
  
  public Class getOutputClass() {
    return Object.class;
  }
}
