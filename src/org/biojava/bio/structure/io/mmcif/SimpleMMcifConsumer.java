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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.biojava.bio.structure.AminoAcidImpl;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.AtomImpl;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.DBRef;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.HetatomImpl;
import org.biojava.bio.structure.NucleotideImpl;
import org.biojava.bio.structure.PDBHeader;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.structure.StructureTools;
import org.biojava.bio.structure.io.PDBParseException;
import org.biojava.bio.structure.io.mmcif.model.AtomSite;
import org.biojava.bio.structure.io.mmcif.model.DatabasePDBremark;
import org.biojava.bio.structure.io.mmcif.model.DatabasePDBrev;
import org.biojava.bio.structure.io.mmcif.model.Entity;
import org.biojava.bio.structure.io.mmcif.model.Exptl;
import org.biojava.bio.structure.io.mmcif.model.Struct;
import org.biojava.bio.structure.io.mmcif.model.StructRef;
import org.biojava.bio.structure.io.mmcif.model.StructRefSeq;

/** A MMcifConsumer implementation that build a in-memory representation of the 
 * content of a mmcif file as a BioJava Structure object.
 *  @author Andreas Prlic
 */

public class SimpleMMcifConsumer implements MMcifConsumer {

	boolean DEBUG = false;
	
	Structure structure;
	Chain current_chain;
	Group current_group;
	int atomCount;
	boolean parseCAOnly;
	List<Chain>   current_model;
	List<Entity> entities;
	List<StructRef> strucRefs;

	

	public  SimpleMMcifConsumer(){
		documentStart();

	}

	public boolean isParseCAOnly() {
		return parseCAOnly;
	}

	public void setParseCAOnly(boolean parseCAOnly) {
		this.parseCAOnly = parseCAOnly;
	}

	public void newEntity(Entity entity) {
		if (DEBUG)
			System.out.println(entity);	
		entities.add(entity);
	}

	public void setStruct(Struct struct) {
		//System.out.println(struct);

		PDBHeader header = structure.getPDBHeader();
		if ( header == null)
			header = new PDBHeader();
		header.setTitle(struct.getTitle());
		header.setIdCode(struct.getEntry_id());
		header.setDescription(struct.getPdbx_descriptor());
		//System.out.println(struct.getPdbx_model_details());

		structure.setPDBHeader(header);
		structure.setPDBCode(struct.getEntry_id());
	}

	/** initiale new group, either Hetatom or AminoAcid */
	private Group getNewGroup(String recordName,Character aminoCode1) {

		Group group;
		if ( recordName.equals("ATOM") ) {
			if (aminoCode1!=null) {

				AminoAcidImpl aa = new AminoAcidImpl() ;
				aa.setAminoType(aminoCode1);
				group = aa ;
			} else {
				// it is a nucleotidee
				NucleotideImpl nu = new NucleotideImpl();
				group = nu;
			}
		}
		else {
			group = new HetatomImpl();
		}
		//System.out.println("new group type: "+ group.getType() );
		return  group ;
	}
	/** test if the chain is already known (is in current_model
	 * ArrayList) and if yes, returns the chain 
	 * if no -> returns null
	 */
	private Chain isKnownChain(String chainID, List<Chain> chains){

		for (int i = 0; i< chains.size();i++){
			Chain testchain =  chains.get(i);
			//System.out.println("comparing chainID >"+chainID+"< against testchain " + i+" >" +testchain.getName()+"<");
			if (chainID.equals(testchain.getName())) {
				//System.out.println("chain "+ chainID+" already known ...");
				return testchain;
			}
		}

		return null;
	}


	/** during mmcif parsing the full atom name string gets truncated, fix this...
	 * 
	 * @param name
	 * @return
	 */
	private String fixFullAtomName(String name){

		if (name.equals("N")){
			return " N  ";
		}
		if (name.equals("CA")){
			return " CA ";
		} 
		if (name.equals("C")){
			return " C  ";
		}
		if (name.equals("O")){
			return " O  ";
		}
		if (name.equals("CB")){
			return " CB ";
		} 
		return name;
	}

