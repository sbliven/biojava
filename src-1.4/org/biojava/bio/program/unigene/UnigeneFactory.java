package org.biojava.bio.program.unigene;

import java.io.*;
import java.net.*;
import org.biojava.bio.*;

public interface UnigeneFactory {
  public UnigeneDB loadUnigene(URL unigeneDir)
  throws IOException, BioException;
  
  public UnigeneDB createUnigene(URL unigeneDir)
  throws IOException, BioException;
}
