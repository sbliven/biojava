package org.biojava.utils.query;

import java.util.*;

/**
 * Utility routines for manipulating and evaluating queryies and queriables.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class QueryTools {
  /**
   * Calculate the union of two <code>Queriable</code>s.
   * <P>
   * The union contains every item in <code>a</code> and every item in
   * <code>b</code>. No item will be in the return set twice. The implementation
   * of this method will attempt to optimize for the common cases where
   * <code>a</code> or <code>b</code> are empty, singletons or SimpleQueryable
   * instances.
   *
   * @param a the first Queryable
   * @param b the seccond Queriable
   * @return the union
   */
  public static Queryable union(Queryable a, Queryable b)
  throws ClassCastException {
    Class clazz = guessClass(a, b);
    
    if(a instanceof Queryable.Empty) {
      return b;
    } else if(b instanceof Queryable.Empty) {
      return a;
    }
    
    Set result = new HashSet();
    addAll(result, a);
    addAll(result, b);
    return new SimpleQueryable(result, clazz);
  }
  
  private static void addAll(Set result, Queryable items) {
    if(items instanceof Queryable.Singleton) {
      result.add(((Queryable.Singleton) items).getItem());
    } else if(items instanceof SimpleQueryable) {
      result.addAll(((SimpleQueryable) items).items);
    } else {
      for(Iterator i = items.iterator(); i.hasNext(); ) {
        result.add(i.next());
      }
    }
  }

  /**
   * Calculate the intersection of two <code>Queriable</code>s.
   * <P>
   * The intersection contains every item in both <code>a</code> and
   * <code>b</code>. By definition, the return set is a propper sub-set of both
   * input sets.
   * The implementation
   * of this method will attempt to optimize for the common cases where
   * <code>a</code> or <code>b</code> are empty, singletons or SimpleQueryable
   * instances.
   *
   * @param a the first Queryable
   * @param b the seccond Queriable
   * @return the union
   */
  public static Queryable intersection(Queryable a, Queryable b) {
    Class clazz = guessClass(a, b);
    
    if(a instanceof Queryable.Empty || b instanceof Queryable.Empty) {
      return new Queryable.Empty(clazz);
    } else {
      if(b instanceof Queryable.Singleton) {
        Queryable q = b;
        b = a;
        a = q;
      }
      if(a instanceof Queryable.Singleton) {
        Object item = ((Queryable.Singleton) a).getItem();
        if(b.contains(item)) {
          return a;
        } else {
          return new Queryable.Empty(clazz);
        }
      } else {
        Set result = new HashSet();
        if(b.size() < a.size()) {
          Queryable q = b;
          b = a;
          a = q;
        }
        for(Iterator i = a.iterator(); i.hasNext(); ) {
          Object item = i.next();
          if(a.contains(item) && b.contains(item)) {
            result.add(item);
          }
        }
        return new SimpleQueryable(result, clazz);
      }
    }
  }

  /**
   * Attempts to guess a Class from <code>a</code> and <code>b</code>.
   */
  public static Class guessClass(Queryable a, Queryable b) {
    Class clazz;
    
    if(a.getQueryClass().isAssignableFrom(b.getQueryClass())) {
      clazz = b.getQueryClass();
    } else if(b.getQueryClass().isAssignableFrom(a.getQueryClass())) {
      clazz = a.getQueryClass();
    } else {
      clazz = Object.class;
    }
    
    return clazz;
  }
  
  /**
   * Create a new Queryable from a set of items and a Class.
   *
   * @param items  a set of Object instances
   * @param clazz  the Class that all items should implement
   * @return a Queryable containing <code>items</code> and qualified by
   *         Class <code>clazz</code>
   */
  public static Queryable createQueryable(Set items, Class clazz) {
    if(items.size() == 0) {
      return new  Queryable.Empty(clazz);
    } else if(items.size() == 1) {
      return new Queryable.Singleton(items.iterator().next());
    } else {
      return new SimpleQueryable(items, clazz);
    }
  }
  
  /**
   * Process a Query designed to select items.
   *
   * @param query  the Query to process
   * @param currentNode  the node to evaluate
   * @param items  a Queriable containing the items to evaluate
   * @return a Queryable containg every item selected by the query
   */
  public static Queryable select(Query query, Node currentNode, Queryable items) {
    Queryable selected;
    if(currentNode instanceof ResultNode) {
      selected = items;
    } else {
      selected = new Queryable.Empty(items.getQueryClass());
    }
    
    Iterator ai = query.getArcsFrom(currentNode).iterator();
    while(ai.hasNext()) {
      Arc arc = (Arc) ai.next();
      Iterator oi = query.getOperations(arc).iterator();
      while(oi.hasNext()) {
        Operation op = (Operation) oi.next();
        Queryable res = op.apply(items);
        selected = QueryTools.union(selected, select(query, arc.to, res));
      }
    }
    
    return selected;
  }
}
