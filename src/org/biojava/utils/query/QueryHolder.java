package org.biojava.utils.query;

import java.util.*;
import org.biojava.utils.NestedException;

/**
 * An implementation of Query that lets you define place-holders for queries
 * that have not yet been defined.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class QueryHolder implements Query {
  private Query query;
  
  public QueryHolder() {
  }
  
  public void setQuery(Query query) throws NestedException {
    if(query != null) {
      throw new NestedException("Can't set query once it has been set.");
    }
    
    this.query = query;
  }
  
  public Set getNodes() {
    return query.getNodes();
  }
  
  public Map getArcsToOperators() {
    return query.getArcsToOperators();
  }
  
  public Set getOperations(Arc arc) {
    return query.getOperations(arc);
  }

  public Set getArcsFrom(Node from) {
    return query.getArcsFrom(from);
  }
  
  public Set getArcsTo(Node to) {
    return query.getArcsTo(to);
  }
}
