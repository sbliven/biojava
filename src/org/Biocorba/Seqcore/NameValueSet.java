/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/NAMEVALUESET.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
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
