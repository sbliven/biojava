package org.biojava.utils.query;

import java.util.Iterator;

/**
 * An operation that is equivalent to following every item in the query set to a
 * destination set (possibly empty) and then returning the union of these.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public abstract class Follow implements Operation {
  public Queryable apply(Queryable items) {
    Queryable result = new Queryable.Empty(getOutputClass());
    for(Iterator i = items.iterator(); i.hasNext(); ) {
      Object o = (Object) i.next();
      Queryable fo = follow(o);
      result = QueryTools.union(result, fo);
    }
    return result;
  }
  
  /**
   * This method should implement the process of following a single item to its
   * result set.
   *
   * @param item the Object to follow from
   * @return a Queryable containing every item (zero, one or more) that is
   *         reached with this follow
   */
  public abstract Queryable follow(Object item);
}
