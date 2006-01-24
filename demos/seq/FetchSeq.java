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

import org.biojavax.RichObjectFactory;
import org.biojavax.bio.db.ncbi.GenbankRichSequenceDB;
import org.biojavax.bio.seq.RichSequence;


/**
 * Fetch sequences by accession from the NCBI efetch server and dump them as genbank.
 * Updated to use RichSequence and BioJavaX.
 *
 * Messages go to stderr and all data output goes to stdout.
 *
 * @author Matthew Pocock
 * @author Mark Schreiber
 */

public class FetchSeq {

  /**
   * Run the program
   * @param args one or more genbank accessions
   */
  public static void main(String[] args) {

    GenbankRichSequenceDB genbank = new GenbankRichSequenceDB();

    for(int i = 0; i < args.length; i++) {

      try {

        System.err.println("Fetching: " + args[i]);
        RichSequence seq = genbank.getRichSequence(args[i]);
        RichSequence.IOTools.writeGenbank(System.out, seq, RichObjectFactory.getDefaultNamespace());
        
        System.err.println("Done.");

      } catch (Throwable t) {
        t.printStackTrace(System.err);
      }
    }
  }
}

