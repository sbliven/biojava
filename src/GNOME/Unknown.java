/*
 * File: SRC/GNOME/UNKNOWN.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package GNOME;
public interface Unknown
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity {
    void ref()
;
    void unref()
;
    org.omg.CORBA.Object query_interface(String repoid)
;
}
