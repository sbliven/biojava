package query;

import java.awt.Color;
import java.util.*;

import org.biojava.utils.query.*;

public class TestQuery {
  public static void main(String[] args) throws Throwable {
    Set carSet = new HashSet();
    Map c1;
    Map c2;
    carSet.add(c1 = buildCar(Color.red, "astra"));
    carSet.add(c2 = buildCar(Color.gray, "metro"));
    
    Set peopleSet = new HashSet();
    peopleSet.add(buildPerson("ted", 32, c1));
    peopleSet.add(buildPerson("matt", 25, c1));
    peopleSet.add(buildPerson("caroline", 25, c2));
    
    Queryable cars = QueryTools.createQueryable(carSet, Map.class);
    Queryable people = QueryTools.createQueryable(peopleSet, Map.class);
    
    // filter people by age
    Node startNode = new SimpleNode("startNode", Map.class);
    Query ageQuery25 = buildAgeQuery(25, startNode);
    Query ageQuery32 = buildAgeQuery(32, startNode);
    Query ageQuery45 = buildAgeQuery(45, startNode);
    
    display("Age is 25", QueryTools.select(ageQuery25, startNode, people));
    display("Age is 32", QueryTools.select(ageQuery32, startNode, people));
    display("Age is 45", QueryTools.select(ageQuery45, startNode, people));
    pause();
    
    Query carQuery = buildCarQuery(startNode);
    
    display("Finding cars", QueryTools.select(carQuery, startNode, people));
    pause();
    
    Query carQuery25 = buildCarByAgeQuery(25, startNode);
    Query carQuery32 = buildCarByAgeQuery(32, startNode);
    Query carQuery45 = buildCarByAgeQuery(45, startNode);
    
    display("Cars where age is 25", QueryTools.select(carQuery25, startNode, people));
    display("Cars where age is 32", QueryTools.select(carQuery32, startNode, people));
    display("Cars where age is 45", QueryTools.select(carQuery45, startNode, people));
    pause();
    
    Query age25or32 = buildAgeQuery(25, 32, startNode);
    Query age25or42 = buildAgeQuery(25, 42, startNode);
    Query age32or42 = buildAgeQuery(32, 42, startNode);
    
    display("Cars where age25 or 32", QueryTools.select(age25or32, startNode, people));
    display("Cars where age25 or 42", QueryTools.select(age25or42, startNode, people));
    display("Cars where age32 or 42", QueryTools.select(age32or42, startNode, people));
    pause();
    
    Query age25CarC1 = buildAgeCarQuery(25, c1, startNode);
    Query age25CarC2 = buildAgeCarQuery(25, c2, startNode);
    
    display("People aged 25 and driving c1", QueryTools.select(age25CarC1, startNode, people));
    display("People aged 25 and driving c2", QueryTools.select(age25CarC2, startNode, people));
    pause();
    
    Query usingC1 = buildPersonByCarQuery(c1, startNode);
    Query usingC2 = buildPersonByCarQuery(c2, startNode);
    
    display("People driving c1", QueryTools.select(usingC1, startNode, people));
    display("People driving c2", QueryTools.select(usingC2, startNode, people));
    pause();

    Query drivingRed = buildPersonByCarColorQuery(Color.red, startNode);
    Query drivingGray = buildPersonByCarColorQuery(Color.gray, startNode);
    
    display("People driving red", QueryTools.select(drivingRed, startNode, people));
    display("People driving gray", QueryTools.select(drivingGray, startNode, people));
    pause();
    
    Query original = optimizableQuery();
    Query optimized = QueryTools.optimize(original);
    display("Original query", original);
    display("Optimized query", optimized);
    pause();
    
  }
  
