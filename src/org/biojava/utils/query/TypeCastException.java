package org.biojava.utils.query;

public class TypeCastException extends org.biojava.utils.NestedError {
  public TypeCastException() {
    super();
  }
  
  public TypeCastException(String message) {
    super(message);
  }
  
  public TypeCastException(Throwable t) {
    super(t);
  }
  
  public TypeCastException(Throwable t, String message) {
    super(t, message);
  }
}
