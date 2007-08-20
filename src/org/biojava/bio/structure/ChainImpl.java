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
 * Created on 12.03.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.bio.structure;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biojava.bio.Annotation;

/**
 * A Chain in a PDB file. It contains several groups which can be of
 * type "amino", "hetatm", "nucleotide".
 * @author Andreas Prlic
 * @author Jules Jacobsen
 * @since 1.4
 */
public class ChainImpl implements Chain {

	public static String DEFAULT_CHAIN_ID = " ";

	String swissprot_id ; 
	String name ; // like in PDBfile
	List <Group> groups;
	Annotation annotation ;

	List<Group> seqResGroups;

	String seqRes;
	String molId;
	Compound mol;
	String organismScientific;
	String molName;

	Map<String, Integer> pdbResnumMap;
	/**
	 *  Constructs a ChainImpl object.
	 */
	public ChainImpl() {
		super();

		name = DEFAULT_CHAIN_ID;
		groups = new ArrayList<Group>() ;
		annotation = Annotation.EMPTY_ANNOTATION;
		seqResGroups = new ArrayList<Group>();
		pdbResnumMap = new HashMap<String,Integer>();

	}


	/** returns an identical copy of this Chain .
	 * @return an identical copy of this Chain 
	 */
	public Object clone() {
		// go through all groups and add to new Chain.
		Chain n = new ChainImpl();
		// copy chain data:

		n.setName( getName());
		n.setSwissprotId ( getSwissprotId());
		for (int i=0;i<groups.size();i++){
			Group g = (Group)groups.get(i);
			n.addGroup((Group)g.clone());
		}
		n.setSeqRes(this.getSeqRes());
		n.setMolId(this.getMolId());
		n.setHeader(this.getHeader());
		n.setOrganismScientific(this.getOrganismScientific());
		return n ;
	}


	public void setAnnotation(Annotation anno){
		annotation = anno;
	}

	public Annotation getAnnotation(){
		return annotation;
	}

	public void setHeader(Compound mol) {
		this.mol = mol;
	}

	public Compound getHeader() {
		return this.mol;
	}

	/** set the Swissprot id of this chains .
	 * @param sp_id  a String specifying the swissprot id value
	 * @see #getSwissprotId
	 */

	public void setSwissprotId(String sp_id){
		swissprot_id = sp_id ;
	}

	/** get the Swissprot id of this chains .
	 * @return a String representing the swissprot id value
	 * @see #setSwissprotId
	 */
	public String getSwissprotId() {
		return swissprot_id ;
	}


	public void addGroup(Group group) {

		group.setParent(this);

		groups.add(group);

		// store the position internally for quick access of this group
		String pdbResnum = group.getPDBCode();
		if ( pdbResnum != null) {
			Integer pos = new Integer(groups.size()-1);
			// ARGH sometimes numbering in PDB files is confusing.
			// e.g. PDB: 1sfe 
			/*
			 * ATOM    620  N   GLY    93     -24.320  -6.591   4.210  1.00 46.82           N  
			 * ATOM    621  CA  GLY    93     -24.960  -6.849   5.497  1.00 47.35           C  
			 * ATOM    622  C   GLY    93     -26.076  -5.873   5.804  1.00 47.24           C  
			 * ATOM    623  O   GLY    93     -26.382  -4.986   5.006  1.00 47.56           O  
             and ...
			 * HETATM 1348  O   HOH    92     -21.853 -16.886  19.138  1.00 66.92           O  
			 * HETATM 1349  O   HOH    93     -26.126   1.226  29.069  1.00 71.69           O  
			 * HETATM 1350  O   HOH    94     -22.250 -18.060  -6.401  1.00 61.97           O 
			 */

			// this check is to give in this case the entry priority that is an AminoAcid / comes first...
			if (  pdbResnumMap.containsKey(pdbResnum)) {
				if ( group instanceof AminoAcid)
					pdbResnumMap.put(pdbResnum,pos);
			} else                
				pdbResnumMap.put(pdbResnum,pos);
		}

	}

	/** return the amino acid at position .
	 * 
	 *
	 * @param position  an int
	 * @return a Group object
	 */
	public Group getGroup(int position) {

		return (Group)groups.get(position);
	}

	/** return an array of all groups of a special type (e.g. amino,
	 * hetatm, nucleotide).
	 * @param type  a String
	 * @return an List object containing the groups of type...

	 */
	public List<Group> getGroups( String type) {
		List<Group> tmp = new ArrayList<Group>() ;
		for (int i=0;i<groups.size();i++){
			Group g = (Group)groups.get(i);
			if (g.getType().equals(type)){
				tmp.add(g);
			}
		}
		//Group[] g = (Group[])tmp.toArray(new Group[tmp.size()]);
		return tmp ;
	}

	/** return all groups of this chain .
	 * @return an ArrayList object representing the Groups of this Chain.
	 */
	public List<Group> getGroups(){
		return groups ;
	}





	public Group getGroupByPDB(String pdbresnum) throws StructureException {
		if ( pdbResnumMap.containsKey(pdbresnum)) {
			Integer pos = (Integer) pdbResnumMap.get(pdbresnum);
			return (Group) groups.get(pos.intValue());
		} else {
			throw new StructureException("unknown PDB residue number " + pdbresnum + " in chain " + name);
		}

	}

