/*
 * File: SRC/GNOME/UNKNOWNHOLDER.JAVA
 * From: IDL\GNOME.11-02-2000.IDL
 * Date: Fri Feb 11 13:56:13 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package GNOME;
public final class UnknownHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public GNOME.Unknown value;
    //	constructors 
    public UnknownHolder() {
	this(null);
    }
    public UnknownHolder(GNOME.Unknown __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        GNOME.UnknownHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = GNOME.UnknownHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return GNOME.UnknownHelper.type();
    }
}
