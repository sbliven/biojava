package BioCorba;

import java.io.*;
import org.omg.CORBA.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bridge.Biocorba.Seqcore.*;
import org.Biocorba.Seqcore.*;

public class CorbaDBClient {
  public static void main(String args []) throws Exception {
    if(args.length != 1) {
      throw new Exception("Use: CorbaDBClient ior_string");
    }
    
    try {
      String ior = args[0];
    
      ORB orb = ORB.init(new String[0], null);
    
      org.omg.CORBA.Object obj = orb.string_to_object(ior);
      SeqDB seqDBRef = SeqDBHelper.narrow(obj);
    
      SequenceDB sequenceDB = new SequenceDBAdapter(seqDBRef);
      for(SequenceIterator si = sequenceDB.sequenceIterator(); si.hasNext(); ) {
        Sequence seq = si.nextSequence();
        System.out.println("Got sequence " + seq.getName());
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
