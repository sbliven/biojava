package org.biojava.utils.query;

import java.util.*;
import java.lang.reflect.*;

/**
 * An n'tuple.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public interface Tuple {
  /**
   * The types for each slot in an n'tuple.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public interface ClassList {
    /**
     * The class associated with a slot index.
     *
     * @param indx the slot index
     * @return the Class at that slot
     */
    public Class getClass(int indx);
    
    /**
     * The number of slots in any n'tuple with this ClassList.
     *
     * @return the size of the n'tuple
     */
    public int size();
  }
  
  /**
   * Retrieve the Object at an index.
   * <P>
   * The Object at an index should be assignable to the Class at the same index
   * in the associated ClassList.
   *
   * @param indx
   * @return the Object at that index
   */
  public Object getObject(int indx);
  
  /**
   * Retrieve the ClassList that defines the types of the slots in this n'tuple.
   *
   * @return the ClassList associated with this Tuple
   */
  public ClassList getClassList();
  
  /**
   * Follow from a Tuple to the Object in a given slot.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public static class FollowObject extends Follow {
    private final int indx;
    private final ClassList classList;
    
    public FollowObject(ClassList classList, int indx) {
      this.classList = classList;
      this.indx = indx;
    }
    
    public Queryable follow(Object item) {
      Tuple ti = (Tuple) item;
      
      return QueryTools.createSingleton(ti.getObject(indx));
    }
    
    public Class getInputClass() {
      return Tuple.class;
    }
    
    public Class getOutputClass() {
      return classList.getClass(indx);
    }
    
    public int getIndex() {
      return indx;
    }
    
    public ClassList getClassList() {
      return classList;
    }
  }
  
  
  /**
   * Follow a Tuple to a new Tuple made by including some arbitrary list of
   * columns from a source Tuple.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public static class Permutate extends Follow {
    private final int[] order;
    
    public Permutate(int[] order) {
      this.order = order;
    }
    
    public Queryable follow(Object item) {
      Tuple ti = (Tuple) item;
      
      return QueryTools.createSingleton(
        new PermutedTuple(ti, order)
      );
    }
    
    public Class getInputClass() {
      return Tuple.class;
    }
    
    public Class getOutputClass() {
      return Tuple.class;
    }
  }
  
  /**
   * Perform a normal Follow operation, but return the Tuple (input, result)
   * rather than just (result).
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public static class FollowToTuple extends Follow {
    private final Follow follow;
    private final ClassList classList;
    
    public FollowToTuple(Follow follow) {
      this.follow = follow;
      classList = new SimpleTuple.ClassList(new Class[] {
        follow.getInputClass(),
        follow.getOutputClass(),
      });
    }
    
    public Queryable follow(Object item)
    throws OperationException {
      Queryable res = follow.follow(item);
      Set items = new HashSet();
      
      for(Iterator i = res.iterator(); i.hasNext(); ) {
        Object o = i.next();
        items.add(new SimpleTuple(new Object[] { item, o }, classList));
      }
      
      return QueryTools.createQueryable(items, getOutputClass());
    }
    
    public Class getInputClass() {
      return follow.getInputClass();
    }
    
    public Class getOutputClass() {
      return Tuple.class;
    }
  }
  
  public static class FilterByIndex extends Filter {
    private final Filter filter;
    private int indx;
    
    public Filter getFilter() {
      return filter;
    }
    
    public int getIndex() {
      return indx;
    }
    
    public FilterByIndex(Filter filter, int indx) {
      this.filter = filter;
      this.indx = indx;
    }
    
    public boolean accept(Object item)
    throws OperationException {
      Tuple tup = (Tuple) item;
      return filter.accept(tup.getObject(indx));
    }
    
    public Class getInputClass() {
      return Tuple.class;
    }
    
    public Class getOutputClass() {
      return Tuple.class;
    }
    
    public int hashCode() {
      return filter.hashCode();
    }
    
    public boolean equals(Object o) {
      if(o instanceof Tuple.FilterByIndex) {
        Tuple.FilterByIndex that = (Tuple.FilterByIndex) o;
        return that.indx == this.indx && that.filter.equals(this.filter);
      } else {
        return false;
      }
    }
  }

  /**
   * Produce a new Tuple by replacing one of the slots with the result of
   * another Follow.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public static class FollowTupleTo extends Follow {
    private final Follow follow;
    private final int indx;
    private final ClassList classList;
    
    public Follow getFollow() {
      return follow;
    }
    
    public int getIndex() {
      return indx;
    }
    
    public ClassList getClassList() {
      return classList;
    }
    
    public FollowTupleTo(int indx, Follow follow, ClassList classList) {
      this.indx = indx;
      this.follow = follow;
      Class[] classes = new Class[classList.size()];
      for(int i = 0; i < classes.length; i++) {
        classes[i] = (i == indx) ? follow.getOutputClass()
                                 : classList.getClass(i);
      }
      this.classList = new SimpleTuple.ClassList(classes);
    }
    
    public Queryable follow(Object item)
    throws OperationException {
      Tuple tup = (Tuple) item;
      if(tup.getClassList().size() != classList.size()) {
        throw new OperationException(
          "Can't apply " + tup.getClassList() + " to " + classList
        );
      }
      Queryable res = follow.follow(tup.getObject(indx));
      Set items = new HashSet();
      
      for(Iterator ri = res.iterator(); ri.hasNext(); ) {
        Object o = ri.next();
        Object [] values = new Object[classList.size()];
        for(int i = 0; i < values.length; i++) {
          values[i] = (i == indx) ? o : tup.getObject(i);
        }
        items.add(new SimpleTuple(values, classList));
      }
      
      return QueryTools.createQueryable(items, getOutputClass());
    }
    
    public Class getInputClass() {
      return Tuple.class;
    }
    
    public Class getOutputClass() {
      return Tuple.class;
    }
    
    public int hashCode() {
      return follow.hashCode() ^ classList.hashCode();
    }
    
    public boolean equals(Object o) {
      if(o instanceof Tuple.FollowTupleTo) {
        Tuple.FollowTupleTo that = (Tuple.FollowTupleTo) o;
        return
          this.getFollow().equals(that.getFollow()) &&
          this.getClassList().equals(that.getClassList()) &&
          this.getIndex() == that.getIndex();
      } else {
        return false;
      }
    }
    
    public String toString() {
      return "FollowTupleTo index: " + indx + " follow: " + follow;
    }
  }
  
  /**
   * Invoke a method on the right-most items in the Tuple.
   * <P>
   * The method will be invoked by pulling one item from the right of the tuple
   * per method argument and then replacing the remaining right-most item with
   * the result of invoking the method on that item. The items consumed will be
   * passed into the method invocation from left-to-right.
   * <P>
   * The method should return a non-void result.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public static class FollowMethod extends Follow {
    private final Method method;
    private final ClassList classList;
    private final int indx;
    
    public Method getMethod() {
      return method;
    }
    
    public ClassList getClassList() {
      return classList;
    }
    
    public FollowMethod(Method method, ClassList classList) {
      this.method = method;
      Class methodClass = method.getDeclaringClass();
      
      Class[] paramTypes = method.getParameterTypes();
      Class returnType = method.getReturnType();
      
      this.indx = classList.size() - paramTypes.length;
      
      if(!methodClass.isAssignableFrom(classList.getClass(indx-1))) {
        throw new IllegalArgumentException("Can't apply " + method + " to "
        + methodClass + " at index " + (indx-1) + " in " + classList);
      }
      for(int i = indx; i < classList.size(); i++) {
        if(!paramTypes[i-indx].isAssignableFrom(classList.getClass(indx))) {
          throw new IllegalArgumentException("Illegal argument type in " + method +
          " at index " + (i) + " in " + classList );
        }
      }
      
      Class[] classArray = new Class[indx];
      
      for(int i = 0; i < indx-1; i++) {
        classArray[i] = classList.getClass(i);
      }
      classArray[indx-1] = returnType;
      
      this.classList = new SimpleTuple.ClassList(classArray);
    }
    
    public Queryable follow(Object item)
    throws OperationException {
      Tuple tup = (Tuple) item;
      Object[] params = new Object[method.getParameterTypes().length];
      for(int i = 0; i < params.length; i++) {
        params[i] = tup.getObject(i+indx);
      }
      
      Object val;
      try {
        val = method.invoke(tup.getObject(indx-1), params);
      } catch (InvocationTargetException ite) {
        throw new OperationException(ite, "Couldn't invoke method " + method + " on " + tup.getObject(indx-1));
      } catch (IllegalAccessException iae) {
        throw new OperationException(iae, "Couldn't access method" + method + " on " + tup.getObject(indx-1));
      } catch (IllegalArgumentException iae) {
        throw new OperationException(
          iae,
          "Wrong arguments for" + 
          "\n\tmethod " + method +
          "\n\ton " + tup.getObject(indx-1) +
          "\n\tin tuple of type " + tup.getClassList() +
          "\n\ton tuple " + tup +
          "\n\twith index " + indx);
      }
      
      if(indx == 1) {
        return QueryTools.createSingleton(val);
      } else {
        Object[] items = new Object[indx];
        for(int i = 0; i < indx-1; i++) {
          items[i] = tup.getObject(i);
        }
        items[indx-1] = val;
        return QueryTools.createSingleton(new SimpleTuple(items, classList));
      }
    }
    
    public Class getInputClass() {
      return Tuple.class;
    }
    
    public Class getOutputClass() {
      if(indx == 1) {
        return method.getReturnType();
      } else {
        return Tuple.class;
      }
    }
    
    public int hashCode() {
      return method.hashCode() ^ classList.hashCode();
    }
    
    public boolean equals(Object o) {
      if(o instanceof Tuple.FollowMethod) {
        Tuple.FollowMethod that = (Tuple.FollowMethod) o;
        return
          that.getClassList().equals(this.getClassList()) &&
          that.getMethod().equals(this.getMethod());
      } else {
        return false;
      }
    }
    
    public String toString() {
      return "Tuple.FollowMethod: " + method + " classList: " + classList;
    }
  }
}
