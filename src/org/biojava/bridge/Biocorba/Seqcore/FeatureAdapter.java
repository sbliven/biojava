package org.biojava.bridge.Biocorba.Seqcore;

import java.util.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;
import org.Biocorba.Seqcore.*;

public class FeatureAdapter implements Feature {
  private SequenceAdapter parent;  
  private SeqFeature seqFeature;
  private Annotation annotation;
  private Location location;
  
  public FeatureAdapter(SequenceAdapter parent, SeqFeature seqFeature) {
    this.parent = parent;
    this.seqFeature = seqFeature;

    SimpleAnnotation ann = new SimpleAnnotation();
    NameValueSet[] qualifiers = seqFeature.qualifiers();
    for(int i = 0; i < qualifiers.length; i++) {
      ann.setProperty(qualifiers[i].name, qualifiers[i].values);
    }
    this.annotation = ann;
    this.location = new RangeLocation(seqFeature.start(), seqFeature.end());
  }
  
  public Location getLocation() {
    return location;
  }
  
  public SymbolList getSymbols() {
    return getLocation().symbols(parent);
  }
  
  public String getSource() {
    return seqFeature.source();
  }
  
  public String getType() {
    return seqFeature.type();
  }
  
  public int countFeatures() {
    return FeatureHolder.EMPTY_FEATURE_HOLDER.countFeatures();
  }
  
  public Iterator features() {
    return FeatureHolder.EMPTY_FEATURE_HOLDER.features();
  }
  
  public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
    return FeatureHolder.EMPTY_FEATURE_HOLDER.filter(fc, recurse);
  }
  
  public Annotation getAnnotation() {
    return annotation;
  }
}
