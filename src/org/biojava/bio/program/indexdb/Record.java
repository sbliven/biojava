package org.biojava.bio.program.indexdb;

import java.io.*;

import org.biojava.utils.io.*;

public interface Record {
  public String getID();
  public RAF getFile();
  public long getOffset();
  public int getLength();
  
  public static class Impl
  implements Record {
    private final String id;
    private final RAF file;
    private final long offset;
    private final int length;
    
    public Impl(String id, RAF file, long offset, int length) {
      if(id == null) {
        throw new NullPointerException("Can't have null id");
      }
      this.id = id;
      this.file = file;
      this.offset = offset;
      this.length = length;
    }
    
    public String  getID()     { return id; }
    public RAF     getFile()   { return file; }
    public long    getOffset() { return offset; }
    public int     getLength() { return length; }
  }
}
