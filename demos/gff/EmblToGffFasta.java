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
package gff;

import java.util.*;
import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.program.gff.*;

/**
 * Converts an EMBL file into a fasta file for the sequence and a GFF file for
 * the features.
 * <P>
 * This program demonstrates how to read in an EMBL file, how to write a fasta
 * file and how to write GFF.
 * <P>
 * Use:<br>
 * <code>java EmblToGffFasta emblFile fastaOut gffOut</code>
 *
 * @author Matthew Pocock
 */
public class EmblToGffFasta {
  public static void main(String [] args) throws Exception {
    if(args.length != 3) {
      throw new Exception("Use: EmblToGffFasta emblFile fastaOut gffOut");
    }
    
    try {
      // make the files early to get exceptions about stupid file names
      File emblFile = new File(args[0]);
      File fastaFile = new File(args[1]);
      File gffFile = new File(args[2]);
      
      // reading embl stuff
      SequenceFormat eFormat = new EmblFormat();
      BufferedReader eReader = new BufferedReader(
        new InputStreamReader(new FileInputStream(emblFile)));
      SequenceFactory sFact = new SimpleSequenceFactory();
      Alphabet alpha = DNATools.getDNA();
      SymbolParser rParser = alpha.getParser("token");

      // fasta stuff
      SequenceFormat fFormat = new FastaFormat();
      OutputStream fastaOut = new FileOutputStream(fastaFile);
      
      // gff stuff
      GFFWriter writer = new GFFWriter(
        new PrintWriter(new OutputStreamWriter(new FileOutputStream(gffFile))));
      SequencesAsGFF seqsAsGFF = new SequencesAsGFF();
      
      // Loop over each sequence in the embl file.
      // Write the sequence to a fasta file
      // Write the features to a .gff file
      SequenceIterator seqI =
        new StreamReader(eReader, eFormat, rParser, sFact);
        
      while(seqI.hasNext()) {
        Sequence seq = seqI.nextSequence();
        fFormat.writeSequence(seq, new PrintStream(fastaOut));
        seqsAsGFF.processSequence(seq, writer);
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}

