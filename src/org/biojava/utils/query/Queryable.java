package org.biojava.utils.query;

import java.util.*;

import org.biojava.utils.*;

/**
 * A set of items with an associated class.
 * <P>
 * This is loosely based upon the Collections Set interface.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public interface Queryable {
  /**
   * The number of items in the Queryable.
   *
   * @return the number of items
   */
  int size();
  
  /**
   * An iterator over every item in this Queryable.
   *
   * @return an Iterator of the items
   */
  Iterator iterator();
  
  /**
   * Every item in this Queryable is castable to this class.
   *
   * @param the class of contained items
   */
  Class getQueryClass();
  
  /**
   * Find out if an item is contained within this Queryable.
   *
   * @return true if it is, false otherwise
   */

   boolean contains(Object item);
  /**
   * An implementation optimized for the case of no items.
   *
   * @author Matthew Pocock
   * @since 1.2
   */

  public class Empty
  implements Queryable {
    private Class clazz;
    
    public Empty(Class clazz) {
      this.clazz = clazz;
    }
    
    public int size() {
      return 0;
    }
    
    public Iterator iterator() {
      return Collections.EMPTY_SET.iterator();
    }
    
    public Class getQueryClass() {
      return clazz;
    }
    
    public boolean contains(Object item) {
      return false;
    }
    
    public int hashCode() {
      return Collections.EMPTY_SET.hashCode();
    }
    
    public boolean equals(Object o) {
      if(o instanceof Queryable) {
        Queryable that = (Queryable) o;
        if(that.size() == this.size()) {
          return true;
        }
      }
      return false;
    }
    
    public String toString() {
      return Collections.EMPTY_SET.toString();
    }
  }
  
  /**
   * An implementation optimized for the case of 1 item.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public class Singleton
  implements Queryable {
    private Object item;
    
    public Object getItem() {
      return item;
    }
    
    public Singleton(Object item) {
      this.item = item;
    }
    
    public int size() {
      return 1;
    }
    
    public Iterator iterator() {
      return Collections.singleton(item).iterator();
    }
    
    public Class getQueryClass() {
      return item.getClass();
    }
    
    public boolean contains(Object item) {
      return this.item.equals(item);
    }
    
    public int hashCode() {
      return item.hashCode();
    }
    
    public boolean equals(Object o) {
      if(o instanceof Queryable) {
        Queryable that = (Queryable) o;
        if(
          that.size() == this.size() &&
          this.item.equals(that.iterator().next())
        ) {
          return true;
        }
      }
      return false;
    }
    
    public String toString() {
      return "{" + item.toString() + "}";
    }
  }
}
