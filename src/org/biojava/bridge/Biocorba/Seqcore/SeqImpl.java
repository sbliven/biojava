package org.biojava.bridge.Biocorba.Seqcore;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

import org.Biocorba.Seqcore.*;

public class SeqImpl
extends PrimarySeqImpl
implements _Seq_Operations {
  public Sequence getSequence() {
    return (Sequence) getSymbolList();
  }
  
  public SeqImpl(Sequence sequence)
  throws IllegalAlphabetException {
    super(sequence, sequence.getName(), sequence.toString(), sequence.getURN());
  }
  
  public SeqImpl(Sequence sequence, String id)
  throws IllegalAlphabetException {
    super(sequence, id);
  }

  public SeqImpl(Sequence sequence,
                 String displayID, String primaryID, String accessionNumber)
  throws IllegalAlphabetException {
    super(sequence, displayID, primaryID, accessionNumber);
  }

  public SeqFeature[] all_features(org.omg.CORBA.portable.ObjectImpl seq)
  throws RequestTooLarge {
    return iteratorToArray(
      seq,
      filterToIterator(FeatureFilter.all)
    );
  }

  public SeqFeatureIterator all_features_iterator(org.omg.CORBA.portable.ObjectImpl seq) {
    Iterator itt = filterToIterator(FeatureFilter.all);
    SeqFeatureIteratorImpl fii = new SeqFeatureIteratorImpl((Seq) seq, itt);
    _SeqFeatureIterator_Tie fit = new _SeqFeatureIterator_Tie(fii);
    seq._orb().connect(fit);
    return fit;    
  }

  public SeqFeature[] features_region(org.omg.CORBA.portable.ObjectImpl seq, int start, int end)
  throws OutOfRange, UnableToProcess, RequestTooLarge {
    return iteratorToArray(
      seq,
      filterToIterator(
        new FeatureFilter.OverlapsLocation(
          new RangeLocation(start, end)
        )
      )
    );
  }

  public SeqFeatureIterator features_region_iterator(org.omg.CORBA.portable.ObjectImpl seq, int start, int end)
  throws OutOfRange, UnableToProcess {
    Iterator itt = filterToIterator(
      new FeatureFilter.OverlapsLocation(
        new RangeLocation(start, end)
      )
    );
    SeqFeatureIteratorImpl fii = new SeqFeatureIteratorImpl((Seq) seq, itt);
    _SeqFeatureIterator_Tie fit = new _SeqFeatureIterator_Tie(fii);
    seq._orb().connect(fit);
    return fit;
  }

  public int max_feature_request(org.omg.CORBA.portable.ObjectImpl seq) {
    return Integer.MAX_VALUE;
  }

  public PrimarySeq get_PrimarySeq(org.omg.CORBA.portable.ObjectImpl seq) {
    return (PrimarySeq) seq;
  }

  private Iterator filterToIterator(FeatureFilter fFilter) {
    Sequence sequence = getSequence();
    FeatureHolder fh = sequence.filter(fFilter, true);
    return fh.features();
  }

  private SeqFeature [] iteratorToArray(org.omg.CORBA.portable.ObjectImpl seq, Iterator fit) {
    List featureList = new ArrayList();
    for(int i = 0; fit.hasNext(); i++) {
      SeqFeatureImpl sfi = new SeqFeatureImpl((Seq) seq, (Feature) fit.next());
      _SeqFeature_Tie sft = new _SeqFeature_Tie(sfi);
      seq._orb().connect(sft);
      featureList.add(sft);
    }
    return (SeqFeature []) featureList.toArray(new SeqFeature[0]);
  }
}

