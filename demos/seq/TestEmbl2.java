package seq;

import java.io.*;
import java.util.*;

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
        
        SequenceFormat ff = new EmblLikeFormat();

      while(seqI.hasNext()) {
        Sequence seq = seqI.nextSequence();
        ff.writeSequence(seq, "embl", System.out);
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }


    public static void printFeatures(FeatureHolder fh, 
				     FeatureFilter ff,
				     PrintStream pw,
				     String prefix)
	throws Exception
    {
	for (Iterator i = fh.filter(ff, false).features(); i.hasNext(); ) {
	    Feature f = (Feature) i.next();
	    pw.print(prefix);
	    pw.print(f.getType());
	    pw.print(" at ");
	    pw.println(f.getLocation().toString());
	    for (Iterator ai = f.getAnnotation().asMap().entrySet().iterator(); ai.hasNext(); ) {
		Map.Entry me = (Map.Entry) ai.next();
		System.out.println(me.getKey() + " : " + me.getValue());
	    }
	    pw.println();
	    printFeatures(f, ff, pw, prefix + "    ");
	}
    }
}
