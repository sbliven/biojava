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
 * Created on Jun 25, 2008
 * 
 */

package performance;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.NoSuchElementException;

import org.biojava.bio.BioException;
import org.biojava.bio.alignment.SequenceAlignment;
import org.biojava.bio.alignment.SmithWaterman;
import org.biojava.bio.alignment.SubstitutionMatrix;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.RichSequence.IOTools;


/**
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @date Jun 25, 2008
 * @since 1.7
 * @author Andreas Prlic
 */
public class SmithWatermanExample {

	/**
	 * This method computes a pairwise local alignment between two given sequences
	 * and prints the result on the standard output stream. The sequences must be
	 * genbank files and the substitution matrix must be defined for the same
	 * alphabet than both sequences.
	 *
	 * @param args
	 *          query sequence file (genbank), subject sequence file (genbank),
	 *          substitution matrix file

	 */
	public static void main(String[] args) {
		try {

			String file1 = "/file1.gb";
			String file2 = "/file2.gb";
			String file3 = "/dna.mat" ;

			long start  = System.currentTimeMillis();
			long maxMem = 0;

			UserDisplay display = new UserDisplay();
			display.setTitle("BioJava performance example - Smith Waterman");
			display.setVisible(true);

			StringBuffer txt = new StringBuffer();
			txt.append("<body>");
			txt.append( "<h1>BioJava performance example</h1>");
			txt.append("Align two sequences of approx 3000 nucleotides length using Smith Waterman.<br><br> ");

			txt.append("Reading sequence 1...<br> ");
			display.setText(txt + "</body>");	

			BufferedReader b1 = getReader(file1);

			RichSequenceIterator rsiQuery = IOTools.readGenbankDNA(b1,
					RichObjectFactory.getDefaultNamespace());

			txt.append("Reading sequence 2...<br> ");
			display.setText(txt + "</body>");

			BufferedReader b2 = getReader(file2);

			RichSequenceIterator rsiSubject = IOTools.readGenbankDNA(b2,
					RichObjectFactory.getDefaultNamespace());

			if (rsiQuery.hasNext() && rsiSubject.hasNext()) {
				RichSequence query = rsiQuery.nextRichSequence();
				RichSequence subject = rsiSubject.nextRichSequence();

				String matrix = getMatrxiFromFile(file3);

				txt.append("Using matrix: <pre>" + matrix + "</pre><br>");
				txt.append("Calculating SmithWaterman of <br>" );
				txt.append("query  : " +query.getDescription() +" (" + query.getName()+")<br>");
				txt.append("subject: " +subject.getDescription() +" (" + subject.getName()+")<br>");
				double match     = 0;
				double replace   = 5;
				double insert    = 2;
				double delete    = 2;
				double gapExtend = 1;
				txt.append("using penalties: match ("+match +"), replace ("+ replace+"),"+
						" insert ("+ insert +" ) delete (" + delete + ")," +
						" gapExtend (" +gapExtend +") <br>");
						
				display.setText(txt + "</body>");



				SubstitutionMatrix smax = new SubstitutionMatrix(
						(FiniteAlphabet) query.getAlphabet(), matrix,file3);

				long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				if (mem0 >  maxMem) 
					maxMem = mem0;	

				SequenceAlignment sa = new SmithWaterman(match, replace, insert, delete, gapExtend, smax);

				mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				if (mem0 >  maxMem) 
					maxMem = mem0;	

				sa.pairwiseAlignment(query, subject);

				mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				if (mem0 >  maxMem) 
					maxMem = mem0;	

				txt.append("done...<br> <pre>");
				txt.append(sa.getAlignmentString());

				long time = System.currentTimeMillis() - start;

				mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				if (mem0 >  maxMem) 
					maxMem = mem0;	

				txt.append("<pre>");

				txt.append( "total processing time: " + (time / 1000) + " sec. " + "<br>");
				txt.append( "mamimum memory: " + (maxMem /1024/1024) + " MB" + "<br>");


				display.setText(txt + "</body>");



			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		} catch (BioException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static BufferedReader getReader(String fileName) throws IOException{
		URL u = SmithWatermanExample.class.getResource(fileName);

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

	private static String getMatrxiFromFile(String fileName) throws IOException{
		BufferedReader reader = getReader(fileName);
		StringBuffer matString = new StringBuffer();

		while (reader.ready())
			matString.append(reader.readLine() + System.getProperty("line.separator"));
		reader.close();
		return matString.toString();

	}

}

