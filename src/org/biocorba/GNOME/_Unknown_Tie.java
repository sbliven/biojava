/*
 * File: SRC/ORG/BIOCORBA/GNOME/_UNKNOWNTIE.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.GNOME;
public class _Unknown_Tie extends org.biocorba.GNOME._UnknownImplBase {
    public org.biocorba.GNOME._Unknown_Operations servant;
    public _Unknown_Tie(org.biocorba.GNOME._Unknown_Operations servant) {
           this.servant = servant;
    }
    public void ref()
    {
        servant.ref(this);
    }
    public void unref()
    {
        servant.unref(this);
    }
    public org.omg.CORBA.Object query_interface(String repoid)
    {
        return servant.query_interface(this, repoid);
    }
}
