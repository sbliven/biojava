/*
 * File: SRC/ORG/BIOCORBA/GNOME/_UNKNOWNOPERATIONS.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.GNOME;
/**

 */public interface _Unknown_Operations {
     void ref(org.omg.CORBA.portable.ObjectImpl unknown)
;
     void unref(org.omg.CORBA.portable.ObjectImpl unknown)
;
     org.omg.CORBA.Object query_interface(org.omg.CORBA.portable.ObjectImpl unknown, String repoid)
;
}
