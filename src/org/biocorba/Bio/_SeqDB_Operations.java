/*
 * File: SRC/ORG/BIOCORBA/BIO/_SEQDBOPERATIONS.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
/**

 */public interface _SeqDB_Operations
	extends org.biocorba.Bio._PrimarySeqDB_Operations {
     org.biocorba.Bio.Seq get_Seq(org.omg.CORBA.portable.ObjectImpl seqDB, String primary_id)
        throws org.biocorba.Bio.UnableToProcess;
}
