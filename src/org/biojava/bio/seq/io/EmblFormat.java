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


package org.biojava.bio.seq.io;

import java.io.*;
import java.util.*;

import org.biojava.bio.seq.*;

/**
 * IO module for embl files.
 * <P>
 * This is a realy bad skeletal implementation. It can read the AC line and the
 * sequence only. It does not yet write.
 * <P>
 * Would anybody like to write this?
 *
 * @author Matthew Pocock
 */
public class EmblFormat implements SequenceFormat {
  public Sequence readSequence(StreamReader.Context context, ResidueParser resParser, SequenceFactory sf)
  throws IllegalResidueException, IOException {
    final BufferedReader in = context.getReader();
    
    String accession;
    String line;
    
    while(true) {
      line = in.readLine();
      if(line == null) {
        context.streamEmpty();
        throw new IOException("Premature end of stream encountered");
      }
      if(line.startsWith("AC")) {
        accession = line.substring(5, line.length()-1);
        break;
      }
    }
    
    do {
      line = in.readLine();
      if(line == null) {
        context.streamEmpty();
        throw new IOException("Premature end of stream encountered");
      }
    } while(!line.startsWith("SQ"));

    line = in.readLine();
    if(line == null) {
      context.streamEmpty();
      throw new IOException("Premature end of stream encountered");
    }
    
    List resList = new ArrayList();
    while(line != null && !line.startsWith("//")) {
      StringTokenizer st = new StringTokenizer(line, " ", false);
      while(st.hasMoreTokens()) {
        String token = st.nextToken();
        if(st.hasMoreTokens()) {
          resList.addAll(resParser.parse(token).toList());
        } else {
          char c = token.charAt(token.length()-1);
          if(!Character.isDigit(c)) {
            resList.addAll(resParser.parse(token).toList());
          }
        }
      }
      line = in.readLine();
    }
    
    if(line != null) {
      do {
        in.mark(180);
        line = in.readLine();
      } while(line != null && line.trim().length() == 0);
      if(line == null) {
        context.streamEmpty();
      } else {
        in.reset();
      }
    }
    
    Annotation ann = new SimpleAnnotation();
    ann.setProperty("id", accession);
    return sf.createSequence(new SimpleResidueList(resParser.alphabet(),
                                                   resList), 
                             "urn://sequence:embl/" + accession, accession, ann);
  }

  /**
   * This is not implemented. It does not write anything to the stream.
   */
  public void writeSequence(Sequence seq, PrintStream os)
  throws IOException {
    
  }
}
