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
 * Created on May 23, 2008
 * 
 */

package structure;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.gui.BiojavaJmol;
import org.biojava.bio.structure.io.PDBFileReader;

/** A simple utility class that checks if JMol is installed, and if yes loads the PDB file that has been given as an argument
 * 
 * @author Andreas Prlic
 * @since 1.7
 */
public class PDBView {

	public static void main(String[] args){
		if ( (args.length < 1) ||
				( ! BiojavaJmol.jmolInClassPath()
				) ) {
			printHelp();
			return;
		}
		
		String pdbFile = args[0];
		
		PDBView view = new PDBView();
		view.show(pdbFile);
	}
	
	public static void printHelp(){
		System.out.println("Usage: PDBView <pdbfilename>");
		System.out.println("Make sure that Jmol is installed in your classpath.");
	}
	
	public void show(String pdbFileName){
		PDBFileReader pdbreader = new PDBFileReader();
		 
		try {
			Structure pdb = pdbreader.getStructure(pdbFileName);
			BiojavaJmol jmol = new BiojavaJmol();
			jmol.setStructure(pdb);
			jmol.evalString("select * ; color chain;");
			jmol.evalString("select *; spacefill off; wireframe off; backbone 0.4;  ");

		} catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
}
