package org.biojava.utils.query;

import java.util.*;

import org.biojava.utils.*;

/**
 * A helper class for creating query objects.
 * <P>
 * Use a QueryBuilder instance to do the book-keeping for making a query graph.
 * The add/remove node/query/arc methods make sure that all the wiring works
 * out. Then, invoke buildQuery to get the query with this architecture.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class QueryBuilder {
  private final Set nodes;
  private final Map operationsToLabel;
  private final Map arcsFrom;
  private final Map arcsTo;
  
  {
    nodes = new HashSet();
    operationsToLabel = new HashMap();
    arcsFrom = new HashMap();
    arcsTo = new HashMap();
  }
  
  public void addQuery(Query query) {
    for(
      Iterator i = query.getArcsToOperators().entrySet().iterator();
      i.hasNext();
    ) {
      Map.Entry arcOp = (Map.Entry) i.next();
      Arc arc = (Arc) arcOp.getKey();
      Operation op = (Operation) arcOp.getValue();
      try {
        addArc(arc, op);
      } catch (OperationException oe) {
        throw new NestedError(oe, "This should never happen");
      }
    }
  }
  
  public Set getNodes() {
    return nodes;
  }
  
  public Set getOperations(Arc arc) {
    Set ops = (Set) operationsToLabel.get(arc);
    if(ops == null) {
      ops = Collections.EMPTY_SET;
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
  
  public void addNode(Node node) {
    nodes.add(node);
  }
  
  public void removeNode(Node node) {
    nodes.remove(node);
    
    for(Iterator i = getArcsFrom(node).iterator(); i.hasNext(); ) {
      Arc arc = (Arc) i.next();
      removeArc(arc);
    }
  }
  
  public void addArc(Arc arc, Operation op)
  throws OperationException {
    if(!op.getInputClass().isAssignableFrom(arc.from.getOutputClass())) {
      throw new OperationException("Can't assign " + arc.from.getOutputClass() +
      " to " + op.getInputClass());
    }
    if(!op.getOutputClass().isAssignableFrom(arc.to.getInputClass())) {
      throw new OperationException("Can't assign " +  op.getOutputClass() +
      " to " + arc.to.getInputClass());
    }

    nodes.add(arc.from);
    nodes.add(arc.to);
    
    getSet(arc, operationsToLabel).add(op);
    getSet(arc.from, arcsFrom).add(arc);
    getSet(arc.to, arcsTo).add(arc);
  }
  
  public void removeArc(Arc arc) {
    operationsToLabel.remove(arc);
    removeSet(arc.from, arc, arcsFrom);
    removeSet(arc.to, arc, arcsTo);
  }
  
  public void removeArc(Arc arc, Operation op) {
    removeSet(arc, op, operationsToLabel);
    if(operationsToLabel.get(arc) == null) {
      removeSet(arc.from, arc, arcsFrom);
      removeSet(arc.to, arc, arcsTo);
    }
  }
  
  private Set getSet(Object key, Map map) {
    Set result = (Set) map.get(key);
    if(result == null) {
      map.put(key, result = new HashSet());
    }
    return result;
  }
  
  private void removeSet(Object key, Object value, Map map) {
    Set set = (Set) map.get(key);
    if(set != null) {
      set.remove(value);
      if(set.size() == 0) {
        map.remove(key);
      }
    }
  }
  
  public Query buildQuery() {
    return new SimpleQuery(nodes, operationsToLabel);
  }
}
