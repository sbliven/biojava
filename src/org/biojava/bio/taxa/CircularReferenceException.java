package org.biojava.bio.taxa;

import org.biojava.bio.*;

public class CircularReferenceException extends BioException {
  public CircularReferenceException() {
    super();
  }
  
  public CircularReferenceException(Throwable cause) {
    super(cause);
  }
  
  public CircularReferenceException(String message) {
    super(message);
  }
  
  public CircularReferenceException(Throwable cause, String message) {
    super(cause, message);
  }
}
