package org.biojava.bio.program.gff;

import org.biojava.bio.*;

public interface GFFDocumentHandler{
  public void startDocument();
  public void endDocument();
  
  public void commentLine(String comment);
  public void recordLine(GFFRecord record);
  
  public void invalidStart(String token, NumberFormatException nfe)
  throws BioException;
  public void invalidEnd(String token, NumberFormatException nfe)
  throws BioException;
  public void invalidScore(String token, NumberFormatException nfe)
  throws BioException;
  public void invalidStrand(String token)
  throws BioException;
  public void invalidFrame(String token, NumberFormatException nfe)
  throws BioException;
}
