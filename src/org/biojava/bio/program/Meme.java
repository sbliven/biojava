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

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;
import org.biojava.bio.dp.*;

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

  public Meme(InputStream is, SymbolParser symParser)
         throws IOException, IllegalSymbolException, IllegalAlphabetException {
    StreamTokenizer st = new StreamTokenizer(
      new BufferedReader(new InputStreamReader(is)));
    st.eolIsSignificant(true);
    st.wordChars('*', '*');
    st.parseNumbers();

    SymbolList sym = null;

   ALPHABET:
    while( true ) {
      int nt = st.nextToken();
      if (nt == st.TT_EOF) {
          return;
      } else if (nt == st.TT_WORD) {
          if(st.sval.startsWith("ALPHABET")) {
            while(st.nextToken() != st.TT_WORD) {}
            sym = symParser.parse(st.sval);
            break ALPHABET;
          }
      }
    }

    while(st.nextToken() != st.TT_EOL) {}
    while(st.nextToken() != st.TT_EOL) {}

   SEQLIST:
    while( true ) {
      if(st.nextToken() == st.TT_WORD) {
          if(st.sval != null && st.sval.startsWith("*"))
            break SEQLIST;
          seqIDs.add(st.sval.intern());
      }
    }

   OUTER:
    while( true ) {
      int motifNo = 0;
      int width = 0;

     FINDMOTIF:
      while( true ) {
	int nt = st.nextToken();
	if (nt == st.TT_EOF) {
            break OUTER;
	} else if (nt == st.TT_WORD) {
            if(st.sval.startsWith("MOTIF")) {
              st.nextToken();			// MOTIF x
              motifNo = (int) st.nval;	// x
              while(st.nextToken() != st.TT_NUMBER) {} // width = w
              width = (int) st.nval;		// w
              break FINDMOTIF;
            }
        }
      }

     FINDWEIGHTS:
      while( true ) {
	int nt = st.nextToken();
	if (nt == st.TT_EOF) {
            break OUTER;
	} else if (nt == st.TT_WORD) {
            if(st.sval.startsWith("log")) {
              while(st.nextToken() != st.TT_EOL) {}
              break FINDWEIGHTS;
            }
        }
      }

      SimpleWeightMatrix matrix = new SimpleWeightMatrix(
        (FiniteAlphabet) symParser.getAlphabet(),
        width,
        DistributionFactory.DEFAULT
      );

      int r = 0;
      int c = 0;
     READMOTIF:
      while( true ) {
	int nt = st.nextToken();
	if (nt == st.TT_EOF) {
            break OUTER;
        } else if (nt == st.TT_EOL) {
            r = 0;
            c++;
            if(c == width)
              break READMOTIF;
        } else if (nt == st.TT_NUMBER) {
          try {
            matrix.getColumn(c).setWeight(sym.symbolAt(r+1), st.nval);
            r++;
          } catch (ChangeVetoException cve) {
            throw new BioError(cve, "Couldn't set up the distribution ");
          }
        }
      }

      motifs.add(matrix);
    }
  }
}
