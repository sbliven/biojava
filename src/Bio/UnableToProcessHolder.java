/*
 * File: SRC/BIO/UNABLETOPROCESSHOLDER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public final class UnableToProcessHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public Bio.UnableToProcess value;
    //	constructors 
    public UnableToProcessHolder() {
	this(null);
    }
    public UnableToProcessHolder(Bio.UnableToProcess __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        Bio.UnableToProcessHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = Bio.UnableToProcessHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return Bio.UnableToProcessHelper.type();
    }
}
