package org.biojava.bio.structure;

import java.util.*;
import java.io.*;
import org.biojava.bio.structure.io.*;
import junit.framework.*;

/**
 *
 * @author Andreas Prlic
 * @since 1.5
 */


public class StructureTest extends TestCase {
    

    /** test if a PDB file can be parsed */
    public void testReadPDBFile() throws Exception {

	InputStream inStream = this.getClass().getResourceAsStream("/files/5pti.pdb");
        assertNotNull(inStream);


	PDBFileParser pdbpars = new PDBFileParser();
	Structure structure = pdbpars.parsePDBFile(inStream) ;

	assertNotNull(structure);
		
	assertTrue(structure.size() == 1);

	Chain c = structure.getChain(0);
	assertTrue(c.getGroups("amino").size()      == 58);
	assertTrue(c.getGroups("hetatm").size()     == 65);
	assertTrue(c.getGroups("nucelotide").size() == 0 );
    }

    

    /** Tests that standard amino acids are working properly */
    public void testStandardAmino() throws Exception {

	AminoAcid arg = StandardAminoAcid.getAminoAcid("ARG");
	assertTrue(arg.size() == 11 );
	
	AminoAcid gly = StandardAminoAcid.getAminoAcid("G");
	assertTrue(gly.size() == 4);

    }



}
