package org.biojava.utils;

import java.util.*;

/**
 * Simple wrapper to assist in list-comparisons.
 *
 * @author Thomas Down
 * @deprecated this violates the List hashCode contract and is very inefficient
 */
public class ListWrapper implements java.io.Serializable {
  private List l; // should be moved private

  public ListWrapper(List l) {
    this.l = l;
  }

  public ListWrapper() {
  }
    
  /**
   * Assigns a list of objects to a different list of objects.
   * This aids in comparison purposes, especially while doing hash lookups etc.
   * Please note that once a list has been assigned with setList, this 
   * function should not be re-called.
   *
   * @param l the list to assign to the current list.
   */
  public void setList(List l) {
    this.l = l;
  }
  
  /**
   * Retrieve the currently wrapped list.
   *
   * @return the currently wrapped list
   */
  public List getList() {
    return l;
  }
    
  /**
   * Two lists are equal if they contain all the same objects in the same order.
   */
  public boolean equals(Object o) {
    if (! (o instanceof ListWrapper)) {
      return false;
    }
    List ol = ((ListWrapper) o).l;
    if (ol.size() != l.size()) {
      return false;
    }
    Iterator i1 = l.iterator();
    Iterator i2 = ol.iterator();
    while (i1.hasNext()) {
      if (i1.next() != i2.next()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Hashcode is the sum of the hashcodes for the list items.
   */
  public int hashCode() {
    int c = 0;
    for (Iterator i = l.iterator(); i.hasNext(); ) {
      c += i.next().hashCode();
    }
    return c;
  }
}

