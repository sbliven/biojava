package GNOME;

import java.lang.*;

public class _Unknown_Tie extends _UnknownImplBase {
  public _Unknown_Operations servant;
  public _Unknown_Tie(_Unknown_Operations servant) {
    this.servant = servant;
  }

  public void ref() {
    servant.ref(this);
  }

  public void unref() {
    servant.unref(this);
  }

  public org.omg.CORBA.Object query_interface(java.lang.String repoid) {
    return servant.query_interface(this, repoid);
  }
}
