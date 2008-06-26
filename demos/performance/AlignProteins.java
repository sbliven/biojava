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


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.align.ClusterAltAligs;
import org.biojava.bio.structure.align.StructurePairAligner;
import org.biojava.bio.structure.align.pairwise.AlternativeAlignment;
import org.biojava.bio.structure.gui.util.AlternativeAlignmentFrame;
import org.biojava.bio.structure.io.PDBFileParser;


/** An example that aligns two protein structures.
 * @author Andreas Prlic
 *  @since 1.7
 *  @date Jun 23, 2008
 *
 */
public class AlignProteins {

	public static void main (String[] args){

		long start  = System.currentTimeMillis();
		long maxMem = 0;

		String fileName1 = "/2hhb.pdb.gz";
		String fileName2 = "/pdb2jho.ent.gz";

		UserDisplay display = new UserDisplay();
		display.setTitle("BioJava performance example");
		display.setVisible(true);

		StringBuffer txt = new StringBuffer();
		txt.append("<body>");
		txt.append( "<h1>BioJava performance example</h1>");
		txt.append("Superimpose the protein structures of Myoglobin (2jho) and Haemoglobin (2hhb)<br><br> ");
		
		txt.append("20 alternative alignments are calculated and clustered according to the similarity between the alignments.");
		txt.append("See how the alternative alignments of the first 4 clusters find the matches of Myoglobin to the different chains of Haemoglobin.<br><br>");
		
		txt.append("The algorithm is based on a variation of the PSC++ algorithm provided by Peter Lackner,  Univ. Salzburg.");
		txt.append("It calculates a a distance matrix based, rigid body protein structure superimposition.<br><br>");
		
		txt.append("Loading ... ");
		txt.append(fileName1);
		txt.append("<br>");
		display.setText(txt + "</body>");	

		try {
			Structure s1 = getStructureFromFile(fileName1);
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			if (mem0 >  maxMem) 
				maxMem = mem0;		

			txt.append("Loading ... " + fileName2 + "<br>");
			display.setText(txt + "</body>");	
			Structure s2 = getStructureFromFile(fileName2);
			mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			if (mem0 >  maxMem) 
				maxMem = mem0;		

			StructurePairAligner aligner = new StructurePairAligner();

			txt.append("Calculating alignment ... ");
			display.setText(txt + "</body>");	
			aligner.align(s1, s2);

			txt.append("done. <br>");

			long time = System.currentTimeMillis() - start;
			mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			if (mem0 >  maxMem) 
				maxMem = mem0;	


			txt.append( "total processing time: " + (time / 1000) + " sec. " + "<br>");
			//txt += "mamimum memory: " + (maxMem /1024/1024) + " MB" + "<br>";
			txt.append("now starting visualisation.<br><br>");
						
			display.setText(txt + "</body>");
			
			// visualisation:
			
			AlternativeAlignment[] aligs = aligner.getAlignments();
			ClusterAltAligs.cluster(aligs);
			
			AlternativeAlignmentFrame frame = new AlternativeAlignmentFrame(s1, s2);
			frame.setStructurePairAligner(aligner);
			frame.setAlternativeAlignments(aligs);
			frame.pack();
			frame.setVisible(true);
			
			frame.showAlternative(0);
			

			

			
		} catch (Exception e){
			txt.append("ERROR: " + e.getMessage());
			display.setText(txt + "</body>");
			e.printStackTrace();
		}

		return;

	}

	public static Structure getStructureFromFile(String fileName) throws IOException{

		URL u = AlignProteins.class.getResource(fileName);

		System.out.println(u);
		if (u == null){
			System.err.println("could not find file " + fileName);

			throw new IOException("could not find file " + fileName);
		}
		URLConnection urlc = u.openConnection();

		InputStream inStream = urlc.getInputStream();


		GZIPInputStream gzip = new GZIPInputStream(inStream);

		PDBFileParser parser = new PDBFileParser();
		parser.setAlignSeqRes(false);
		parser.setParseSecStruc(false);
		parser.setParseCAOnly(false); // we want all atom...
		return parser.parsePDBFile(gzip);



	}
}
