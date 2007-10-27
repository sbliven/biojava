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
import org.biojava.bio.structure.io.PDBFileParser;
import org.biojava.bio.structure.io.PDBFileReader;


/** a simple demo to demonstrating how to load a PDB file
 * 
 * @author Andreas Prlic
 *
 */
public class LoadStructure {

	public static void main(String[] args){

		LoadStructure demo = new LoadStructure();

		if (args.length == 0){
			demo.readStructureFromStream();
		} else {
			// the user provided a Path to a PDB file
			demo.loadStructure(args[0]);
		}
		
		demo.loadStructureById();

	}

	
	/** access a structure from a directory by using a PDB code.
	 * The PDBFileReader class takes care of compressed PDB files
	 * 
	 * @param pdbCode
	 * @return
	 */
	public Structure loadStructureById() {
		String path = "/path/to/PDB/directory";

		PDBFileReader pdbreader = new PDBFileReader();
		pdbreader.setPath(path);
		Structure structure = null;
		try {
			structure = pdbreader.getStructureById("5pti");
		} catch (IOException e){
			e.printStackTrace();
		}
		return structure;
		
	}
	
	
	public Structure loadStructure(String pathToPDBFile){
		PDBFileReader pdbreader = new PDBFileReader();

		Structure structure = null;
		try{
			structure = pdbreader.getStructure(pathToPDBFile);
			System.out.println(structure);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return structure;
	}

	public  Structure readStructureFromStream() {

		InputStream inStream = this.getClass().getResourceAsStream("/files/5pti.pdb");

		Structure structure = null;
		PDBFileParser pdbpars = new PDBFileParser();
		try {
			structure = pdbpars.parsePDBFile(inStream) ;
			System.out.println(structure);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return structure;

	}

}
