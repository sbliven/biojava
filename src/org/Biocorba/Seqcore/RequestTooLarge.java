/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/REQUESTTOOLARGE.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public final class RequestTooLarge
	extends org.omg.CORBA.UserException implements org.omg.CORBA.portable.IDLEntity {
    //	instance variables
    public String reason;
    public int suggested_size;
    //	constructors
    public RequestTooLarge() {
	super();
    }
    public RequestTooLarge(String __reason, int __suggested_size) {
	super();
	reason = __reason;
	suggested_size = __suggested_size;
    }
}
