package org.biojava.bio.seq.db;

import org.biojava.bio.*;

public class IllegalIDException extends BioException {
  public IllegalIDException() {
    super();
  }
  
  public IllegalIDException(Throwable t) {
    super(t);
  }
  
  public IllegalIDException(String message) {
    super(message);
  }
  
  public IllegalIDException(Throwable t, String message) {
    super(t, message);
  }
}
