package org.biojava.bridge.biocorba;

import GNOME.*;

public class UnknownImpl implements _UnknownOperations {
  private int refCounter;
  
  public void ref() {
    refCounter++;
  }
  
  /**
   * I think that this should release the object from the server when no
   * clients are referencing it.
   */
  public void unref() {
    refCounter--;
/*    if(refCounter == 0) {
      super._release();
    } */
  }
  
  public org.omg.CORBA.Object query_interface(String repoid) {
    return null;
  }
}