	public void newAtomSite(AtomSite atom) {

		atomCount++;
		//TODO: add support for MAX_ATOMS

		String fullname = fixFullAtomName(atom.getLabel_atom_id());

		if ( parseCAOnly){
			// yes , user wants to get CA only
			// only parse CA atoms...
			if (! fullname.equals(" CA ")){
				//System.out.println("ignoring " + line);
				atomCount--;
				return;
			}
		}

		String chain_id      = atom.getLabel_asym_id();
		String recordName    = atom.getGroup_PDB();
		//String residueNumber = atom.getLabel_seq_id();
		String residueNumber = atom.getAuth_seq_id();
		String groupCode3    = atom.getLabel_comp_id();

		Character aminoCode1 = null;
		if ( recordName.equals("ATOM") )
			aminoCode1 = StructureTools.get1LetterCode(groupCode3);

		/*System.out.println(atom);
		System.out.print("chain " + chain_id);
		System.out.print(" record:" + recordName);
		System.out.print(" nr:"+ residueNumber);
		System.out.print(" group3:"+ groupCode3 );
		System.out.println(" amino1:"+aminoCode1);
		 */
		try {
			if (current_chain == null) {
				current_chain = new ChainImpl();
				current_chain.setName(chain_id);
			}
			if (current_group == null) {

				current_group = getNewGroup(recordName,aminoCode1);

				current_group.setPDBCode(residueNumber);
				current_group.setPDBName(groupCode3);
			}


			//System.out.println("chainid: >"+chain_id+"<, current_chain.id:"+ current_chain.getName() );
			// check if chain id is the same
			if ( ! chain_id.equals(current_chain.getName())){
				//System.out.println("end of chain: "+current_chain.getName()+" >"+chain_id+"<");

				// end up old chain...
				current_chain.addGroup(current_group);

				// see if old chain is known ...
				Chain testchain ;
				testchain = isKnownChain(current_chain.getName(),current_model);
				if ( testchain == null) {
					current_model.add(current_chain);		
				}


				//see if chain_id of new residue is one of the previous chains ...
				testchain = isKnownChain(chain_id,current_model);
				if (testchain != null) {
					//System.out.println("already known..."+ chain_id);
					current_chain = (ChainImpl)testchain ;

				} else {
					//System.out.println("creating new chain..."+ chain_id);

					//current_model.add(current_chain);
					current_chain = new ChainImpl();
					current_chain.setName(chain_id);
				}

				current_group = getNewGroup(recordName,aminoCode1);

				current_group.setPDBCode(residueNumber);
				current_group.setPDBName(groupCode3);
			}


			// check if residue number is the same ...
			// insertion code is part of residue number
			if ( ! residueNumber.equals(current_group.getPDBCode())) {	    
				//System.out.println("end of residue: "+current_group.getPDBCode()+" "+residueNumber);
				current_chain.addGroup(current_group);

				current_group = getNewGroup(recordName,aminoCode1);

				current_group.setPDBCode(residueNumber);
				current_group.setPDBName(groupCode3);

			}

			//see if chain_id is one of the previous chains ...

			Atom a = convertAtom(atom);

			current_group.addAtom(a);
			//System.out.println(current_group);

		} catch (PDBParseException e){
			e.printStackTrace();
		}

	}

	/** convert a MMCif AtomSite object to a BioJava Atom object
	 * 
	 * @param atom the mmmcif AtomSite record
	 * @return an Atom
	 */
	private Atom convertAtom(AtomSite atom){
		Atom a = new AtomImpl();
		a.setPDBserial(Integer.parseInt(atom.getId()));
		a.setName(atom.getLabel_atom_id());
		double x = Double.parseDouble (atom.getCartn_x());
		double y = Double.parseDouble (atom.getCartn_y());
		double z = Double.parseDouble (atom.getCartn_z());
		a.setX(x);
		a.setY(y);
		a.setZ(z);

		double occupancy = Double.parseDouble(atom.getOccupancy());
		a.setOccupancy(occupancy);

		a.setFullName(atom.getLabel_atom_id());
		String alt = atom.getLabel_alt_id();
		if (( alt != null ) && ( alt.length() > 0)){
			a.setAltLoc(new Character(alt.charAt(0)));
		} else {
			a.setAltLoc(new Character(' '));
		}
		return a;

	}

	public void documentStart() {
		structure = new StructureImpl();

		current_chain = null;
		current_group = null;
		atomCount = 0;
		parseCAOnly = false;
		current_model = new ArrayList<Chain>();
		entities = new ArrayList<Entity>();
		strucRefs = new ArrayList<StructRef>();

	}

	public void documentEnd() {
		// a problem occured earlier so current_chain = null ...
		// most likely the buffered reader did not provide data ...
		if ( current_chain != null ) {
			current_chain.addGroup(current_group);
			if (isKnownChain(current_chain.getName(),current_model) == null) {
				current_model.add(current_chain);
			}
		}

		structure.addModel(current_model);

		//TODO: add support for these:

		//structure.setConnections(connects);
		//structure.setCompounds(compounds);
		//structure.setDBRefs(dbrefs);

		//if ( alignSeqRes ){

		//SeqRes2AtomAligner aligner = new SeqRes2AtomAligner();
		//aligner.align(structure,seqResChains);
		//}

		//linkChains2Compound(structure);


	}


	/** This method will return the parsed protein structure, once the parsing has been finished
	 * 
	 * @return a BioJava protein structure object
	 */
	public Structure getStructure() {

		return structure;
	}

