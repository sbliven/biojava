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
      System.out.println("Loading gff");
      GFFRecordFilter.SourceFilter filter = new GFFRecordFilter.SourceFilter();
      filter.setSource("hand_built");
      GFFEntrySet gffEntries = loadGFF(new File(args[1]), filter);
      System.out.println("Features:");
      for(Iterator i = gffEntries.lineIterator(); i.hasNext(); ) {
        Object o = i.next();
        if(o instanceof GFFRecord) {
          GFFRecord rec = (GFFRecord) o;
          System.out.println("\t" + rec.getSeqName());
        }
      }
    
      // add the features to the sequences
      System.out.println("Adding features from gff to sequences");
      gffEntries.getAnnotator().annotate(seqDB);
    
      // print something out...
      System.out.println("Printing out annotated sequences");
      for(SequenceIterator si = seqDB.sequenceIterator(); si.hasNext(); ) {
        Sequence seq = si.nextSequence();
        System.out.println("\t" + seq.getName());
        for(Iterator i = seq.features(); i.hasNext(); ) {
          Feature feature = (Feature) i.next();
          System.out.println(
            "\t\t" + feature
          );
        }
      }
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
  
  private static GFFEntrySet loadGFF(File gffFile, GFFRecordFilter filter)
  throws Exception {
    BufferedReader bReader = new BufferedReader(
      new InputStreamReader(new FileInputStream(gffFile)));
    return new GFFEntrySet(new GFFParser(), bReader, filter);
  }
}
