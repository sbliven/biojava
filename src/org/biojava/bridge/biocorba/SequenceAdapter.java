package org.biojava.bridge.biocorba;

import java.util.Iterator;
import java.util.List;

import org.biocorba.Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

public class SequenceAdapter extends ResidueListAdapter implements Sequence {
  public Seq getSeq() {
    return (Seq) getPrimarySeq();
  }
  
  public SequenceAdapter(Seq seq)
  throws IllegalAlphabetException, IllegalResidueException, SeqException {
    super(seq);
  }
  
  public Feature createFeature(MutableFeatureHolder fh, Location loc,
                               String type, String source,
                               Annotation annotation)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException("createFeature not supported by " +
                                            getClass().toString());
  }
  
  public String getName() {
    return getSeq().display_id();
  }
  
  public String getURN() {
    return "urn:sequence/" + getSeq().accession_number();
  }
  
  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }
  
  public int countFeatures() {
    return -1;
  }
  
  public Iterator features() {
    final SeqFeatureIterator sfi = getSeq().all_features_iterator();
    return new Iterator() {
      public boolean hasNext() {
        return sfi.has_more();
      }
      public Object next() {
        SeqFeature sf = sfi.next();
        return new FeatureAdapter(SequenceAdapter.this, sf);
      }
      public void remove()
      throws UnsupportedOperationException {
        throw new UnsupportedOperationException("remove not supported");
      }
    };
  }
  
  public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
    // this should return an immutable empty feathure holder.
    return new SimpleFeatureHolder();
  }
}
