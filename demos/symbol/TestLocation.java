package symbol;

import org.biojava.bio.symbol.*;

public class TestLocation {
  public static void main(String[] args) {
    Location[] base = {
      new PointLocation(10),
      new PointLocation(23),
      new RangeLocation(5, 15),
      new RangeLocation(30, 40),
      new RangeLocation(20, 50),
      new RangeLocation(100, 120),
      new RangeLocation(30, 40).union(new RangeLocation(70, 80))
    };
    
    for(int i = 0; i < base.length; i++) {
      System.out.println(i + "\t" + base[i]);
    }
    
    System.out.println("equals");
    dump(base, new BooleanBinaryOp() {
      public boolean calculate(Location a, Location b) {
        return LocationTools.areEqual(a, b);
      }
    });
    
    System.out.println("overlaps");
    dump(base, new BooleanBinaryOp() {
      public boolean calculate(Location a, Location b) {
        return LocationTools.overlaps(a, b);
      }
    });
    
    System.out.println("contains");
    dump(base, new BooleanBinaryOp() {
      public boolean calculate(Location a, Location b) {
        return LocationTools.contains(a, b);
      }
    });
    
    System.out.println("union");
    dump(base, new LocationBinaryOp() {
      public Location calculate(Location a, Location b) {
        return LocationTools.union(a, b);
      }
    });

    System.out.println("intersection");
    dump(base, new LocationBinaryOp() {
      public Location calculate(Location a, Location b) {
        return LocationTools.intersection(a, b);
      }
    });
  }
  
  public static void dump(Location[] base, BooleanBinaryOp op) {
    for(int i = 0; i < base.length; i++) {
      for(int j = 0; j < base.length; j++) {
        System.out.print("\t" + op.calculate(base[i], base[j]));
      }
      System.out.print("\n");
    }
  }
  
  public static void dump(Location[] base, LocationBinaryOp op) {
    for(int i = 0; i < base.length; i++) {
      for(int j = 0; j < base.length; j++) {
        System.out.print("\t" + op.calculate(base[i], base[j]));
      }
      System.out.print("\n");
    }
  }
  
  public static interface BooleanBinaryOp {
    public boolean calculate(Location a, Location b);
  }
  
  public static interface LocationBinaryOp {
    public Location calculate(Location a, Location b);
  }
}
