import java.io.*;
import java.util.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.tools.*;
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
      gffEntries.getAnnotator().annotate(seqDB);
    
      // now converting back to gff
      System.out.println("Dumping sequence features as GFF");
      PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
      GFFWriter writer = new GFFWriter(out);
      SequencesAsGFF seqsAsGFF = new SequencesAsGFF();
      
      seqsAsGFF.processDB(seqDB, writer);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private static SequenceDB loadSequences(File seqFile)
  throws Exception {
    HashSequenceDB seqDB = new HashSequenceDB(HashSequenceDB.byURN);

    Alphabet alpha = AlphabetManager.instance().alphabetForName("DNA");
    ResidueParser rParser = alpha.getParser("symbol");
    SequenceFactory sFact = new SimpleSequenceFactory();
    SequenceFormat sFormat = new FastaFormat();
    InputStream seqDBI = new FileInputStream(seqFile);
    SequenceIterator seqI = new StreamReader(seqDBI, sFormat, rParser, sFact);
    
    while(seqI.hasNext()) {
      try {
        seqDB.addSequence(seqI.nextSequence());
      } catch (SeqException se) {
        se.printStackTrace();
      }
    }
    
    return seqDB;
  }
}
