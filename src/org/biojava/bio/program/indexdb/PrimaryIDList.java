package org.biojava.bio.program.indexdb;

import java.io.*;
import java.util.*;

import org.biojava.utils.*;

class PrimaryIDList
extends SearchableFileAsList {
  private Comparator INDEX_COMPARATOR = new Comparator() {
    public int compare(Object a, Object b) {
      String as;
      String bs;
      
      if(a instanceof Record) {
        as = ((Record) a).getID();
      } else {
        as = (String) a;
      }
      
      if(b instanceof Record) {
        bs = ((Record) b).getID();
      } else {
        bs = (String) b;
      }
      
      return BioStore.STRING_CASE_SENSITIVE_ORDER.compare(as, bs);
    }
  };
  
  private BioStore store;
  
  public PrimaryIDList(File file, int recordLen, BioStore store)
  throws IOException {
    super(file, recordLen);
    this.store = store;
  }
  
  public PrimaryIDList(File file, BioStore store)
  throws IOException {
    super(file);
    this.store = store;
  }
  
  protected Object parseRecord(byte[] buffer) {
    int lastI = 0;
    int newI = 0;
    while(buffer[newI] != '\t') {
      newI++;
    }
    String id = new String(buffer, lastI, newI - lastI);
    
    lastI = ++newI;
    while(buffer[newI] != '\t') {
      newI++;
    }
    File file = store.getFileForID(Integer.parseInt(new String(buffer, lastI, newI - lastI).trim()));
    
    lastI = ++newI;
    while(buffer[newI] != '\t') {
      newI++;
    }
    long start = Long.parseLong(new String(buffer, lastI, newI - lastI));
    
    newI++;
    int length = Integer.parseInt(
      new String(buffer, newI, buffer.length - newI).trim()
    );
    
    return new Record.Impl(id, file, start, length);
  }
  
  protected void generateRecord(byte[] buffer, Object item)
  throws NestedException {
    try {
      Record indx = (Record) item;
      
      String id = indx.getID();
      if(id == null) {
        throw new NestedException("Can't process null ID: " + indx);
      }
      int fileID = store.getIDForFile(indx.getFile());
      String start = String.valueOf(indx.getOffset());
      String length = String.valueOf(indx.getLength());
      
      int i = 0;
      byte[] str;
      
      str = id.getBytes();
      for(int j = 0; j < str.length; j++) {
        buffer[i++] = str[j];
      }
      
      buffer[i++] = '\t';
      
      str = String.valueOf(fileID).getBytes();
      for(int j = 0; j < str.length; j++) {
        buffer[i++] = str[j];
      }
      
      buffer[i++] = '\t';
      
      str = start.getBytes();
      for(int j = 0; j < str.length; j++) {
        buffer[i++] = str[j];
      }
      
      buffer[i++] = '\t';
      
      str = length.getBytes();
      for(int j = 0; j < str.length; j++) {
        buffer[i++] = str[j];
      }
      
      while(i < buffer.length) {
        buffer[i++] = ' ';
      }
    } catch (IOException ioe) {
      throw new NestedException("Could not build record");
    }
  }
  
  public Comparator getComparator() {
    return INDEX_COMPARATOR;
  }
}

