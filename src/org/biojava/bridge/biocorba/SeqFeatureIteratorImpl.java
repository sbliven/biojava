package org.biojava.bridge.biocorba;

import java.util.*;

import org.biocorba.Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

final public class SeqFeatureIteratorImpl
extends UnknownImpl
implements _SeqFeatureIterator_Operations {
  private Seq parent;
  private Iterator it;
  
  public SeqFeatureIteratorImpl(Seq parent, Iterator it) {
    this.parent = parent;
    this.it = it;
  }
  
  public SeqFeature next(org.omg.CORBA.portable.ObjectImpl seqFeatureIterator) {
    Feature feat = (Feature) it.next();
    SeqFeatureImpl seqFeatureImpl = new SeqFeatureImpl(parent, feat);
    _SeqFeature_Tie seqFeatureTie = new _SeqFeature_Tie(seqFeatureImpl);
    seqFeatureIterator._orb().connect(seqFeatureTie);
    return seqFeatureTie;
  }
  
  public boolean has_more(org.omg.CORBA.portable.ObjectImpl seqFeatureIterator) {
    return it.hasNext();
  }
}
