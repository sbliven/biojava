package org.biojava.utils.query;

import org.biojava.utils.NestedException;

public class OperationException extends NestedException {
  public OperationException() {
    super();
  }
  
  public OperationException(String message) {
    super(message);
  }
  
  public OperationException(Throwable t) {
    super(t);
  }
  
  public OperationException(Throwable t, String message) {
    super(t, message);
  }
}
