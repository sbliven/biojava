/*
 * File: SRC/ORG/BIOCORBA/BIO/REQUESTTOOLARGEHOLDER.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public final class RequestTooLargeHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public org.biocorba.Bio.RequestTooLarge value;
    //	constructors 
    public RequestTooLargeHolder() {
	this(null);
    }
    public RequestTooLargeHolder(org.biocorba.Bio.RequestTooLarge __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        org.biocorba.Bio.RequestTooLargeHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = org.biocorba.Bio.RequestTooLargeHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return org.biocorba.Bio.RequestTooLargeHelper.type();
    }
}
