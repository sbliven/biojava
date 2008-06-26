/*
 *                  BioJava development code
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
 * Created on Jun 23, 2008
 * 
 */

package performance;

import org.biojava.bio.seq.impl.RevCompSequence;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.io.FastaFormat;
import org.biojavax.bio.seq.io.FastaHeader;

import java.io.BufferedReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;

/** Read DNA sequence and write their reverse complement. 
 * This is based on the benchmark provided at:
 * http://shootout.alioth.debian.org/gp4/benchmark.php?test=revcomp&lang=all

 * 
 * @author Andy Yates
 * @author Andreas Prlic
 * @date Jun 23, 2008
 * @since 1.7
 */
public class ReverseComplement {
	
	public static void main(String[] args) throws Exception {
	
		String fastaLocation;
		if(args.length > 0) {
			fastaLocation = args[0];
		}
		else {
			fastaLocation = "/input.fasta";
		}
		
		UserDisplay display = new UserDisplay();
		display.setTitle("Reverse complement - BioJava");
		display.setVisible(true);

		StringBuffer txt = new StringBuffer("<body>");
		txt .append( "<h1>BioJava - Reverse Complement</h1>");
		txt.append( "Read DNA sequence and write their reverse complement.<br>");
		txt.append( "This is based on the benchmark  provided at:");
		txt.append( "http://shootout.alioth.debian.org/gp4/benchmark.php?test=revcomp&amp;lang=all <br>");
		txt.append( " in short the rules are :    <br>"); 
		txt.append( "<ul><li> read line-by-line a redirected FASTA format file from stdin</li>");
		txt.append( "<li>for each sequence:");
		txt.append( "<ul>write the id, description, and the reverse-complement sequence in FASTA format to stdout</ul></li></ul>");

		txt.append( "Loading ... " + fastaLocation + "<br>");
		display.setText(txt + "</body>");	
		
		long start = System.currentTimeMillis();
		
		FastaHeader fastaHeader = new FastaHeader();
		fastaHeader.setShowAccession(true);
		fastaHeader.setShowDescription(false);
		fastaHeader.setShowIdentifier(false);
		fastaHeader.setShowName(false);
		fastaHeader.setShowNamespace(false);
		fastaHeader.setShowVersion(false);

		FastaFormat fastaFormat = new FastaFormat();
		fastaFormat.setHeader(fastaHeader);
		fastaFormat.setLineWidth(60);

		BufferedReader br = getReader(fastaLocation);
		RichSequenceIterator iter = RichSequence.IOTools.readFastaDNA(br, null);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();		
		PrintStream ps = new PrintStream(baos);

		
		txt.append( "Output:<br>");
		display.setText(txt + "</body>");	

		
		while(iter.hasNext()) {
			RichSequence seq = iter.nextRichSequence();
			RevCompSequence rev = new RevCompSequence(seq);
			rev.setName(seq.getAccession()+" "+seq.getDescription());
			fastaFormat.writeSequence(rev, ps);			
		}
		
		
		txt.append( "<br><pre>" + baos.toString() +"<pre><br>");		
		long time = System.currentTimeMillis() - start;
		txt.append( "total processing time: " + (time ) + " milli sec. " + "<br><br>");
		txt.append( " BioJava provides a very fast and competetive implementation of this benchmark problem.<br>");
		
		display.setText(txt + "</body>");
		
		
	}
	
	private static BufferedReader getReader(String fileName) throws IOException{
		URL u = ReverseComplement.class.getResource(fileName);

		System.out.println(u);
		if (u == null){
			System.err.println("could not find file " + fileName);

			throw new IOException("could not find file " + fileName);
		}
		URLConnection urlc = u.openConnection();

		InputStream inStream = urlc.getInputStream();
		
		BufferedReader r = new BufferedReader(new InputStreamReader(inStream));
		return r;
		
		
	}
}