package org.biojava.bio.seq.db;

import java.util.*;
import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.io.*;

// not thread-safe
public class BioIndex implements IndexStore {
  private static Comparator COMPARATOR = new Comparator() {
    public int compare(Object a, Object b) {
      String as;
      String bs;
      
      if(a instanceof Index) {
        as = ((Index) a).getID();
      } else {
        as = (String) a;
      }
      
      if(b instanceof Index) {
        bs = ((Index) b).getID();
      } else {
        bs = (String) b;
      }
      
      return String.CASE_INSENSITIVE_ORDER.compare(a, b);
    }
    
    public boolean equals(Object o) {
      return o == COMPARATOR;
    }
  };
  
  private Map fileIDToFile;
  private RandomAccessFile indxFile;
  private int recordLength;
  private List indxList;
  private Set idSet = new ListAsSet();
  private int commitedRecords;
  private String name;
  private SequenceFormat format;
  private SequenceBuilderFactory sbFactory;
  private SymbolTokenization symbolTokenization;
  
  public String getName() {
    return this.name;
  }
  
  public Index fetch(String id)
  throws IllegalIDException, BioException {
    int indx = Collections.binarySearch(indxList, id, COMPARATOR);
    if(indx < 0) {
      throw new IllegalIDException("Can't find sequence for " + id);
    }
    
    return (Index) indxList.get(indx);
  }
  
  public void store(Index indx) {
    indxList.add(indx);
  }
  
  public void commit()
  throws BioException {
    try {
      Collections.sort(indxList, COMPARATOR);
      
      commitedRecords = indxList.size();
    } catch (Exception e) {
      rollback();
      throw new BioException(e, "Unable to commit. Rolled back to be safe");
    }
  }
  
  public void rollback() {
    try {
      indxFile.setLength((long) commitedRecords * (long) recordLength);
    } catch (Throwable t) {
      throw new BioError(
        t, "Could not roll back. " +
        "The index store will be in an inconsistent state " +
        "and should be discarded"
      );
    }
  }
  
  public Set getIDs() {
    return idSet;
  }
  
  public Set getFiles() {
    return new HashSet(fileIDToFile.values());
  }
  
  public SequenceFormat getFormat() {
    return format;
  }
  
  public SequenceBuilderFactory getSBFactory() {
    return sbFactory;
  }
  
  public SymbolTokenization getSymbolParser() {
    return symbolTokenization;
  }
  
  // records stored as:
  // seqID(\w+) \t fileID(\w+) \t start(\d+) \t length(\d+) ' ' * \n
  private class FileAsList
  extends AbstractList
  implements RandomAccess {
    private int lastIndx;
    private Index lastRec;
    private byte[] buffer;
    
    {
      buffer = new byte[recordLength];
    }
    
    public Object get(int indx) {
      if(indx < 0 || indx >= size()) {
        throw new IndexOutOfBoundsException();
      }
      
      if(indx == lastIndx) {
        return lastRec;
      }
      
      long offset = indx * recordLength;
      try {
        indxFile.seek(offset);
        indxFile.readFully(buffer);
      } catch (IOException ioe) {
        throw new BioError(ioe, "Failed to seek for record");
      }
      
      int lastI = 0;
      int newI = 0;
      while(buffer[newI] != '\t') {
        newI++;
      }
      String id = new String(buffer, lastI, newI);
      
      while(buffer[newI] != '\t') {
        newI++;
      }
      File file = (File) fileIDToFile.get(new String(buffer, lastI, newI).trim());

      while(buffer[newI] != '\t') {
        newI++;
      }
      long start = Long.parseLong(new String(buffer, lastI, newI));
      
      int length = Integer.parseInt(
        new String(buffer, newI + 1, recordLength)
      );
      
      lastIndx = indx;
      lastRec = new SimpleIndex(file, start, length, id);
      return lastRec;
    }
    
    public int size() {
      try {
        return (int) (indxFile.length() / (long) recordLength);
      } catch (IOException ioe) {
        throw new BioError(ioe, "Can't read file length");
      }
    }
  }
  
  private class ListAsSet
  extends AbstractSet {
    public Iterator iterator() {
      return indxList.iterator();
    }
    
    public int size() {
      return indxList.size();
    }
  }
}
