package org.Biocorba.Seqcore;

import java.lang.*;

public class _SeqFeature_Tie extends _SeqFeatureImplBase {
  public _SeqFeature_Operations servant;
  public _SeqFeature_Tie(_SeqFeature_Operations servant) {
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

  public java.lang.String type() {
    return servant.type(this);
  }

  public java.lang.String source() {
    return servant.source(this);
  }

  public java.lang.String seq_primary_id() {
    return servant.seq_primary_id(this);
  }

  public int start() {
    return servant.start(this);
  }

  public int end() {
    return servant.end(this);
  }

  public short strand() {
    return servant.strand(this);
  }

  public org.Biocorba.Seqcore.NameValueSet[] qualifiers() {
    return servant.qualifiers(this);
  }

  public boolean PrimarySeq_is_available() {
    return servant.PrimarySeq_is_available(this);
  }

  public org.Biocorba.Seqcore.PrimarySeq get_PrimarySeq() throws org.Biocorba.Seqcore.UnableToProcess {
    return servant.get_PrimarySeq(this);
  }
}
