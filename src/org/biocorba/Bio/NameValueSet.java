/*
 * File: SRC/ORG/BIOCORBA/BIO/NAMEVALUESET.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public final class NameValueSet implements org.omg.CORBA.portable.IDLEntity {
    //	instance variables
    public String name;
    public String[] values;
    //	constructors
    public NameValueSet() { }
    public NameValueSet(String __name, String[] __values) {
	name = __name;
	values = __values;
    }
}
