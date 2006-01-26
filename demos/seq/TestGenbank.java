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
import java.util.*;
import org.biojavax.Namespace;
import org.biojavax.Note;
import org.biojavax.RichAnnotation;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;




/**
 * Demo GenBank reading
 * @author Matthew Pocock
 * @author Mark Schreiber
 * @author Richard Holland
 */ 
public class TestGenbank {
    public static void main(String [] args) {
        try {
            if(args.length !=	1) {
                throw new Exception("Use: seq.TestGenbank genbankFile");
            }
            
            File genbankFile = new File(args[0]);
            BufferedReader gReader = new BufferedReader(
                    new	InputStreamReader(new FileInputStream(genbankFile)));
            
            Namespace ns = RichObjectFactory.getDefaultNamespace();
            RichSequenceIterator seqI =
                    RichSequence.IOTools.readGenbankDNA(gReader, ns);
            
            while(seqI.hasNext()) {
                RichSequence seq = seqI.nextRichSequence();
                System.out.println(
                        seq.toString() +
                        " has " + seq.countFeatures() + 
                        " features");
                
                for(Iterator i = seq.features(); i.hasNext(); ) {
                    RichFeature f = (RichFeature) i.next();
                    System.out.println(
                            "\t" + f.getType() + 
                            "\t" + f.getLocation() + 
                            "\t" + f.getAnnotation().asMap());
                }
                
                
                
                // Annotation testing
               RichAnnotation theAnnotation = (RichAnnotation)seq.getAnnotation();
               Iterator notesIterator = theAnnotation.getNoteSet().iterator();
               while (notesIterator.hasNext()) {
                   System.out.println();
                   Note note = (Note)notesIterator.next();
                   System.out.println(note);
               }
                
                System.out.println("\n\nRE-GENERATING GENBANK");
                RichSequence.IOTools.writeGenbank(System.out, seq, ns);
            }
            
            
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
