package org.biojava.bridge.Biocorba.Seqcore;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

import org.Biocorba.Seqcore.*;
import org.biojava.bridge.GNOME.*;

public class SeqFeatureImpl
extends UnknownImpl
implements _SeqFeature_Operations {
  private Seq parentSeq;
  private Feature feature;
  
  public Feature getFeature() {
    return feature;
  }
  
  public SeqFeatureImpl(Seq parentSeq, Feature feature) {
    this.parentSeq = parentSeq;
    this.feature = feature;
  }
  
  public String type(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    return getFeature().getType();
  }

  public String source(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    return getFeature().getSource();
  }

  public String seq_primary_id(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    return parentSeq.primary_id();
  }

  public int start(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    return getFeature().getLocation().getMin();
  }

  public int end(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    return getFeature().getLocation().getMax();
  }

  public short strand(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    return 0;
  }

  public NameValueSet[] qualifiers(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    Annotation annotation = getFeature().getAnnotation();
    Set keys = annotation.keys();
    NameValueSet [] nameValueSetA = new NameValueSet[keys.size()];
    Iterator keyI = keys.iterator();
    for(int i = 0; keyI.hasNext(); i++) {
      Object key = keyI.next();
      Object value = annotation.getProperty(key);
      String [] values = { value.toString() };
      nameValueSetA[i] = new NameValueSet(key.toString(), values);
    }
    return nameValueSetA;
  }

  public boolean PrimarySeq_is_available(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    return true;
  }

  public PrimarySeq get_PrimarySeq(org.omg.CORBA.portable.ObjectImpl seqFeature)
  throws UnableToProcess {
    return parentSeq;
  }
}

