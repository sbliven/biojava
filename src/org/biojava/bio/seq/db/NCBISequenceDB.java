package org.biojava.bio.seq.db;

import java.net.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

public class NCBISequenceDB
extends WebSequenceDB {
  public static URL defaultServer;
  public static String defaultCGI;
  private static SequenceFormat format;
  
  static {
    try {
      URL defaultServer = new URL("http://www.ncbi.nlm.nih.gov");
    } catch (MalformedURLException e) {
      throw new BioError(
        e,
        "Could not initalize NCBISequenceDB as the default server URL barfed"
      );
    }
    String defaultCGI = "entrez/viewer.fcgi";
    SequenceFormat format = new FastaFormat();
  }
  
  protected SequenceFormat getSequenceFormat() {
    return format;
  }
  
  protected Alphabet getAlphabet() {
    return DNATools.getDNA();
  }
  
  protected URL getAddress(String id)
  throws MalformedURLException {
    String query = "view=gb&txt=on&db=nucleotide&form=1&title=no&term=" + id;
    
    return new URL(defaultServer, defaultCGI + "?" + query);
  }
  
  public String getName() {
    return "NCBI-Genbank";
  }
}
