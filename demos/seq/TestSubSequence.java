/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package seq;

import java.io.*;
import java.util.*;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;


/**
 * Demo the use of SubSequence
 * @author Thomas Down
 * @author Mark Schreiber
 */
public class TestSubSequence {
  public static void main(String [] args) {
    try {
      if(args.length != 3) {
        throw new Exception("Use: seq.TestSubSequence seqFile from to");
      }
      
      File file = new File(args[0]);
      
      int from = Integer.parseInt(args[1]);
      int to = Integer.parseInt(args[2]);
      
      //set up auto recognition, by loading the classes they can be used
      // for auto recognition
      Class.forName("org.biojavax.bio.seq.io.EMBLFormat");
      Class.forName("org.biojavax.bio.seq.io.GenbankFormat");
      Class.forName("org.biojavax.bio.seq.io.FastaFormat");
      
      RichSequenceIterator seqI =
        RichSequence.IOTools.readFile(file, RichObjectFactory.getDefaultNamespace());
        
      while(seqI.hasNext()) {
        RichSequence seq = seqI.nextRichSequence();
        
	RichSequence subSeq = RichSequence.Tools.subSequence(
                seq, from, to, seq.getNamespace(), 
                seq.getName()+" from: "+from+" to: "+to, 
                "", "", 1, new Double(1.0));
        
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
