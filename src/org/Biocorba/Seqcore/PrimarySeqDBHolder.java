/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/PRIMARYSEQDBHOLDER.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public final class PrimarySeqDBHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public org.Biocorba.Seqcore.PrimarySeqDB value;
    //	constructors 
    public PrimarySeqDBHolder() {
	this(null);
    }
    public PrimarySeqDBHolder(org.Biocorba.Seqcore.PrimarySeqDB __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        org.Biocorba.Seqcore.PrimarySeqDBHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = org.Biocorba.Seqcore.PrimarySeqDBHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.Biocorba.Seqcore.PrimarySeqDBHelper.type();
    }
}
