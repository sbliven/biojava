package org.biojava.utils.query;

import java.util.*;
import java.lang.reflect.*;

import org.biojava.utils.*;

/**
 * Utility routines for manipulating and evaluating queryies and queriables.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class QueryTools {
  /**
   * Calculate the union of two <code>Queryable</code>s.
   * <P>
   * The union contains every item in <code>a</code> and every item in
   * <code>b</code>. No item will be in the return set twice. The implementation
   * of this method will attempt to optimize for the common cases where
   * <code>a</code> or <code>b</code> are empty, singletons or SimpleQueryable
   * instances.
   *
   * @param a the first Queryable
   * @param b the seccond Queryable
   * @return the union
   */
  public static Queryable union(Queryable a, Queryable b)
  throws ClassCastException {
    Class clazz = guessClass(a, b);
    
    if(a instanceof Queryable.Empty) {
      return b;
    } else if(b instanceof Queryable.Empty) {
      return a;
    }
    
    Set result = new HashSet();
    addAll(result, a);
    addAll(result, b);
    return new SimpleQueryable(result, clazz);
  }
  
  /**
   * Implementation optimized union.
   */
  private static void addAll(Set result, Queryable items) {
    if(items instanceof Queryable.Singleton) {
      result.add(((Queryable.Singleton) items).getItem());
    } else if(items instanceof SimpleQueryable) {
      result.addAll(((SimpleQueryable) items).items);
    } else {
      for(Iterator i = items.iterator(); i.hasNext(); ) {
        result.add(i.next());
      }
    }
  }

  /**
   * Calculate the intersection of two <code>Queryable</code>s.
   * <P>
   * The intersection contains every item in both <code>a</code> and
   * <code>b</code>. By definition, the return set is a sub-set of both
   * input sets.
   * The implementation
   * of this method will attempt to optimize for the common cases where
   * <code>a</code> or <code>b</code> are empty, singletons or SimpleQueryable
   * instances.
   *
   * @param a the first Queryable
   * @param b the seccond Queryable
   * @return the union
   */
  public static Queryable intersection(Queryable a, Queryable b) {
    Class clazz = guessClass(a, b);
    
    if(a instanceof Queryable.Empty || b instanceof Queryable.Empty) {
      return new Queryable.Empty(clazz);
    } else {
      if(b instanceof Queryable.Singleton) {
        Queryable q = b;
        b = a;
        a = q;
      }
      if(a instanceof Queryable.Singleton) {
        Object item = ((Queryable.Singleton) a).getItem();
        if(b.contains(item)) {
          return a;
        } else {
          return new Queryable.Empty(clazz);
        }
      } else {
        Set result = new HashSet();
        if(b.size() < a.size()) {
          Queryable q = b;
          b = a;
          a = q;
        }
        for(Iterator i = a.iterator(); i.hasNext(); ) {
          Object item = i.next();
          if(a.contains(item) && b.contains(item)) {
            result.add(item);
          }
        }
        return new SimpleQueryable(result, clazz);
      }
    }
  }

  public static Queryable subtraction(Queryable a, Queryable b) {
    if(a instanceof Queryable.Empty) {
      return a;
    } else if(a instanceof Queryable.Singleton) {
      if(b.contains(((Queryable.Singleton) a).getItem())) {
        return new Queryable.Empty(a.getQueryClass());
      } else {
        return a;
      }
    } else if(b instanceof Queryable.Empty) {
      return a;
    } else {
      Set result = new HashSet();
      for(Iterator i = a.iterator(); i.hasNext(); ) {
        Object o = i.next();
        if(!b.contains(o)) {
          result.add(o);
        }
      }
      return createQueryable(result, a.getQueryClass());
    }
  }
  /**
   * Attempts to guess a Class from <code>a</code> and <code>b</code>.
   */
  public static Class guessClass(Queryable a, Queryable b) {
    Class clazz;
    
    if(a.getQueryClass().isAssignableFrom(b.getQueryClass())) {
      clazz = b.getQueryClass();
    } else if(b.getQueryClass().isAssignableFrom(a.getQueryClass())) {
      clazz = a.getQueryClass();
    } else {
      clazz = Object.class;
    }
    
    return clazz;
  }
  
  /**
   * Create a new Queryable from a set of items and a Class.
   *
   * @param items  a set of Object instances
   * @param clazz  the Class that all items should implement
   * @return a Queryable containing <code>items</code> and qualified by
   *         Class <code>clazz</code>
   */
  public static Queryable createQueryable(Set items, Class clazz) {
    if(items.size() == 0) {
      return new  Queryable.Empty(clazz);
    } else if(items.size() == 1) {
      return new Queryable.Singleton(items.iterator().next());
    } else {
      return new SimpleQueryable(items, clazz);
    }
  }
  
  public static Queryable createSingleton(Object item) {
    return new Queryable.Singleton(item);
  }
  
  /**
   * Process a Query designed to select items.
   *
   * @param query  the Query to process
   * @param currentNode  the node to evaluate
   * @param items  a Queryable containing the items to evaluate
   * @return a Queryable containg every item selected by the query
   * @throws OperationException if the query fails
   */
  public static Queryable select(Query query, Node currentNode, Queryable items)
  throws OperationException {
    Queryable selected;
    if(currentNode instanceof ResultNode) {
      selected = items;
    } else {
      selected = new Queryable.Empty(items.getQueryClass());
    }
    
    Iterator ai = query.getArcsFrom(currentNode).iterator();
    while(ai.hasNext()) {
      Arc arc = (Arc) ai.next();
      Iterator oi = query.getOperations(arc).iterator();
      while(oi.hasNext()) {
        Operation op = (Operation) oi.next();
        Queryable res;
        try {
          res = op.apply(items);
        } catch (OperationException oe) {
          throw new OperationException(oe, "Failed to execute " + arc + " " + op);
        } catch (ClassCastException cce) {
          throw new OperationException(cce, "Failed to execute " + arc + " " + op);
        }
        selected = QueryTools.union(selected, select(query, arc.to, res));
      }
    }
    
    return selected;
  }
  
  /**
   * This method returns a Query that will match the same items as the input
   * query but which should be more efficient.
   * <P>
   * Currently, this method will attempt to find lists of Filter operators and
   * replace them with single FilterSet operators.
   *
   * @param query  the input Query
   * @param an optimized view of this
   */
  public static Query optimize(Query query)
  throws OperationException {
    // find all nodes that have only 1 entry and 1 exit and for which these are
    // labeld with filter operations
    Queryable mergableNodes = select(
      findMergableFiltersQuery,
      findMergableFiltersStart,
      createSingleton(query)
    );
    
    if(mergableNodes.size() == 0) {
      return query;
    }
    
    QueryBuilder qb = new QueryBuilder();
    qb.addQuery(query);
    // build (from, to, { Filter }) for each chain and delete unneeded paths as
    // we go
    while(mergableNodes.size() > 0) {
      Set chainOps = new HashSet();
      Set visitedNodes = new HashSet();
      Node node = (Node) mergableNodes.iterator().next();
      Node from = node;
      Node to = node;
      
      while(mergableNodes.contains(to)) {
        Arc arc =
          (Arc) query.getArcsFrom(to).iterator().next();
        Operation op =
          (Operation) query.getOperations(arc).iterator().next();
        chainOps.add(op);
        visitedNodes.add(to);
        to = arc.from;
      }

      while(mergableNodes.contains(from)) {
        Arc arc =
          (Arc) query.getArcsTo(from).iterator().next();
        Operation op =
          (Operation) query.getOperations(arc).iterator().next();
        chainOps.add(op);
        visitedNodes.add(from);
        from = arc.to;
      }
      
      for(Iterator ni = visitedNodes.iterator(); ni.hasNext(); ) {
        Node n = (Node) ni.next();
        qb.removeNode(n);
      }
      mergableNodes = subtraction(
        mergableNodes,
        createQueryable(visitedNodes, mergableNodes.getQueryClass())
      );
      qb.addArc(new Arc(from, to), new FilterSet(chainOps));
    }
    
    return qb.buildQuery();
  }
  
  private static final Query findMergableFiltersQuery;
  private static final Node findMergableFiltersStart;
  
  static {
      try {
    Tuple.ClassList queryNodeClassList
      = new SimpleTuple.ClassList( new Class[] {Query.class, Node.class} );
    Tuple.ClassList queryArcClassList
      = new SimpleTuple.ClassList( new Class[] {Query.class, Arc.class} );
    Tuple.ClassList queryOperationClassList
      = new SimpleTuple.ClassList( new Class[] {Query.class, Operation.class} );
    Tuple.ClassList queryQuerySetClassList
      = new SimpleTuple.ClassList( new Class[] {Query.class, Query.class, Set.class} );

    Operation doubleQueryNode = new Tuple.Permutate(new int[] { 0, 0, 1 });
    
    Method Query_getArcsFrom;
    Method Query_getArcsTo;
    Method Query_getOperations;
    Method Query_getNodes;
    try {
      Query_getArcsFrom = Query.class.getMethod(
        "getArcsFrom",
        new Class[] { Node.class }
      );
      Query_getArcsTo = Query.class.getMethod(
        "getArcsTo",
        new Class[] { Node.class }
      );
      Query_getOperations = Query.class.getMethod(
        "getOperations",
        new Class[] { Arc.class }
      );
      Query_getNodes = Query.class.getMethod(
        "getNodes",
        Follow.FollowMethod.EMPTY_CLASSES
      );
        
    } catch (NoSuchMethodException nsme) {
      throw new NestedError(nsme);
    }

    QueryBuilder countQB = new QueryBuilder();
    Node countStart = new SimpleNode("starting set", Set.class);
    Node setItems = new SimpleNode("set items", Operation.class);
    Node size = new SimpleResultNode("set size", Integer.class);
    countQB.addArc(new Arc(countStart, setItems),
                   new Follow.FollowCollectionToMembers(Operation.class));
    countQB.addArc(new Arc(setItems, size),
                   new Operation.Count(Operation.class));
    Query countQuery = countQB.buildQuery();
      
    // build query to find query,node tuples with just one way in or out where
    // that way is an instance of Filter
    QueryBuilder arcsQB = new QueryBuilder();
    Node arcsFromStart = new SimpleNode("arcs from start", Tuple.class);
    Node arcsFromQueryQueryNode = new SimpleNode("from prepared", Tuple.class);
    Node arcsToStart = new SimpleNode("arcs to start", Tuple.class);
    Node arcsToQueryQueryNode = new SimpleNode("to prepared", Tuple.class);
    Node queryArcs = new SimpleNode("query,{arc}", Tuple.class);
    Node queryArc = new SimpleNode("query,arc", Tuple.class);
    Node opSet = new SimpleNode("operator set", Set.class);
    Node opSet1 = new SimpleNode("size is 1", Set.class);
    Node ops = new SimpleNode("operator", Operation.class);
    Node filterOps = new SimpleResultNode("filter op", Filter.class);
    
    arcsQB.addArc(new Arc(arcsFromStart, arcsFromQueryQueryNode),
                  doubleQueryNode);
    arcsQB.addArc(new Arc(arcsFromQueryQueryNode, queryArcs),
                  new Tuple.FollowMethod(Query_getArcsFrom, queryNodeClassList));
    arcsQB.addArc(new Arc(arcsToStart, arcsToQueryQueryNode),
                  doubleQueryNode);
    arcsQB.addArc(new Arc(arcsToQueryQueryNode, queryArcs),
                  new Tuple.FollowMethod(Query_getArcsTo, queryNodeClassList));
    arcsQB.addArc(new Arc(queryArcs, queryArc),
                  new Tuple.FollowTupleTo(
                    1,
                    new Follow.FollowCollectionToMembers(Arc.class),
                    queryOperationClassList));
    arcsQB.addArc(new Arc(queryArc, opSet),
                  new Tuple.FollowMethod(Query_getOperations,
                                         queryArcClassList));
    arcsQB.addArc(new Arc(opSet, opSet1),
                  new FilterByQuery(countQuery,
                                    countStart,
                                    Filter.CompareInteger.EQ,
                                    1));
    arcsQB.addArc(new Arc(opSet1, ops),
                  new Follow.FollowCollectionToMembers(Operation.class));
    arcsQB.addArc(new Arc(ops, filterOps),
                  new Filter.ByClass(Operation.class, Filter.class));
    
    Query arcsQuery = arcsQB.buildQuery();
    
    QueryBuilder fmfqb = new QueryBuilder();
    
    findMergableFiltersStart =  new SimpleNode("start", Query.class);
    Node qns =                  new SimpleNode("query,{node}", Tuple.class);
    Node qn =                   new SimpleNode("query,node", Tuple.class);
    Node oneFrom =              new SimpleNode("oneFrom", Tuple.class);
    Node oneTo =                new SimpleNode("oneTo", Tuple.class);
    Node nodes =                new SimpleResultNode("nodes", Node.class);
    
    fmfqb.addArc(new Arc(findMergableFiltersStart, qns),
                 new Tuple.FollowToTuple(new Follow.FollowMethod(Query_getNodes)));
    fmfqb.addArc(new Arc(qns, qn),
                 new Tuple.FollowTupleTo(
                   1,
                   new Follow.FollowCollectionToMembers(Node.class),
                   queryQuerySetClassList));
    fmfqb.addArc(new Arc(qn, oneFrom),
                 new FilterByQuery(arcsQuery,
                                   arcsFromStart,
                                   Filter.CompareInteger.EQ,
                                   1));
    
    fmfqb.addArc(new Arc(oneFrom, oneTo),
                 new FilterByQuery(arcsQuery,
                                   arcsToStart,
                                   Filter.CompareInteger.EQ,
                                   1));
    
    fmfqb.addArc(new Arc(oneTo, nodes),
                 new Tuple.FollowObject(queryNodeClassList, 1));
    
    findMergableFiltersQuery = fmfqb.buildQuery();
      } catch (OperationException ex) {
	  throw new NestedError(ex, "Couldn't initialize QueryTools");
      }
  }
}
