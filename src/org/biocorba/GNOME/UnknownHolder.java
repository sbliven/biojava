/*
 * File: SRC/ORG/BIOCORBA/GNOME/UNKNOWNHOLDER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.GNOME;
public final class UnknownHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public org.biocorba.GNOME.Unknown value;
    //	constructors 
    public UnknownHolder() {
	this(null);
    }
    public UnknownHolder(org.biocorba.GNOME.Unknown __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        org.biocorba.GNOME.UnknownHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = org.biocorba.GNOME.UnknownHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.biocorba.GNOME.UnknownHelper.type();
    }
}
