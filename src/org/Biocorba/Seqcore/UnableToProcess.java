/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/UNABLETOPROCESS.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
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
