package org.biojava.bio.program.indexdb;

import java.io.*;


public interface Record {
  public String getID();
  public File getFile();
  public long getOffset();
  public int getLength();
  
  public static class Impl
  implements Record {
    private final String id;
    private final File file;
    private final long offset;
    private final int length;
    
    public Impl(String id, File file, long offset, int length) {
      this.id = id;
      this.file = file;
      this.offset = offset;
      this.length = length;
    }
    
    public String getID()     { return id; }
    public File   getFile()   { return file; }
    public long   getOffset() { return offset; }
    public int    getLength() { return length; }
  }
}
