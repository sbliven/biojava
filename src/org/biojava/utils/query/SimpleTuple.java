package org.biojava.utils.query;

/**
 * A no-frills implementation of Tuple.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class SimpleTuple implements Tuple {
  private final Object[] items;
  private final Tuple.TypeList typeList;
  
  /**
   * Construct a new SimpleTuple with this list of items and a typeList.
   */
  public SimpleTuple(Object[] items, Tuple.TypeList typeList) {
    if(items.length != typeList.size()) {
      throw new IllegalArgumentException(
        "Items and typeList must be the same length: " + items.length + ":"
        + typeList.size()
      );
    }
    for(int i = 0; i < items.length; i++) {
      if(!typeList.getType(i).isInstance(items[i])) {
        throw new IllegalArgumentException(
          "Item " + i + " is not of type " + typeList.getType(i) + " but "
          + items[i].getClass()
        );
      }
    }
    this.items = items;
    this.typeList = typeList;
  }
  
  public Object getObject(int indx) {
    try {
      return items[indx];
    } catch (IndexOutOfBoundsException iobe) {
      throw new IndexOutOfBoundsException("Can't access index " + indx + ":"
      + items.length);
    }
  }
  
  public Tuple.TypeList getTypeList() {
    return typeList;
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer("(");
    sb.append(getObject(0).toString());
    for(int i = 1; i < getTypeList().size(); i++) {
      sb.append(",");
      sb.append(getObject(i).toString());
    }
    sb.append(")");
    return sb.toString();
  }
  
  public int hashCode() {
    int hc = getObject(0).hashCode();
    for(int i = 1; i < getTypeList().size(); i++) {
      hc = hc ^ getObject(i).hashCode();
    }
    return hc;
  }
  
  public boolean equals(Object o) {
    if(o instanceof Tuple) {
      Tuple tup = (Tuple) o;
      if(!getTypeList().equals(tup.getTypeList())) {
        return false;
      }
      
      for(int i = 0; i < getTypeList().size(); i++) {
        if(getObject(i).equals(tup.getObject(i))) {
          return false;
        }
      }
      
      return true;
    }
    
    return false;
  }
  
  /**
   * A no-frills implementation of Tuple.TypeList.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public static final class TypeList extends Tuple.TypeList {
    private final Type[] types;
    
    public TypeList(Type[] types) {
      this.types = types;
    }
    
    public Type getType(int indx) {
      return types[indx];
    }
    
    public int size() {
      return types.length;
    }
  }
}
