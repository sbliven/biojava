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
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;

import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;

/**
 * Another EMBL demo
 * @author Thomas Down
 * @author Mark Schreiber
 */
public class TestEmbl2 {
    
  /**
   * @param args an EMBL dna file
   */
  public static void main(String [] args) {
    try {
      if(args.length != 1) {
        throw new Exception("Use: seq.TestEmbl2 emblFile");
      }
      
      Namespace ns = RichObjectFactory.getDefaultNamespace();
      
      File emblFile = new File(args[0]);
      BufferedReader eReader = new BufferedReader(
        new InputStreamReader(new FileInputStream(emblFile)));
      
      RichSequenceIterator seqI = 
              RichSequence.IOTools.readEMBLDNA(eReader, ns);
        
        
      while(seqI.hasNext()) {
        RichSequence seq = seqI.nextRichSequence();
        System.out.println(seq.getName() + " has " + seq.countFeatures() + " features");

	printFeatures(seq, FeatureFilter.all, System.out, "");
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
