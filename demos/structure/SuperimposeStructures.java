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
 * created at Oct 27, 2007
 */
package structure;

import java.io.IOException;
import java.io.InputStream;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.align.StructurePairAligner;
import org.biojava.bio.structure.align.pairwise.AlternativeAlignment;
import org.biojava.bio.structure.gui.BiojavaJmol;
import org.biojava.bio.structure.io.PDBFileParser;


/** A demo that show how to calculate structure superimpositions
 * 
 * @author Andreas Prlic
 *
 */
public class SuperimposeStructures {

	public static void main(String[] args){
		SuperimposeStructures demo = new SuperimposeStructures();

		demo.run();

	}

	public void run(){
		
		// first load two example structures
		InputStream inStream1 = this.getClass().getResourceAsStream("/files/5pti.pdb");        
		InputStream inStream2 = this.getClass().getResourceAsStream("/files/1tap.pdb");

		Structure structure1 = null;
		Structure structure2 = null;

		PDBFileParser pdbpars = new PDBFileParser();
		try {
			structure1 = pdbpars.parsePDBFile(inStream1) ;
			structure2 = pdbpars.parsePDBFile(inStream2);

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// calculate structure superimposition for two complete structures
		StructurePairAligner aligner = new StructurePairAligner();

		
		try {
			// align the full 2 structures with default parameters.
			// see StructurePairAligner for more options and how to align
			// any set of Atoms
			aligner.align(structure1,structure2);

			AlternativeAlignment[] aligs = aligner.getAlignments();
			AlternativeAlignment a = aligs[0];          
			System.out.println(a);
			
			// display the alignment in Jmol
			
			// first get an artificial structure for the alignment
			Structure artificial = a.getAlignedStructure(structure1, structure2);
			
			
			// and then send it to Jmol (only will work if Jmol is in the Classpath) 
			BiojavaJmol jmol = new BiojavaJmol();
			jmol.setTitle(artificial.getName());
			jmol.setStructure(artificial);
						
			// color the two structures
			
			
			jmol.evalString("select *; backbone 0.4; wireframe off; spacefill off; " + 
					"select not protein and not solvent; spacefill on;");
			jmol.evalString("select */1 ; color red; model 1; ");
			
			
			// now color the equivalent residues ...
			
			String[] pdb1 = a.getPDBresnum1();
			for (String res : pdb1 ){		
				jmol.evalString("select " + res + "/1 ; backbone 0.6; color white;");
			}
			
			jmol.evalString("select */2; color blue; model 2;");
			String[] pdb2 = a.getPDBresnum2();
			for (String res :pdb2 ){
				jmol.evalString("select " + res + "/2 ; backbone 0.6; color yellow;");
			}
			
			
			// now show both models again.
			jmol.evalString("model 0;");

		} catch (StructureException e){
			e.printStackTrace();
		}
	}
}
