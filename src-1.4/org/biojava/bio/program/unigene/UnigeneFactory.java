package org.biojava.bio.program.unigene;

import java.io.*;
import java.net.*;
import org.biojava.bio.*;

public interface UnigeneFactory {
  public UnigeneDB loadUnigene(URL unigeneURL)
  throws BioException;
  
  public UnigeneDB createUnigene(URL unigeneURL)
  throws BioException;

  public boolean canAccept(URL unigeneURL);
}
