package org.biojava.utils.query;

/**
 * A no-frills implementation of Tuple.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class SimpleTuple implements Tuple {
  private final Object[] items;
  private final Tuple.ClassList classList;
  
  /**
   * Construct a new SimpleTuple with this list of items and a classList.
   */
  public SimpleTuple(Object[] items, Tuple.ClassList classList) {
    if(items.length != classList.size()) {
      throw new IllegalArgumentException(
        "Items and classList must be the same length: " + items.length + ":"
        + classList.size()
      );
    }
    for(int i = 0; i < items.length; i++) {
      if(!classList.getClass(i).isInstance(items[i])) {
        throw new IllegalArgumentException(
          "Item " + i + " is not of type " + classList.getClass(i) + " but "
          + items[i].getClass()
        );
      }
    }
    this.items = items;
    this.classList = classList;
  }
  
  public Object getObject(int indx) {
    try {
      return items[indx];
    } catch (IndexOutOfBoundsException iobe) {
      throw new IndexOutOfBoundsException("Can't access index " + indx + ":"
      + items.length);
    }
  }
  
  public Tuple.ClassList getClassList() {
    return classList;
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer("(");
    sb.append(getObject(0).toString());
    for(int i = 1; i < getClassList().size(); i++) {
      sb.append(",");
      sb.append(getObject(i).toString());
    }
    sb.append(")");
    return sb.toString();
  }
  
  /**
   * A no-frills implementation of Tuple.ClassList.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public static final class ClassList implements Tuple.ClassList {
    private final Class[] classes;
    
    public ClassList(Class[] classes) {
      this.classes = classes;
    }
    
    public Class getClass(int indx) {
      return classes[indx];
    }
    
    public int size() {
      return classes.length;
    }
    
    public String toString() {
      StringBuffer sb = new StringBuffer(getClass(0).toString());
      for(int i = 1; i < size(); i++) {
        sb.append(",");
        sb.append(getClass(i).toString());
      }
      return sb.toString();
    }
  }
}
