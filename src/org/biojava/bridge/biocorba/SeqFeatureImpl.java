package org.biojava.bridge.biocorba;

import java.util.*;

import org.biocorba.Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

final public class SeqFeatureImpl
extends UnknownImpl
implements _SeqFeature_Operations {
  private Seq parent;
  private Feature feat;
  
  public SeqFeatureImpl(Seq parent, Feature feat) {
    this.parent = parent;
    this.feat = feat;
  }
  
  public String type(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    return feat.getType();
  }
  
  public String source(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    return feat.getSource();
  }
  
  public String seq_primary_id(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    return parent.primary_id();
  }
  
  public int start(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    return feat.getLocation().getMin();
  }
  
  public int end(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    return feat.getLocation().getMax();
  }
  
  public short strand(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    return 0;
  }
  
  public NameValueSet[] qualifiers(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    Annotation annotation = feat.getAnnotation();
    Set keys = annotation.keys();
    
    NameValueSet [] nvs = new NameValueSet[keys.size()];
    
    int i = 0;
    Iterator kIt = keys.iterator();
    while(kIt.hasNext()) {
      NameValueSet nv = new NameValueSet();
      Object key = kIt.next();
      nv.name = key.toString();
      String [] val = { annotation.getProperty(key).toString() };
      nv.values = val;
      // seqFeature._orb().connect(nv); // do I need to register this?
      nvs[i++] = nv;
    }
    
    return nvs;
  }
  
  public boolean has_PrimarySeq(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    return true;
  }
  
  public PrimarySeq get_PrimarySeq(org.omg.CORBA.portable.ObjectImpl seqFeature) {
    
    return parent;
  }
}
