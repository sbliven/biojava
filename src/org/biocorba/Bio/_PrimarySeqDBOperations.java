/*
 * File: SRC/ORG/BIOCORBA/BIO/_PRIMARYSEQDBOPERATIONS.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
/**

 */public interface _PrimarySeqDBOperations
	extends org.biocorba.GNOME._UnknownOperations {
     String database_name()
;
     short database_version()
;
     org.biocorba.Bio.PrimarySeqStream make_stream()
;
     org.biocorba.Bio.PrimarySeq get_PrimarySeq(String primary_id)
        throws org.biocorba.Bio.UnableToProcess;
}
