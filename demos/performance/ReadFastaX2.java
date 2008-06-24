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
 * Created on Jun 19, 2008
 * 
 */

package performance;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;


import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Feature.Template;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.Symbol;
import org.biojavax.Namespace;
import org.biojavax.RankedCrossRef;
import org.biojavax.RankedDocRef;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.BioEntryRelationship;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.io.FastaFormat;
import org.biojavax.bio.seq.io.RichSeqIOListener;
import org.biojavax.bio.taxa.NCBITaxon;

/** Demo class that reads all chromosomes of Drosophila and prints out their sizes.
 * See <a href="http://biojava.org/wiki/BioJava:Performance">http://biojava.org/wiki/BioJava:Performance</a>
 * 
 * @author Andreas Prlic
 * @since 1.7
 */
public class ReadFastaX2 {

	public static void main (String[] args){

		try {
			UserDisplay display = new UserDisplay();
			display.setTitle("BioJava performance example - BioJavaX");
			display.setVisible(true);
			
			String txt = "<body>";
			txt += "<h1>BioJava performance example</h1> Read all chromosomes of Drosophila and print the length of each using BioJavaX.<br> ";
			
			
			long start = System.currentTimeMillis();
			
			//		setup file input
			String fileName = "/dmel-all-chromosome-r5.8.fasta.gz";

			URL u = ReadFastaX2.class.getResource(fileName);
			System.out.println(u);
			
							
			txt += "reading " + fileName + " (47 MB)";
			System.out.println(txt);
			display.setText(txt + "</body>");
			
			URLConnection urlc = u.openConnection();
			
			InputStream inStream = urlc.getInputStream();
			if (inStream == null){
				System.err.println("could not find file " + fileName);
				txt += "could not find file " + fileName;
				display.setText(txt + "</body>");
				return;
			}

			GZIPInputStream gzip = new GZIPInputStream(inStream);

			InputStream is = new BufferedInputStream(gzip);
			BufferedInputStream bis = new BufferedInputStream(gzip);
			

			SimpleNamespace ns = null;

			ns = new SimpleNamespace("biojava");

			// You can use any of the convenience methods found in the BioJava 1.6 API
			RichSequenceIterator rsi = RichSequence.IOTools.readHashedFastaDNA(bis,ns);
			long total  = 0;
			long maxMem = 0;
			// Since a single file can contain more than a sequence, you need to iterate over
			// rsi to get the information.
			txt += "<table><tr><td><b>name</b></td><td><b>length</b></td></th>";
			while(rsi.hasNext()){
				RichSequence seq = rsi.nextRichSequence();
				total += seq.length();
				
				System.out.println(seq.getName() + "\t" + seq.length());
				txt += "<tr><td>" + seq.getName() + "</td><td>" + seq.length() + "</td></tr>";
				display.setText(txt + "</body>");
				
				long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				if (mem0 >  maxMem) 
					maxMem = mem0;	
			}
		
			txt += "</table>";
			
			txt += "Total length is " + total + "<br>";
			
			long time = System.currentTimeMillis() - start;
			
			txt += "total processing time: " + (time / 1000) + " sec. " + "<br>";
			txt += "mamimum memory: " + (maxMem /1024/1024) + " MB" + "<br>";
			display.setText(txt + "</body>");
			
			
		}
		catch(Exception be){
			be.printStackTrace();
			System.exit(-1);
		}

	}
}

	