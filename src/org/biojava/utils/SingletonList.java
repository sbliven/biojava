package org.biojava.utils;

import java.util.*;
import java.io.Serializable;
  
public class SingletonList extends AbstractList implements Serializable {
  private final Object obj;
  
  public SingletonList(Object obj) {
    this.obj = obj;
  }
  
  public int size() {
    return 1;
  }
  
  public Object get(int i) throws IndexOutOfBoundsException {
    if(i == 0) {
      return obj;
    } else {
      throw new IndexOutOfBoundsException("Can't access item " + i + " of 1");
    }
  }
}
