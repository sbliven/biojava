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
package remotealignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;

import org.biojava.bio.BioException;
import org.biojavax.SimpleNamespace;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;

import org.biojavax.bio.alignment.blast.RemoteQBlastService;
import org.biojavax.bio.alignment.blast.RemoteQBlastAlignmentProperties;
import org.biojavax.bio.alignment.blast.RemoteQBlastOutputProperties;
import org.biojavax.bio.alignment.blast.RemoteQBlastOutputFormat;

public class RemoteQBlastServiceTest {
	/**
	 * The program take only a string with a path toward a sequence file
	 * 
	 * For this example, I keep it simple with a single FASTA formatted file
	 * 
	 */
	public static void main(String[] args) {

		RemoteQBlastService rbw;
		RemoteQBlastOutputProperties rof;
		InputStream is;
		ArrayList<String> rid = new ArrayList<String>();
		String request = "";

		try {
			rbw = new RemoteQBlastService();
			SimpleNamespace ns = new SimpleNamespace("bj_blast");
			RichSequenceIterator rs = RichSequence.IOTools.readFastaDNA(
					new BufferedReader(new FileReader(args[0])), ns);

			/*
			 * You would imagine that one would blast a bunch of sequences of
			 * identical nature with identical parameters...
			 */
			RemoteQBlastAlignmentProperties rqb = new RemoteQBlastAlignmentProperties();
			rqb.setBlastProgram("blastn");
			rqb.setBlastDatabase("nr");

			/*
			 * First, let's send all the sequences to the QBlast service and
			 * keep the RID for fetching the results at some later moments
			 * (actually, in a few seconds :-))
			 */
			while (rs.hasNext()) {

				RichSequence rr = rs.nextRichSequence();
				request = rbw.sendAlignmentRequest(rr, rqb);
				rid.add(request);
			}

			/*
			 * Let's check that our requests have been processed. If completed,
			 * let's look at the alignments with my own selection of output and
			 * alignment formats.
			 */
			for (String aRid : rid) {
				System.out.println("trying to get BLAST results for RID "
						+ aRid);
				boolean wasBlasted = false;

				while (!wasBlasted) {
					wasBlasted = rbw.isReady(aRid, System.currentTimeMillis());
				}

				rof = new RemoteQBlastOutputProperties();
				rof.setOutputFormat(RemoteQBlastOutputFormat.XML);
				rof.setAlignmentOutputFormat(RemoteQBlastOutputFormat.QUERY_ANCHORED);
				rof.setDescriptionNumber(10);
				rof.setAlignmentNumber(10);

				Set<String> test = rof.getOutputOptions();
				
				for(String str : test){
					System.out.println(str);
				}
				
				is = rbw.getAlignmentResults(request, rof);

				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));

				String line = null;

				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}

			}
		}
		/*
		 * What happens if the file can't be read
		 */
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
		/*
		 * What happens if the file is not a FASTA file
		 */
		catch (BioException bio) {
			bio.printStackTrace();
		}
	}
}