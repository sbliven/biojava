/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/ANONYMOUSSEQHOLDER.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public final class AnonymousSeqHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public org.Biocorba.Seqcore.AnonymousSeq value;
    //	constructors 
    public AnonymousSeqHolder() {
	this(null);
    }
    public AnonymousSeqHolder(org.Biocorba.Seqcore.AnonymousSeq __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        org.Biocorba.Seqcore.AnonymousSeqHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = org.Biocorba.Seqcore.AnonymousSeqHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.Biocorba.Seqcore.AnonymousSeqHelper.type();
    }
}
