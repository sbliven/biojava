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

import java.io.BufferedReader;
import java.io.FileReader;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.Symbol;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;



/**
 * Counts the GC content of a sequence in fasta format
 * @author Mark Schreiber
 */ 
public class GCContent {
    
    /**
     * Run the program
     * @param args a fasta file
     */
    public static void main(String[] args)
        throws Exception
    {
        if (args.length != 1)
	    throw new Exception("usage: java seq.GCContent filename.fa");
	String fileName = args[0];
        BufferedReader br = new BufferedReader(new FileReader(fileName));
	RichSequenceIterator it = 
                RichSequence.IOTools.readFastaDNA(br, RichObjectFactory.getDefaultNamespace());
        
	// Iterate over all sequences in the stream
	while (it.hasNext()) {
	    Sequence seq = it.nextRichSequence();
	    System.out.println("Length: " + seq.length());
	    int gc = 0;
	    for (int pos = 1; pos <= seq.length(); ++pos) {
		Symbol sym = seq.symbolAt(pos);
		if (sym == DNATools.g() || sym == DNATools.c())
		    ++gc;
	    }
	    System.out.println(seq.getName() + ": " + 
			       ((gc * 100.0) / seq.length()) + 
			       " %");
	}
    }			       
}
