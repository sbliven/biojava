package org.biojava.utils.query;

/**
 * A pair of nodes that define an arc in a query graph.
 * <P>
 * This class is for the utility of indexing arcs within queries only. It is
 * intended as read-only, but if we need mutable arcs for performance reasons,
 * they can be easily added.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public final class Arc {
  /** The Node defining the source of the arc. */
  public final Node from;
  
  /** The Node defining the destination of the arc. */
  public final Node to;
  
  /**
   * Create a new Arc between two nodes.
   *
   * @param from  the source of the arc
   * @param to  the destination of the arc
   */
  public Arc(Node from, Node to) {
    this.from = from;
    this.to = to;
  }
  
  public int hashCode() {
    return from.hashCode() ^ to.hashCode();
  }
  
  public boolean equals(Object o) {
    if(o instanceof Arc) {
      Arc a = (Arc) o;
      return this.from.equals(a.from) && this.to.equals(a.to);
    } else {
      return false;
    }
  }
  
  public String toString() {
    return "(" + from + ", " + to + ")";
  }
}

