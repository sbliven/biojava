package org.biojava.utils.query;

/**
 * A no-frills implementation of node.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class SimpleNode implements Node {
  private final Type type;
  private final String label;
  
  public SimpleNode(String label, Type type) {
    this.label = label;
    this.type = type;
  }
  
  public Type getInputType() {
    return type;
  }
  
  public Type getOutputType() {
    return type;
  }
  
  public String getLabel() {
    return label;
  }
  
  public String toString() {
    return label;
  }
}
