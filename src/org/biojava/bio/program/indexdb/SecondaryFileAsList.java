package org.biojava.bio.program.indexdb;

import java.io.*;
import java.util.*;

import org.biojava.utils.*;

class SecondaryFileAsList
extends SearchableFileAsList {
  private Comparator KEY_VALUE_COMPARATOR = new Comparator() {
    public int compare(Object a, Object b) {
      String as = a.toString();
      String bs = b.toString();
      
      return BioStore.STRING_CASE_SENSITIVE_ORDER.compare(as, bs);
    }
  };
  
  public SecondaryFileAsList(File file, int recordLen)
  throws IOException {
    super(file, recordLen);
  }
  
  public SecondaryFileAsList(File file)
  throws IOException {
    super(file);
  }
  
  protected Object parseRecord(byte[] buffer) {
    int tabI = 0;
    while(buffer[tabI] != '\t') {
      tabI++;
    }
    String prim = new String(buffer, 0, tabI);
    tabI++;
    String sec = new String(buffer, tabI, buffer.length - tabI).trim();
    
    return new KeyPair.Impl(prim, sec);
  }
  
  protected void generateRecord(byte[] buffer, Object item) {
    KeyPair kp = (KeyPair) item;
    
    int i = 0;
    byte[] str;
    
    str = kp.getPrimary().getBytes();
    for(int j = 0; j < str.length; j++) {
      buffer[i++] = str[j];
    }
    
    buffer[i++] = '\t';
    
    str = kp.getSecondary().getBytes();
    for(int j = 0; j < str.length; j++) {
      buffer[i++] = str[j];
    }
    
    while(i < buffer.length) {
      buffer[i++] = ' ';
    }
  }
  
  public Comparator getComparator() {
    return KEY_VALUE_COMPARATOR;
  }
}
