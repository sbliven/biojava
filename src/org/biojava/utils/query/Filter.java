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
  public Queryable follow(Object item)
  throws OperationException {
    if(accept(item)) {
      return new Queryable.Singleton(item);
    } else {
      return new Queryable.Empty(getOutputClass());
    }
  }
  
  public Queryable apply(Queryable items)
  throws OperationException {
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
  public abstract boolean accept(Object item)
  throws OperationException;
  
  /**
   * An implementation of Filter that will reject every item.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public static final class RejectAll
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
  public static final class AcceptAll
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
  
  /**
   * Filters by class.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public final static class ByClass extends Filter {
    private Class inputClass;
    private Class outputClass;
    
    public ByClass(Class inputClass, Class outputClass) {
      this.inputClass = inputClass;
      this.outputClass = outputClass;
    }
    
    public boolean accept(Object item) {
      return getOutputClass().isInstance(item);
    }
    
    public Class getInputClass() {
      return inputClass;
    }
    
    public Class getOutputClass() {
      return outputClass;
    }
  }
   
  /**
   * Accept an integer based upon some other integer and a comparison operator.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public final static class CompareInteger extends Filter {
    private final int value;
    private final Comparison cmp;
    
    public CompareInteger(int value, Comparison cmp) {
      this.value = value;
      this.cmp = cmp;
    }
    
    public int getValue() {
      return value;
    }

    public Comparison getComparsion() {
      return cmp;
    }
    
    public boolean accept(Object item) {
      Integer i = (Integer) item;
      return cmp.compare(i.intValue(), value);
    }

    public Class getInputClass() {
      return Integer.class;
    }
    
    public Class getOutputClass() {
      return Integer.class;
    }
    
    /**
     * Compare two integers and return wether to accept the first one
     * conditional upon the second.
     *
     * @author Matthew Pocock
     * @since 1.2
     */
    public interface Comparison {
      boolean compare(int a, int b);
    }
    
    public static final Comparison LT = new Comparison() {
      public boolean compare(int a, int b) {
        return a < b;
      }
    };
    public static final Comparison LTEQ = new Comparison() {
      public boolean compare(int a, int b) {
        return a <= b;
      }
    };
    public static final Comparison EQ = new Comparison() {
      public boolean compare(int a, int b) {
        return a == b;
      }
    };
    public static final Comparison GTEQ = new Comparison() {
      public boolean compare(int a, int b) {
        return a >= b;
      }
    };
    public static final Comparison GT = new Comparison() {
      public boolean compare(int a, int b) {
        return a > b;
      }
    };
    public static final Comparison NEQ = new Comparison() {
      public boolean compare(int a, int b) {
        return a != b;
      }
    };
  }
}
