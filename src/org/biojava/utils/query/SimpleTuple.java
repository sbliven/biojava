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
    this.items = items;
    this.classList = classList;
  }
  
  public Object getObject(int indx) {
    return items[indx];
  }
  
  public Tuple.ClassList getClassList() {
    return classList;
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer(getObject(0).toString());
    for(int i = 1; i < getClassList().size(); i++) {
      sb.append(",");
      sb.append(getObject(i).toString());
    }
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
