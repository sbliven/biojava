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
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import com.sun.xml.tree.*;
import com.sun.xml.parser.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.tools.*;
import org.biojava.bio.dp.*;


/**
 * This demonstrates gapped sequences as a view onto an ungapped sequence.
 */
public class TestGappedResidueList {
  public static void main(String [] args) throws Exception {
    ResidueList res = createResidueList(10);
    final int trials = 10;
    
    System.out.println("Starting with:\n" + res.seqString());
    GappedResidueList gl = new GappedResidueList(res);
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
      } while(gl.residueAt(pos) != AlphabetManager.instance().getGapResidue());
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
  }
  
  public static ResidueList createResidueList(int length)
  throws IllegalResidueException {
    List l = new ArrayList(length);
    for(int i = 0; i < length; i++) {
      l.add(DNATools.forIndex((int) (4.0*Math.random())));
    }
    return new SimpleResidueList(DNATools.getAlphabet(), l);
  }
}
