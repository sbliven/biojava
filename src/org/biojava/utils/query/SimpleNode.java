package org.biojava.utils.query;

/**
 * A no-frills implementation of node.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class SimpleNode implements Node {
  private final Class clazz;
  private final String label;
  
  public SimpleNode(String label, Class clazz) {
    this.label = label;
    this.clazz = clazz;
  }
  
  public Class getInputClass() {
    return clazz;
  }
  
  public Class getOutputClass() {
    return clazz;
  }
  
  public String toString() {
    return label;
  }
}
