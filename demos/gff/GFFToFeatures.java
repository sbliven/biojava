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

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.program.gff.*;

/**
 * This tests the gff code to check that we can read in features, add them to
 * a sequence, and then print something out.
 */
public class GFFToFeatures {
  public static void main(String [] args) throws Exception {
    if(args.length != 2) {
      throw new Exception("Use: GFFToFeatures sequence.fa features.gff");
    }

    try {    
      // load in the sequences
      System.out.println("Loading sequences");
      SequenceDB seqDB = loadSequences(new File(args[0]));
      System.out.println("Sequences:");
      for(SequenceIterator si = seqDB.sequenceIterator(); si.hasNext(); ) {
        Sequence seq = si.nextSequence();
        System.out.println("\t" + seq.getName());
      }
    
      // load in the GFF
      System.out.println("Loading gff with 'hand_built' source");

      GFFEntrySet gffEntries = new GFFEntrySet();

      GFFRecordFilter.SourceFilter sFilter = new GFFRecordFilter.SourceFilter();
      sFilter.setSource("hand_built");
      GFFFilterer filterer = new GFFFilterer(gffEntries.getAddHandler(), sFilter);

      GFFParser parser = new GFFParser();
      parser.parse(
        new BufferedReader(
          new InputStreamReader(
            new FileInputStream(
              new File(args[1])))),
        filterer);

      // add the features to the sequences
      System.out.println("Adding features from gff to sequences");
      SequenceDB aSeqDB = new AnnotatedSequenceDB(seqDB, gffEntries.getAnnotator());
    
      // now converting back to gff
      System.out.println("Dumping sequence features as GFF");
      PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
      GFFWriter writer = new GFFWriter(out);
      SequencesAsGFF seqsAsGFF = new SequencesAsGFF();
      
      seqsAsGFF.processDB(aSeqDB, writer);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private static SequenceDB loadSequences(File seqFile)
  throws Exception {
    HashSequenceDB seqDB = new HashSequenceDB(IDMaker.byName);

    Alphabet alpha = AlphabetManager.alphabetForName("DNA");
    SymbolParser rParser = alpha.getParser("token");
    SequenceBuilderFactory sFact = new EmblProcessor.Factory(SimpleSequenceBuilder.FACTORY);
    SequenceFormat sFormat = new FastaFormat();
    InputStream seqDBI = new FileInputStream(seqFile);
    SequenceIterator seqI = new StreamReader(seqDBI, sFormat, rParser, sFact);
    
    while(seqI.hasNext()) {
      try {
        seqDB.addSequence(seqI.nextSequence());
      } catch (BioException se) {
        se.printStackTrace();
      }
    }
    
    return seqDB;
  }
}
