/*
 * File: SRC/ORG/BIOCORBA/BIO/_SEQDBOPERATIONS.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
/**

 */public interface _SeqDBOperations
	extends org.biocorba.Bio._PrimarySeqDBOperations {
     org.biocorba.Bio.Seq get_Seq(String primary_id)
        throws org.biocorba.Bio.UnableToProcess;
}
