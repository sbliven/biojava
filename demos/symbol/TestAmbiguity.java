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
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.*;

/**
 * This demonstrates building an alignment from a set of sequences of the same
 * length.
 */
public class TestAmbiguity {

  public static void main(String [] args) {

    try {

      {
        final String dnaText = "agctrymkswhbvdn-AGCTRYMKSWHBVDN";
        final FiniteAlphabet dna = DNATools.getDNA();

        final SymbolTokenization sParser = dna.getTokenization("token");
        final SymbolList dnaList = new SimpleSymbolList(sParser, dnaText);

        for(int i = 1; i <= dnaList.length(); i++) {
          final Symbol s = dnaList.symbolAt(i);
          System.out.print(s.getName() + " -> {");
            Iterator j = ((FiniteAlphabet) s.getMatches()).iterator();
            if(j.hasNext()) {
              System.out.print(((Symbol) j.next()).getName());
            }
            while(j.hasNext()) {
              System.out.print(", " + ((Symbol) j.next()).getName());
            }
          System.out.println("}");
        }
      }


      {
        System.out.println("Now Testing Prot");
        final String protText = "agctrymkswhvdn-AGCTRYMKSWHVDN";
        final FiniteAlphabet protein = ProteinTools.getAlphabet();
        //dna.addSymbol(AlphabetManager.getGapSymbol());//problem here, even when explicitly adding the gap symbol it doesn't parse
//<<<<<<< TestAmbiguity.java
//        final SymbolTokenization protParser = protein.getTokenization("token");
//        final SymbolList protList = protParser.parse(protText);
//=======
        final SymbolTokenization protParser = protein.getTokenization("token");
        final SymbolList protList = new SimpleSymbolList(protParser, protText);
//>>>>>>> 1.7

        for(int i = 1; i <= protList.length(); i++) {
          final Symbol s = protList.symbolAt(i);
          System.out.print(s.getName() + " -> {");
            Iterator j = ((FiniteAlphabet) s.getMatches()).iterator();
            if(j.hasNext()) {
              System.out.print(((Symbol) j.next()).getName());
            }
            while(j.hasNext()) {
              System.out.print(", " + ((Symbol) j.next()).getName());
            }
          System.out.println("}");
        }
      }

      {
        System.out.println("Now Testing Prot-Term");
        final String protTText = "agctrymkswhvdn-AGCTRYMKSWHVDN*";
        final FiniteAlphabet proteinT = ProteinTools.getTAlphabet();
        //dna.addSymbol(AlphabetManager.getGapSymbol());//problem here, even when explicitly adding the gap symbol it doesn't parse
//<<<<<<< TestAmbiguity.java
//        final SymbolTokenization protTParser = proteinT.getTokenization("token");
//        final SymbolList protTList = protTParser.parse(protTText);
//=======
        final SymbolTokenization protTParser = proteinT.getTokenization("token");
        final SymbolList protTList = new SimpleSymbolList(protTParser, protTText);
//>>>>>>> 1.7

        for(int i = 1; i <= protTList.length(); i++) {
          final Symbol s = protTList.symbolAt(i);
          System.out.print(s.getName() + " -> {");
            Iterator j = ((FiniteAlphabet) s.getMatches()).iterator();
            if(j.hasNext()) {
              System.out.print(((Symbol) j.next()).getName());
            }
            while(j.hasNext()) {
              System.out.print(", " + ((Symbol) j.next()).getName());
            }
          System.out.println("}");
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
