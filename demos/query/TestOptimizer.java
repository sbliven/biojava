package query;

import java.awt.Color;
import java.util.*;

import org.biojava.utils.query.*;

public class TestOptimizer {
  public static final Operation OPPERATION = new Operation.Count(Object.class);
  public static final Operation FILTER = new Filter.AcceptAll(Object.class);
  
  public static void main(String[] args) throws Throwable {
    //doOptimize(emptyQuery(), "i", "i");
    doOptimize(selfFilter(), "i", "i");
    doOptimize(selfOperation(), "i", "i");
    doOptimize(selfFilterAndOperation(), "i", "i");
    doOptimize(randomQuery(), "node 0", "node 1");
  }
  
  public static void doOptimize(Query query, String startName, String endName)
  throws OperationException {
    Node startNode
      = (Node) QueryTools.findNodeByLabel(query, startName).iterator().next();
    Node endNode
      = (Node) QueryTools.findNodeByLabel(query, endName).iterator().next();
    
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
    Node n = new SimpleNode("i", Object.class);
    qb.addArc(new Arc(n, n), FILTER);
    return qb.buildQuery();
  }
  
  public static Query selfOperation()
  throws OperationException {
    QueryBuilder qb = new QueryBuilder();
    Node n = new SimpleNode("i", Object.class);
    qb.addArc(new Arc(n, n), OPPERATION);
    return qb.buildQuery();
  }

  public static Query selfFilterAndOperation()
  throws OperationException {
    QueryBuilder qb = new QueryBuilder();
    Node n = new SimpleNode("i", Object.class);
    qb.addArc(new Arc(n, n), FILTER);
    qb.addArc(new Arc(n, n), OPPERATION);
    return qb.buildQuery();
  }

  public static void display(String message, Query query) {
    System.out.println(message);
    System.out.println("Nodes: " + query.getNodes().size());
    for(Iterator ni = query.getNodes().iterator(); ni.hasNext(); ) {
      Node n = (Node) ni.next();
      for(Iterator ai = query.getArcsFrom(n).iterator(); ai.hasNext(); ) {
        Arc a = (Arc) ai.next();
        System.out.println("\t" + a);
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
      nodes[i] = new SimpleNode("node " + i, Object.class);
      System.out.println("Created node: " + nodes[i]);
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
    try {
      while(System.in.read() != '\n') {}
    } catch (java.io.IOException ioe) {
    }
  }
}
