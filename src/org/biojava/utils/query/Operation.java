package org.biojava.utils.query;

/**
 * An Operation takes an entire set of Queryable objects and produces a new
 * Set. Some implementations of this will return a set that is the union of
 * applying some operation to every member of the set. Others may return sets
 * that are not.
 *
 * @author Matthew Pocock
 */
public interface Operation {
  /**
   * All of the items in the input set will be castable to this Class.
   *
   * @return the Class of all input objects
   */
  public Class getInputClass();

  /**
   * All of the items in the output set will be castable to this Class.
   *
   * @return the Class of all output objects
   */
  public Class getOutputClass();
  
  /**
   * Return a Queryable by applying this Operation to <code>items</code>.
   * The items set should be castable to getInputClass and the result set should
   * be castable to getOutputClass.
   *
   * @param items a Queryable containing each item to process
   * @return a Queryable produced by applying this operation
   */
  public Queryable apply(Queryable items);
}
