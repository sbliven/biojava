package org.biojava.utils.query;

/**
 * A no-frills implementation of result node.
 *
 * @author Matthew Pocock
 * @since 1.2
 */

public class SimpleResultNode extends SimpleNode implements ResultNode {
  public SimpleResultNode(String label, Class clazz) {
    super(label, clazz);
  }
  
  public String toString() {
    return super.toString() + " *";
  }
}
