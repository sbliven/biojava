/*
 * File: SRC/GNOME/_UNKNOWNTIE.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package GNOME;
public class _UnknownTie extends GNOME._UnknownImplBase {
    public GNOME._UnknownOperations servant;
    public _UnknownTie(GNOME._UnknownOperations servant) {
           this.servant = servant;
    }
    public void ref()
    {
        servant.ref();
    }
    public void unref()
    {
        servant.unref();
    }
    public org.omg.CORBA.Object query_interface(String repoid)
    {
        return servant.query_interface(repoid);
    }
}
