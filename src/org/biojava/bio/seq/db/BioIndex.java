package org.biojava.bio.seq.db;

import java.util.*;
import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.io.*;

// not thread-safe
public class BioIndex implements IndexStore {
  private static Comparator STRING_CASE_SENSITIVE_ORDER = new Comparator() {
    public int compare(Object a, Object b) {
      return ((String) a).compareTo(b);
    }
  };
  
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
      
      return STRING_CASE_SENSITIVE_ORDER.compare(a, b);
    }
    
    public boolean equals(Object o) {
      return o == COMPARATOR;
    }
  };
  
  private File indexDirectory;
  
  private Map fileIDToFile;
  private Map fileToFileID;
  
  private RandomAccessFile indxFile;
  
  private int recordLength;
  private List indxList;
  private Set idSet = new ListAsSet();
  private int commitedRecords;
  private String name;
  private SequenceFormat format;
  private SequenceBuilderFactory sbFactory;
  private SymbolTokenization symbolTokenization;
  
  public BioIndex(
    File indexDirectory
  ) {
    this.indexDirectory = indexDirectory;
  }
  
  private File getFileForID(String id) {
    return (File) fileIDToFile.get(id);
  }
  
  private String getIDForFile(File file) {
    return (String) fileToFileID.get(file);
  }
  
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
      File file = getFileForID(new String(buffer, lastI, newI).trim());

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
    
    public boolean add(Object o) {
      Index indx = (Index) o;
      
      String id = indx.getID();
      String fileID = getIDForFile(indx.getFile());
      String start = String.valueOf(indx.getStart());
      String length = String.valueOf(indx.getLength());
      
      int i = 0;
      byte[] str;
      
      str = id.getBytes();
      for(int j = 0; j < str.length; j++) {
        buffer[i++] = str[j];
      }
      
      buffer[i++] = '\t';
      
      str = fileID.getBytes();
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
      
      while(i < buffer.length - 1) {
        buffer[i++] = ' ';
      }
      
      buffer[i] = '\n';
      
      try {
        indxFile.seek(indxFile.length());
        indxFile.write(buffer);
      } catch (IOException ioe) {
        throw new BioError(ioe, "Failed to write index");
      }
      
      return true;
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
