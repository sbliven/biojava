package org.biojava.utils.query;

/**
 * A node in a query graph that represents an intermediate Queryable.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public interface Node {
  public Class getInputClass();
  public Class getOutputClass();
}
