package org.biojava.utils.query;

import java.util.*;
import java.lang.reflect.*;

/**
 * An operation that is equivalent to following every item in the query set to a
 * destination set (possibly empty) and then returning the union of these.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public abstract class Follow implements Operation {
  public Queryable apply(Queryable items)
  throws OperationException {
    Queryable result = new Queryable.Empty(getOutputClass());
    for(Iterator i = items.iterator(); i.hasNext(); ) {
      Object o = (Object) i.next();
      Queryable fo = follow(o);
      result = QueryTools.union(result, fo);
    }
    return result;
  }
  
  public boolean isSubsetOf(Operation op) {
    return op instanceof Filter.RejectAll || op.equals(this);
  }
  
  public boolean isDisjoint(Operation op) {
    return false;
  }
  
  /**
   * This method should implement the process of following a single item to its
   * result set.
   *
   * @param item the Object to follow from
   * @return a Queryable containing every item (zero, one or more) that is
   *         reached with this follow
   */
  public abstract Queryable follow(Object item)
  throws OperationException;
  
  /**
   * Follow from a collection to all items within it.
   * <P>
   * This is usefull for merging collections into a single Queryable if, for
   * Set of items was returned from a method.
   *
   * @since 1.2
   * @author Matthew Pocock
   */
  public static final class FollowCollectionToMembers extends Follow {
    private final Class outputClass;
    
    public FollowCollectionToMembers(Class outputClass) {
      this.outputClass = outputClass;
    }
    
    public Queryable follow(Object item) {
      Set si = (Set) item;
      return QueryTools.createQueryable(
        si,
        getOutputClass()
      );
    }
    
    public Class getInputClass() {
      return Set.class;
    }
    
    public Class getOutputClass() {
      return outputClass;
    }
  }
  
  /**
   * Follow from items to the results of a method invocation.
   * <P>
   * Ususaly, you will build FollowMethod instances from methods defined in
   * interfaces to give you generic query access to all instances.
   *
   * @since 1.2
   * @author Matthew Pocock
   */
  public static final class FollowMethod extends Follow {
    public static final Object[] EMPTY_ARGS = new Object[] {};
    public static final Class[] EMPTY_CLASSES = new Class[] {};
    private final Method method;
    
    public FollowMethod(Method method) {
      if(method.getParameterTypes().length > 0) {
        throw new IllegalArgumentException("Method must be no-args");
      }
      this.method = method;
    }
    
    public Queryable follow(Object item)
    throws OperationException {
      try {
        return QueryTools.createSingleton(method.invoke(item, EMPTY_ARGS));
      } catch (InvocationTargetException ite) {
        throw new OperationException(ite, "Couldn't invoke method");
      } catch (IllegalAccessException iae) {
        throw new OperationException(iae, "Couldn't access method");
      }
    }
    
    public Class getInputClass() {
      return method.getDeclaringClass();
    }
    
    public Class getOutputClass() {
      return method.getReturnType();
    }
  }
}
