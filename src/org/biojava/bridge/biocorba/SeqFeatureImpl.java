package org.biojava.bridge.biocorba;

import java.util.*;

import GNOME.*;
import Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

final public class SeqFeatureImpl
extends UnknownImpl
implements _SeqFeatureOperations {
  private SeqImpl parent;
  private Feature feat;
  
  public SeqFeatureImpl(SeqImpl parent, Feature feat) {
    this.parent = parent;
    this.feat = feat;
  }
  
  public String type() {
    return feat.getType();
  }
  
  public String source() {
    return feat.getSource();
  }
  
  public String seq_primary_id() {
    return parent.primary_id();
  }
  
  public int start() {
    return feat.getLocation().getMin();
  }
  
  public int end() {
    return feat.getLocation().getMax();
  }
  
  public short strand() {
    return 0;
  }
  
  public Bio.NameValueSet[] qualifiers() {
    Annotation annotation = feat.getAnnotation();
    Set keys = annotation.keys();
    
    Bio.NameValueSet [] nvs = new NameValueSet[keys.size()];
    
    int i = 0;
    Iterator kIt = keys.iterator();
    while(kIt.hasNext()) {
      Bio.NameValueSet nv = new Bio.NameValueSet();
      Object key = kIt.next();
      nv.name = key.toString();
      String [] val = { annotation.getProperty(key).toString() };
      nv.values = val;
      nvs[i++] = nv;
    }
    
    return nvs;
  }
  
  public boolean has_PrimarySeq() {
    return true;
  }
  
  public Bio.PrimarySeq get_PrimarySeq() {
    _PrimarySeqTie primarySeqTie = new _PrimarySeqTie(parent);
    
    return primarySeqTie;
  }
}