  public static void display(String message, Queryable items) {
    System.out.println(message);
    System.out.println(items);
    for(Iterator i = items.iterator(); i.hasNext(); ) {
      Object o = i.next();
      System.out.println("\t" + o);
    }
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
  
  public static Query buildAgeQuery(int age, Node startNode) {
    QueryBuilder qb = new QueryBuilder();
    Node selectNode = new SimpleResultNode("selectNode", Map.class);

    qb.addArc(
      new Arc(startNode, selectNode),
      new MapValueFilter("age", new Integer(age))
    );
    
    return qb.buildQuery();
  }
  
  public static Query buildCarQuery(Node startNode) {
    QueryBuilder qb = new QueryBuilder();
    Node selectNode = new SimpleResultNode("selectNode", Map.class);

    qb.addArc(
      new Arc(startNode, selectNode),
      new MapFollower("car", Map.class)
    );
    
    return qb.buildQuery();
  }
  
  public static Query buildCarByAgeQuery(int age, Node startNode) {
    QueryBuilder qb = new QueryBuilder();
    
    Node selectNode = new SimpleResultNode("selectNode", Map.class);
    Node ageSelected = new SimpleNode("ageSelected", Map.class);

    qb.addArc(
      new Arc(startNode, ageSelected),
      new MapValueFilter("age", new Integer(age))
    );
    qb.addArc(
      new Arc(ageSelected, selectNode),
      new MapFollower("car", Map.class)
    );
    
    return qb.buildQuery();
  }
  
  public static Query buildAgeQuery(int age1, int age2, Node startNode) {
    QueryBuilder qb = new QueryBuilder();
    
    Node selectNode = new SimpleResultNode("selectNode", Map.class);
    Arc startSelect = new Arc(startNode, selectNode);

    qb.addArc(startSelect, new MapValueFilter("age", new Integer(age1)));
    qb.addArc(startSelect, new MapValueFilter("age", new Integer(age2)));
    
    return qb.buildQuery();
  }
  
  public static Query buildAgeCarQuery(int age, Map car, Node startNode) {
    QueryBuilder qb = new QueryBuilder();
    
    Node aged = new SimpleNode("aged", Map.class);
    Node selectNode = new SimpleResultNode("selectNode", Map.class);
    
    qb.addArc(
      new Arc(startNode, aged),
      new MapValueFilter("age", new Integer(age))
    );
    qb.addArc(
      new Arc(aged, selectNode),
      new MapValueFilter("car", car)
    );
    
    return qb.buildQuery();
  }
  
  public static Query buildPersonByCarQuery(Map car, Node startNode) {
    Node selectNode = new SimpleResultNode("selectNode", Map.class);
    
    // select cars,  and count them
    QueryBuilder qb1 = new QueryBuilder();
    Node cars = new SimpleNode("cars", Map.class);
    Node ourCar = new SimpleResultNode("ourCar", Map.class);
    qb1.addArc(new Arc(startNode, cars), new MapFollower("car", Map.class));
    qb1.addArc(new Arc(cars, ourCar), new Filter.Equals(car, Map.class));
    Query carQuery = qb1.buildQuery();
    
    Filter sizeFilter = new FilterByQuery(
      carQuery, startNode, Filter.CompareInteger.EQ, 1
    );
    
    QueryBuilder qb2 = new QueryBuilder();
    qb2.addArc(new Arc(startNode, selectNode), sizeFilter);
    
    return qb2.buildQuery();
  }
  
  public static Query optimizableQuery() {
    Node[] nodes = new Node[10];
    double follow = 0.4;
    double filter = 0.2;
    
    QueryBuilder qb = new QueryBuilder();
    
    for(int i = 0; i < nodes.length; i++ ) {
      nodes[i] = new SimpleNode("node " + i, Object.class);
    }
    
    for(int i = 0; i < nodes.length; i++) {
      for(int j = 0; j < nodes.length; j++) {
        double p = Math.random();
        if(p <= follow) {
          if(p <= filter) {
            qb.addArc(new Arc(nodes[i], nodes[j]), new Filter.AcceptAll(Object.class));
          } else {
            qb.addArc(new Arc(nodes[i], nodes[j]), new Operation.Count(Object.class));
          }
        }
      }
    }
    
    return qb.buildQuery();
  }
  
  public static Query buildPersonByCarColorQuery(Color color, Node startNode) {
    Node selectNode = new SimpleResultNode("selectNode", Map.class);
    
    // select cars,  and count them
    QueryBuilder qb1 = new QueryBuilder();
    Node cars = new SimpleNode("cars", Map.class);
    Node ourCar = new SimpleResultNode("ourCar", Map.class);
    qb1.addArc(new Arc(startNode, cars), new MapFollower("car", Map.class));
    qb1.addArc(new Arc(cars, ourCar), new MapValueFilter("color", color));
    Query carQuery = qb1.buildQuery();
    
    Filter sizeFilter = new FilterByQuery(
      carQuery, startNode, Filter.CompareInteger.EQ, 1
    );
    
    QueryBuilder qb2 = new QueryBuilder();
    qb2.addArc(new Arc(startNode, selectNode), sizeFilter);
    
    return qb2.buildQuery();
  }

  public static Map buildCar(Color color, String make) {
    Map car = new HashMap();
    car.put("color", color);
    car.put("make", make);
    return car;
  }
  
  public static Map buildPerson(String name, int age, Map car) {
    Map person = new HashMap();
    person.put("name", name);
    person.put("age", new Integer(age));
    person.put("car", car);
    return person;
  }
  
  public static class MapFollower extends Follow {
    private Class clazz;
    private final Object key;
    
    public MapFollower(Object key, Class clazz) {
      this.key = key;
      this.clazz = clazz;
    }
    
    public Queryable follow(Object item) {
      return QueryTools.createQueryable(
        Collections.singleton(((Map) item).get(key)),
        clazz
      );
    }
    
    public Class getInputClass() {
      return Map.class;
    }
    
    public Class getOutputClass() {
      return clazz;
    }
  }
  
  public static class MapValueFilter extends Filter {
    private Object key;
    private Object value;
    
    public MapValueFilter(Object key, Object value) {
      this.key = key;
      this.value = value;
    }
    
    public boolean accept(Object o) {
      Map m = (Map) o;
      return m.get(key).equals(value);
    }
    
    public Class getInputClass() {
      return Map.class;
    }
    
    public Class getOutputClass() {
      return Map.class;
    }
    
    public String toString() {
      return "filter: " + key + " => " + value;
    }
  }
  
  public static void pause() {
    try {
      while(System.in.read() != '\n') {}
    } catch (java.io.IOException ioe) {
    }
  }
}
