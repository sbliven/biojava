/*
 * File: SRC/ORG/BIOCORBA/BIO/_PRIMARYSEQOPERATIONS.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
/**

 */public interface _PrimarySeq_Operations
	extends org.biocorba.GNOME._Unknown_Operations {
     org.biocorba.Bio.SeqType type(org.omg.CORBA.portable.ObjectImpl seq)
;
     int length(org.omg.CORBA.portable.ObjectImpl seq)
;
     String get_seq(org.omg.CORBA.portable.ObjectImpl seq)
        throws org.biocorba.Bio.RequestTooLarge;
     String get_subseq(org.omg.CORBA.portable.ObjectImpl seq, int start, int end)
        throws org.biocorba.Bio.OutOfRange, org.biocorba.Bio.RequestTooLarge;
     String display_id(org.omg.CORBA.portable.ObjectImpl seq)
;
     String primary_id(org.omg.CORBA.portable.ObjectImpl seq)
;
     String accession_number(org.omg.CORBA.portable.ObjectImpl seq)
;
     int version(org.omg.CORBA.portable.ObjectImpl seq)
;
     int max_request_length(org.omg.CORBA.portable.ObjectImpl seq)
;
}
