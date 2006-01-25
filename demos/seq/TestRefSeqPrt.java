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

package	seq;

import java.io.*;
import org.biojavax.Namespace;
import org.biojavax.RichAnnotation;
import org.biojavax.RichObjectFactory;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;


/**
 * Demo of reading RefSeqProtein
 * @author Matthew Pocock
 * @author Greg Cox
 * @author Mark Schreiber
 */
public class TestRefSeqPrt {
    
    /**
     * @param args a refseq protein file
     */
    public static void main(String [] args) {
        try {
            if(args.length !=	1) {
                throw new Exception("Use: seq.TestRefSeqPrt refseqFile");
            }
            
            File genbankFile = new File(args[0]);
            
            BufferedReader gReader = new BufferedReader(
                    new	InputStreamReader(new FileInputStream(genbankFile)));
            
            Namespace ns = (Namespace)RichObjectFactory.getObject(
                             SimpleNamespace.class, 
                             new Object[]{"ref"}
                          );
            
            RichSequenceIterator seqI =
                    RichSequence.IOTools.readGenbankProtein(gReader, ns);
            
            while(seqI.hasNext()) {
                RichSequence seq = seqI.nextRichSequence();
                System.out.println(
                        seq.toString() + 
                        " has " + seq.countFeatures() + " features");
                System.out.println("\tand is " + seq.length());
                
                // Annotation testing
		RichAnnotation theAnnotation = (RichAnnotation)seq.getAnnotation();
		java.util.Set notes = seq.getNoteSet();
		java.util.Iterator theOtherIterator = notes.iterator();
		while(theOtherIterator.hasNext())
		{
			System.out.println();
			Object note = theOtherIterator.next();
			System.out.println(note);
		}
            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
