/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.bio.seq.db;

import java.util.*;
import java.net.*;
import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.io.*;

/**
 * Functions for access to a web based database that returns sequences
 * in a variety of formats.
 *
 * @author Jason Stajich
 */

abstract public class WebSequenceDB implements SequenceDBLite {
  abstract SequenceFormat getSequenceFormat();
  abstract URL getAddress(String id);
  
  public Sequence getSequence(String id) throws BioException
  {
    if( id.equals("") ) {
      throw new BioException("did not specify a valid id for getSequence");
    }
    
    URL queryURL = getAddress(id);      
    
    Sequence seq = null;
    try {
      System.err.println("query is "+ queryURL.toString());
      URLConnection connection = queryURL.openConnection();
      SequenceFormat sFormat = getSequenceFormat();
      SequenceBuilder sbuilder = new SimpleSequenceBuilder();
      FastaDescriptionLineParser sFact =
        new FastaDescriptionLineParser(sbuilder);
      Alphabet alpha = DNATools.getDNA();
      SymbolParser rParser = alpha.getParser("token");
      System.err.println("got data from "+ queryURL);
      SequenceIterator seqI = new
        StreamReader(connection.getInputStream(),
                     (SequenceFormat)sFormat, rParser, 
                     (SequenceBuilderFactory)sFact);

      BufferedReader in = new BufferedReader(new 
					     InputStreamReader(connection.getInputStream()));
    
      if( seqI.hasNext() ) {
        seq = seqI.nextSequence();
        System.out.println(seq.getName() + " has " + 
                           seq.countFeatures() + " features");
      }
      String line;
      
      while( in.ready() ) {
        line = in.readLine();   
        System.out.println(line);
      }
      
    }
    catch ( Exception e ){
      throw new BioException(e);
    }
    return seq;
  }
  
}
