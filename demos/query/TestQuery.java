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
    Query ageQuery25 = buildAgeQuery(25);
    Query ageQuery32 = buildAgeQuery(32);
    Query ageQuery45 = buildAgeQuery(45);
    
    System.out.println("nodes: " + ageQuery25.getNodes());
    System.out.println("arcs->ops: " + ageQuery25.getArcsToOperators());
    
    display("Age is 25", ageQuery25, people);
    display("Age is 32", ageQuery32, people);
    display("Age is 45", ageQuery45, people);
    pause();
    
    Query carQuery = buildCarQuery();
    
    display("Finding cars", carQuery, people);
    pause();
    
    Query carQuery25 = buildCarByAgeQuery(25);
    Query carQuery32 = buildCarByAgeQuery(32);
    Query carQuery45 = buildCarByAgeQuery(45);
    
    display("Cars where age is 25", carQuery25, people);
    display("Cars where age is 32", carQuery32, people);
    display("Cars where age is 45", carQuery45, people);
    pause();
    
    Query age25or32 = buildAgeQuery(25, 32);
    Query age25or42 = buildAgeQuery(25, 42);
    Query age32or42 = buildAgeQuery(32, 42);
    
    display("Cars where age25 or 32", age25or32, people);
    display("Cars where age25 or 42", age25or42, people);
    display("Cars where age32 or 42", age32or42, people);
    pause();
    
    Query age25CarC1 = buildAgeCarQuery(25, c1);
    Query age25CarC2 = buildAgeCarQuery(25, c2);
    
    display("People aged 25 and driving c1", age25CarC1, people);
    display("People aged 25 and driving c2", age25CarC2, people);
    pause();
    
    Query usingC1 = buildPersonByCarQuery(c1);
    Query usingC2 = buildPersonByCarQuery(c2);
    
    display("People driving c1", usingC1, people);
    display("People driving c2", usingC2, people);
    pause();

    Query drivingRed = buildPersonByCarColorQuery(Color.red);
    Query drivingGray = buildPersonByCarColorQuery(Color.gray);
    
    display("People driving red", drivingRed, people);
    display("People driving gray", drivingGray, people);
    pause();
  }
  
  public static void display(String message, Query query, Queryable items)
  throws OperationException {
    Node startNode
      = (Node) QueryTools.findNodeByLabel(query, "start").iterator().next();
    Node endNode
      = (Node) QueryTools.findNodeByLabel(query, "end").iterator().next();
    System.out.println(message);
    Queryable result = QueryTools.select(query, startNode, endNode, items);
    for(Iterator i = result.iterator(); i.hasNext(); ) {
      Object o = i.next();
      System.out.println("\t" + o);
    }
  }
  
  public static Query buildAgeQuery(int age)
  throws OperationException {
    QueryBuilder qb = new QueryBuilder();
    Node startNode = new SimpleNode("start", Map.class);
    Node endNode = new SimpleNode("end", Map.class);

    qb.addArc(
      new Arc(startNode, endNode),
      new MapValueFilter("age", new Integer(age))
    );
    
    return qb.buildQuery();
  }
  
  public static Query buildCarQuery()
  throws OperationException {
    QueryBuilder qb = new QueryBuilder();
    Node startNode = new SimpleNode("start", Map.class);
    Node endNode = new SimpleNode("end", Map.class);

    qb.addArc(
      new Arc(startNode, endNode),
      new MapFollower("car", Map.class)
    );
    
    return qb.buildQuery();
  }
  
  public static Query buildCarByAgeQuery(int age)
  throws OperationException {
    QueryBuilder qb = new QueryBuilder();
    
    Node startNode = new SimpleNode("start", Map.class);
    Node endNode = new SimpleNode("end", Map.class);
    Node ageSelected = new SimpleNode("ageSelected", Map.class);

    qb.addArc(
      new Arc(startNode, ageSelected),
      new MapValueFilter("age", new Integer(age))
    );
    qb.addArc(
      new Arc(ageSelected, endNode),
      new MapFollower("car", Map.class)
    );
    
    return qb.buildQuery();
  }
  
  public static Query buildAgeQuery(int age1, int age2)
  throws OperationException {
    QueryBuilder qb = new QueryBuilder();
    
    Node startNode = new SimpleNode("start", Map.class);
    Node endNode = new SimpleNode("end", Map.class);
    Arc startSelect = new Arc(startNode, endNode);

    qb.addArc(startSelect, new MapValueFilter("age", new Integer(age1)));
    qb.addArc(startSelect, new MapValueFilter("age", new Integer(age2)));
    
    return qb.buildQuery();
  }
  
  public static Query buildAgeCarQuery(int age, Map car)
  throws OperationException {
    QueryBuilder qb = new QueryBuilder();
    
    Node startNode = new SimpleNode("start", Map.class);
    Node aged = new SimpleNode("aged", Map.class);
    Node endNode = new SimpleNode("end", Map.class);
    
    qb.addArc(
      new Arc(startNode, aged),
      new MapValueFilter("age", new Integer(age))
    );
    qb.addArc(
      new Arc(aged, endNode),
      new MapValueFilter("car", car)
    );
    
    return qb.buildQuery();
  }
  
  public static Query buildPersonByCarQuery(Map car)
  throws OperationException {
    Node startNode = new SimpleNode("start", Map.class);
    Node endNode = new SimpleNode("end", Map.class);
    
    // select cars,  and count them
    QueryBuilder qb1 = new QueryBuilder();
    Node cars = new SimpleNode("cars", Map.class);
    Node ourCar = new SimpleNode("ourCar", Map.class);
    qb1.addArc(new Arc(startNode, cars), new MapFollower("car", Map.class));
    qb1.addArc(new Arc(cars, ourCar), new Filter.Equals(car, Map.class));
    Query carQuery = qb1.buildQuery();
    
    Filter sizeFilter = new FilterByQuery(
      carQuery, startNode, ourCar,
      Filter.CompareInteger.EQ, 1
    );
    
    QueryBuilder qb2 = new QueryBuilder();
    qb2.addArc(new Arc(startNode, endNode), sizeFilter);
    
    return qb2.buildQuery();
  }
  
  public static Query buildPersonByCarColorQuery(Color color)
  throws OperationException {
    Node startNode = new SimpleNode("start", Map.class);
    Node endNode = new SimpleNode("end", Map.class);
    
    // select cars,  and count them
    QueryBuilder qb1 = new QueryBuilder();
    Node cars = new SimpleNode("cars", Map.class);
    Node ourCar = new SimpleNode("ourCar", Map.class);
    qb1.addArc(new Arc(startNode, cars), new MapFollower("car", Map.class));
    qb1.addArc(new Arc(cars, ourCar), new MapValueFilter("color", color));
    Query carQuery = qb1.buildQuery();
    
    Filter sizeFilter = new FilterByQuery(
      carQuery, startNode, ourCar,
      Filter.CompareInteger.EQ, 1
    );
    
    QueryBuilder qb2 = new QueryBuilder();
    qb2.addArc(new Arc(startNode, endNode), sizeFilter);
    
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
