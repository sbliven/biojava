package org.biojava.utils;

public interface Commitable {
  public void commit()
  throws NestedException;
  
  public void rollback();
}
