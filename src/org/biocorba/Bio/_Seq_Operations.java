/*
 * File: SRC/ORG/BIOCORBA/BIO/_SEQOPERATIONS.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.biocorba.Bio;
/**

 */public interface _Seq_Operations
	extends org.biocorba.Bio._PrimarySeq_Operations {
     org.biocorba.Bio.SeqFeature[] all_features(org.omg.CORBA.portable.ObjectImpl seq)
        throws org.biocorba.Bio.RequestTooLarge;
     org.biocorba.Bio.SeqFeatureIterator all_features_iterator(org.omg.CORBA.portable.ObjectImpl seq)
;
     org.biocorba.Bio.SeqFeature[] features_region(org.omg.CORBA.portable.ObjectImpl seq, int start, int end)
        throws org.biocorba.Bio.OutOfRange, org.biocorba.Bio.UnableToProcess, org.biocorba.Bio.RequestTooLarge;
     org.biocorba.Bio.SeqFeatureIterator features_region_iterator(org.omg.CORBA.portable.ObjectImpl seq, int start, int end)
        throws org.biocorba.Bio.OutOfRange, org.biocorba.Bio.UnableToProcess;
     int max_feature_request(org.omg.CORBA.portable.ObjectImpl seq)
;
     org.biocorba.Bio.PrimarySeq get_PrimarySeq(org.omg.CORBA.portable.ObjectImpl seq)
;
}
