package org.biojava.bio.program.gff;

public class SimpleGFFRecord implements GFFRecord {
  private String seqName;
  private String source;
  private String feature;
  private int start;
  private int end;
  private double score;
  private int strand;
  private int frame;
  private String groupAttributes;
  private String comment;
  
  public void setSeqName(String seqName) {
    this.seqName = seqName;
  }
  
  public String getSeqName() {
    return seqName;
  }
  
  public void setSource(String source) {
    this.source = source;
  }
  
  public String getSource() {
    return source;
  }
  
  public void setFeature(String feature) {
    this.feature = feature;
  }
  
  public String getFeature() {
    return feature;
  }
  
  public void setStart(int start) {
    this.start = start;
  }
  
  public int getStart() {
    return start;
  }
  
  public void setEnd(int end) {
    this.end = end;
  }
  
  public int getEnd() {
    return end;
  }
  
  public void setScore(double score) {
    this.score = score;
  }
  
  public double getScore() {
    return score;
  }
  
  public void setStrand(int strand) {
    this.strand = strand;
  }
  
  public int getStrand() {
    return strand;
  }
  
  public void setFrame(int frame) {
    this.frame = frame;
  }
  
  public int getFrame() {
    return frame;
  }
  
  public void setGroupAttributes(String ga) {
    this.groupAttributes = ga;
  }
  
  public String getGroupAttributes() {
    return groupAttributes;
  }
  
  public void setComment(String comment) {
    this.comment = comment;
  }
  
  public String getComment() {
    return comment;
  }
}

