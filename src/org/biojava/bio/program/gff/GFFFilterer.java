package org.biojava.bio.program.gff;

import java.io.*;
import org.biojava.bio.*;

public class GFFFilterer implements GFFDocumentHandler {
  private GFFDocumentHandler handler;
  private GFFRecordFilter filter;

  public GFFFilterer(GFFDocumentHandler handler, GFFRecordFilter filter) {
    this.handler = handler;
    this.filter = filter;
  }
  
  public void startDocument() {
    handler.startDocument();
  }
  
  public void endDocument() {
    handler.endDocument();
  }
  
  public void commentLine(String comment) {
    handler.commentLine(comment);
  }
  
  public void recordLine(GFFRecord record) {
    if(filter.accept(record)) {
      handler.recordLine(record);
    }
  }
  
  public void invalidStart(String token, NumberFormatException nfe)
  throws BioException {
    handler.invalidStart(token, nfe);
  }
  
  public void invalidEnd(String token, NumberFormatException nfe)
  throws BioException {
    handler.invalidEnd(token, nfe);
  }
  
  public void invalidScore(String token, NumberFormatException nfe)
  throws BioException {
    handler.invalidScore(token, nfe);
  }
  
  public void invalidStrand(String token)
  throws BioException {
    handler.invalidStrand(token);
  }
  
  public void invalidFrame(String token, NumberFormatException nfe)
  throws BioException {
    handler.invalidFrame(token, nfe);
  }
}
