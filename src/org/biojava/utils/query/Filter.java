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
      return new Queryable.Empty(getOutputType());
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
    return QueryTools.createQueryable(matches, getOutputType());
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
    private final Type type;
    
    public RejectAll(Type type) {
      this.type = type;
    }
    
    public boolean accept(Object item) {
      return false;
    }
    
    public Type getInputType() {
      return type;
    }
    
    public Type getOutputType() {
      return type;
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
    private final Type type;

    public AcceptAll(Type type) {
      this.type = type;
    }
    
    public boolean accept(Object item) {
      return true;
    }

    public Type getInputType() {
      return type;
    }
    
    public Type getOutputType() {
      return type;
    }
  }
  
  public static final class Not extends Filter {
    private final Filter filter;
    
    public Not(Filter filter) {
      this.filter = filter;
    }
    
    public Filter getFilter() {
      return filter;
    }
    
    public boolean accept(Object item)
    throws OperationException {
      return !filter.accept(item);
    }
    
    public Type getInputType() {
      return getFilter().getInputType();
    }
    
    public Type getOutputType() {
      return getFilter().getOutputType(); // is this true?
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
    private final Type type;
    
    public Equals(Object item, Type type) {
      this.item = item;
      this.type = type;
    }
    
    public Object getItem() {
      return item;
    }
    
    public boolean accept(Object item) {
      return this.item.equals(item);
    }

    public Type getInputType() {
      return type;
    }
    
    public Type getOutputType() {
      return type;
    }
    
    public int hashCode() {
      return item.hashCode();
    }
    
    public boolean equals(Object o) {
      if(o instanceof Filter.Equals) {
        Filter.Equals fe = (Filter.Equals) o;
        return
          this.item.equals(fe.item);
      }
      
      return false;
    }
    
    public String toString() {
      return "Filter.Equals[" + item + "]";
    }
  }
  
  /**
   * Filters by Type.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public final static class ByType extends Filter {
    private Type inputType;
    private Type outputType;
    
    public ByType(Type inputType, Type outputType) {
      if(!inputType.isAssignableFrom(outputType)) {
        throw new TypeCastException(
          "Can't filter to a type that is not a subtype: " +
          inputType + " " + outputType
        );
      }
      this.inputType = inputType;
      this.outputType = outputType;
    }
    
    public boolean accept(Object item) {
      return getOutputType().isInstance(item);
    }
    
    public Type getInputType() {
      return inputType;
    }
    
    public Type getOutputType() {
      return outputType;
    }
    
    public int hashCode() {
      return getInputType().hashCode() ^ getOutputType().hashCode();
    }
    
    public boolean equals(Object o) {
      if(o instanceof Filter.ByType) {
        Filter.ByType fbt = (Filter.ByType) o;
        return
          getInputType().equals(fbt.getInputType()) &&
          getOutputType().equals(fbt.getOutputType());
      }
      
      return false;
    }
    
    public String toString() {
      return "Filter.ByType[" + inputType + " -> " + outputType + "]";
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

    public Type getInputType() {
      return JavaType.getType(Integer.class);
    }
    
    public Type getOutputType() {
      return JavaType.getType(Integer.class);
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
