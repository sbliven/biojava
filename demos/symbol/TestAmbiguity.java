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
package symbol;

import java.util.*;

import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.DNATools;

/**
 * This demonstrates building an alignment from a set of sequences of the same
 * length.
 */
public class TestAmbiguity {
  public static void main(String [] args) {
    try {
      String things = "agctrymkswhbvdn-";
      FiniteAlphabet dna = DNATools.getDNA();
      SymbolParser sParser = dna.getParser("token");
      SymbolList sList = sParser.parse(things);
      for(int i = 1; i <= sList.length(); i++) {
        Symbol s = sList.symbolAt(i);
        System.out.print(s.getName() + " -> {");
        if(s instanceof AmbiguitySymbol) {
          AmbiguitySymbol as = (AmbiguitySymbol) s;
          Iterator j = ((FiniteAlphabet) as.getMatchingAlphabet()).iterator();
          if(j.hasNext()) {
            System.out.print(((Symbol) j.next()).getName());
          }
          while(j.hasNext()) {
            System.out.print(", " + ((Symbol) j.next()).getName());
          }
        } else {
          System.out.print(s.getName());
        }
        System.out.println("}");
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
