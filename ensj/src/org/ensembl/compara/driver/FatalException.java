package org.ensembl.compara.driver;

public final class FatalException extends java.lang.RuntimeException {
  
  private static final long serialVersionUID = 1L;

  public FatalException(String message) {
    super(message);
  }
  
  public FatalException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
