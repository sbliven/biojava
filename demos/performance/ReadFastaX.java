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


import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;

/** Read a FASTA file and print the length of all sequences in the file. 
 * Uses the slower biojavax implementation.
 * 
 * @author Andreas Prlic
 *  @since 1.7
 *  @date Jun 19, 2008
 */
public class ReadFastaX {

	public static void main (String[] args){

		try {
			UserDisplay display = new UserDisplay();
			display.setTitle("BioJava performance example - BioJavaX");
			display.setVisible(true);
			
			StringBuffer txt = new StringBuffer("<body>");
			txt.append("<h1>BioJava performance example</h1> Read all chromosomes of Drosophila and print the length of each using BioJavaX.<br> ");
			
			
			long start = System.currentTimeMillis();
			
			//		setup file input
			String fileName = "/dmel-all-chromosome-r5.8.fasta.gz";

			URL u = ReadFastaX.class.getResource(fileName);
			System.out.println(u);
			
							
			txt.append( "reading " + fileName + " (47 MB)");
			System.out.println(txt);
			display.setText(txt + "</body>");
			
			URLConnection urlc = u.openConnection();
			
			InputStream inStream = urlc.getInputStream();
			if (inStream == null){
				System.err.println("could not find file " + fileName);
				txt.append( "could not find file " + fileName);
				display.setText(txt + "</body>");
				return;
			}

			GZIPInputStream gzip = new GZIPInputStream(inStream);

			InputStream is = new BufferedInputStream(gzip);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			SimpleNamespace ns = null;

			ns = new SimpleNamespace("biojava");

			// You can use any of the convenience methods found in the BioJava 1.6 API
			RichSequenceIterator rsi = RichSequence.IOTools.readFastaDNA(br,ns);
			long total  = 0;
			long maxMem = 0;
			// Since a single file can contain more than a sequence, you need to iterate over
			// rsi to get the information.
			txt.append( "<table><tr><td><b>name</b></td><td><b>length</b></td></th>");
			while(rsi.hasNext()){
				RichSequence seq = rsi.nextRichSequence();
				total += seq.length();
				
				System.out.println(seq.getName() + "\t" + seq.length());
				txt.append( "<tr><td>" + seq.getName() + "</td><td>" + seq.length() + "</td></tr>");
				display.setText(txt + "</body>");
				
				long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				if (mem0 >  maxMem) 
					maxMem = mem0;	
			}
			txt.append( "</table>");
			
			txt.append( "Total length is " + total + "<br>");
			
			long time = System.currentTimeMillis() - start;
			
			txt.append( "total processing time: " + (time / 1000) + " sec. " + "<br>");
			txt.append("mamimum memory: " + (maxMem /1024/1024) + " MB" + "<br>");
			display.setText(txt + "</body>");
			
			
		}
		catch(Exception be){
			be.printStackTrace();
			System.exit(-1);
		}
	}
}
