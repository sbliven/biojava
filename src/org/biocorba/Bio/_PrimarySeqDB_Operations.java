/*
 * File: SRC/ORG/BIOCORBA/BIO/_PRIMARYSEQDBOPERATIONS.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
/**

 */public interface _PrimarySeqDB_Operations
	extends org.biocorba.GNOME._Unknown_Operations {
     String database_name(org.omg.CORBA.portable.ObjectImpl primarySeqDB)
;
     short database_version(org.omg.CORBA.portable.ObjectImpl primarySeqDB)
;
     org.biocorba.Bio.PrimarySeqStream make_stream(org.omg.CORBA.portable.ObjectImpl primarySeqDB)
;
     org.biocorba.Bio.PrimarySeq get_PrimarySeq(org.omg.CORBA.portable.ObjectImpl primarySeqDB, String primary_id)
        throws org.biocorba.Bio.UnableToProcess;
}
