package org.biojava.utils.query;

import java.util.*;

/**
 * A no-frills implementation of Query.
 *
 * @author Matthew Pocock
 * @version 1.2
 */
class SimpleQuery extends QueryData implements Query {
  SimpleQuery(QueryData qd) {
    super(qd);
  }
  
  public int hashCode() {
    return nodes.hashCode();
  }
  
  public boolean equals(Object o) {
    if(o instanceof Query) {
      Query that = (Query) o;
      return
        that.getNodes().equals(this.getNodes()) &&
        that.getArcsToOperators().equals(this.getArcsToOperators());
    } else {
      return false;
    }
  }
}
