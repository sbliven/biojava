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
package org.biojava.bio.structure.io.mmcif;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.biojava.bio.structure.PDBHeader;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.io.mmcif.model.AtomSite;
import org.biojava.bio.structure.io.mmcif.model.DatabasePDBrev;
import org.biojava.bio.structure.io.mmcif.model.Entity;
import org.biojava.bio.structure.io.mmcif.model.Struct;

/** A MMcifConsumer implementation that build a in-memory representation of the 
 * content of a mmcif file as a BioJava Structure object.
 *  @author Andreas Prlic
 */

public class SimpleMMcifConsumer implements MMcifConsumer {

	Structure structure;

	public  SimpleMMcifConsumer(){
		structure = null;
	}

	public void newEntity(Entity entity) {
		System.out.println(entity);	
	}

	public void setStruct(Struct struct) {
		System.out.println(struct);

		PDBHeader header = structure.getPDBHeader();
		if ( header == null)
			header = new PDBHeader();
		header.setTitle(struct.getTitle());
		header.setIdCode(struct.getEntry_id());
		header.setDescription(struct.getPdbx_descriptor());
		System.out.println(struct.getPdbx_model_details());

		structure.setPDBHeader(header);
		structure.setPDBCode(struct.getEntry_id());
	}

	public void newAtomSite(AtomSite atom) {
		//System.out.println(atom);

	}

	public void documentStart() {
		structure = new StructureImpl();

	}

	public void documentEnd() {
		// end parsing a file
		System.out.println(structure);
	}



	public Structure getStructure() {
		// TODO Auto-generated method stub
		return null;
	}

	public void newDatabasePDBrev(DatabasePDBrev dbrev) {
		System.out.println("got a database revision:" + dbrev);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
		PDBHeader header = structure.getPDBHeader();
		if ( header == null) {
			header = new PDBHeader();
		}


		if (dbrev.getNum().equals("1")){

			try {
				
				String date = dbrev.getDate_original();
				Date dep = dateFormat.parse(date);
				header.setDepDate(dep);
				Date mod = dateFormat.parse(dbrev.getDate());
				header.setModDate(mod);
				
			} catch (ParseException e){
				e.printStackTrace();
			}		
		} else {
			try {
				
				Date mod = dateFormat.parse(dbrev.getDate());
				header.setModDate(mod);
		
			} catch (ParseException e){
				e.printStackTrace();
			}
		}

		structure.setPDBHeader(header);
	}

}
