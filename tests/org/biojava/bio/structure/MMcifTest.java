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
 * created at Apr 26, 2008
 */
package org.biojava.bio.structure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.biojava.bio.structure.io.PDBFileParser;
import org.biojava.bio.structure.io.mmcif.MMcifConsumer;
import org.biojava.bio.structure.io.mmcif.MMcifParser;
import org.biojava.bio.structure.io.mmcif.SimpleMMcifConsumer;
import org.biojava.bio.structure.io.mmcif.SimpleMMcifParser;

import junit.framework.TestCase;

public class MMcifTest extends TestCase {

	public void testLoad(){
		String fileName = "/files/5pti.cif";
		InputStream inStream = this.getClass().getResourceAsStream(fileName);
		assertNotNull(inStream);

		MMcifParser parser = new SimpleMMcifParser();

		SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();

		parser.addMMcifConsumer(consumer);

		try {
			parser.parse(new BufferedReader(new InputStreamReader(inStream)));
		} catch (IOException e){
			fail(e.getMessage());
		}
		Structure cifStructure = consumer.getStructure();
		assertNotNull(cifStructure);


		// load the PDB file via the PDB parser
		Structure pdbStructure = null;
		InputStream pinStream = this.getClass().getResourceAsStream("/files/5pti.pdb");
		assertNotNull(inStream);

		PDBFileParser pdbpars = new PDBFileParser();
		try {
			pdbStructure = pdbpars.parsePDBFile(pinStream) ;
		} catch (IOException e) {
			e.printStackTrace();
		}

		assertNotNull(pdbStructure);


		// now compare the results
		try {
			//System.out.println(pdbStructure);
			//System.out.println(cifStructure);

			// compare amino acids in chain 1:
			Chain a_pdb = pdbStructure.getChainByPDB("A");
			Chain a_cif = cifStructure.getChainByPDB("A");

			String pdb_seq = a_pdb.getAtomSequence();
			String cif_seq = a_cif.getAtomSequence();

			assertEquals("the sequences obtained from PDB and mmCif don't match!",pdb_seq, cif_seq);

			List<DBRef> pdb_dbrefs= pdbStructure.getDBRefs();
			List<DBRef> cif_dbrefs= cifStructure.getDBRefs();
			
			assertEquals("nr of DBrefs found does not match!", pdb_dbrefs.size(),cif_dbrefs.size());
			
			DBRef p = pdb_dbrefs.get(0);
			DBRef c = cif_dbrefs.get(0);
			
						
			// gna: this does not work, since UNP and SWS is used inconsistently!
			//String pdb_dbref = p.toPDB();
			//String cif_dbref = c.toPDB();			
			//assertEquals("DBRef is not equal",pdb_dbref,cif_dbref);
			// therefore:

			assertEquals(p.getDatabase(), "UNP");
			assertEquals(c.getDatabase(), "SWS");
					
			assertEquals(p.getChainId(),c.getChainId());
			assertEquals(p.getDbAccession(),c.getDbAccession());
			assertEquals(p.getDbIdCode(),c.getDbIdCode());
			assertEquals(p.getDbSeqBegin(),c.getDbSeqBegin());
			assertEquals(p.getDbSeqEnd(),c.getDbSeqEnd());
			assertEquals(p.getId(),c.getId());
			assertEquals(p.getIdbnsBegin(),c.getIdbnsBegin());
			assertEquals(p.getIdbnsEnd(),c.getIdbnsEnd());
			assertEquals(p.getIdCode(),c.getIdCode());
			assertEquals(p.getInsertBegin(),c.getInsertBegin());
			assertEquals(p.getInsertEnd(),c.getInsertEnd());
			
		} catch (StructureException ex){
			fail(ex.getMessage());
		}

	}



}