	public void newDatabasePDBrev(DatabasePDBrev dbrev) {
		//System.out.println("got a database revision:" + dbrev);
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

	public void newDatabasePDBremark(DatabasePDBremark remark) {
		//System.out.println(remark);
		String id = remark.getId();
		if (id.equals("2")){


			//this remark field contains the resolution information:
			String line = remark.getText();

			int i = line.indexOf("ANGSTROM");
			if ( i > 5) {
				// line contains ANGSTROM info...
				String resolution = line.substring(i-5,i).trim();
				// convert string to float
				float res = 99 ;
				try {
					res = Float.parseFloat(resolution);

				} catch (NumberFormatException e) {
					System.err.println(e.getMessage());
					System.err.println("could not parse resolution from line and ignoring it " + line);
					return ;


				}
				Map<String,Object> header = structure.getHeader();
				header.put("resolution",new Float(res));
				structure.setHeader(header);

				PDBHeader pdbHeader = structure.getPDBHeader();
				pdbHeader.setResolution(res);

			}

		}
	}

	public void newExptl(Exptl exptl) {

		PDBHeader pdbHeader = structure.getPDBHeader();
		pdbHeader.setTechnique(exptl.getMethod());
		Map<String,Object> header = structure.getHeader();
		header.put("technique",exptl.getMethod());

	}

	public void newStructRef(StructRef sref) {
		if (DEBUG)
			System.out.println(sref);
		strucRefs.add(sref);
	}

	private StructRef getStructRef(String ref_id){
		for (StructRef structRef : strucRefs) {
			if (structRef.getId().equals(ref_id)){
				return structRef;
			}

		}
		return null;

	}

	/** create a DBRef record from the StrucRefSeq record:
	 *  <pre>
  PDB record 					DBREF
  Field Name 					mmCIF Data Item 	 
  Section   	  				n.a.   	 
  PDB_ID_Code   	  			_struct_ref_seq.pdbx_PDB_id_code   	 
  Strand_ID   	 			 	_struct_ref_seq.pdbx_strand_id   	 
  Begin_Residue_Number   	  	_struct_ref_seq.pdbx_auth_seq_align_beg   	 
  Begin_Ins_Code   	  			_struct_ref_seq.pdbx_seq_align_beg_ins_code   	 
  End_Residue_Number   	  		_struct_ref_seq.pdbx_auth_seq_align_end   	 
  End_Ins_Code   	  			_struct_ref_seq.pdbx_seq_align_end_ins_code   	 
  Database   	  				_struct_ref.db_name   	 
  Database_Accession_No   	  	_struct_ref_seq.pdbx_db_accession   	 
  Database_ID_Code   	  		_struct_ref.db_code   	 
  Database_Begin_Residue_Number	_struct_ref_seq.db_align_beg   	 
  Databaes_Begin_Ins_Code   	_struct_ref_seq.pdbx_db_align_beg_ins_code   	 
  Database_End_Residue_Number  	_struct_ref_seq.db_align_end   	 
  Databaes_End_Ins_Code   	  	_struct_ref_seq.pdbx_db_align_end_ins_code
  </pre>   	  
	 * 
	 * 
	 */
	public void newStructRefSeq(StructRefSeq sref) {
		if (DEBUG)
			System.out.println(sref);
		DBRef r = new DBRef();

		
		if (DEBUG)
			System.out.println( " " + sref.getPdbx_PDB_id_code() + " " + sref.getPdbx_db_accession());
		r.setIdCode(sref.getPdbx_PDB_id_code());
		r.setDbAccession(sref.getPdbx_db_accession());
		r.setDbIdCode(sref.getPdbx_db_accession());
			
		
		//TODO: make DBRef chain IDs a string for chainIDs that are longer than one char...
		r.setChainId(new Character(sref.getPdbx_strand_id().charAt(0)));
		StructRef structRef = getStructRef(sref.getRef_id());
		r.setDatabase(structRef.getDb_name());
		r.setDbIdCode(structRef.getDb_code());

		
		int seqbegin = Integer.parseInt(sref.getPdbx_auth_seq_align_beg());
		int seqend   = Integer.parseInt(sref.getPdbx_auth_seq_align_end());
		Character begin_ins_code = new Character(sref.getPdbx_seq_align_beg_ins_code().charAt(0));
		Character end_ins_code   = new Character(sref.getPdbx_seq_align_beg_ins_code().charAt(0));

		if (begin_ins_code == '?')
			begin_ins_code = ' ';
		
		if (end_ins_code == '?')
			end_ins_code = ' ';
		
		r.setSeqBegin(seqbegin);
		r.setInsertBegin(begin_ins_code);

		r.setSeqEnd(seqend);
		r.setInsertEnd(end_ins_code);

		int dbseqbegin = Integer.parseInt(sref.getDb_align_beg());
		int dbseqend   = Integer.parseInt(sref.getDb_align_end());
		Character db_begin_in_code = new Character(sref.getPdbx_db_align_beg_ins_code().charAt(0));
		Character db_end_in_code   = new Character(sref.getPdbx_db_align_end_ins_code().charAt(0));

		if (db_begin_in_code == '?')
			db_begin_in_code = ' ';
		
		if (db_end_in_code == '?')
			db_end_in_code = ' ';
		
		
		r.setDbSeqBegin(dbseqbegin);
		r.setIdbnsBegin(db_begin_in_code);

		r.setDbSeqEnd(dbseqend);
		r.setIdbnsEnd(db_end_in_code);
		
		List<DBRef> dbrefs = structure.getDBRefs();
		if ( dbrefs == null)
			dbrefs = new ArrayList<DBRef>();
		dbrefs.add(r);
		
		if ( DEBUG)
			System.out.println(r.toPDB());
		
		structure.setDBRefs(dbrefs);

	}


}


