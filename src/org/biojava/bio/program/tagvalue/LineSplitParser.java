package org.biojava.bio.program.tagvalue;

import java.io.*;
import java.util.*;

import org.biojava.utils.ParserException;

public class LineSplitParser
  implements
    TagValueParser
{
  public static final LineSplitParser EMBL;
  public static final LineSplitParser GENBANK;
  
  static {
    EMBL = new LineSplitParser();
    EMBL.setEndOfRecord("//");
    EMBL.setSplitOffset(5);
    EMBL.setTrimTag(true);
    EMBL.setTrimValue(false);
    EMBL.setContinueOnEmptyTag(false);
    EMBL.setMergeSameTag(true);
    
    GENBANK = new LineSplitParser();
    GENBANK.setEndOfRecord("///");
    GENBANK.setSplitOffset(12);
    GENBANK.setTrimTag(true);
    GENBANK.setTrimValue(false);
    GENBANK.setContinueOnEmptyTag(true);
    GENBANK.setMergeSameTag(false);
  }
  
  private String endOfRecord = null;
  
  private int splitOffset;
  
  private boolean trimTag;
  
  private boolean trimValue;
  
  private boolean continueOnEmptyTag;
  
  private boolean mergeSameTag;
  
  private String tag;
  
  public LineSplitParser() {}
  
  public void setEndOfRecord(String endOfRecord) {
    this.endOfRecord = endOfRecord;
  }
  
  public String getEndOfRecord() {
    return endOfRecord;
  }
  
  public void setSplitOffset(int splitOffset) {
    this.splitOffset = splitOffset;
  }
  
  public int getSplitOffset() {
    return splitOffset;
  }
  
  public void setTrimTag(boolean trimTag) {
    this.trimTag = trimTag;
  }
  
  public boolean getTrimTag() {
    return trimTag;
  }
  public void setTrimValue(boolean trimValue) {
    this.trimValue = trimValue;
  }
  
  public boolean getTrimValue() {
    return trimValue;
  }
  
  public void setContinueOnEmptyTag(boolean continueOnEmptyTag) {
    this.continueOnEmptyTag = continueOnEmptyTag;
  }
  
  public boolean getContinueOnEmptyTag() {
    return continueOnEmptyTag;
  }
  
  public void setMergeSameTag(boolean mergeSameTag) {
    this.mergeSameTag = mergeSameTag;
  }
  
  public boolean getMergeSameTag() {
    return mergeSameTag;
  }
  
  public TagValue parse(Object o) {
    String line = o.toString();
    
    if(line.startsWith(endOfRecord)) {
      return null;
    }
    
    String tag = line.substring(0, splitOffset);
    if(trimTag) {
      tag = tag.trim();
    }
    
    String value = line.substring(splitOffset);
    if(trimValue) {
      value = value.trim();
    }
    
    if(continueOnEmptyTag && (tag.length() == 0)) {
      return new TagValue(this.tag, value, false);
    } else if(mergeSameTag && tag.equals(this.tag)) {
      return new TagValue(tag, value, false);
    } else {
      return new TagValue(this.tag = tag, value, true);
    }
  }
}
