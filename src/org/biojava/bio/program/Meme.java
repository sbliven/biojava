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


package org.biojava.bio.program;

import java.util.*;
import java.io.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.alignment.*;

/**
 * The results of a meme run.
 *
 * @author Matthew Pocock
 */
public class Meme {
  private List motifs;
  private List seqIDs;

  {
    motifs = new ArrayList();
    seqIDs = new ArrayList();
  }

  public List getMotifs() {
    return motifs;
  }

  public List getSeqIDs() {
    return seqIDs;
  }

  public Meme(InputStream is, ResidueParser resParser)
         throws IOException, IllegalResidueException {
    StreamTokenizer st = new StreamTokenizer(
      new BufferedReader(new InputStreamReader(is)));
    st.eolIsSignificant(true);
    st.wordChars('*', '*');
    st.parseNumbers();

    ResidueList res = null;

   ALPHABET:
    while( true ) {
      switch(st.nextToken()) {
        case st.TT_EOF:
          return;
        case st.TT_WORD:
          if(st.sval.startsWith("ALPHABET")) {
            while(st.nextToken() != st.TT_WORD) {}
            res = resParser.parse(st.sval);
            break ALPHABET;
          }
          break;
      }
    }

    while(st.nextToken() != st.TT_EOL) {}
    while(st.nextToken() != st.TT_EOL) {}

   SEQLIST:
    while( true ) {
      switch(st.nextToken()) {
        case st.TT_WORD:
          if(st.sval != null && st.sval.startsWith("*"))
            break SEQLIST;
          seqIDs.add(st.sval.intern());
          break;
      }
    }

   OUTER:
    while( true ) {
      int motifNo = 0;
      int width = 0;

     FINDMOTIF:
      while( true ) {
        switch(st.nextToken()) {
          case st.TT_EOF:
            break OUTER;
          case st.TT_WORD:
            if(st.sval.startsWith("MOTIF")) {
              st.nextToken();			// MOTIF x
              motifNo = (int) st.nval;	// x
              while(st.nextToken() != st.TT_NUMBER) {} // width = w
              width = (int) st.nval;		// w
              break FINDMOTIF;
            }
            break;
        }
      }

     FINDWEIGHTS:
      while( true ) {
        switch(st.nextToken()) {
          case st.TT_EOF:
            break OUTER;
          case st.TT_WORD:
            if(st.sval.startsWith("log")) {
              while(st.nextToken() != st.TT_EOL) {}
              break FINDWEIGHTS;
            }
            break;
        }
      }

      SimpleWeightMatrix matrix = new SimpleWeightMatrix(resParser.alphabet(), width);

      int r = 0;
      int c = 0;
     READMOTIF:
      while( true ) {
        switch(st.nextToken()) {
          case st.TT_EOF:
            break OUTER;
          case st.TT_EOL:
            r = 0;
            c++;
            if(c == width)
              break READMOTIF;
            break;
          case st.TT_NUMBER:
            matrix.setWeight(res.residueAt(r+1), c, st.nval);
            r++;
            break;
        }
      }

      motifs.add(matrix);
    }
  }
}
