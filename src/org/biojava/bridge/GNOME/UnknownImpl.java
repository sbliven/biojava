package org.biojava.bridge.GNOME;

import GNOME.*;

public class UnknownImpl implements _Unknown_Operations {
  private int refCounter;
  
  public void ref(org.omg.CORBA.portable.ObjectImpl unknown) {
    refCounter++;
  }
  
  /**
   * I think that this should release the object from the server when no
   * clients are referencing it.
   */
  public void unref(org.omg.CORBA.portable.ObjectImpl unknown) {
    refCounter--;
    if(refCounter == 0) {
      unknown._release();
    }
  }
  
  public org.omg.CORBA.Object query_interface(org.omg.CORBA.portable.ObjectImpl unknown, String repoid) {
    return null;
  }
}
