package org.biojava.bridge.Biocorba.Seqcore;

import java.util.*;

import org.biojava.bio.seq.*;
import org.Biocorba.Seqcore.*;
import org.biojava.bridge.GNOME.*;

public class SeqFeatureIteratorImpl
extends UnknownImpl
implements _SeqFeatureIterator_Operations {
  private Seq parentSeq;
  private Iterator itt;
  
  public Seq getParentSeq() {
    return parentSeq;
  }
  
  public SeqFeatureIteratorImpl(Seq parentSeq, Iterator itt) {
    this.parentSeq = parentSeq;
    this.itt = itt;
  }

  public SeqFeature next(org.omg.CORBA.portable.ObjectImpl seqFeatureIterator)
  throws EndOfStream, UnableToProcess {
    try {
      Feature feat = (Feature) itt.next();
      SeqFeatureImpl sfi = new SeqFeatureImpl(getParentSeq(), feat);
      _SeqFeature_Tie sft = new _SeqFeature_Tie(sfi);
      seqFeatureIterator._orb().connect(sft);
      return sft;
    } catch (NoSuchElementException e) {
      throw new EndOfStream();
    }
  }

  public boolean has_more(org.omg.CORBA.portable.ObjectImpl seqFeatureIterator)  {
    return itt.hasNext();
  }
}

