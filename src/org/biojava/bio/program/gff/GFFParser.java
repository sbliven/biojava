package org.biojava.bio.program.gff;

import java.util.*;
import java.io.*;

import org.biojava.bio.*;

public class GFFParser {
  public void parse(BufferedReader bReader, GFFDocumentHandler handler)
  throws IOException, BioException {
    handler.startDocument();
    ArrayList aList = new ArrayList();
    for(String line = bReader.readLine(); line != null; line = bReader.readLine()) {
      aList.clear();
      if(line.startsWith("#")) {
        handler.commentLine(line);
      } else {
        StringTokenizer st = new StringTokenizer(line, "\t", false);
        int i = 0;
        while(st.hasMoreTokens() && i < 8) {
          aList.add(st.nextToken());
        }
        String rest = null;
        String comment = null;
        if(st.hasMoreTokens()) {
          try {
            rest = st.nextToken(((char) 0) + "");
          } catch (NoSuchElementException nsee) {
          }
        }
        if(rest != null) {
          int ci = rest.indexOf("#");
          if (ci != -1) {
            comment = rest.substring(ci);
            rest = rest.substring(0, ci);
          }
        }
        handler.recordLine(createRecord(handler, aList, rest, comment));
      }
    }
    handler.endDocument();
  }
  
  protected GFFRecord createRecord(GFFDocumentHandler handler, ArrayList aList, String rest, String comment)
  throws BioException {
    SimpleGFFRecord record = new SimpleGFFRecord();
    
    record.setSeqName((String) aList.get(0));
    record.setSource((String) aList.get(1));
    record.setFeature((String) aList.get(2));
    
    try {
      record.setStart(Integer.parseInt( (String) aList.get(3)));
    } catch (NumberFormatException nfe) {
      handler.invalidStart((String) aList.get(3), nfe);
    }
    
    try {
      record.setEnd(Integer.parseInt( (String) aList.get(4)));
    } catch (NumberFormatException nfe) {
      handler.invalidEnd((String) aList.get(3), nfe);
    }
    
    String score = (String) aList.get(5);
    if(score == null || score.equals("") || score.equals(".")) {
      record.setScore(GFFRecord.NO_SCORE);
    } else {
      try {
        record.setScore(Double.parseDouble(score));
      } catch (NumberFormatException nfe) {
        handler.invalidScore(score, nfe);
      }
    }
    
    String strand = (String) aList.get(6);
    if(strand == null || strand.equals("") || strand.equals(".")) {
      record.setStrand(GFFRecord.NO_STRAND);
    } else {
      if(strand.equals("+")) {
        record.setStrand(GFFRecord.POSITIVE_STRAND);
      } else if(strand.equals("-")) {
        record.setStrand(GFFRecord.NEGATIVE_STRAND);
      } else {
        handler.invalidStrand(strand);
      }
    }
    
    String frame = (String) aList.get(7);
    if(frame.equals(".")) {
      record.setFrame(GFFRecord.NO_FRAME);
    } else {
      try {
        record.setFrame(Integer.parseInt(frame));
      } catch (NumberFormatException nfe) {
        handler.invalidFrame((String) aList.get(7), nfe);
      }
    }
    
    record.setGroupAttributes(rest);
    record.setComment(comment);
    
    return record;
  }
}
