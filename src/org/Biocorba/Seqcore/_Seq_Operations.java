package org.Biocorba.Seqcore;

import java.lang.*;

public interface _Seq_Operations extends org.Biocorba.Seqcore._PrimarySeq_Operations {

  public org.Biocorba.Seqcore.SeqFeature[] all_features(org.omg.CORBA.portable.ObjectImpl seq) throws org.Biocorba.Seqcore.RequestTooLarge;

  public org.Biocorba.Seqcore.SeqFeatureIterator all_features_iterator(org.omg.CORBA.portable.ObjectImpl seq);

  public org.Biocorba.Seqcore.SeqFeature[] features_region(org.omg.CORBA.portable.ObjectImpl seq, int start, int end) throws org.Biocorba.Seqcore.OutOfRange, org.Biocorba.Seqcore.UnableToProcess, org.Biocorba.Seqcore.RequestTooLarge;

  public org.Biocorba.Seqcore.SeqFeatureIterator features_region_iterator(org.omg.CORBA.portable.ObjectImpl seq, int start, int end) throws org.Biocorba.Seqcore.OutOfRange, org.Biocorba.Seqcore.UnableToProcess;

  public int max_feature_request(org.omg.CORBA.portable.ObjectImpl seq);

  public org.Biocorba.Seqcore.PrimarySeq get_PrimarySeq(org.omg.CORBA.portable.ObjectImpl seq);
}
