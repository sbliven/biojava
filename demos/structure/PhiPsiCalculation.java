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

import java.util.List;

import org.biojava.bio.structure.AminoAcid;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;

/** a demo showing how to calculate Phi and Psi angles for a protein structure.
 * also shows how to access the underlying data
 * 
 * @author Andreas Prlic
 *
 */
public class PhiPsiCalculation {

	public static void main(String[] args){
		LoadStructure demo1 = new LoadStructure();
		Structure structure = demo1.readStructureFromStream();

		PhiPsiCalculation demo2 = new PhiPsiCalculation();
		demo2.calcPhiPsi(structure);
	}

	
	/** calculate Phi and Psi angles for the AminoAcids in the first
	 * chain of a structure
	 * 
	 * @param structure
	 */
	public void calcPhiPsi(Structure structure){


		// get the first chain from the structure

		Chain chain  = structure.getChain(0);

		// A protein chain consists of a number of groups. These can be either
		// AminoAcids, Hetatom or Nucleotide groups.
		//
		// Note: BioJava provides access to both the ATOM and SEQRES data in a PDB file.
		// since we are interested in doing calculations here, we only request the groups 
		// from the ATOM records

		//  get the Groups of the chain that are AminoAcids.
		List<Group> groups = chain.getAtomGroups("amino");

		AminoAcid a;
		AminoAcid b;
		AminoAcid c ;

		for ( int i=0; i < groups.size(); i++){

			// since we requested only groups of type "amino" they will always be amino acids
			// Nucleotide and Hetatom groups will not be present in the groups list.

			b = (AminoAcid)groups.get(i);

			double phi =360.0;
			double psi =360.0;

			if ( i > 0) {
				a = (AminoAcid)groups.get(i-1) ;
				try {

					// the Calc class provides utility methods for various calculations on
					// structures, groups and atoms

					phi = Calc.getPhi(a,b);	   			   
				} catch (StructureException e){		    
					e.printStackTrace();
					phi = 360.0 ;
				}
			}
			if ( i < groups.size()-1) {
				c = (AminoAcid)groups.get(i+1) ;
				try {
					psi = Calc.getPsi(b,c);
				}catch (StructureException e){
					e.printStackTrace();
					psi = 360.0 ;
				}
			}

			System.out.print(b.getPDBCode() + " " + b.getPDBName() + ":"  );

			System.out.println(String.format("\tphi: %+7.2f psi: %+7.2f", phi, psi));

		}
	}
}