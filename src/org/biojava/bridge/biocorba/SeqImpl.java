package org.biojava.bridge.biocorba;

import java.util.*;

import GNOME.*;
import Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

final public class SeqImpl
extends PrimarySeqImpl
implements _SeqOperations {
  public SeqImpl(Sequence seq) {
    super(seq);
  }
  
  public Bio.SeqFeature [] all_features() {
    FeatureHolder fh = getSequence().filter(FeatureFilter.all, true);
    _SeqFeatureTie [] featureA = new _SeqFeatureTie[fh.countFeatures()];

    int i = 0;
    for(Iterator it = fh.features(); it.hasNext(); i++) {
      Feature f = (Feature) it.next();
      SeqFeatureImpl seqFeatureImpl = new SeqFeatureImpl(this, f);
      _SeqFeatureTie seqFeatureTie = new _SeqFeatureTie(seqFeatureImpl);

      featureA[i] = seqFeatureTie;
    }
    
    return featureA;
  }
  
  public Bio.SeqFeatureIterator all_features_iterator() {
    FeatureHolder fh = getSequence().filter(FeatureFilter.all, true);
    Iterator i = fh.features();
    SeqFeatureIteratorImpl seqFeatureIteratorImpl = new SeqFeatureIteratorImpl(this, i);
    _SeqFeatureIteratorTie seqFeatureIteratorTie = new _SeqFeatureIteratorTie(seqFeatureIteratorImpl);
    
    return seqFeatureIteratorTie;
  }
  
  public Bio.SeqFeature [] features_region(int start, int end) {
    FeatureHolder fh = getSequence().filter(
      new FeatureFilter.OverlapsLocation(new RangeLocation(start, end)),
      true);
    _SeqFeatureTie [] featureA = new _SeqFeatureTie[fh.countFeatures()];

    int i = 0;
    for(Iterator it = fh.features(); it.hasNext(); i++) {
      Feature f = (Feature) it.next();
      SeqFeatureImpl seqFeatureImpl = new SeqFeatureImpl(this, f);
      _SeqFeatureTie seqFeatureTie = new _SeqFeatureTie(seqFeatureImpl);

      featureA[i] = seqFeatureTie;
    }
    
    return featureA;
  }
  
  public Bio.SeqFeatureIterator features_region_iterator(int start, int end) {
    FeatureHolder fh = getSequence().filter(
      new FeatureFilter.OverlapsLocation(new RangeLocation(start, end)),
      true);
    Iterator i = fh.features();
    SeqFeatureIteratorImpl seqFeatureIteratorImpl = new SeqFeatureIteratorImpl(this, i);
    _SeqFeatureIteratorTie seqFeatureIteratorTie = new _SeqFeatureIteratorTie(seqFeatureIteratorImpl);
    
    return seqFeatureIteratorTie;
  }
  
  public Bio.PrimarySeq get_PrimarySeq() {
    _PrimarySeqTie primarySeqTie = new _PrimarySeqTie(this);
    
    return primarySeqTie;
  }
  
  public int max_feature_request() {
    return Integer.MAX_VALUE;
  }
}
