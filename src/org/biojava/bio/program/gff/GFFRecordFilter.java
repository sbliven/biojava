package org.biojava.bio.program.gff;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * A filter that will accept or reject a GFFEntry.
 */
public interface GFFRecordFilter {
  /**
   * Return wether or not to accept this entry.
   *
   * @param entry the GFFRecord to filter
   * @return true if the record should be accepted or false otherwise
   */
  boolean accept(GFFRecord record);
  
  final static GFFRecordFilter ACCEPT_ALL = new AcceptAll();
  
  public class AcceptAll implements GFFRecordFilter {
    public boolean accept(GFFRecord record) {
      return true;
    }
  }
  
  public class SequenceFilter implements GFFRecordFilter {
    private String seqName;
    
    public String getSeqName() {
      return seqName;
    }
    
    public void setSeqName(String seqName) {
      this.seqName = seqName;
    }
    
    public boolean accept(GFFRecord record) {
      return record.getSeqName().equals(seqName);
    }
  }
  
  public class SourceFilter implements GFFRecordFilter {
    private String source;
    
    public String getSource() {
      return source;
    }
    
    public void setSource(String source) {
      this.source = source;
    }
    
    public boolean accept(GFFRecord record) {
      return record.getSource().equals(source);
    }
  }
  
  public class FeatureFilter implements GFFRecordFilter {
    private String feature;
    
    public void setFeature(String feature) {
      this.feature = feature;
    }
    
    public String getFeature() {
      return feature;
    }
    
    public boolean accept(GFFRecord record) {
      return record.getFeature().equals(feature);
    }
  }
}
