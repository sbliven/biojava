/*
 * File: SRC/ORG/BIOCORBA/BIO/NAMEVALUESETLISTHOLDER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public final class NameValueSetListHolder
    implements org.omg.CORBA.portable.Streamable
{
    //	instance variable 
    public org.biocorba.Bio.NameValueSet[] value;
    //	constructors 
    public NameValueSetListHolder() {
	this(null);
    }
    public NameValueSetListHolder(org.biocorba.Bio.NameValueSet[] __arg) {
	value = __arg;
    }
    public void _write(org.omg.CORBA.portable.OutputStream out) {
        org.biocorba.Bio.NameValueSetListHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = org.biocorba.Bio.NameValueSetListHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.biocorba.Bio.NameValueSetListHelper.type();
    }
}
