package org.Biocorba.Seqcore;

import java.lang.*;

public class _PrimarySeqDB_Tie extends _PrimarySeqDBImplBase {
  public _PrimarySeqDB_Operations servant;
  public _PrimarySeqDB_Tie(_PrimarySeqDB_Operations servant) {
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

  public java.lang.String database_name() {
    return servant.database_name(this);
  }

  public short database_version() {
    return servant.database_version(this);
  }

  public org.Biocorba.Seqcore.PrimarySeqIterator make_PrimarySeqIterator() {
    return servant.make_PrimarySeqIterator(this);
  }

  public org.Biocorba.Seqcore.PrimarySeq get_PrimarySeq(java.lang.String primary_id) throws org.Biocorba.Seqcore.UnableToProcess {
    return servant.get_PrimarySeq(this, primary_id);
  }
}
