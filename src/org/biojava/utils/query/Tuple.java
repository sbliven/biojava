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
  public abstract class TypeList implements Type {
    /**
     * The type associated with a slot index.
     *
     * @param indx the slot index
     * @return the Type at that slot
     */
    public abstract Type getType(int indx);
    
    /**
     * The number of slots in any n'tuple with this TypeList.
     *
     * @return the size of the n'tuple
     */
    public abstract int size();
    
    public boolean isInstance(Object obj) {
      if(obj instanceof Tuple) {
        Tuple tup = (Tuple) obj;
        return isAssignableFrom(tup.getTypeList());
      }
      
      return false;
    }
    
    public boolean isAssignableFrom(Type type) {
      if(type instanceof Tuple.TypeList) {
        Tuple.TypeList typeList = (Tuple.TypeList) type;
        if(size() != typeList.size()) {
          return false;
        }
        for(int i = 0; i < size(); i++) {
          if(!getType(i).equals(typeList.getType(i))) {
            return false;
          }
          return true;
        }
      }
      
      return false;
    }
    
    public int hashCode() {
      int hc = getType(0).hashCode();
      for(int i = 1; i < size(); i++) {
        hc = hc ^ getType(i).hashCode();
      }
      return hc;
    }
    
    public boolean equals(Object o) {
      if(o instanceof Tuple.TypeList) {
        Tuple.TypeList tl = (Tuple.TypeList) o;
        if(size() == tl.size()) {
          for(int i = 0; i < size(); i++) {
            if(getType(i) != tl.getType(i)) {
              return false;
            }
          }
          return true;
        }
      }
      
      return false;
    }
    
    public String getName() {
      StringBuffer sb = new StringBuffer("(");
      sb.append(getType(0).toString());
      for(int i = 1; i < size(); i++) {
        sb.append(",");
        sb.append(getType(i).toString());
      }
      sb.append(")");
      return sb.toString();
    }
      
    public String toString() {
      return getName();
    }
  }
  
  /**
   * Retrieve the Object at an index.
   * <P>
   * The Object at an index should be assignable to the Type at the same index
   * in the associated TypeList.
   *
   * @param indx
   * @return the Object at that index
   */
  public Object getObject(int indx);
  
  /**
   * Retrieve the TypeList that defines the types of the slots in this n'tuple.
   *
   * @return the TypeList associated with this Tuple
   */
  public TypeList getTypeList();
  
  /**
   * Follow from a Tuple to the Object in a given slot.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public static class FollowObject extends Follow {
    private final int indx;
    private final TypeList typeList;
    
    public FollowObject(TypeList typeList, int indx) {
      this.typeList = typeList;
      this.indx = indx;
    }
    
    public Queryable follow(Object item) {
      Tuple ti = (Tuple) item;
      
      return QueryTools.createSingleton(ti.getObject(indx));
    }
    
    public Type getInputType() {
      return typeList;
    }
    
    public Type getOutputType() {
      return typeList.getType(indx);
    }
    
    public int getIndex() {
      return indx;
    }
    
    public TypeList getTypeList() {
      return typeList;
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
    private final Tuple.TypeList input;
    private final PermutedTuple.TypeList output;
    
    public Permutate(int[] order, Tuple.TypeList inputTypeList) {
      this.order = order;
      this.input = inputTypeList;
      this.output = new PermutedTuple.TypeList(order, input);
    }
    
    public Queryable follow(Object item) {
      Tuple ti = (Tuple) item;
      
      return QueryTools.createSingleton(
        new PermutedTuple(ti, output)
      );
    }
    
    public Type getInputType() {
      return input;
    }
    
    public Type getOutputType() {
      return output;
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
    private final TypeList typeList;
    
    public FollowToTuple(Follow follow) {
      this.follow = follow;
      typeList = new SimpleTuple.TypeList(new Type[] {
        follow.getInputType(),
        follow.getOutputType(),
      });
    }
    
    public Queryable follow(Object item)
    throws OperationException {
      Queryable res = follow.follow(item);
      Set items = new HashSet();
      
      for(Iterator i = res.iterator(); i.hasNext(); ) {
        Object o = i.next();
        items.add(new SimpleTuple(new Object[] { item, o }, typeList));
      }
      
      return QueryTools.createQueryable(items, getOutputType());
    }
    
    public Type getInputType() {
      return follow.getInputType();
    }
    
    public Type getOutputType() {
      return typeList;
    }
  }
  
  public static class FilterByIndex extends Filter {
    private final Filter filter;
    private final int indx;
    private final Tuple.TypeList typeList;
    
    public Filter getFilter() {
      return filter;
    }
    
    public int getIndex() {
      return indx;
    }
    
    public FilterByIndex(Filter filter, int indx, Tuple.TypeList typeList) {
      this.filter = filter;
      this.indx = indx;
      this.typeList = typeList;
    }
    
    public boolean accept(Object item)
    throws OperationException {
      Tuple tup = (Tuple) item;
      return filter.accept(tup.getObject(indx));
    }
    
    public Type getInputType() {
      return typeList;
    }
    
    public Type getOutputType() {
      return typeList;
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
    
    public String toString() {
      return
        "Tuple.FilterByIndex[index=" + indx +
        " filter=" + filter + "]";
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
    private final TypeList inputTypeList;
    private final TypeList outputTypeList;
    
    public Follow getFollow() {
      return follow;
    }
    
    public int getIndex() {
      return indx;
    }
    
    public FollowTupleTo(int indx, Follow follow, TypeList typeList) {
      this.indx = indx;
      this.follow = follow;
      this.inputTypeList = typeList;
      Type[] types = new Type[typeList.size()];
      for(int i = 0; i < types.length; i++) {
        types[i] = (i == indx) ? follow.getOutputType()
                                 : typeList.getType(i);
      }
      this.outputTypeList = new SimpleTuple.TypeList(types);
    }
    
    public Queryable follow(Object item)
    throws OperationException {
      Tuple tup = (Tuple) item;
      if(!inputTypeList.isAssignableFrom(tup.getTypeList())) {
        throw new TypeCastException(
          "Can't apply " + tup.getTypeList() + " to " + inputTypeList
        );
      }
      Queryable res = follow.follow(tup.getObject(indx));
      Set items = new HashSet();
      
      for(Iterator ri = res.iterator(); ri.hasNext(); ) {
        Object o = ri.next();
        Object [] values = new Object[outputTypeList.size()];
        for(int i = 0; i < values.length; i++) {
          values[i] = (i == indx) ? o : tup.getObject(i);
        }
        items.add(new SimpleTuple(values, outputTypeList));
      }
      
      return QueryTools.createQueryable(items, getOutputType());
    }
    
    public Type getInputType() {
      return inputTypeList;
    }
    
    public Type getOutputType() {
      return outputTypeList;
    }
    
    public int hashCode() {
      return follow.hashCode() ^ inputTypeList.hashCode();
    }
    
    public boolean equals(Object o) {
      if(o instanceof Tuple.FollowTupleTo) {
        Tuple.FollowTupleTo that = (Tuple.FollowTupleTo) o;
        return
          this.getFollow().equals(that.getFollow()) &&
          this.getInputType().equals(that.getInputType()) &&
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
    private final TypeList inputTypeList;
    private final TypeList outputTypeList;
    private final int indx;
    
    public Method getMethod() {
      return method;
    }
    
    public FollowMethod(Method method, TypeList typeList) {
      this.method = method;
      this.inputTypeList = typeList;
      
      Type methodType = JavaType.getType(method.getDeclaringClass());
      Type paramTypes = JavaType.getType(method.getParameterTypes());
      Type returnType = JavaType.getType(method.getReturnType());
      Tuple.TypeList tl = (paramTypes instanceof Tuple.TypeList)
        ? (Tuple.TypeList) paramTypes : null;
      
      this.indx = typeList.size()
        - ((tl == null)
          ? 1 : tl.size());
      
      if(!methodType.isAssignableFrom(typeList.getType(indx-1))) {
        throw new IllegalArgumentException("Can't apply " + method + " to "
        + methodType + " at index " + (indx-1) + " in " + typeList);
      }
      if(tl == null) {
        if(!paramTypes.isAssignableFrom(typeList.getType(indx))) {
          throw new TypeCastException("Illegal argument type in " + method +
          " at index " + (indx) + " in " + typeList );
        }
      } else {
        for(int i = indx; i < tl.size(); i++) {
          if(!tl.getType(i-indx).isAssignableFrom(typeList.getType(indx))) {
            throw new TypeCastException("Illegal argument type in " + method +
            " at index " + (i) + " in " + typeList );
          }
        }
      }
      
      Type[] typeArray = new Type[indx];
      
      for(int i = 0; i < indx-1; i++) {
        typeArray[i] = typeList.getType(i);
      }
      typeArray[indx-1] = returnType;
      
      this.outputTypeList = new SimpleTuple.TypeList(typeArray);
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
          "\n\tin tuple of type " + tup.getTypeList() +
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
        return QueryTools.createSingleton(new SimpleTuple(items, outputTypeList));
      }
    }
    
    public Type getInputType() {
      return inputTypeList;
    }
    
    public Type getOutputType() {
      if(indx == 1) {
        return outputTypeList.getType(0);
      } else {
        return outputTypeList;
      }
    }
    
    public int hashCode() {
      return method.hashCode() ^ getInputType().hashCode();
    }
    
    public boolean equals(Object o) {
      if(o instanceof Tuple.FollowMethod) {
        Tuple.FollowMethod that = (Tuple.FollowMethod) o;
        return
          that.getInputType().equals(this.getInputType()) &&
          that.getMethod().equals(this.getMethod());
      } else {
        return false;
      }
    }
    
    public String toString() {
      return "Tuple.FollowMethod: " + method + " type: " + inputTypeList;
    }
  }
}
