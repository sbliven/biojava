package org.Biocorba.Seqcore;

import java.lang.*;

public class _SeqFeatureIterator_Tie extends _SeqFeatureIteratorImplBase {
  public _SeqFeatureIterator_Operations servant;
  public _SeqFeatureIterator_Tie(_SeqFeatureIterator_Operations servant) {
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

  public org.Biocorba.Seqcore.SeqFeature next() throws org.Biocorba.Seqcore.EndOfStream, org.Biocorba.Seqcore.UnableToProcess {
    return servant.next(this);
  }

  public boolean has_more() {
    return servant.has_more(this);
  }
}
