/*
 * File: SRC/BIO/SEQ.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public interface Seq
    extends org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity,
	    Bio.PrimarySeq {
    Bio.SeqFeature[] all_features()
        throws Bio.RequestTooLarge;
    Bio.SeqFeatureIterator all_features_iterator()
;
    Bio.SeqFeature[] features_region(int start, int end)
        throws Bio.OutOfRange, Bio.UnableToProcess, Bio.RequestTooLarge;
    Bio.SeqFeatureIterator features_region_iterator(int start, int end)
        throws Bio.OutOfRange, Bio.UnableToProcess;
    int max_feature_request()
;
    Bio.PrimarySeq get_PrimarySeq()
;
}
