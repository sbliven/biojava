package org.biojava.bio.program.indexdb;

import java.io.*;
import java.util.*;

import org.biojava.utils.*;

abstract class SearchableFileAsList
extends
  FileAsList
implements
  SearchableList
{
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
    do {
      int mid = (min + max) / 2;
      
      bytes = rawGet(mid);
      int cmp = cmp(bytes, idBytes);
      
      if(cmp < 0) {
        if(min != mid) {
          min = mid;
        } else {
          min = mid+1;
        }
      } else if(cmp > 0) {
        if(max != mid) {
          max = mid;
        } else {
          max = mid-1;
        }
      } else if(cmp == 0) {
        return parseRecord(bytes);
      }
    } while(min <= max);
    
    throw new NoSuchElementException("No element with id: " + id);
  }
  
  public List searchAll(String id) {
    // binary search by id
    byte[] idBytes = id.getBytes();
    byte[] bytes;
    
    int min = 0;
    int max = size()-1;
    int mid = -1;
    do {
      mid = (min + max) / 2;
      
      bytes = rawGet(mid);
      int cmp = cmp(bytes, idBytes);
      
      if(cmp < 0) {
        if(min != mid) {
          min = mid;
        } else {
          min = mid+1;
        }
      } else if(cmp > 0) {
        if(max != mid) {
          max = mid;
        } else {
          max = mid-1;
        }
      } else if(cmp == 0) {
        break;
      }
    } while(min <= max);
    
    if(min > max) {
      throw new NoSuchElementException("No element with id: " + id);
    }
    
    ArrayList items = new ArrayList();
    
    // scan back through file for all items with the same ID
    for(int i = mid-1; i >= 0; i--) {
      bytes = rawGet(i);
      if(cmp(bytes, idBytes) != 0) {
        break;
      }
      items.add(parseRecord(bytes));
    }
    
    // scan forward through file for all items with the same ID
    for(int i = mid; i < size(); i++) {
      bytes = rawGet(i);
      if(cmp(bytes, idBytes) != 0) {
        System.out.println("Stopped at: " + i);
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

