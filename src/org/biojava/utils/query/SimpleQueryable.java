package org.biojava.utils.query;

import java.util.*;

/**
 * An implementation of Queryable with some simple set-wise mutators.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class SimpleQueryable implements Queryable {
  // package private to allow speed-optimizations - can this be done better?
  final Set items;
  private final Class clazz;
  
  public SimpleQueryable(Set items, Class clazz) {
    this.items = items;
    this.clazz = clazz;
  }
  
  public int size() {
    return items.size();
  }
  
  public Iterator iterator() {
    return items.iterator();
  }
  
  public Class getQueryClass() {
    return clazz;
  }
  
  public boolean contains(Object item) {
    return items.contains(item);
  }
}
