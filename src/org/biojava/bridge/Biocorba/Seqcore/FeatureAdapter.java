package org.biojava.bridge.Biocorba.Seqcore;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
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
      try {
        ann.setProperty(qualifiers[i].name, qualifiers[i].values);
      } catch (ChangeVetoException cve) {
        throw new BioError(
          cve,
          "Assertion failure: Couldn't modify my annotations"
        );
      }
    }
    this.annotation = ann;
    this.location = new RangeLocation(seqFeature.start(), seqFeature.end());
  }
  
  public Feature.Template makeTemplate() {
    Feature.Template ft = new Feature.Template();
    ft.location = getLocation();
    ft.source = getSource();
    ft.type = getType();
    ft.annotation = getAnnotation();
    return ft;
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
  
    public FeatureHolder getParent() {
	return parent;
    }

    public Sequence getSequence() {
	return parent;
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

  public Feature createFeature(Feature.Template template)
  throws ChangeVetoException {
    throw new ChangeVetoException("FeatureAdapter is immutable");
  }

  public void removeFeature(Feature f)
  throws ChangeVetoException {
    throw new ChangeVetoException("FeatureAdapter is immutable");
  }
  
  public void addChangeListener(ChangeListener cl) {}
  public void addChangeListener(ChangeListener cl, ChangeType ct) {}
  public void removeChangeListener(ChangeListener cl) {}
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
}
