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
import org.biojava.bio.seq.SequenceTools;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;


/**
 * Demo of reverse complementing a fasta DNA file
 * @author Matthew Pocock
 * @author Mark Schreiber
 */
public class ReverseComplement {
  
   /**
    * Run the program
    * @param args a dna fasta file
    */
  public static void main(String[] args)
  throws Exception {
    if(args.length < 1) {
      System.err.println("Use: seq.ReverseComplement inFile");
      System.exit(1);
    }

    BufferedReader seqIn = new BufferedReader(
      new FileReader(
        new File(args[0])
      )
    );

    OutputStream seqOut = System.out;
    Namespace ns = RichObjectFactory.getDefaultNamespace();

    for(RichSequenceIterator si = RichSequence.IOTools.readFastaDNA(seqIn, ns);
        si.hasNext(); ) {
        
      RichSequence seq = si.nextRichSequence();
      RichSequence rev = RichSequence.Tools.enrich(SequenceTools.reverseComplement(seq));
      RichSequence.IOTools.writeFasta(seqOut, rev, ns);
    }
  }
}
