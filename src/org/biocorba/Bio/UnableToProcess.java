/*
 * File: SRC/ORG/BIOCORBA/BIO/UNABLETOPROCESS.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
public final class UnableToProcess
	extends org.omg.CORBA.UserException implements org.omg.CORBA.portable.IDLEntity {
    //	instance variables
    public String reason;
    //	constructors
    public UnableToProcess() {
	super();
    }
    public UnableToProcess(String __reason) {
	super();
	reason = __reason;
    }
}
