package org.biojava.utils.query;

class PermutedTuple implements Tuple {
  private final Tuple source;
  private final int[] order;
  private final Tuple.ClassList classList;
  
  public PermutedTuple(Tuple source, int[] order) {
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
    return source.getObject(order[indx]);
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
}
