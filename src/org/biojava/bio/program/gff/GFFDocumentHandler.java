package org.biojava.bio.program.gff;

import org.biojava.bio.*;

public interface GFFDocumentHandler{
  void startDocument();
  void endDocument();
  
  void commentLine(String comment);
  void recordLine(GFFRecord record);
  
  void invalidStart(String token, NumberFormatException nfe)
  throws BioException;
  void invalidEnd(String token, NumberFormatException nfe)
  throws BioException;
  void invalidScore(String token, NumberFormatException nfe)
  throws BioException;
  void invalidStrand(String token)
  throws BioException;
  void invalidFrame(String token, NumberFormatException nfe)
  throws BioException;
}
