package org.biojava.utils.io;

import java.io.*;

public class RAF extends RandomAccessFile {
  private File file;
  
  public RAF(File file, String mode)
  throws FileNotFoundException {
    super(file, mode);
    this.file = file;
  }
  
  public RAF(String name, String mode)
  throws FileNotFoundException {
    super(name, mode);
    this.file = new File(name);
  }
  
  public File getFile() {
    return file;
  }
}
