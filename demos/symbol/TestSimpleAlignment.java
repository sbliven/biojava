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

import java.util.*;

import org.biojava.bio.symbol.*;

/**
 * This demonstrates building an alignment from a set of sequences of the same
 * length.
 */
public class TestSimpleAlignment {
  public static void main(String [] args) {
    try {
      // make three random sequences
      SymbolList res1 = Tools.createSymbolList(10);
      SymbolList res2 = Tools.createSymbolList(10);
      SymbolList res3 = Tools.createSymbolList(10);
    
      // think of three names
      String name1 = "pigs";
      String name2 = "dogs";
      String name3 = "cats";
    
      // create a map of name->sequence
      Map aMap = new HashMap();
      aMap.put(name1, res1);
      aMap.put(name2, res2);
      aMap.put(name3, res3);
    
      // make an alignment
      Alignment ali = new SimpleAlignment(aMap);
      
      // print out each row in the alignment
      System.out.println("Sequences in alignment");
      for(Iterator i = ali.getLabels().iterator(); i.hasNext(); ) {
        String label = (String) i.next();
        SymbolList rl = ali.symbolListForLabel(label);
        System.out.println(label + ":\t" + rl.seqString());
      }
      
      // print out each column
      System.out.println("Columns");
      for(int i = 1; i <= ali.length(); i++) {
        System.out.println(i + ":\t" + ali.symbolAt(i).getName());
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
