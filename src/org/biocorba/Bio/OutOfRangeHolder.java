/*
 * File: SRC/ORG/BIOCORBA/BIO/OUTOFRANGEHOLDER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public final class OutOfRangeHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public org.biocorba.Bio.OutOfRange value;
    //	constructors 
    public OutOfRangeHolder() {
	this(null);
    }
    public OutOfRangeHolder(org.biocorba.Bio.OutOfRange __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        org.biocorba.Bio.OutOfRangeHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = org.biocorba.Bio.OutOfRangeHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.biocorba.Bio.OutOfRangeHelper.type();
    }
}
