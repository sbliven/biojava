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
import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * This demonstrates gapped sequences as a view onto an ungapped sequence.
 *
 * @author Matthew Pocock
 */
public class TestGappedSymbolList {
  public static void main(String [] args) {
    try {
    SymbolList res = Tools.createSymbolList(10);
    final int trials = 10;
    
    System.out.println("Starting with:\n" + res.seqString());
    GappedSymbolList gl = new GappedSymbolList(res);
    System.out.println("Gapped version:\n" + gl.seqString());
    gl.dumpBlocks();
    for(int i = 0; i < trials; i++) {
      int pos = (int) (Math.random() * (double) gl.length() + 1.0);
//      System.out.println("Inserting gap at " + pos);
      gl.addGapInView(pos);
//      gl.dumpBlocks();
      System.out.println(gl.seqString());
    }
    for(int i = 0; i < trials; i++) {
      int pos;
      do {
        pos = (int) (Math.random() * (double) gl.length()) + 1;
      } while(gl.symbolAt(pos) != gl.getAlphabet().getGapSymbol());
//      System.out.println("Removing gap at " + pos);
      gl.removeGap(pos);
//      gl.dumpBlocks();
      System.out.println(gl.seqString());
    }
    for(int i = 0; i < trials; i++) {
      int pos = (int) (Math.random() * ((double) res.length() + 1.0)) + 1;
//      System.out.println("Inserting gap at " + pos);
      gl.addGapInSource(pos);
//      gl.dumpBlocks();
      System.out.println(gl.seqString());
    }
    for(int i = 1; i <= gl.length(); i++) {
      System.out.println(gl.viewToSource(i));
    }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
