package org.biojava.utils.query;

import java.util.*;

import org.biojava.utils.*;

class QueryData {
  protected final Set nodes;
  protected final Map operationsToLabel;
  protected final Map arcsFrom;
  protected final Map arcsTo;
  
  {
    nodes = new HashSet();
    operationsToLabel = new HashMap();
    arcsFrom = new HashMap();
    arcsTo = new HashMap();
  }
  
  protected QueryData() {};
  
  protected QueryData(QueryData qd) {
    this.nodes.addAll(qd.nodes);
    this.operationsToLabel.putAll(qd.operationsToLabel);
    this.arcsFrom.putAll(qd.arcsFrom);
    this.arcsTo.putAll(qd.arcsTo);
  }
  
  public final Set getNodes() {
    return Collections.unmodifiableSet(nodes);
  }
  
  public final Set getOperations(Arc arc) {
    Set ops = (Set) operationsToLabel.get(arc);
    if(ops == null) {
      ops = Collections.EMPTY_SET;
    } else {
      ops = Collections.unmodifiableSet(ops);
    }
    return ops;
  }
  
  public final Map getArcsToOperators() {
    return Collections.unmodifiableMap(operationsToLabel);
  }
  
  public final Set getArcsFrom(Node from) {
    Set fromSet = (Set) arcsFrom.get(from);
    if(fromSet == null) {
      fromSet = Collections.EMPTY_SET;
    } else {
      fromSet = Collections.unmodifiableSet(fromSet);
    }
    return fromSet;
  }
  
  public final Set getArcsTo(Node to) {
    Set toSet = (Set) arcsTo.get(to);
    if(toSet == null) {
      toSet = Collections.EMPTY_SET;
    } else {
      toSet = Collections.unmodifiableSet(toSet);
    }
    return toSet;
  }
}
