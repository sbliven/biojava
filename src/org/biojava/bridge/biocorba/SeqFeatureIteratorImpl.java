package org.biojava.bridge.biocorba;

import java.util.*;

import GNOME.*;
import Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

final public class SeqFeatureIteratorImpl
extends UnknownImpl
implements _SeqFeatureIteratorOperations {
  private SeqImpl parent;
  private Iterator it;
  
  public SeqFeatureIteratorImpl(SeqImpl parent, Iterator it) {
    this.parent = parent;
    this.it = it;
  }
  
  public Bio.SeqFeature next() {
    Feature feat = (Feature) it.next();
    SeqFeatureImpl seqFeatureImpl = new SeqFeatureImpl(parent, feat);
    _SeqFeatureTie seqFeatureTie = new _SeqFeatureTie(seqFeatureImpl);
    
    return seqFeatureTie;
  }
  
  public boolean has_more() {
    return it.hasNext();
  }
}
