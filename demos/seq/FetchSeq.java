package seq;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;

/**
 * Fetch sequences by accession from the NCBI efetch server and dump them as genbank.
 *
 * Messages go to stderr and all data output goes to stdout.
 *
 * @author Matthew Pocock
 */
public class FetchSeq {
  public static void main(String[] args) {
    GenbankSequenceDB genbank = new GenbankSequenceDB();

    for(int i = 0; i < args.length; i++) {
      try {
        System.err.println("Fetching: " + args[i]);
        
        Sequence seq = genbank.getSequence(args[i]);
        SeqIOTools.writeGenbank(System.out, seq);
        
        System.err.println("Done.");
      } catch (Throwable t) {
        t.printStackTrace(System.err);
      }
    }
  }
}
