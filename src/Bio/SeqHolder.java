/*
 * File: SRC/BIO/SEQHOLDER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public final class SeqHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public Bio.Seq value;
    //	constructors 
    public SeqHolder() {
	this(null);
    }
    public SeqHolder(Bio.Seq __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        Bio.SeqHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = Bio.SeqHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return Bio.SeqHelper.type();
    }
}
