package org.biojava.bio;

import java.util.*;

import org.biojava.utils.*;

public interface PropertyConstraint {
  public boolean accept(Object value);
  
  public boolean subConstraintOf(PropertyConstraint subConstraint);
  
  public class Anything implements PropertyConstraint {
    public Anything() {}
    public boolean accept(Object value) {
      return true;
    }
    
    public boolean subConstraintOf(PropertyConstraint subConstraint) {
      return true;
    }
  }
  
  public class ByClass implements PropertyConstraint {
    private Class cl;
    
    public ByClass(Class cl) {
      this.cl = cl;
    }
    
    public Class getPropertyClass() {
      return cl;
    }
    
    public boolean accept(Object value) {
      return cl.isInstance(value);
    }
    
    public boolean subConstraintOf(PropertyConstraint subConstraint) {
      if(subConstraint instanceof ByClass) {
        ByClass sc = (ByClass) subConstraint;
        return cl.isAssignableFrom(sc.getPropertyClass());
      }
      
      return false;
    }
  }
  
  public class ByAnnotationType implements PropertyConstraint {
    private AnnotationType annType;
    
    public ByAnnotationType(AnnotationType annType) {
      this.annType = annType;
    }
    
    public AnnotationType getAnnotationType() {
      return annType;
    }
    
    public boolean accept(Object value) {
      if(value instanceof Annotation) {
        return annType.instanceOf((Annotation) value);
      }
      
      return false;
    }
    
    public boolean subConstraintOf(PropertyConstraint subConstraint) {
      if(subConstraint instanceof ByAnnotationType) {
        ByAnnotationType at = (ByAnnotationType) subConstraint;
        return annType.subTypeOf(at.getAnnotationType());
      }
      
      return false;
    }
  }
  
  public class IsCollectionOf implements PropertyConstraint {
    private PropertyConstraint elementType;
    private int minTimes;
    private int maxTimes;
    
    protected IsCollectionOf(PropertyConstraint elementType) {
      this(elementType, 0, Integer.MAX_VALUE);
    }
    
    protected IsCollectionOf(
      PropertyConstraint elementType,
      int minTimes,
      int maxTimes
    ) {
      this.elementType = elementType;
      this.minTimes = minTimes;
      this.maxTimes = maxTimes;
    }
    
    public PropertyConstraint getElementType() {
      return elementType;
    }
    
    public int getMinTimes() {
      return minTimes;
    }
    
    public int getMaxTimes() {
      return maxTimes;
    }
    
    protected Class getCollectionClass() {
      return Collection.class;
    }
    
    public boolean accept(Object item) {
      if(item instanceof Collection) {
        Collection c = (Collection) item;
        int size = c.size();
        return
          (size >= minTimes) &&
          (size <= maxTimes) &&
          getCollectionClass().isInstance(item);
      }
      
      return false;
    }
    
    public boolean subConstraintOf(PropertyConstraint subC) {
      if(subC instanceof IsCollectionOf) {
        IsCollectionOf ico = (IsCollectionOf) subC;
        return
          (minTimes <= ico.getMinTimes()) &&
          (maxTimes >= ico.getMaxTimes()) &&
          (elementType.subConstraintOf(ico.getElementType())) &&
          (getCollectionClass().isAssignableFrom(ico.getCollectionClass()));
      }
      
      return false;
    }
  }
  
  public class IsList extends IsCollectionOf {
    public IsList(PropertyConstraint con) {
      super(con);
    }
    
    public IsList(PropertyConstraint con, int minTimes, int maxTimes) {
      super(con, minTimes, maxTimes);
    }
    
    protected Class getCollectionClass() {
      return List.class;
    }
  }
  
  public class IsSet extends IsCollectionOf {
    public IsSet(PropertyConstraint con) {
      super(con);
    }
    
    public IsSet(PropertyConstraint con, int minTimes, int maxTimes) {
      super(con, minTimes, maxTimes);
    }
    
    protected Class getCollectionClass() {
      return Set.class;
    }
  }
  
  public class Enumeration implements PropertyConstraint {
    private Set values;
    
    public Enumeration(Set values) {
      this.values = values;
    }
    
    public Set getValues() {
      return values;
    }
    
    public boolean accept(Object value) {
      return values.contains(value);
    }
    
    public boolean subConstraintOf(PropertyConstraint subConstraint) {
      if(subConstraint instanceof Enumeration) {
        Enumeration subE = (Enumeration) subConstraint;
        
        return values.containsAll(subE.getValues());
      }
      
      return false;
    }
  }
}

