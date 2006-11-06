/*
 * Created on 13-Nov-2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.ensembl.util;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author arne
 * Should behave like a Hashtable but as well like an LRU cache
 * Objects which are put in are expected to be put in under more than
 * one key, so the keys for an objects will be tracked
 */
public class LruCache {

  private Hashtable listObjects;
  private Hashtable objIdentity;

  private ListElement header;
  private int size;
  private int maxSize;

  public LruCache(int maxSize) {
    if (maxSize < 1) {
      throw new IllegalArgumentException("Cache has to be minimally 1 Element big");
    }
    this.maxSize = maxSize;
    header = new ListElement();
    listObjects = new Hashtable();
    objIdentity = new Hashtable();
    size = 0;
  }

  /**
   * Convenience method that converts long to 
   * Long for storage.
   * @param key key for value in map.
   * @return value if present, otherwise null.
   */
  public Object get(long key) {
    return get(new Long(key));
  }
  
  public Object get(Object key) {
    ListElement obj = (ListElement) listObjects.get(key);

    if (obj != null) {
      header.moveTop(obj);
      return obj.content;
    } else {
      return null;
    }
  }


  /**
   * Convenience method that converts long to 
   * Long for storage.
   * @param value value to be stored.
   * @param key key for value in map.
   */
  public void put(Object value, long key) {
    put(value, new Long(key)); 
  }

  public void put(Object value, Object key) {
    if (listObjects.containsKey(key)) {
      // this key is already in, just update lru position
      ListElement elem = (ListElement) listObjects.get(key);
      header.moveTop(elem);
      return;
    }

    if (objIdentity.containsKey(value)) {
      // this value is already in, just need the new key
      Object oldKeys[] = (Object[]) objIdentity.get(value);
      Object newKeys[] = new Object[oldKeys.length + 1];
      for (int i = 0; i < oldKeys.length; i++) {
        newKeys[i] = oldKeys[i];
      }
      newKeys[oldKeys.length] = key;
      objIdentity.put(value, newKeys);
      ListElement elem = (ListElement) listObjects.get(oldKeys[0]);
      listObjects.put(key, elem);
      return;
    }

    // real addition
    ListElement elem = header.createTop(value);
    listObjects.put(key, elem);
    Object[] keys = new Object[1];
    keys[0] = key;
    objIdentity.put(value, keys);
    size++;

    if (size > maxSize)
      removeValue(header.dropBottom());

  }

  public Object removeValue(Object value) {
    
    if (value==null) return null;
    
    Object[] deadKeys = (Object[]) objIdentity.get(value);
    
    // not in cache
    if (deadKeys==null) return null;
    
    objIdentity.remove(value);
    for (int i = 0; i < deadKeys.length; i++) {
      listObjects.remove(deadKeys[i]);
    }
    size--;
    
    return value;
  }

  public Object removeValueByKey(Object key) {
    Object value = get(key);
    if (value!=null)
      removeValue(value);
      
    return value;
  }

  public void put(Object value, Object key1, Object key2) {
    put(value, key1);
    put(value, key2);
  }

  public void put(Object value, Object key1, Object key2, Object key3) {
    put(value, key1);
    put(value, key2);
    put(value, key3);
  }

  public int getSize() {
    return size;
  }
  public int listSize() {
    return header.size();
  }

  public String toString() {
    StringBuffer str = new StringBuffer();
    str.append(listObjects.toString());
    str.append("\n");
    str.append(objIdentity.toString());
    str.append("\n");
    str.append(header.toString(1));
    return str.toString();
  }

  public void clear() {
    header.next = header;
    header.prev = header;
    listObjects.clear();
    objIdentity.clear();
    size = 0;
  }

  public Collection values() {
    LinkedList r = new LinkedList();
    for (Iterator iter = listObjects.values().iterator(); iter.hasNext();){ 
      ListElement e = (ListElement) iter.next();
      if (e!=null) 
        r.add(e.getContent());
    }
    return r;
  }


  public Set keys() {
    return listObjects.keySet();
  }


}
