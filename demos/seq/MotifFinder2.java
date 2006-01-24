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

package seq;

import java.io.*;
import java.util.*;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;

/**
 *Modified version of MotifFinder.java
 *This program will search a fasta file for a motif.
 *It will match motifs and/or sequences containing ambiguity symbols.
 *@author Matthew Pocock
 *@author Andy Hammer
 *@author Mark Schreiber
 */

public class MotifFinder2 {

  /**
   * Run the Program
   * @param args a motif (eg acgtnnc) and a fasta file
   */
  public static void main(String[] args) throws BioException, IOException {
    if (args.length < 2) {
      System.err.println("Usage: seq.MotifFinder2 <motif> <seqfile.fasta>");
    }
    SymbolList motif = DNATools.createDNA(args[0]);
    int motLength = motif.length();
    SymbolList reverseMotif = DNATools.reverseComplement(motif);
    int count =0;
    String fileName = args[1];
    FileReader stream = new FileReader(fileName);
    BufferedReader br = new BufferedReader(stream);
    RichSequenceIterator si = RichSequence.IOTools.readFastaDNA(br, RichObjectFactory.getDefaultNamespace());
    
    while (si.hasNext()) {
      RichSequence sequence = si.nextRichSequence();
      int seqLength = sequence.length();
      for (int pos = 1; pos <= seqLength - motLength + 1; ++pos) {
        SymbolList sub = sequence.subList(pos, pos + motLength - 1);
        if (compare(sub, motif)) {
          count++;
          report(sequence.getName(), sub.seqString(), pos, pos + motLength - 1, "+");
        }
        if (compare(sub, reverseMotif)) {
          count++;
          report(sequence.getName(), sub.seqString(), pos, pos + motLength - 1, "-");
        }
      }
    }
    System.out.println("found "+count+" instances of "+ motif.seqString());
  }//End main()

  private static void report(String name,
                             String result,
                             int start,
                             int end,
                             String strand) {
    System.out.println(name + ":" +
                       result + '\t' +
                       start + '\t' +
                       end + '\t' +
                       strand);
  }//End report()

  private static boolean compare(SymbolList sl1, SymbolList sl2)
      throws BioException {
    if (sl1.length() != sl2.length()) {
      throw new BioException("Lengths don't match");
    }

    for (int pos = 1; pos <= sl1.length(); ++pos) {
      boolean status=false;
      Symbol symbolA = (Symbol) sl1.symbolAt(pos);
      FiniteAlphabet A = ((FiniteAlphabet) symbolA.getMatches());
      Symbol symbolB = (Symbol) sl2.symbolAt(pos);
      FiniteAlphabet B = ((FiniteAlphabet) symbolB.getMatches());
      Iterator ia = A.iterator();
      int hits=0;
      while(ia.hasNext()){
        Symbol s = (Symbol)ia.next();
        if((B.contains(s))){
          hits++;
          break;
        }
      }
      if(hits==0){
        return false;
      }
    }
    return true;
  }//End compare()

}//End class MotifFinder2
