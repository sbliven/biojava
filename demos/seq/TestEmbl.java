package seq;

import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

public class TestEmbl {
  public static void main(String [] args) {
    try {
      if(args.length != 1) {
        throw new Exception("Use: TestEmbl emblFile");
      }
      
      File emblFile = new File(args[0]);
      SequenceFormat eFormat = new EmblLikeFormat();
      BufferedReader eReader = new BufferedReader(
        new InputStreamReader(new FileInputStream(emblFile)));
      SequenceBuilderFactory sFact = new EmblProcessor.Factory(SimpleSequenceBuilder.FACTORY);
      Alphabet alpha = DNATools.getDNA();
      SymbolParser rParser = alpha.getParser("token");
      SequenceIterator seqI =
        new StreamReader(eReader, eFormat, rParser, sFact);
        
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
