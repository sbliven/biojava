package query;

import java.awt.Color;
import java.util.*;

import org.biojava.utils.query.*;

public class TestOptimizer {
  public static final Type Type_Object;
  public static final Operation OPPERATION;
  public static final Operation FILTER;
  
  static {
    Type_Object = JavaType.getType(Object.class);
    OPPERATION = new Operation.Count(Type_Object);
    FILTER = new Filter.AcceptAll(Type_Object);
  }
  
  public static void main(String[] args) throws Throwable {
    //doOptimize(emptyQuery(), "i", "i");
    doOptimize("self filter", selfFilter(), "i", "i");
    doOptimize("self operation", selfOperation(), "i", "i");
    doOptimize("self filter and operation", selfFilterAndOperation(), "i", "i");
    
    doOptimize("two unconnected", twoUnconnected(), "a", "b");
    doOptimize("two connected a->b", twoConnected(), "a", "b");
    doOptimize("two connected b<-a", twoConnected(), "b", "a");
    
    Query random = randomQuery();
    for(Iterator i = random.getNodes().iterator(); i.hasNext(); ) {
      Node ni = (Node) i.next();
      for(Iterator j = random.getNodes().iterator(); j.hasNext(); ) {
        Node nj = (Node) j.next();
        doOptimize("random network", randomQuery(), ni.toString(), nj.toString());
      }
    }
  }
  
  public static void doOptimize(String message, Query query, String startName, String endName)
  throws OperationException {
    Node startNode
      = (Node) QueryTools.findNodeByLabel(query, startName).iterator().next();
    Node endNode
      = (Node) QueryTools.findNodeByLabel(query, endName).iterator().next();
    
    System.out.println("About to optimize " + message + " starting at " + startNode + " and ending at " + endNode);
    Query optimized = QueryTools.optimize(query, startNode, endNode);
    display("Original query", query);
    display("Optimized query", optimized);
    pause();
  }
  
  public static Query emptyQuery() {
    QueryBuilder qb = new QueryBuilder();
    return qb.buildQuery();
  }
  
  public static Query selfFilter()
  throws OperationException {
    QueryBuilder qb = new QueryBuilder();
    Node n = new SimpleNode("i", Type_Object);
    qb.addArc(new Arc(n, n), FILTER);
    return qb.buildQuery();
  }
  
  public static Query selfOperation()
  throws OperationException {
    QueryBuilder qb = new QueryBuilder();
    Node n = new SimpleNode("i", Type_Object);
    qb.addArc(new Arc(n, n), OPPERATION);
    return qb.buildQuery();
  }

  public static Query selfFilterAndOperation()
  throws OperationException {
    QueryBuilder qb = new QueryBuilder();
    Node n = new SimpleNode("i", Type_Object);
    qb.addArc(new Arc(n, n), FILTER);
    qb.addArc(new Arc(n, n), OPPERATION);
    return qb.buildQuery();
  }

  public static Query twoUnconnected() {
    QueryBuilder qb = new QueryBuilder();
    Node a = new SimpleNode("a", Type_Object);
    Node b = new SimpleNode("b", Type_Object);
    
    qb.addNode(a);
    qb.addNode(b);
    
    return qb.buildQuery();
  }

  public static Query twoConnected()
  throws OperationException {
    QueryBuilder qb = new QueryBuilder();
    Node a = new SimpleNode("a", Type_Object);
    Node b = new SimpleNode("b", Type_Object);
    
    qb.addNode(a);
    qb.addNode(b);
    qb.addArc(new Arc(a, b), OPPERATION);
    
    return qb.buildQuery();
  }

  public static void display(String message, Query query) {
    System.out.println(message);
    System.out.println("Nodes: " + query.getNodes().size());
    for(Iterator ni = query.getNodes().iterator(); ni.hasNext(); ) {
      Node n = (Node) ni.next();
      System.out.println(n);
      for(Iterator ai = query.getArcsFrom(n).iterator(); ai.hasNext(); ) {
        Arc a = (Arc) ai.next();
        System.out.println("\t" + a.to);
        for(Iterator oi = query.getOperations(a).iterator(); oi.hasNext(); ) {
          System.out.println("\t\t-> " + oi.next());
        }
      }
    }
  }
  
  
  public static Query randomQuery()
  throws OperationException {
    Node[] nodes = new Node[10];
    double follow = 0.1;
    double filter = 0.075;
    
    QueryBuilder qb = new QueryBuilder();
    
    for(int i = 0; i < nodes.length; i++ ) {
      nodes[i] = new SimpleNode("node " + i, Type_Object);
      qb.addNode(nodes[i]);
    }
    
    for(int i = 0; i < nodes.length; i++) {
      for(int j = 0; j < nodes.length; j++) {
        double p = Math.random();
        if(p <= follow) {
          if(p <= filter) {
            qb.addArc(new Arc(nodes[i], nodes[j]), FILTER);
          } else {
            qb.addArc(new Arc(nodes[i], nodes[j]), OPPERATION);
          }
        }
      }
    }
    
    return qb.buildQuery();
  }
  
  public static void pause() {
    /*try {
      while(System.in.read() != '\n') {}
    } catch (java.io.IOException ioe) {
    }*/
  }
}
