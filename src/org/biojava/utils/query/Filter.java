package org.biojava.utils.query;

import java.util.*;

/**
 * An operation that is equivalent to including or excluding every item based
 * upon a simple boolean outcome.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public abstract class Filter extends Follow {
  public Queryable follow(Object item) {
    if(accept(item)) {
      return new Queryable.Singleton(item);
    } else {
      return new Queryable.Empty(getOutputClass());
    }
  }
  
  public Queryable apply(Queryable items) {
    Set matches = new HashSet();
    for(Iterator i = items.iterator(); i.hasNext(); ) {
      Object o = i.next();
      if(accept(o)) {
        matches.add(o);
      }
    }
    return QueryTools.createQueryable(matches, getOutputClass());
  }
  /**
   * Decide wether to accept or reject an item.
   *
   * @param item  the Object to accept or reject
   * @return true if it should be accepted, false otherwise
   */
  public abstract boolean accept(Object item);
  
  /**
   * An implementation of Filter that will reject every item.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public final class RejectAll
  extends Filter {
    private final Class clazz;
    
    public RejectAll(Class clazz) {
      this.clazz = clazz;
    }
    
    public boolean accept(Object item) {
      return false;
    }
    
    public Class getInputClass() {
      return clazz;
    }
    
    public Class getOutputClass() {
      return clazz;
    }
  }
  
  /**
   * An implementation of Filter that will accept every item.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public final class AcceptAll
  extends Filter {
    private final Class clazz;

    public AcceptAll(Class clazz) {
      this.clazz = clazz;
    }
    
    public boolean accept(Object item) {
      return false;
    }

    public Class getInputClass() {
      return clazz;
    }
    
    public Class getOutputClass() {
      return clazz;
    }
  }
  
  /**
   * Accepts an item if it is equal to the Object associated with the Filter.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public final static class Equals extends Filter {
    private final Object item;
    private final Class clazz;
    
    public Equals(Object item, Class clazz) {
      this.item = item;
      this.clazz = clazz;
    }
    
    public Object getItem() {
      return item;
    }
    
    public boolean accept(Object item) {
      return this.item.equals(item);
    }

    public Class getInputClass() {
      return clazz;
    }
    
    public Class getOutputClass() {
      return clazz;
    }
  }
}
