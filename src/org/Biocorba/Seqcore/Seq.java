/*
 * File: ..\SRC/ORG/BIOCORBA/SEQCORE/SEQ.JAVA
 * From: BIOCORBA.11-02-2000.IDL
 * Date: Fri Feb 11 14:53:21 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package org.Biocorba.Seqcore;
public interface Seq
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity,
	    org.Biocorba.Seqcore.PrimarySeq {
    org.Biocorba.Seqcore.SeqFeature[] all_features()
        throws org.Biocorba.Seqcore.RequestTooLarge;
    org.Biocorba.Seqcore.SeqFeatureIterator all_features_iterator()
;
    org.Biocorba.Seqcore.SeqFeature[] features_region(int start, int end)
        throws org.Biocorba.Seqcore.OutOfRange, org.Biocorba.Seqcore.UnableToProcess, org.Biocorba.Seqcore.RequestTooLarge;
    org.Biocorba.Seqcore.SeqFeatureIterator features_region_iterator(int start, int end)
        throws org.Biocorba.Seqcore.OutOfRange, org.Biocorba.Seqcore.UnableToProcess;
    int max_feature_request()
;
    org.Biocorba.Seqcore.PrimarySeq get_PrimarySeq()
;
}
