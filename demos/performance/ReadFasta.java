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


import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import java.util.zip.GZIPInputStream;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;


/** Read a FASTA file and print the length of all sequences in the file.
 * 
 * @author David Huen
 * @author Andreas Prlic
 *  @since 1.7
 *  @date Jun 19, 2008
 *
 */
public class ReadFasta {

	/**
	 *  The program takes two args: the first is the file name of the Fasta file.
	 *  The second is the name of the Alphabet. Acceptable names are DNA RNA or PROTEIN.
	 * @param args parameter array
	 **/
	public static void main(String[] args) {

		try {

			UserDisplay display = new UserDisplay();
			display.setTitle("BioJava performance example");
			display.setVisible(true);

			StringBuffer txt = new StringBuffer();
			txt.append("<body>");
			txt.append("<h1>BioJava performance example</h1> Read all chromosomes of Drosophila and print the length of each.<br> ");

			long start = System.currentTimeMillis();


//			setup file input
			String fileName = "/dmel-all-chromosome-r5.8.fasta.gz";
			URL u = ReadFasta.class.getResource(fileName);


			System.out.println(u);


			String alphabet = "DNA";

			txt.append("reading " + fileName + " (47 MB)");
			System.out.println(txt);
			display.setText(txt + "</body>");

			URLConnection urlc = u.openConnection();

			InputStream inStream = urlc.getInputStream();

			if (inStream == null){
				System.err.println("could not find file " + fileName);
				txt.append("could not find file " + fileName);
				display.setText(txt + "</body>");
				return;
			}
			display.setText(txt + "</body>");
			GZIPInputStream gzip = new GZIPInputStream(inStream);

			BufferedInputStream is = new BufferedInputStream(gzip);

			//get the appropriate Alphabet
			Alphabet alpha = AlphabetManager.alphabetForName(alphabet);

			//get a SequenceDB of all sequences in the file
			SequenceDB db = SeqIOTools.readFasta(is, alpha);

			long maxMem = 0;

			//list sequences and length
			SequenceIterator sI = db.sequenceIterator();
			long total = 0;
			txt.append("<table><tr><td><b>name</b></td><td><b>length</b></td></th>");
			while (sI.hasNext()) {
				Sequence seq = sI.nextSequence();
				System.out.println(seq.getName() + "\t" + seq.length());
				txt.append("<tr><td>" + seq.getName() + "</td><td>" + seq.length() + "</td></tr>");
				display.setText(txt + "</body>");
				total += seq.length();


				long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				if (mem0 >  maxMem) 
					maxMem = mem0;							

			}
			txt.append( "</table>");

			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			if (mem0 >  maxMem) 
				maxMem = mem0;	
			txt.append("Total length is " + total + "<br>");

			long time = System.currentTimeMillis() - start;



			txt.append( "total processing time: " + (time / 1000) + " sec. " + "<br>");
			txt.append( "mamimum memory: " + (maxMem /1024/1024) + " MB" + "<br>");

			display.setText(txt + "</body>");

		} catch (Exception ex) {
			ex.printStackTrace();			
			//not in fasta format or wrong alphabet
		}	
	}
}





