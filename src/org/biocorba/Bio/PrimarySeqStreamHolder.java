/*
 * File: SRC/ORG/BIOCORBA/BIO/PRIMARYSEQSTREAMHOLDER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public final class PrimarySeqStreamHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public org.biocorba.Bio.PrimarySeqStream value;
    //	constructors 
    public PrimarySeqStreamHolder() {
	this(null);
    }
    public PrimarySeqStreamHolder(org.biocorba.Bio.PrimarySeqStream __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        org.biocorba.Bio.PrimarySeqStreamHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = org.biocorba.Bio.PrimarySeqStreamHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.biocorba.Bio.PrimarySeqStreamHelper.type();
    }
}
