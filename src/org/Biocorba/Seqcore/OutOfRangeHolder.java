/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/OUTOFRANGEHOLDER.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public final class OutOfRangeHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public org.Biocorba.Seqcore.OutOfRange value;
    //	constructors 
    public OutOfRangeHolder() {
	this(null);
    }
    public OutOfRangeHolder(org.Biocorba.Seqcore.OutOfRange __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        org.Biocorba.Seqcore.OutOfRangeHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = org.Biocorba.Seqcore.OutOfRangeHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.Biocorba.Seqcore.OutOfRangeHelper.type();
    }
}
