/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/SEQDB.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public interface SeqDB
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity,
	    org.Biocorba.Seqcore.PrimarySeqDB {
    org.Biocorba.Seqcore.Seq get_Seq(String primary_id)
        throws org.Biocorba.Seqcore.UnableToProcess;
    String[] get_primaryidList()
;
}
