package seq;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.SubSequence;
import org.biojava.bio.seq.io.*;

public class TestSubSequence {
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
      SymbolTokenization rParser = alpha.getTokenization("token");
      SequenceIterator seqI =
        new StreamReader(eReader, eFormat, rParser, sFact);
        
      while(seqI.hasNext()) {
        Sequence seq = seqI.nextSequence();
	Sequence subSeq = new SubSequence(seq, 1000, 3000);
        System.out.println(subSeq.getName() + " has " + subSeq.countFeatures() + " features");
	printFeatures(subSeq, System.out, "");
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }

    public static void printFeatures(FeatureHolder fh, 
				     PrintStream pw,
				     String prefix)
	throws Exception
    {
	for (Iterator i = fh.features(); i.hasNext(); ) {
	    Feature f = (Feature) i.next();
	    pw.print(prefix);
	    pw.print(f.getType());
	    pw.print(" at ");
	    pw.print(f.getLocation().toString());
	    pw.println();
	    printFeatures(f, pw, prefix + "    ");
	}
    }
}
