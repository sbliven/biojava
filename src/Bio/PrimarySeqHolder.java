/*
 * File: SRC/BIO/PRIMARYSEQHOLDER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public final class PrimarySeqHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public Bio.PrimarySeq value;
    //	constructors 
    public PrimarySeqHolder() {
	this(null);
    }
    public PrimarySeqHolder(Bio.PrimarySeq __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        Bio.PrimarySeqHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = Bio.PrimarySeqHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return Bio.PrimarySeqHelper.type();
    }
}
