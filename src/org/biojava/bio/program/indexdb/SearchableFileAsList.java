package org.biojava.bio.program.indexdb;

import java.io.*;
import java.util.*;

import org.biojava.utils.*;

abstract class SearchableFileAsList
extends FileAsList {
  public SearchableFileAsList(File file, int recordLen)
  throws IOException {
    super(file, recordLen);
  }
  
  public SearchableFileAsList(File file)
  throws IOException {
    super(file);
  }
  
  public Object search(String id) {
    // binary search by id
    byte[] idBytes = id.getBytes();
    byte[] bytes;
    
    int min = 0;
    int max = size()-1;
    int cnt = 0;
    do { 
      int mid = (min + max + (++cnt) % 2) / 2;
      
      bytes = rawGet(mid);
      int cmp = cmp(bytes, idBytes);
      if(cmp < 0) {
        min = mid;
      } else if(cmp > 0) {
        max = mid;
      } else {
        return parseRecord(bytes);
      }
    } while(min != max);
    
    throw new NoSuchElementException("No element with id: " + id);
  }
  
  public List searchAll(String id) {
    // binary search by id
    byte[] idBytes = id.getBytes();
    byte[] bytes;
    
    int prev = size() / 2;
    int jumpSize = size() / 4;
    do {
      bytes = rawGet(prev);
      int cmp = cmp(bytes, idBytes);
      if(cmp == 0) {
        break;
      }
      
      prev = prev += cmp * jumpSize;
      jumpSize /= 2;
    } while (jumpSize > 0);
    
    ArrayList items = new ArrayList();
    
    // scan back through file for all items with the same ID
    for(int i = prev-1; ; i--) {
      bytes = rawGet(i);
      if(cmp(bytes, idBytes) != 0) {
        break;
      }
      items.add(parseRecord(bytes));
    }
    
    // scan forward through file for all items with the same ID
    for(int i = prev; ; i++) {
      bytes = rawGet(i);
      if(cmp(bytes, idBytes) != 0) {
        break;
      }
      items.add(parseRecord(bytes));
    }
    
    return items;
  }
  
  private int cmp(byte[] a, byte[] b) {
    int iMax = Math.min(a.length, b.length);
    for(int i = 0; i < iMax; i++) {
      if(a[i] < b[i]) return -1;
      if(a[i] > b[i]) return +1;
    }
    
    return 0;
  }
}

