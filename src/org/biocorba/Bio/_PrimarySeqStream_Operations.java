/*
 * File: SRC/ORG/BIOCORBA/BIO/_PRIMARYSEQSTREAMOPERATIONS.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
/**

 */public interface _PrimarySeqStream_Operations
	extends org.biocorba.GNOME._Unknown_Operations {
     org.biocorba.Bio.PrimarySeq next(org.omg.CORBA.portable.ObjectImpl primarySeqStream)
        throws org.biocorba.Bio.EndOfStream, org.biocorba.Bio.UnableToProcess;
     boolean has_more(org.omg.CORBA.portable.ObjectImpl primarySeqStream)
;
}
