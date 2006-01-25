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
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;

/**
 * Demo reading an EMBL file
 * @author Matthew Pocock
 * @author Mark Schreiber
 */
public class TestEmbl {
    
  /**
   * @param args an EMBL dna file
   */ 
  public static void main(String [] args) {
    try {
      if(args.length != 1) {
        throw new Exception("Use: seq.TestEmbl emblFile");
      }
      
      File emblFile = new File(args[0]);
      BufferedReader br = new BufferedReader(new FileReader(emblFile));
      Namespace ns = RichObjectFactory.getDefaultNamespace();
      
      RichSequenceIterator seqI =
        RichSequence.IOTools.readEMBLDNA(br, ns);
        
      while(seqI.hasNext()) {
        RichSequence seq = seqI.nextRichSequence();
        System.out.println(seq.getName() + " has " + seq.countFeatures() 
            + " features and "+seq.getComments().size()+" comments");
        RichSequence.IOTools.writeEMBL(System.out, seq, ns);
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}


