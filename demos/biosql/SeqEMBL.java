package biosql;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.db.biosql.*;

public class SeqEMBL {
  public static void main(String[] args)
          throws Exception {
    if (args.length < 4) {
      System.err.println("Usage SeqEMBL <url> <user> <password> <database> [id1] [id2] ...");
      return;
    }

    String dbURL = args[0];
    String dbUser = args[1];
    String dbPass = args[2];
    String bioName = args[3];

    SequenceDB seqDB = new BioSQLSequenceDB(dbURL, dbUser, dbPass, bioName, false);
    SequenceFormat ff = new EmblLikeFormat();

    if (args.length < 5) {
      SequenceIterator si = seqDB.sequenceIterator();
      while (si.hasNext()) {
        ff.writeSequence(si.nextSequence(), System.out);
      }
    } else {
      for (int i = 4; i < args.length; ++i) {
        Sequence seq = seqDB.getSequence(args[i]);
        ff.writeSequence(seq, System.out);
      }
    }
  }
}
