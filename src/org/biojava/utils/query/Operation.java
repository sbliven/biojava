package org.biojava.utils.query;

import java.util.*;

/**
 * An Operation takes an entire set of Queryable objects and produces a new
 * Set. Some implementations of this will return a set that is the union of
 * applying some operation to every member of the set. Others may return sets
 * that are not.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public interface Operation {
  /**
   * All of the items in the input set will be castable to this Type.
   *
   * @return the Type of all input objects
   */
  public Type getInputType();

  /**
   * All of the items in the output set will be castable to this Type.
   *
   * @return the Type of all output objects
   */
  public Type getOutputType();
  
  /**
   * Return wether this operation selects a subset of the items selected
   * by op.
   * <P>
   * Given any queryable, you can apply both op and this to it to produce two
   * sets of items. If the items returned by this is always guaranteed to be a
   * subset of the items returned by super regardless of the input set,
   * then return true. Otherwise return false. The empty set is vacuously a
   * subset of all sets. The set of everything is clearly a propper
   * superset of all sets. Any set is a subset of itself.
   *
   * @param op  the prospective subset operator
   * @return true if sub returns a subset of items returned by super
   */
  public boolean isSubsetOf(Operation op);
  
  /**
   * Return wether the two operations are guaranteed to return disjoint items.
   * <P>
   * Given any queryable, you can apply both operations to it to produce two
   * sets of items. If the items returned by the two operations have an empty 
   * intersection given any input set then return true. Otherwise return false.
   * The empty set is disjoint from itself as the intersection is empty.
   * <P>
   * Trivialy, given two sets A and B, A is disjoint from B if A is some propper
   * subset of C and B is some subset of not(C).
   *
   * @param op  the seccond Operation
   * @return true if the two operations are disjoint
   */
  public boolean isDisjoint(Operation op);
  
  /**
   * Return a Queryable by applying this Operation to <code>items</code>.
   * The items set should be castable to getInputType and the result set should
   * be castable to getOutputType.
   *
   * @param items a Queryable containing each item to process
   * @return a Queryable produced by applying this operation
   * @throws ClassCastException if the type-checking fails
   * @throws OperationException if the operation fails
   */
  public Queryable apply(Queryable items)
  throws OperationException;
  
  /**
   * Returns a count of the number of items in the Queryable.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public static final class Count implements Operation {
    private final Type inputType;
    
    public Count(Type inputType) {
      this.inputType = inputType;
    }
    
    public Type getInputType() {
      return inputType;
    }
    
    public Type getOutputType() {
      return JavaType.getType(Integer.class);
    }
    
    public boolean isSubsetOf(Operation op) {
      return op instanceof Filter.RejectAll || op.equals(this);
    }
    
    public boolean isDisjoint(Operation op) {
      return !this.equals(op);
    }
    
    public Queryable apply(Queryable items) {
      return QueryTools.createQueryable(
        Collections.singleton(new Integer(items.size())),
        getOutputType()
      );
    }
  }
}
