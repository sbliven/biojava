package org.biojava.utils.query;

class PermutedTuple implements Tuple {
  private final Tuple source;
  private final int[] order;
  private final Tuple.ClassList classList;
  
  public PermutedTuple(Tuple source, int[] order)
  throws IllegalArgumentException {
    int size = source.getClassList().size();
    for(int i = 0; i < order.length; i++) {
      if(order[i] >= size) {
        throw new IllegalArgumentException("Can't create perumutation: "
        + i + ":" + order[i] + " >= " + size);
      }
    }
    
    this.source = source;
    this.order = order;
    classList = new Tuple.ClassList() {
      public Class getClass(int indx) {
        return PermutedTuple.this.source.getClassList().getClass(
          PermutedTuple.this.order[indx]
        );
      }
      
      public int size() {
        return PermutedTuple.this.order.length;
      }
    
      public String toString() {
        StringBuffer sb = new StringBuffer(getClass(0).toString());
        for(int i = 1; i < size(); i++) {
          sb.append(",");
          sb.append(getClass(i).toString());
        }
        return sb.toString();
      }
    };
  }
  
  public Object getObject(int indx) {
    try {
      return source.getObject(order[indx]);
    } catch (IndexOutOfBoundsException iobe) {
      throw new IndexOutOfBoundsException(
        "index " + indx + " not in array length " + order.length
      );
    }
  }
  
  public Tuple.ClassList getClassList() {
    return classList;
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer("(");
    sb.append(order[0] + ":" + getObject(0).toString());
    for(int i = 1; i < getClassList().size(); i++) {
      sb.append("," + order[i] + ":");
      sb.append(getObject(i).toString());
    }
    sb.append(")");
    return sb.toString();
  }
}