	public Group[] getGroupsByPDB(String pdbresnumStart, String pdbresnumEnd) 
	throws StructureException {

		List<Group> retlst = new ArrayList<Group>();

		Iterator iter = groups.iterator();
		boolean adding = false;
		boolean foundStart = false;

		while ( iter.hasNext()){
			Group g = (Group) iter.next();
			if ( g.getPDBCode().equals(pdbresnumStart)) {
				adding = true;
				foundStart = true;
			}

			if ( adding)
				retlst.add(g);

			if ( g.getPDBCode().equals(pdbresnumEnd)) {
				if ( ! adding)
					throw new StructureException("did not find start PDB residue number " + pdbresnumStart + " in chain " + name);
				adding = false;
				break;
			}
		}

		if ( ! foundStart){
			throw new StructureException("did not find start PDB residue number " + pdbresnumStart + " in chain " + name);
		}
		if ( adding) {
			throw new StructureException("did not find end PDB residue number " + pdbresnumEnd + " in chain " + name);
		}

		return (Group[]) retlst.toArray(new Group[retlst.size()] );
	}



	public int getLength() {return groups.size();  }


	public int getLengthAminos() {

		List g = getGroups("amino");
		return g.size() ;
	}

	public int getLengthSeqRes() {
		//new method returns the length of the sequence defined in the SEQRES records
		String g = this.getSeqRes();
		return g.length();
	}

	/** get and set the name of this chain (Chain id in PDB file ).
	 * @param nam a String specifying the name value
	 * @see #getName
	 * 
	 */

	public void   setName(String nam) { name = nam;   }

	/** get and set the name of this chain (Chain id in PDB file ).
	 * @return a String representing the name value
	 * @see #setName
	 */
	public String getName()           {	return name;  }


	/** string representation. */
	public String toString(){
		String newline = System.getProperty("line.separator");
		String str = "Chain >"+getName() + "< total length:" + getLength() + " residues";
		// loop over the residues

		for ( int i = 0 ; i < groups.size();i++){
			Group gr = (Group) groups.get(i);
			str += gr.toString() + newline ;
		} 
		return str ;

	}

	/** get amino acid sequence.
	 * @return a String representing the sequence.	    
	 */
	public String getSequence(){

		List aminos = getGroups("amino");
		StringBuffer sequence = new StringBuffer() ;
		for ( int i=0 ; i< aminos.size(); i++){
			AminoAcid a = (AminoAcid)aminos.get(i);
			sequence.append( a.getAminoType());
		}

		String s = sequence.toString();

		return s ;
	}
	public void setSeqRes(String seq) {
		this.seqRes = seq;
	}

	public String getSeqRes() {
		String s = "";
		StringBuffer sequence = null;
		try {
			List aminos = getSeqResGroups("amino");
			//System.out.println(aminos);
			sequence = new StringBuffer();
			for (int i = 0; i < aminos.size(); i++) {
				AminoAcid a = (AminoAcid) aminos.get(i);
				if (a.getRecordType().equals("SEQRES")) {
					//System.out.println(a.toString());
					sequence.append(a.getAminoType());
				}
				s = sequence.toString();
			}
		} catch (Exception e) {
			return s;
		}
		return s;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public void setOrganismScientific(String os) {
		this.organismScientific = os;
		//To change body of implemented methods use File | Settings | File Templates.
	}


	public String getOrganismScientific() {
		return organismScientific;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public void setMolName(String moleculeName){
		this.molName = moleculeName;
	}

	public String getMolName(){
		return molName;
	}

	public void setMolId(String molId) {
		this.molId = molId;
	}

	public String getMolId() {
		return this.molId;
	}

	public void addSeqResGroup(Group group) {
		//To change body of implemented methods use File | Settings | File Templates.
		group.setParent(this);

		seqResGroups.add(group);

		// store the position internally for quick access of this group
		String pdbResnum = group.getPDBCode();
		if (pdbResnum != null) {
			Integer pos = new Integer(seqResGroups.size() - 1);
			// ARGH sometimes numbering in PDB files is confusing.
			// e.g. PDB: 1sfe
			/*
			 * ATOM    620  N   GLY    93     -24.320  -6.591   4.210  1.00 46.82           N
			 * ATOM    621  CA  GLY    93     -24.960  -6.849   5.497  1.00 47.35           C
			 * ATOM    622  C   GLY    93     -26.076  -5.873   5.804  1.00 47.24           C
			 * ATOM    623  O   GLY    93     -26.382  -4.986   5.006  1.00 47.56           O
            and ...
			 * HETATM 1348  O   HOH    92     -21.853 -16.886  19.138  1.00 66.92           O
			 * HETATM 1349  O   HOH    93     -26.126   1.226  29.069  1.00 71.69           O
			 * HETATM 1350  O   HOH    94     -22.250 -18.060  -6.401  1.00 61.97           O
			 */

			// this check is to give in this case the entry priority that is an AminoAcid / comes first...
			if (pdbResnumMap.containsKey(pdbResnum)) {
				if (group instanceof AminoAcid)
					pdbResnumMap.put(pdbResnum, pos);
			} else
				pdbResnumMap.put(pdbResnum, pos);
		}
	}

	public Group getSeqResGroup(int position) {

        return seqResGroups.get(position);
   }

   public List<Group> getSeqResGroups(String type) {
	   //TODO: this is missing still
       return seqResGroups;
   }

   public List<Group> getSeqResGroups() {
       return seqResGroups;
   }

   public Group getSeqResGroupByPDB(String pdbresnum) throws StructureException {
       return null; 
   }

   public Group[] getSeqResGroupsByPDB(String pdbresnumStart, String pdbresnumEnd) throws StructureException {
       return new Group[0];  
   }
}
