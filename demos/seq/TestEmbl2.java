package seq;

import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

public class TestEmbl2 {
  public static void main(String [] args) {
    try {
      if(args.length != 1) {
        throw new Exception("Use: TestEmbl2 emblFile");
      }
      
      File emblFile = new File(args[0]);
      BufferedReader eReader = new BufferedReader(
        new InputStreamReader(new FileInputStream(emblFile)));
      SequenceIterator seqI = SeqIOTools.readEmbl(eReader);
        
        
      while(seqI.hasNext()) {
        Sequence seq = seqI.nextSequence();
        System.out.println(seq.getName() + " has " + seq.countFeatures() + " features");
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
