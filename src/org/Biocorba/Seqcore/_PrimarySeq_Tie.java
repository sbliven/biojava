package org.Biocorba.Seqcore;

import java.lang.*;

public class _PrimarySeq_Tie extends _PrimarySeqImplBase {
  public _PrimarySeq_Operations servant;
  public _PrimarySeq_Tie(_PrimarySeq_Operations servant) {
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
}
