package org.biojava.utils.query;

import java.util.*;

/**
 * A no-frills implementation of Query.
 *
 * @author Matthew Pocock
 * @version 1.2
 */
class SimpleQuery implements Query {
  private final Set nodes;
  private final Map operationsToLabel;
  private final Map arcsFrom;
  private final Map arcsTo;
  
  public SimpleQuery(Set nodes, Map operationsToLabel) {
    this.nodes = nodes;
    this.operationsToLabel = operationsToLabel;
    this.arcsFrom = new HashMap();
    this.arcsTo = new HashMap();
    
    for(Iterator i = operationsToLabel.keySet().iterator(); i.hasNext(); ) {
      Arc arc = (Arc) i.next();
      getSet(arc.from, arcsFrom).add(arc);
      getSet(arc.to, arcsTo).add(arc);
    }
  }
  
  public Set getNodes() {
    return nodes;
  }
  
  public Set getOperations(Arc arc) {
    Set ops = (Set) operationsToLabel.get(arc);
    if(ops == null) {
      if(nodes.contains(arc.from) && nodes.contains(arc.to)) {
        ops = Collections.EMPTY_SET;
      } else {
        throw new NullPointerException("Can't find data for arc: " + arc);
      }
    }
    return ops;
  }
  
  public Map getArcsToOperators() {
    return operationsToLabel;
  }
  
  public Set getArcsFrom(Node from) {
    Set fromSet = (Set) arcsFrom.get(from);
    if(fromSet == null) {
      fromSet = Collections.EMPTY_SET;
    }
    return fromSet;
  }
  
  public Set getArcsTo(Node to) {
    Set toSet = (Set) arcsTo.get(to);
    if(toSet == null) {
      toSet = Collections.EMPTY_SET;
    }
    return toSet;
  }
  
  private Set getSet(Object key, Map map) {
    Set result = (Set) map.get(key);
    if(result == null) {
      map.put(key, result = new HashSet());
    }
    return result;
  }
  
  public int hashCode() {
    return nodes.hashCode();
  }
  
  public boolean equals(Object o) {
    if(o instanceof Query) {
      Query that = (Query) o;
      return
        that.getNodes() == this.getNodes() &&
        that.getArcsToOperators() == this.getArcsToOperators();
    } else {
      return false;
    }
  }
}
