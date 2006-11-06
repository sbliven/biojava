package org.ensembl.compara.driver;

public final class NonFatalException extends java.lang.RuntimeException {
  
  private static final long serialVersionUID = 1L;

  public NonFatalException(String message) {
    super(message);
  }
  
  public NonFatalException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
