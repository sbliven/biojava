package org.biojava.utils.query;

class PermutedTuple implements Tuple {
  static class TypeList extends Tuple.TypeList {
    private final int[] order;
    private final Tuple.TypeList typeList;
    
    public TypeList(int[] order, Tuple.TypeList typeList) {
      this.order = order;
      this.typeList = typeList;
    }
    
    public int size() {
      return order.length;
    }
    
    public int mapIndex(int indx) {
      try {
        return order[indx];
      } catch (IndexOutOfBoundsException iobe) {
        throw new IndexOutOfBoundsException(
          "index " + indx + " not in array length " + order.length
        );
      }
    }
    
    public Type getType(int indx) {
      return typeList.getType(mapIndex(indx));
    }
    
    public Type getOriginalType() {
      return typeList;
    }
  }
  
  private final Tuple source;
  private final PermutedTuple.TypeList typeList;
  
  public PermutedTuple(Tuple source, PermutedTuple.TypeList typeList)
  throws IllegalArgumentException {
    if(!typeList.getOriginalType().isAssignableFrom(source.getTypeList())) {
      throw new TypeCastException(
        "Can't cast from " + source.getTypeList()
        + " to " + typeList.getOriginalType()
      );
    }

    this.source = source;
    this.typeList = typeList;
  }
  
  public Object getObject(int indx) {
    return source.getObject(typeList.mapIndex(indx));
  }
  
  public Tuple.TypeList getTypeList() {
    return typeList;
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer("(");
    sb.append(typeList.mapIndex(0) + ":" + getObject(0).toString());
    for(int i = 1; i < typeList.size(); i++) {
      sb.append("," + typeList.mapIndex(i) + ":");
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
        if(!getObject(i).equals(tup.getObject(i))) {
          return false;
        }
      }
      
      return true;
    }
    
    return false;
  }
}
