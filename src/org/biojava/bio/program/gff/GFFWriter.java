package org.biojava.bio.program.gff;

import java.io.*;
import org.biojava.bio.*;

public class GFFWriter implements GFFDocumentHandler {
  private PrintWriter out;
  
  public GFFWriter(PrintWriter out) {
    this.out = out;
  }
  
  public void startDocument() {}
  public void endDocument()   {
    out.flush();
  }
  
  public void commentLine(String comment) {
    out.println(comment);
  }
  
  public void recordLine(GFFRecord record) {
    out.print(
      record.getSeqName() + "\t" +
      record.getSource()  + "\t" +
      record.getFeature() + "\t" +
      record.getStart()   + "\t" +
      record.getEnd()     + "\t"
    );
    double score = record.getScore();
    if(score == GFFRecord.NO_SCORE) {
      out.print(".\t");
    } else {
      out.print(score + "\t");
    }
    
    int strand = record.getStrand();
    if(strand == GFFRecord.POSITIVE_STRAND) {
      out.print("+\t");
    } else if(strand == GFFRecord.NEGATIVE_STRAND) {
      out.print("-\t");
    } else {
      out.print(".\t");
    }
    
    int frame = record.getFrame();
    if(frame == GFFRecord.NO_FRAME) {
      out.print(".");
    } else {
      out.print(frame + "");
    }
    
    String ga = record.getGroupAttributes();
    if(ga != null && ga.length() > 0) {
      out.print("\t" + ga);
    }
    
    String comment = record.getComment();
    if(comment != null && comment.length() > 0) {
      if(ga != null && ga.length() > 0) {
        out.print(" ");
      }
      out.print(comment);
    }
    
    out.println("");
  }
  
  public void invalidStart(String token, NumberFormatException nfe)
  throws BioException {}
  public void invalidEnd(String token, NumberFormatException nfe)
  throws BioException {}
  public void invalidScore(String token, NumberFormatException nfe)
  throws BioException {}
  public void invalidStrand(String token)
  throws BioException {}
  public void invalidFrame(String token, NumberFormatException nfe)
  throws BioException {}
}
