/*
 * File: SRC/BIO/NAMEVALUESETHOLDER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public final class NameValueSetHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public Bio.NameValueSet value;
    //	constructors 
    public NameValueSetHolder() {
	this(null);
    }
    public NameValueSetHolder(Bio.NameValueSet __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        Bio.NameValueSetHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = Bio.NameValueSetHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return Bio.NameValueSetHelper.type();
    }
}
