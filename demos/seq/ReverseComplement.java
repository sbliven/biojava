package seq;

import java.io.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

public class ReverseComplement {
  public static void main(String[] args)
  throws Exception {
    if(args.length < 2) {
      System.err.println("Use: seq.ReverseComplement inFile, outFile");
      System.exit(1);
    }

    BufferedReader seqIn = new BufferedReader(
      new FileReader(
        new File(args[0])
      )
    );

    OutputStream seqOut = new FileOutputStream(new File(args[1]));

    for(SequenceIterator si = SeqIOTools.readFastaDNA(seqIn); si.hasNext(); ) {
      Sequence seq = si.nextSequence();
      Sequence rev = SequenceTools.reverseComplement(seq);
      SeqIOTools.writeFasta(seqOut, rev);
    }

    seqOut.flush();
    seqOut.close();
  }
}
