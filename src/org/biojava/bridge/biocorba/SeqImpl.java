package org.biojava.bridge.biocorba;

import java.util.*;

import org.biocorba.Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

final public class SeqImpl
extends PrimarySeqImpl
implements _Seq_Operations {
  public SeqImpl(Sequence seq) {
    super(seq);
  }

  public Sequence getSequence() {
    return (Sequence) super.getResidueList();
  }
  
  public SeqFeature [] all_features(org.omg.CORBA.portable.ObjectImpl seq) {
    FeatureHolder fh = getSequence().filter(FeatureFilter.all, true);
    _SeqFeature_Tie [] featureA = new _SeqFeature_Tie[fh.countFeatures()];

    int i = 0;
    for(Iterator it = fh.features(); it.hasNext(); i++) {
      Feature f = (Feature) it.next();
      SeqFeatureImpl seqFeatureImpl = new SeqFeatureImpl((Seq) seq, f);
      _SeqFeature_Tie seqFeatureTie = new _SeqFeature_Tie(seqFeatureImpl);
      seq._orb().connect(seqFeatureTie);
      featureA[i] = seqFeatureTie;
    }
    
    return featureA;
  }
  
  public SeqFeatureIterator all_features_iterator(org.omg.CORBA.portable.ObjectImpl seq) {
    FeatureHolder fh = getSequence().filter(FeatureFilter.all, true);
    Iterator i = fh.features();
    SeqFeatureIteratorImpl seqFeatureIteratorImpl = new SeqFeatureIteratorImpl((Seq) seq, i);
    _SeqFeatureIterator_Tie seqFeatureIteratorTie = new _SeqFeatureIterator_Tie(seqFeatureIteratorImpl);
    seq._orb().connect(seqFeatureIteratorTie);
    return seqFeatureIteratorTie;
  }
  
  public SeqFeature [] features_region(org.omg.CORBA.portable.ObjectImpl seq, int start, int end) {
    FeatureHolder fh = getSequence().filter(
      new FeatureFilter.OverlapsLocation(new RangeLocation(start, end)),
      true);
    _SeqFeature_Tie [] featureA = new _SeqFeature_Tie[fh.countFeatures()];

    int i = 0;
    for(Iterator it = fh.features(); it.hasNext(); i++) {
      Feature f = (Feature) it.next();
      SeqFeatureImpl seqFeatureImpl = new SeqFeatureImpl((Seq) seq, f);
      _SeqFeature_Tie seqFeatureTie = new _SeqFeature_Tie(seqFeatureImpl);
      seq._orb().connect(seqFeatureTie);
      featureA[i] = seqFeatureTie;
    }
    
    return featureA;
  }
  
  public SeqFeatureIterator features_region_iterator(org.omg.CORBA.portable.ObjectImpl seq, int start, int end) {
    FeatureHolder fh = getSequence().filter(
      new FeatureFilter.OverlapsLocation(new RangeLocation(start, end)),
      true);
    Iterator i = fh.features();
    SeqFeatureIteratorImpl seqFeatureIteratorImpl = new SeqFeatureIteratorImpl((Seq) seq, i);
    _SeqFeatureIterator_Tie seqFeatureIteratorTie = new _SeqFeatureIterator_Tie(seqFeatureIteratorImpl);
    seq._orb().connect(seqFeatureIteratorTie);
    return seqFeatureIteratorTie;
  }
  
  public PrimarySeq get_PrimarySeq(org.omg.CORBA.portable.ObjectImpl seq) {
    PrimarySeqImpl primarySeqImpl = new PrimarySeqImpl(getResidueList());
    _PrimarySeq_Tie primarySeqTie = new _PrimarySeq_Tie(primarySeqImpl);
    seq._orb().connect(primarySeqTie);
    return primarySeqTie;
  }
  
  public int max_feature_request(org.omg.CORBA.portable.ObjectImpl seq) {
    return Integer.MAX_VALUE;
  }
}
