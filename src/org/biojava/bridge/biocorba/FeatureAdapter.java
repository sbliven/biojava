package org.biojava.bridge.biocorba;

import java.util.Iterator;
import java.util.List;
import java.util.Collections;

import org.biocorba.Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

public class FeatureAdapter implements Feature {
  private Sequence seq;
  private SeqFeature seqFeature;
  
  public Sequence getSeq() {
    return seq;
  }
  
  public SeqFeature getSeqFeature() {
    return seqFeature;
  }
  
  public FeatureAdapter(Sequence seq, SeqFeature seqFeature) {
    this.seq = seq;
    this.seqFeature = seqFeature;
  }
  
  public Location getLocation() {
    SeqFeature sf = getSeqFeature();
    return new RangeLocation(sf.start(), sf.end());
  }
  
  public ResidueList getResidues() {
    return getLocation().residues(getSeq());
  }
  
  public String getSource() {
    return getSeqFeature().source();
  }
  
  public String getType() {
    return getSeqFeature().type();
  }
 
  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }
  
  public int countFeatures() {
    return 0;
  }
  
  public Iterator features() {
    return Collections.EMPTY_SET.iterator();
  }
  
  public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
    // this should return an immutable empty feathure holder.
    return new SimpleFeatureHolder();
  }
}
