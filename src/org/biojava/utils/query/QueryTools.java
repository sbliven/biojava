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
   * <P>
   * This will evaluate <code>graph</code> starting at <code>currentNode</code>
   * in <code>query</code> using the data in <code>items</code>. Every time the
   * <code>targetNode</code> is reached, all the current itmes will be added to
   * the return set (Setwise union).
   * <P>
   * You can take a single query and use it to perform different funcitons by
   * using different starting and target nodes.
   *
   * @param query  the Query to process
   * @param startNode  the node to evaluate
   * @param endNode the node to select items from
   * @param items  a Queryable containing the items to evaluate
   * @return a Queryable containg every item selected by the query
   * @throws OperationException if the query fails
   */
  public static Queryable select(
    Query query,
    Node startNode,
    Node endNode,
    Queryable items
  ) throws OperationException {
    final boolean debug = false;
    
    if(debug) System.out.println("Executing at " + startNode + " with " + items);

    if(items.size() == 0) {
      if(debug) System.out.println("No items to process. Returning empty set");
      return items;
    }
    
    Queryable selected;
    if(startNode == endNode) {
      if(debug) System.out.println("reached end node - selecting current items");
      selected = items;
    } else {
      selected = new Queryable.Empty(items.getQueryClass());
    }
    
    if(debug) System.out.println("Following " + query.getArcsFrom(startNode).size() +
    " paths.");
    Iterator ai = query.getArcsFrom(startNode).iterator();
    while(ai.hasNext()) {
      Arc arc = (Arc) ai.next();
      if(debug) System.out.println("Following arc: " + arc);
      Iterator oi = query.getOperations(arc).iterator();
      while(oi.hasNext()) {
        Operation op = (Operation) oi.next();
        if(debug) System.out.println("Evaluating operator: " + arc + " -> " + op);
        Queryable res;
        try {
          res = op.apply(items);
        } catch (OperationException oe) {
          throw new OperationException(oe, "Failed to execute " + arc + " " + op);
        } catch (ClassCastException cce) {
          throw new OperationException(cce, "Failed to execute " + arc + " " + op);
        } catch (IllegalArgumentException iae) {
          throw new OperationException(iae, "Failed to execute " + arc + " " + op);
        }
        if(res.size() != 0) {
          selected = QueryTools.union(selected, select(
            query, 
            arc.to,
            endNode,
            res
          ));
        }
      }
    }
    if(debug) System.out.println("Selected " + startNode + " " + selected);
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
   * @param startNode  the Node that a query would start from
   * @param targetNode  the Node that will signal that items should be selected
   * @param an optimized view of this
   */
  public static Query optimize(Query query, Node startNode, Node targetNode)
  throws OperationException {
    // find all nodes that have only 1 entry and 1 exit and for which these are
    // labeld with filter operations
    Queryable mergableNodes = select(
      findMergableFiltersQuery,
      findMergableFiltersStart,
      findMergableFiltersEnd,
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
      
      System.out.println("Optimizing from " + node);
      
      // walk from->to along chain
      do {
        Arc arc =
          (Arc) query.getArcsFrom(to).iterator().next();
        Operation op =
          (Operation) query.getOperations(arc).iterator().next();
        chainOps.add(op);
        visitedNodes.add(to);
        to = arc.to;
        System.out.println("walked to " + to + " by following arc " + arc);
      } while(to != node && mergableNodes.contains(to));

      // walk from<-to along chain
      do {
        Arc arc =
          (Arc) query.getArcsTo(from).iterator().next();
        Operation op =
          (Operation) query.getOperations(arc).iterator().next();
        chainOps.add(op);
        visitedNodes.add(from);
        from = arc.from;
        System.out.println("walked to " + from + " by following arc " + arc);
      } while(from != node && mergableNodes.contains(from));
      
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
  private static final Node findMergableFiltersEnd;
  
  private static final Query collectionSizeIs1Query;
  private static final Node collectionSizeIs1Start;
  private static final Node collectionSizeIs1End;
  
  static {
    try {
    Tuple.ClassList queryNodeClassList
      = new SimpleTuple.ClassList( new Class[] {Query.class, Node.class} );
    Tuple.ClassList queryQueryNodeClassList
      = new SimpleTuple.ClassList( new Class[] {Query.class, Query.class, Node.class} );
    Tuple.ClassList querySetClassList
    = new SimpleTuple.ClassList( new Class[] {Query.class, Set.class } );
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
    Method Collection_size;
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
      Collection_size = Collection.class.getMethod(
        "size",
        Follow.FollowMethod.EMPTY_CLASSES
      );
    } catch (NoSuchMethodException nsme) {
      throw new NestedError(nsme);
    }

    // mini query that counts the number of items in a set and then checks that
    // it is 1
    QueryBuilder sqb = new QueryBuilder();
    collectionSizeIs1Start = new SimpleNode("sizeCheckStart", Set.class);
    Node setSize = new SimpleNode("setSize", Integer.class);
    collectionSizeIs1End = new SimpleNode("sizeIs1", Integer.class);
    sqb.addArc(new Arc(collectionSizeIs1Start, setSize),
               new Follow.FollowMethod(Collection_size));
    sqb.addArc(new Arc(setSize, collectionSizeIs1End),
               new Filter.Equals(new Integer(1), Integer.class));
    collectionSizeIs1Query = sqb.buildQuery();
    
    // build query to find query,node tuples with just one way in or out where
    // that way is an instance of Filter
    QueryBuilder arcsQB = new QueryBuilder();
    Node arcsFromStart = new SimpleNode("arcs from start (query,node)", Tuple.class);
    Node arcsFromQueryQueryNode = new SimpleNode("from prepared (query,query,node)", Tuple.class);
    Node arcsToStart = new SimpleNode("arcs to start (query,node)", Tuple.class);
    Node arcsToQueryQueryNode = new SimpleNode("to prepared (query,query,node)", Tuple.class);
    Node queryArcs = new SimpleNode("query,{arc}", Tuple.class);
    Node queryArcs1 = new SimpleNode("query,{arc} size is 1", Tuple.class);
    Node queryArc = new SimpleNode("query,arc", Tuple.class);
    Node opSet = new SimpleNode("operator set", Set.class);
    Node opSet1 = new SimpleNode("size is 1", Set.class);
    Node ops = new SimpleNode("operator", Operation.class);
    Node filterOps = new SimpleNode("filter op", Filter.class);
    
    arcsQB.addArc(new Arc(arcsFromStart, arcsFromQueryQueryNode),
                  doubleQueryNode);
    arcsQB.addArc(new Arc(arcsFromQueryQueryNode, queryArcs),
                  new Tuple.FollowMethod(Query_getArcsFrom, queryQueryNodeClassList));
    arcsQB.addArc(new Arc(arcsToStart, arcsToQueryQueryNode),
                  doubleQueryNode);
    arcsQB.addArc(new Arc(arcsToQueryQueryNode, queryArcs),
                  new Tuple.FollowMethod(Query_getArcsTo, queryQueryNodeClassList));
    arcsQB.addArc(new Arc(queryArcs, queryArcs1),
                  new Tuple.FilterByIndex(
                    new FilterByQuery(collectionSizeIs1Query,
                                      collectionSizeIs1Start,
                                      collectionSizeIs1End,
                                      Filter.CompareInteger.EQ,
                                      1),
                    1));
    arcsQB.addArc(new Arc(queryArcs1, queryArc),
                  new Tuple.FollowTupleTo(
                    1,
                    new Follow.FollowCollectionToMembers(Arc.class),
                    queryOperationClassList));
    arcsQB.addArc(new Arc(queryArc, opSet),
                  new Tuple.FollowMethod(Query_getOperations,
                                         queryArcClassList));
    arcsQB.addArc(new Arc(opSet, opSet1),
                  new FilterByQuery(collectionSizeIs1Query,
                                    collectionSizeIs1Start,
                                    collectionSizeIs1End,
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
    findMergableFiltersEnd =    new SimpleNode("nodes", Node.class);
    
    fmfqb.addArc(new Arc(findMergableFiltersStart, qns),
                 new Tuple.FollowToTuple(new Follow.FollowMethod(Query_getNodes)));
    fmfqb.addArc(new Arc(qns, qn),
                 new Tuple.FollowTupleTo(
                   1,
                   new Follow.FollowCollectionToMembers(Node.class),
                   querySetClassList));
    fmfqb.addArc(new Arc(qn, oneFrom),
                 new FilterByQuery(arcsQuery,
                                   arcsFromStart,
                                   filterOps,
                                   Filter.CompareInteger.EQ,
                                   1));
    
    fmfqb.addArc(new Arc(oneFrom, oneTo),
                 new FilterByQuery(arcsQuery,
                                   arcsToStart,
                                   filterOps,
                                   Filter.CompareInteger.EQ,
                                   1));
    
    fmfqb.addArc(new Arc(oneTo, findMergableFiltersEnd),
                 new Tuple.FollowObject(queryNodeClassList, 1));
    
    findMergableFiltersQuery = fmfqb.buildQuery();
  } catch (OperationException oe) {
    throw new NestedError(oe, "Can't initialize optimization queries");
  }
  }
  
  public static Class convertPrimatives(Class inputClass) {
    if(inputClass.isPrimitive()) {
      if(false) ; // syntactic sugar to make the rest line up
      else if(inputClass == Boolean.TYPE)   inputClass = Boolean.class;
      else if(inputClass == Character.TYPE) inputClass = Character.class;
      else if(inputClass == Byte.TYPE)      inputClass = Byte.class;
      else if(inputClass == Short.TYPE)     inputClass = Short.class;
      else if(inputClass == Integer.TYPE)   inputClass = Integer.class;
      else if(inputClass == Long.TYPE)      inputClass = Long.class;
      else if(inputClass == Float.TYPE)     inputClass = Float.class;
      else if(inputClass == Double.TYPE)    inputClass = Double.class;
      else throw new IllegalArgumentException("Can't convert VOID");
    }
    
    return inputClass;
  }
  
  public static Queryable findNodeByLabel(Query query, String label) {
    try {
      Method Query_getNodes;
      Method Node_getLabel;
      try {
        Query_getNodes = Query.class.getMethod(
          "getNodes",
          Follow.FollowMethod.EMPTY_CLASSES
        );
        Node_getLabel = Node.class.getMethod(
          "getLabel",
          Follow.FollowMethod.EMPTY_CLASSES
        );
      } catch (NoSuchMethodException nsme) {
        throw new NestedError(nsme);
      }
      
      // build query to check if node label is equal to label argument
      QueryBuilder nlqb = new QueryBuilder();
      
      Node nodeLabelQueryStart = new SimpleNode("start", Node.class);
      Node labelNode = new SimpleNode("label", String.class);
      Node nodeLabelQueryEnd = new SimpleNode("end", String.class);
      
      nlqb.addArc(new Arc(nodeLabelQueryStart, labelNode),
                  new Follow.FollowMethod(Node_getLabel));
      nlqb.addArc(new Arc(labelNode, nodeLabelQueryEnd),
                  new Filter.Equals(label, String.class));
      
      Query nodeLabelQuery = nlqb.buildQuery();
      
      // biuld query to extract nodes from query with the given label
      QueryBuilder findNodeQB = new QueryBuilder();
      
      Node start = new SimpleNode("start", Query.class);
      Node nodeSet = new SimpleNode("nodeSet", Set.class);
      Node node = new SimpleNode("node", Node.class);
      Node end = new SimpleNode("end", Node.class);
      
      findNodeQB.addArc(new Arc(start, nodeSet),
                        new Follow.FollowMethod(Query_getNodes));
      findNodeQB.addArc(new Arc(nodeSet, node),
                        new Follow.FollowCollectionToMembers(Node.class));
      findNodeQB.addArc(new Arc(node, end),
                        new FilterByQuery(nodeLabelQuery,
                                          nodeLabelQueryStart,
                                          nodeLabelQueryEnd,
                                          Filter.CompareInteger.EQ,
                                          1));
      
      Query findNodeByLabelQuery = findNodeQB.buildQuery();
      
      return QueryTools.select(findNodeByLabelQuery,
                               start,
                               end,
                               QueryTools.createSingleton(query));
    } catch (OperationException oe) {
      throw new NestedError("This should never fail");
    }
  }
  
  /**
   * Create a recursive query that will find all nodes that are on a path to
   * queryEnd if you start from queryStart and travel through the query.
   */
  private static Queryable pruneQuery(
    Query query, Node queryStart, Node queryEnd
  ) throws OperationException {
    Method Query_getArcsFrom;
    Field Arc_to;
    try {
      Query_getArcsFrom = Query.class.getMethod("getArcsFrom",
                                                new Class[] { Node.class } );
      Arc_to = Arc.class.getField("to");
    } catch (NoSuchMethodException nsme) {
      throw new NestedError(nsme);
    } catch (NoSuchFieldException nsfe) {
      throw new NestedError(nsfe);
    }
    
    Tuple.ClassList ClassList_queryNode = new SimpleTuple.ClassList(
      new Class[] {Query.class,
                   Node.class});
    Tuple.ClassList ClassList_queryQueryNode = new SimpleTuple.ClassList(
      new Class[] {Query.class,
                   Query.class,
                   Node.class});
    Tuple.ClassList ClassList_querySet = new SimpleTuple.ClassList(
      new Class[] {Query.class,
                   Set.class});
    Tuple.ClassList ClassList_queryArc = new SimpleTuple.ClassList(
      new Class[] {Query.class,
                   Arc.class});
    
    QueryBuilder pruneQB = new QueryBuilder();
    QueryHolder pruneHolder = new QueryHolder();
    
    Operation acceptAllTuples = new Filter.AcceptAll(Tuple.class);
    
    Node pruneStart = new SimpleNode("start", Tuple.class);
    Node accepted = new SimpleNode("accepted", Tuple.class);
    Node inPath = new SimpleNode("inPath", Tuple.class);
    Node path = new SimpleNode("path", Tuple.class);
    Node pruneEnd = new SimpleNode("end", Node.class);
    Node pruneInput = new SimpleNode("pruneInput", Tuple.class);
    Node arcForNodeInput = new SimpleNode("arcForNodeInput", Tuple.class);
    Node queryArcs = new SimpleNode("queryArcs", Tuple.class);
    Node queryArc = new SimpleNode("queryArc", Tuple.class);
    
    // arc to check for node==endNode - accept this case
    pruneQB.addArc(new Arc(pruneStart, accepted),
                   new Tuple.FilterByIndex(new Filter.Equals(queryEnd,
                                                             Node.class),
                                           1));
    
    // pass on all (query,node) tuples that have dependants in path to endNode,
    // along with all (query,node) tuples in that path
    pruneQB.addArc(new Arc(pruneStart, inPath),
                   new FilterByQuery(pruneHolder,
                                     pruneInput,
                                     pruneEnd,
                                     Filter.CompareInteger.GT,
                                     0));
    pruneQB.addArc(new Arc(inPath, accepted),
                   acceptAllTuples);
    pruneQB.addArc(new Arc(inPath, path),
                   new FollowQuery(pruneHolder,
                                   pruneInput,
                                   pruneEnd));
    pruneQB.addArc(new Arc(path, accepted),
                   acceptAllTuples);

    // accepted query,node - extract node and put it into pruneEnd
    pruneQB.addArc(new Arc(accepted, pruneEnd),
                   new Tuple.FollowObject(ClassList_queryNode, 1));

                   Query pruneQuery = pruneQB.buildQuery();

    // follow from pruneInput through query,node to query,arc to query,node
    // again and feed this into pruneStart
    pruneQB.addArc(new Arc(pruneInput, arcForNodeInput),
                   new Tuple.Permutate(new int[] { 0, 0, 1 }));
    pruneQB.addArc(new Arc(arcForNodeInput, queryArcs),
                   new Tuple.FollowMethod(Query_getArcsFrom,
                                          ClassList_querySet));
    pruneQB.addArc(new Arc(queryArcs, queryArc),
                   new Tuple.FollowTupleTo(
                     1,
                     new Follow.FollowCollectionToMembers(Arc.class),
                     ClassList_queryArc));
    pruneQB.addArc(new Arc(queryArc, pruneStart),
                   new Follow.FollowField(Arc_to));
    
    pruneQuery = pruneQB.buildQuery();
    try {
      pruneHolder.setQuery(pruneQuery);
    } catch (NestedException ne) {
      throw new NestedError(ne, "Something is badly wrong");
    }
    
    Tuple startTup = new SimpleTuple(
      new Object[] {query, queryStart},
      ClassList_queryNode
    );
    return QueryTools.select(
      pruneQuery,
      pruneStart,
      pruneEnd,
      QueryTools.createSingleton(startTup)
    );
  }
}
