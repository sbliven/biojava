/*
 * File: SRC/GNOME/UNKNOWN.JAVA
 * From: IDL\GNOME.11-02-2000.IDL
 * Date: Fri Feb 11 13:56:13 2000
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
