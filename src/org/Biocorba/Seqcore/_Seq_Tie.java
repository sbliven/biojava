package org.Biocorba.Seqcore;

import java.lang.*;

public class _Seq_Tie extends _SeqImplBase {
  public _Seq_Operations servant;
  public _Seq_Tie(_Seq_Operations servant) {
    this.servant = servant;
  }

  public void ref() {
    servant.ref(this);
  }

  public void unref() {
    servant.unref(this);
  }

  public org.omg.CORBA.Object query_interface(java.lang.String repoid) {
    return servant.query_interface(this, repoid);
  }

  public org.Biocorba.Seqcore.SeqType type() {
    return servant.type(this);
  }

  public int length() {
    return servant.length(this);
  }

  public java.lang.String get_seq() throws org.Biocorba.Seqcore.RequestTooLarge {
    return servant.get_seq(this);
  }

  public java.lang.String get_subseq(int start, int end) throws org.Biocorba.Seqcore.OutOfRange, org.Biocorba.Seqcore.RequestTooLarge {
    return servant.get_subseq(this, start, end);
  }

  public int max_request_length() {
    return servant.max_request_length(this);
  }

  public java.lang.String display_id() {
    return servant.display_id(this);
  }

  public java.lang.String primary_id() {
    return servant.primary_id(this);
  }

  public java.lang.String accession_number() {
    return servant.accession_number(this);
  }

  public int version() {
    return servant.version(this);
  }

  public org.Biocorba.Seqcore.SeqFeature[] all_features() throws org.Biocorba.Seqcore.RequestTooLarge {
    return servant.all_features(this);
  }

  public org.Biocorba.Seqcore.SeqFeatureIterator all_features_iterator() {
    return servant.all_features_iterator(this);
  }

  public org.Biocorba.Seqcore.SeqFeature[] features_region(int start, int end) throws org.Biocorba.Seqcore.OutOfRange, org.Biocorba.Seqcore.UnableToProcess, org.Biocorba.Seqcore.RequestTooLarge {
    return servant.features_region(this, start, end);
  }

  public org.Biocorba.Seqcore.SeqFeatureIterator features_region_iterator(int start, int end) throws org.Biocorba.Seqcore.OutOfRange, org.Biocorba.Seqcore.UnableToProcess {
    return servant.features_region_iterator(this, start, end);
  }

  public int max_feature_request() {
    return servant.max_feature_request(this);
  }

  public org.Biocorba.Seqcore.PrimarySeq get_PrimarySeq() {
    return servant.get_PrimarySeq(this);
  }
}
