package org.biojava.bio.program.indexdb;

import java.util.*;
import org.biojava.utils.*;

class CacheList
extends
  AbstractList
implements
  Commitable,
  SearchableList
{
  private SearchableList delegate;
  private List shadow;
  
  public CacheList(SearchableList delegate) {
    this.delegate = delegate;
    shadow = new ArrayList();
    int l = delegate.size();
    for(int i = 0; i < l; i++) {
      shadow.add(null);
    }
  }
  
  public int size() {
    return shadow.size();
  }
  
  public Object get(int indx) {
    Object o = shadow.get(indx);
    if(o == null) {
      o = delegate.get(indx);
    }
    
    return o;
  }
  
  public Object set(int indx, Object val) {
    return shadow.set(indx, val);
  }
  
  public boolean add(Object val) {
    return shadow.add(val);
  }
  
  public Object search(String id) {
    return ((SearchableList) delegate).search(id);
  }
  
  public List searchAll(String id) {
    return ((SearchableList) delegate).searchAll(id);
  }
  
  public void commit()
  throws NestedException {
    delegate.clear();
    
    for(Iterator i = shadow.iterator(); i.hasNext(); ) {
      delegate.add(i.next());
    }
    
    ((Commitable) delegate).commit();
  }
  
  public void rollback() {
    ((Commitable) delegate).rollback();
    shadow.clear();
    int l = delegate.size();
    for(int i = 0; i < l; i++) {
      shadow.add(null);
    }
  }
  
  public Comparator getComparator() {
    return delegate.getComparator();
  }
}
