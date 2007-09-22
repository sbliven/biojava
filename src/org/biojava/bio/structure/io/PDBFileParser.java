/*
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
 * Created on 16.03.2004
 *
 */
package org.biojava.bio.structure.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.structure.AminoAcid;
import org.biojava.bio.structure.AminoAcidImpl;
import org.biojava.bio.structure.AtomImpl;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.DBRef;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.GroupIterator;
import org.biojava.bio.structure.HetatomImpl;
import org.biojava.bio.structure.Compound;
import org.biojava.bio.structure.NucleotideImpl;
import org.biojava.bio.structure.PDBHeader;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;





/**
 * A PDB file parser.
 * @author Andreas Prlic
 * @author Jules Jacobsen
 * @since 1.4
 * 
 * <p>
 * Q: How can I get a Structure object from a PDB file?
 * </p>
 * <p>
 * A:
 * <pre>
 String filename =  "path/to/pdbfile.ent" ;

 PDBFileReader pdbreader = new PDBFileReader();

 try{
 Structure struc = pdbreader.getStructure(filename);
 System.out.println(struc);
 } catch (Exception e) {
 e.printStackTrace();
 }
 </pre>
 *
 * 
 */
public class PDBFileParser  {

	private final boolean DEBUG = false;

	// required for parsing:
	private Structure     structure;
	private List<Chain>   current_model; // contains the ATOM records for each model
	private Chain         current_chain;
	private Group         current_group;

	private List<Chain>   seqResChains; // contains all the chains for the SEQRES records

	// for conversion 3code 1code
	private static  SymbolTokenization threeLetter ;
	private static  SymbolTokenization oneLetter ;

	// for printing
	private static final String NEWLINE;

	private Map <String,Object>  header ;
	private PDBHeader pdbHeader;
	private List<Map<String, Integer>> connects ;
	private List<Map<String,String>> helixList;
	private List<Map<String,String>> strandList;
	private List<Map<String,String>> turnList;

	private int lengthCheck ;

	private Compound current_compound;
	private List<Compound> compounds = new ArrayList<Compound>();
	private List<DBRef> dbrefs;

	// for parsing COMPOUND and SOURCE Header lines
	private int molTypeCounter = 1;
	//private int continuationNo;
	private String continuationField;
	private String continuationString = "";
	private DateFormat dateFormat;

	private static  final List<String> compndFieldValues = new ArrayList<String>(
			Arrays.asList(
					"MOL_ID:", "MOLECULE:", "CHAIN:", "SYNONYM:",
					"EC:", "FRAGMENT:", "ENGINEERED:", "MUTATION:",
					"BIOLOGICAL_UNIT:", "OTHER_DETAILS:"));


	private static final List<String> ignoreCompndFieldValues = new ArrayList<String>(
			Arrays.asList("HETEROGEN:","ENGINEEREED:","FRAGMENT,",
					"MUTANT:","SYNTHETIC:"));
	// ENGINEEREED in pdb219d

	boolean parseSecStruc;
	boolean alignSeqRes;

	public static final String PDB_AUTHOR_ASSIGNMENT = "PDB_AUTHOR_ASSIGNMENT";
	public static final String HELIX  = "HELIX";
	public static final String STRAND = "STRAND";
	public static final String TURN   = "TURN";

	// there is a file format change in PDB 3.0 and nucleotides are being renamed

	static private Map<String, Integer> nucleotides30 ;
	static private Map<String, Integer> nucleotides23 ;

	int atomCount;
	/** the maximum number of atoms that will be parsed before the parser switches to a CA-only 
	 * representation of the PDB file. If this limit is exceeded also the SEQRES groups will be 
	 * ignored.
	 */
	public static final int ATOM_CA_THRESHOLD = 500000; 

	/**  the maximum nuber of atoms we will add to a structure
     this protects from memory overflows in the few eally big protein structures.
	 */
	public static final int MAX_ATOMS = 700000; // tested with java -Xmx300M

	private boolean atomOverflow;

	/** set the flag to only read in Ca atoms - this is useful for parsing large structures like 1htq
	 * 
	 */
	public boolean parseCAOnly;


	static {
		nucleotides30 = new HashMap<String,Integer>();
		nucleotides30.put("DA",1);
		nucleotides30.put("DC",1);
		nucleotides30.put("DG",1);
		nucleotides30.put("DT",1);
		nucleotides30.put("DI",1);
		nucleotides30.put("A",1);
		nucleotides30.put("G",1);
		nucleotides30.put("C",1);
		nucleotides30.put("U",1);
		nucleotides30.put("I",1);

		//TODO: check if they are always HETATMs, in that case this will not be necessary
		// the DNA linkers - the +C , +G, +A  +T +U and +I have been replaced with these:
		nucleotides30.put("TAF",1); // 2'-DEOXY-2'-FLUORO-ARABINO-FURANOSYL THYMINE-5'-PHOSPHATE
		nucleotides30.put("TC1",1); // 3-(5-PHOSPHO-2-DEOXY-BETA-D-RIBOFURANOSYL)-2-OXO-1,3-DIAZA-PHENOTHIAZINE
		nucleotides30.put("TFE",1); // 2'-O-[2-(TRIFLUORO)ETHYL] THYMIDINE-5'-MONOPHOSPHATE
		nucleotides30.put("TFO",1); // [2-(6-AMINO-9H-PURIN-9-YL)-1-METHYLETHOXY]METHYLPHOSPHONIC ACID"
		nucleotides30.put("TGP",1); // 5'-THIO-2'-DEOXY-GUANOSINE PHOSPHONIC ACID
		nucleotides30.put("THX",1); // PHOSPHONIC ACID 6-({6-[6-(6-CARBAMOYL-3,6,7,8-TETRAHYDRO-3,6-DIAZA-AS-INDACENE-2-CARBONYL)-3,6,7,8-TETRAHYDRO-3,6-DIAZA-AS-INDOCENE-2-CARBONYL]-3,6,7,8-TETRAHYDRO-3,6-DIAZA-AS-INDACENE-2-CARBONL}-AMINO)-HEXYL ESTER 5-(5-METHYL-2,4-DIOXO-3,4-DIHYDRO-2H-PYRIMIDIN-1-YL)-TETRAHYDRO-FURAN-2-YLMETHYL ESTER
		nucleotides30.put("TLC",1); // 2-O,3-ETHDIYL-ARABINOFURANOSYL-THYMINE-5'-MONOPHOSPHATE
		nucleotides30.put("TLN",1); //  [(1R,3R,4R,7S)-7-HYDROXY-3-(THYMIN-1-YL)-2,5-DIOXABICYCLO[2.2.1]HEPT-1-YL]METHYL DIHYDROGEN PHOSPHATE"
		nucleotides30.put("TP1",1); // 2-(METHYLAMINO)-ETHYLGLYCINE-CARBONYLMETHYLENE-THYMINE
		nucleotides30.put("TPC",1); // 5'-THIO-2'-DEOXY-CYTOSINE PHOSPHONIC ACID
		nucleotides30.put("TPN",1); // 2-AMINOETHYLGLYCINE-CARBONYLMETHYLENE-THYMINE



		// store nucleic acids (C, G, A, T, U, and I), and 
		// the modified versions of nucleic acids (+C, +G, +A, +T, +U, and +I), and
		nucleotides23  = new HashMap<String,Integer>();
		String[] names = {"C","G","A","T","U","I","+C","+G","+A","+T","+U","+I"};
		for (int i = 0; i < names.length; i++) {
			String n = names[i];
			nucleotides23.put(n,1);		
		}

		try {
			Alphabet alpha_prot = ProteinTools.getAlphabet();
			threeLetter = alpha_prot.getTokenization("name");
			oneLetter  = alpha_prot.getTokenization("token");
		} catch (Exception e) {
			e.printStackTrace() ;
		}

		NEWLINE = System.getProperty("line.separator");

	}



	public PDBFileParser() {

		structure     = null           ;
		current_model = new ArrayList<Chain>();
		current_chain = null           ;
		current_group = null           ;
		header        = init_header() ;
		pdbHeader 	  = new PDBHeader();
		connects      = new ArrayList<Map<String,Integer>>() ;

		parseSecStruc = false;
		alignSeqRes   = true;

		helixList     = new ArrayList<Map<String,String>>();
		strandList    = new ArrayList<Map<String,String>>();
		turnList      = new ArrayList<Map<String,String>>();
		current_compound = new Compound();
		dbrefs        = new ArrayList<DBRef>();

		dateFormat = new SimpleDateFormat("dd-MMM-yy");
		atomCount = 0;
		atomOverflow = false;

		parseCAOnly = false;

	}
	public boolean isParseCAOnly() {
		return parseCAOnly;
	}
	public void setParseCAOnly(boolean parseCAOnly) {
		this.parseCAOnly = parseCAOnly;
	}



	/** flag if the SEQRES amino acids shouldbe aligned with the ATOM amino acids
	 * 
	 * @return flag
	 */
	public boolean isAlignSeqRes() {
		return alignSeqRes;
	}



	/** define if the SEQRES in the structure shouldbe aligned with the ATOM records
	 * if yes, the AminoAcids in structure.getSeqRes will have the coordinates set.
	 * @param alignSeqRes
	 */
	public void setAlignSeqRes(boolean alignSeqRes) {
		this.alignSeqRes = alignSeqRes;
	}




	/** is secondary structure assignment being parsed from the file?
	 * default is null
	 * @return boolean if HELIX STRAND and TURN fields are being parsed
	 */
	public boolean isParseSecStruc() {
		return parseSecStruc;
	}

	/** a flag to tell the parser to parse the Author's secondary structure assignment from the file
	 * default is set to false, i.e. do NOT parse.
	 * @param parseSecStruc
	 */
	public void setParseSecStruc(boolean parseSecStruc) {
		this.parseSecStruc = parseSecStruc;
	}

	/** initialize the header. */
	private Map<String,Object> init_header(){


		HashMap<String,Object> header = new HashMap<String,Object> ();
		header.put ("idCode","");		
		header.put ("classification","")         ;
		header.put ("depDate","0000-00-00");
		header.put ("title","");
		header.put ("technique","");
		header.put ("resolution",null);
		header.put ("modDate","0000-00-00");
		//header.put ("journalRef","");
		//header.put ("author","");
		//header.put ("compound","");
		return header ;
	}


	/**
	 * Returns a time stamp.
	 * @return a String representing the time stamp value
	 */
	protected String getTimeStamp(){

		Calendar cal = Calendar.getInstance() ;
		// Get the components of the time
		int hour24 = cal.get(Calendar.HOUR_OF_DAY);     // 0..23
		int min = cal.get(Calendar.MINUTE);             // 0..59
		int sec = cal.get(Calendar.SECOND);             // 0..59
		String s = "time: "+hour24+" "+min+" "+sec;
		return s ;
	}


	/** convert three character amino acid codes into single character
	 *  e.g. convert CYS to C 
	 *  @return a character
	 *  @param code3 a three character amino acid representation String
	 *  @throws IllegalSymbolException
	 */

	public Character convert_3code_1code(String code3) 
	throws IllegalSymbolException
	{
		Symbol sym   =  threeLetter.parseToken(code3) ;
		String code1 =  oneLetter.tokenizeSymbol(sym);

		return new Character(code1.charAt(0)) ;

	}


	/** convert a three letter code into single character.
	 * catches for unusual characters
	 * 
	 * @param code3
	 * @return null if group is a nucleotide code
	 */
	private Character get1LetterCode(String groupCode3){

		Character aminoCode1 = null;
		try {
			// is it a standard amino acid ?
			aminoCode1 = convert_3code_1code(groupCode3);
		} catch (IllegalSymbolException e){
			// hm groupCode3 is not standard
			// perhaps it is an nucleotide?
			if ( isNucleotide(groupCode3) ) {
				//System.out.println("nucleotide, aminoCode1:"+aminoCode1);
				aminoCode1= null;
			} else {
				// does not seem to be so let's assume it is 
				//  nonstandard aminoacid and label it "X"
				System.out.println("unknown amino acid "+groupCode3 );
				aminoCode1 = new Character('x');
			}
		}

		return aminoCode1;

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

	/* test if the threelettercode of an ATOM entry corresponds to a
	 * nucleotide or to an aminoacid
	 */
	private boolean isNucleotide(String groupCode3){

		String code = groupCode3.trim();
		if ( nucleotides30.containsKey(code)){    	
			return true;    		
		}

		if ( nucleotides23.containsKey(code)){
			return true;
		}

		return false ;
	}

	// Handler methods to deal with PDB file records properly.
	/**
	 Handler for
	 HEADER Record Format

	 COLUMNS        DATA TYPE       FIELD           DEFINITION
	 ----------------------------------------------------------------------------------
	 1 -  6        Record name     "HEADER"
	 11 - 50        String(40)      classification  Classifies the molecule(s)
	 51 - 59        Date            depDate         Deposition date.  This is the date
	 the coordinates were received by
	 the PDB
	 63 - 66        IDcode          idCode          This identifier is unique within PDB

	 */
	private void pdb_HEADER_Handler(String line) {
		//System.out.println(line);

		String classification  = line.substring (10, 50).trim() ;
		String deposition_date = line.substring (50, 59).trim() ;
		String pdbCode          = line.substring (62, 66).trim() ;       

		header.put("idCode",pdbCode);
		structure.setPDBCode(pdbCode);
		header.put("classification",classification);
		header.put("depDate",deposition_date);

		pdbHeader.setIdCode(pdbCode);
		pdbHeader.setClassification(classification);


		try {
			Date dep = dateFormat.parse(deposition_date);
			pdbHeader.setDepDate(dep);
		} catch (ParseException e){
			e.printStackTrace();
		}

	}



	/** parses the following record:

	 <pre>
    COLUMNS       DATA TYPE        FIELD        DEFINITION
    --------------------------------------------------------------------
     1 -  6       Record name      "HELIX "
     8 - 10       Integer          serNum       Serial number of the helix.
                                                This starts at 1 and increases
                                                incrementally.
    12 - 14       LString(3)       helixID      Helix identifier. In addition
                                                to a serial number, each helix is
                                                given an alphanumeric character
                                                helix identifier.
    16 - 18       Residue name     initResName  Name of the initial residue.
    20            Character        initChainID  Chain identifier for the chain
                                                containing this helix.
    22 - 25       Integer          initSeqNum   Sequence number of the initial
                                                residue.
    26            AChar            initICode    Insertion code of the initial
                                                residue.
    28 - 30       Residue name     endResName   Name of the terminal residue of
                                                the helix.
    32            Character        endChainID   Chain identifier for the chain
                                                containing this helix.
    34 - 37       Integer          endSeqNum    Sequence number of the terminal
                                                residue.
    38            AChar            endICode     Insertion code of the terminal
                                                residue.
    39 - 40       Integer          helixClass   Helix class (see below).
    41 - 70       String           comment      Comment about this helix.
    72 - 76       Integer          length       Length of this helix.
</pre>
	 */

	private void pdb_HELIX_Handler(String line){
		String initResName = line.substring(15,18).trim();
		String initChainId = line.substring(19,20);
		String initSeqNum  = line.substring(21,25).trim();
		String initICode   = line.substring(25,26);
		String endResName  = line.substring(27,30).trim();
		String endChainId  = line.substring(31,32);
		String endSeqNum   = line.substring(33,37).trim();
		String endICode    = line.substring(37,38);

		//System.out.println(initResName + " " + initChainId + " " + initSeqNum + " " + initICode + " " +
		//        endResName + " " + endChainId + " " + endSeqNum + " " + endICode);

		Map<String,String> m = new HashMap<String,String>();

		m.put("initResName",initResName);
		m.put("initChainId", initChainId);
		m.put("initSeqNum", initSeqNum);
		m.put("initICode", initICode);
		m.put("endResName", endResName);
		m.put("endChainId", endChainId);
		m.put("endSeqNum",endSeqNum);
		m.put("endICode",endICode);

		helixList.add(m);

	}

	/** 
      Handler for
      <pre>
      COLUMNS     DATA TYPE        FIELD           DEFINITION
--------------------------------------------------------------
 1 -  6     Record name      "SHEET "
 8 - 10     Integer          strand       Strand number which starts at 1 
                                          for each strand within a sheet 
                                          and increases by one.
12 - 14     LString(3)       sheetID      Sheet identifier.
15 - 16     Integer          numStrands   Number of strands in sheet.
18 - 20     Residue name     initResName  Residue name of initial residue.
22          Character        initChainID  Chain identifier of initial 
                                          residue in strand.
23 - 26     Integer          initSeqNum   Sequence number of initial 
                                          residue in strand.
27          AChar            initICode    Insertion code of initial residue
                                          in strand.
29 - 31     Residue name     endResName   Residue name of terminal residue.
33          Character        endChainID   Chain identifier of terminal
                                          residue.
34 - 37     Integer          endSeqNum    Sequence number of terminal
                                          residue.
38          AChar            endICode     Insertion code of terminal 
                                          residue.
39 - 40     Integer          sense        Sense of strand with respect to
                                          previous strand in the sheet. 0
                                          if first strand, 1 if parallel,
                                          -1 if anti-parallel.
42 - 45     Atom             curAtom      Registration. Atom name in 
                                          current strand.
46 - 48     Residue name     curResName   Registration. Residue name in
                                          current strand.
50          Character        curChainId   Registration. Chain identifier in
                                          current strand.
51 - 54     Integer          curResSeq    Registration. Residue sequence
                                          number in current strand.
55          AChar            curICode     Registration. Insertion code in
                                          current strand.
57 - 60     Atom             prevAtom     Registration. Atom name in
                                          previous strand.
61 - 63     Residue name     prevResName  Registration. Residue name in
                                          previous strand.
65          Character        prevChainId  Registration. Chain identifier in
                                          previous strand.
66 - 69     Integer          prevResSeq   Registration. Residue sequence
                                          number in previous strand.
70          AChar            prevICode    Registration. Insertion code in
                                              previous strand.
</pre>


	 */
	private void pdb_SHEET_Handler( String line){


		String initResName = line.substring(17,20).trim();
		String initChainId = line.substring(21,22);
		String initSeqNum  = line.substring(22,26).trim();
		String initICode   = line.substring(26,27);
		String endResName  = line.substring(28,31).trim();
		String endChainId  = line.substring(32,33);
		String endSeqNum   = line.substring(33,37).trim();
		String endICode    = line.substring(37,38);

		//System.out.println(initResName + " " + initChainId + " " + initSeqNum + " " + initICode + " " +
		//        endResName + " " + endChainId + " " + endSeqNum + " " + endICode);

		Map<String,String> m = new HashMap<String,String>();

		m.put("initResName",initResName);
		m.put("initChainId", initChainId);
		m.put("initSeqNum", initSeqNum);
		m.put("initICode", initICode);
		m.put("endResName", endResName);
		m.put("endChainId", endChainId);
		m.put("endSeqNum",endSeqNum);
		m.put("endICode",endICode);

		strandList.add(m);
	}


	/** 
	 * Handler for TURN lines
     <pre>
     COLUMNS      DATA TYPE        FIELD         DEFINITION
--------------------------------------------------------------------
 1 -  6      Record name      "TURN "
 8 - 10      Integer          seq           Turn number; starts with 1 and
                                            increments by one.
12 - 14      LString(3)       turnId        Turn identifier
16 - 18      Residue name     initResName   Residue name of initial residue in
                                            turn.
20           Character        initChainId   Chain identifier for the chain
                                            containing this turn.
21 - 24      Integer          initSeqNum    Sequence number of initial residue
                                            in turn.
25           AChar            initICode     Insertion code of initial residue 
                                            in turn.
27 - 29      Residue name     endResName    Residue name of terminal residue 
                                            of turn.
31           Character        endChainId    Chain identifier for the chain
                                            containing this turn.
32 - 35      Integer          endSeqNum     Sequence number of terminal 
                                            residue of turn.
36           AChar            endICode      Insertion code of terminal residue
                                            of turn.
41 - 70      String           comment       Associated comment.

     </pre>
	 * @param line
	 */
	private void pdb_TURN_Handler( String line){
		String initResName = line.substring(15,18).trim();
		String initChainId = line.substring(19,20);
		String initSeqNum  = line.substring(20,24).trim();
		String initICode   = line.substring(24,25);
		String endResName  = line.substring(26,29).trim();
		String endChainId  = line.substring(30,31);
		String endSeqNum   = line.substring(31,35).trim();
		String endICode    = line.substring(35,36);

		//System.out.println(initResName + " " + initChainId + " " + initSeqNum + " " + initICode + " " +
		//        endResName + " " + endChainId + " " + endSeqNum + " " + endICode);

		Map<String,String> m = new HashMap<String,String>();

		m.put("initResName",initResName);
		m.put("initChainId", initChainId);
		m.put("initSeqNum", initSeqNum);
		m.put("initICode", initICode);
		m.put("endResName", endResName);
		m.put("endChainId", endChainId);
		m.put("endSeqNum",endSeqNum);
		m.put("endICode",endICode);

		turnList.add(m);
	}

	/** 
	 Handler for 
	 REVDAT Record format:

	 COLUMNS       DATA TYPE      FIELD         DEFINITION
	 ----------------------------------------------------------------------------------
	 1 -  6       Record name    "REVDAT"
	 8 - 10       Integer        modNum        Modification number.
	 11 - 12       Continuation   continuation  Allows concatenation of multiple
	 records.
	 14 - 22       Date           modDate       Date of modification (or release for
	 new entries).  This is not repeated
	 on continuation lines.
	 24 - 28       String(5)      modId         Identifies this particular
	 modification.  It links to the
	 archive used internally by PDB.
	 This is not repeated on continuation
	 lines.
	 32            Integer        modType       An integer identifying the type of
	 modification.  In case of revisions
	 with more than one possible modType,
	 the highest value applicable will be
	 assigned.
	 40 - 45       LString(6)     record        Name of the modified record.
	 47 - 52       LString(6)     record        Name of the modified record.
	 54 - 59       LString(6)     record        Name of the modified record.
	 61 - 66       LString(6)     record        Name of the modified record.
	 */
	private void pdb_REVDAT_Handler(String line) {

		String modDate = (String) header.get("modDate");
		if ( modDate.equals("0000-00-00") ) {
			// modDate is still initialized
			String modificationDate = line.substring (13, 22).trim() ;       
			header.put("modDate",modificationDate);

			try {
				Date dep = dateFormat.parse(modificationDate);
				pdbHeader.setModDate(dep);
			} catch (ParseException e){
				e.printStackTrace();
			}

		}
	}

	/** @author Jules Jacobsen
	 * Handler for
	 * SEQRES record format
	 * SEQRES records contain the amino acid or nucleic acid sequence of residues in each chain of the macromolecule that was studied.
	 * <p/>
	 * Record Format
	 * <p/>
	 * COLUMNS        DATA TYPE       FIELD         DEFINITION
	 * ---------------------------------------------------------------------------------
	 * 1 -  6        Record name     "SEQRES"
	 * <p/>
	 * 9 - 10        Integer         serNum        Serial number of the SEQRES record
	 * for the current chain.  Starts at 1
	 * and increments by one each line.
	 * Reset to 1 for each chain.
	 * <p/>
	 * 12             Character       chainID       Chain identifier.  This may be any
	 * single legal character, including a
	 * blank which is used if there is
	 * only one chain.
	 * <p/>
	 * 14 - 17        Integer         numRes        Number of residues in the chain.
	 * This value is repeated on every
	 * record.
	 * <p/>
	 * 20 - 22        Residue name    resName       Residue name.
	 * <p/>
	 * 24 - 26        Residue name    resName       Residue name.
	 * <p/>
	 * 28 - 30        Residue name    resName       Residue name.
	 * <p/>
	 * 32 - 34        Residue name    resName       Residue name.
	 * <p/>
	 * 36 - 38        Residue name    resName       Residue name.
	 * <p/>
	 * 40 - 42        Residue name    resName       Residue name.
	 * <p/>
	 * 44 - 46        Residue name    resName       Residue name.
	 * <p/>
	 * 48 - 50        Residue name    resName       Residue name.
	 * <p/>
	 * 52 - 54        Residue name    resName       Residue name.
	 * <p/>
	 * 56 - 58        Residue name    resName       Residue name.
	 * <p/>
	 * 60 - 62        Residue name    resName       Residue name.
	 * <p/>
	 * 64 - 66        Residue name    resName       Residue name.
	 * <p/>
	 * 68 - 70        Residue name    resName       Residue name.
	 */
	private void pdb_SEQRES_Handler(String line)
	throws PDBParseException {
//		System.out.println("PDBFileParser.pdb_SEQRES_Handler: BEGIN");
//		System.out.println(line);

		//TODO: treat the following residues as amino acids?
		/*
        MSE Selenomethionine
        CSE Selenocysteine
        PTR Phosphotyrosine
        SEP Phosphoserine
        TPO Phosphothreonine
        HYP 4-hydroxyproline
        5HP Pyroglutamic acid; 5-hydroxyproline
        PCA Pyroglutamic Acid
        LYZ 5-hydroxylysine
        GLX Glu or Gln
        ASX Asp or Asn
        GLA gamma-carboxy-glutamic acid
                 1         2         3         4         5         6         7
        1234567890123456789012345678901234567890123456789012345678901234567890
        SEQRES   1 A  376  LYS PRO VAL THR VAL LYS LEU VAL ASP SER GLN ALA THR 
        SEQRES   1 A   21  GLY ILE VAL GLU GLN CYS CYS THR SER ILE CYS SER LEU
        SEQRES   2 A   21  TYR GLN LEU GLU ASN TYR CYS ASN
        SEQRES   1 B   30  PHE VAL ASN GLN HIS LEU CYS GLY SER HIS LEU VAL GLU
        SEQRES   2 B   30  ALA LEU TYR LEU VAL CYS GLY GLU ARG GLY PHE PHE TYR
        SEQRES   3 B   30  THR PRO LYS ALA
        SEQRES   1 C   21  GLY ILE VAL GLU GLN CYS CYS THR SER ILE CYS SER LEU
        SEQRES   2 C   21  TYR GLN LEU GLU ASN TYR CYS ASN
        SEQRES   1 D   30  PHE VAL ASN GLN HIS LEU CYS GLY SER HIS LEU VAL GLU
        SEQRES   2 D   30  ALA LEU TYR LEU VAL CYS GLY GLU ARG GLY PHE PHE TYR
        SEQRES   3 D   30  THR PRO LYS ALA
		 */

		//System.out.println(line);
		String recordName = line.substring(0, 6).trim();
		String chainID    = line.substring(11, 12);  
		String newLength   = line.substring(13,17).trim();
		String subSequence = line.substring(18, 70);

		//System.out.println("newLength " + newLength );

		if ( lengthCheck == -1 ){
			lengthCheck = Integer.parseInt(newLength);
		}

		//String residueNumber = line.substring(22, 27).trim() ;
		StringTokenizer subSequenceResidues = new StringTokenizer(subSequence);

		Character aminoCode1 = null;
		if (! recordName.equals(AminoAcid.SEQRESRECORD)) {
			// should not have been called
			return;
		}

		current_chain = isKnownChain(chainID, seqResChains);
		if ( current_chain == null) {

			current_chain = new ChainImpl();
			current_chain.setName(chainID);				

		}


		while (subSequenceResidues.hasMoreTokens()) {

			String threeLetter = subSequenceResidues.nextToken();

			aminoCode1 = get1LetterCode(threeLetter);
			//System.out.println(aminoCode1);
			//if (aminoCode1 == null) {
			// could be a nucleotide...
			// but getNewGroup takes care of that and converts ATOM records with aminoCode1 == nnull to nucleotide...
			//System.out.println(line);
			// b
			//} 
			current_group = getNewGroup("ATOM", aminoCode1);				
			current_group.setPDBName(threeLetter);
			if ( current_group instanceof AminoAcid){
				AminoAcid aa = (AminoAcid)current_group;
				aa.setRecordType(AminoAcid.SEQRESRECORD);
			}
			// add the current group to the new chain.
			current_chain.addGroup(current_group);

		}
		Chain test = isKnownChain(chainID, seqResChains);

		if ( test == null)
			seqResChains.add(current_chain);

		current_group = null;	
		current_chain = null;
		//structure.setSeqRes(seqResChains);

		//		 the current chain is finished!
		//if ( current_chain.getLength() != lengthCheck ){
		//	System.err.println("the length of chain " + current_chain.getName() + "(" + 
		//			current_chain.getLength() + ") does not match the expected " + lengthCheck);
		//}

		lengthCheck = Integer.parseInt(newLength);

	}



	/** Handler for
	 TITLE Record Format

	 COLUMNS        DATA TYPE       FIELD          DEFINITION
	 ----------------------------------------------------------------------------------
	 1 -  6        Record name     "TITLE "
	 9 - 10        Continuation    continuation   Allows concatenation of multiple
	 records.
	 11 - 70        String          title          Title of the experiment.


	 */
	private void pdb_TITLE_Handler(String line) {
		String title = line.substring(10,70).trim();
		String t= (String)header.get("title") ;
		t += title + " ";
		header.put("title",t);
		pdbHeader.setTitle(t);
	}

	/**
	 * Handler for
	 * COMPND
	 * <p/>
	 * Overview
	 * <p/>
	 * The COMPND record describes the macromolecular contents of an entry. Each macromolecule found in the entry is described by a set of token: value pairs, and is referred to as a COMPND record component. Since the concept of a molecule is difficult to specify exactly, PDB staff may exercise editorial judgment in consultation with depositors in assigning these names.
	 * <p/>
	 * For each macromolecular component, the molecule name, synonyms, number assigned by the Enzyme Commission (EC), and other relevant details are specified.
	 * <p/>
	 * Record Format
	 * <p/>
	 * COLUMNS        DATA TYPE         FIELD          DEFINITION
	 * ----------------------------------------------------------------------------------
	 * 1 -  6        Record name       "COMPND"
	 * <p/>
	 * 9 - 10        Continuation      continuation   Allows concatenation of multiple
	 * records.
	 * <p/>
	 * 11 - 70        Specification     compound       Description of the molecular
	 * list                             components.
	 * <p/>
	 * Details
	 * <p/>
	 * The compound record is a Specification list. The specifications, or tokens, that may be used are listed below:
	 * <p/>
	 * TOKEN                   VALUE DEFINITION
	 * ---------------------------------------------------------------------------------
	 * MOL_ID                  Numbers each component; also used in SOURCE to associate
	 * the information.
	 * <p/>
	 * MOLECULE                Name of the macromolecule.
	 * <p/>
	 * CHAIN                   Comma-separated list of chain identifier(s). "NULL" is
	 * used to indicate a blank chain identifier.
	 * <p/>
	 * FRAGMENT                Specifies a domain or region of the molecule.
	 * <p/>
	 * SYNONYM                 Comma-separated list of synonyms for the MOLECULE.
	 * <p/>
	 * EC                      The Enzyme Commission number associated with the
	 * molecule. If there is more than one EC number, they
	 * are presented as a comma-separated list.
	 * <p/>
	 * ENGINEERED              Indicates that the molecule was produced using
	 * recombinant technology or by purely chemical synthesis.
	 * <p/>
	 * MUTATION                Describes mutations from the wild type molecule.
	 * <p/>
	 * BIOLOGICAL_UNIT         If the MOLECULE functions as part of a larger
	 * biological unit, the entire functional unit may be
	 * described.
	 * <p/>
	 * OTHER_DETAILS           Additional comments.
	 * <p/>
	 * In the general case the PDB tends to reflect the biological/functional view of the molecule. For example, the hetero-tetramer hemoglobin molecule is treated as a discrete component in COMPND.
	 * <p/>
	 * In the case of synthetic molecules, e. g., hybrids, the description will be provided by the depositor.
	 * <p/>
	 * No specific rules apply to the ordering of the tokens, except that the occurrence of MOL_ID or FRAGMENT indicates that the subsequent tokens are related to that specific molecule or fragment of the molecule.
	 * <p/>
	 * Physical layout of these items may be altered by PDB staff to improve human readability of the COMPND record.
	 * <p/>
	 * Asterisks in nucleic acid names (in MOLECULE) are for ease of reading.
	 * <p/>
	 * When insertion codes are given as part of the residue name, they must be given within square brackets, i.e., H57[A]N. This might occur when listing residues in FRAGMENT, MUTATION, or OTHER_DETAILS.
	 * <p/>
	 * For multi-chain molecules, e.g., the hemoglobin tetramer, a comma-separated list of CHAIN identifiers is used.
	 * <p/>
	 * When non-blank chain identifiers occur in the entry, they must be specified.
	 * <p/>
	 * NULL is used to indicate blank chain identifiers. E.g., CHAIN: NULL, CHAIN: NULL, B, C.
	 * <p/>
	 * For enzymes, if no EC number has been assigned, "EC: NOT ASSIGNED" is used.
	 * <p/>
	 * ENGINEERED is followed either by "YES" or by a comment.
	 * <p/>
	 * For the token MUTATION, the following set of examples illustrate the conventions used by PDB to represent various types of mutations.
	 * <p/>
	 * MUTATION TYPE         DESCRIPTION                     FORM
	 * ------------------------------------------------------------------------------
	 * Simple substitution   His 57 replaced by Asn          H57N
	 * <p/>
	 * His 57A replaced by Asn, in
	 * chain C only                    Chain C, H57[A]N
	 * <p/>
	 * Insertion             His and Pro inserted before
	 * Lys 48                          INS(HP-K48)
	 * <p/>
	 * Deletion              Arg 141 of chains A and C
	 * deleted, not deleted in
	 * chain B                         Chain A, C, DEL(R141)
	 * <p/>
	 * His 23 through ARG 26 deleted   DEL(23-26)
	 * <p/>
	 * His 23C and Arg 26 deleted
	 * from chain B only               Chain B, DEL(H23[C],R26)
	 * <p/>
	 * When there are more than ten mutations:
	 * <p/>
	 * - All the mutations are listed in the SEQADV record.
	 * <p/>
	 * - Some mutations may be listed in MUTATION in COMPND to highlight the most important ones, at the depositor's discretion.
	 * <p/>
	 * New tokens may be added by the PDB as needed.
	 * <p/>
	 * Verification/Validation/Value Authority Control
	 * <p/>
	 * CHAIN must match the chain identifiers(s) of the molecule(s). EC numbers are checked against the Enzyme Data Bank.
	 * <p/>
	 * Relationships to Other Record Types
	 * <p/>
	 * Each molecule given a MOL_ID in COMPND must be listed and given the biological source information in SOURCE. In the case of mutations, the SEQADV records will present differences from the reference molecule. REMARK record may further describe the contents of the entry. Also see verification above.
	 * <p/>
	 * Example
	 * <p/>
	 * 1         2         3         4         5         6         7
	 * 1234567890123456789012345678901234567890123456789012345678901234567890
	 * COMPND    MOL_ID: 1;                                                    1ALI   4
	 * COMPND    MOL_ID: 1;
	 * COMPND   2 MOLECULE: HEMOGLOBIN;
	 * COMPND   3 CHAIN: A, B, C, D;
	 * COMPND   4 ENGINEERED: YES;
	 * COMPND   5 MUTATION: CHAIN B, D, V1A;
	 * COMPND   6 BIOLOGICAL_UNIT: HEMOGLOBIN EXISTS AS AN A1B1/A2B2
	 * COMPND   7 TETRAMER;
	 * COMPND   8 OTHER_DETAILS: DEOXY FORM
	 * <p/>
	 * COMPND    MOL_ID: 1;
	 * COMPND   2 MOLECULE: COWPEA CHLOROTIC MOTTLE VIRUS;
	 * COMPND   3 CHAIN: A, B, C;
	 * COMPND   4 SYNONYM: CCMV;
	 * COMPND   5 MOL_ID: 2;
	 * COMPND   6 MOLECULE: RNA (5'-(*AP*UP*AP*U)-3');
	 * COMPND   7 CHAIN: D, F;
	 * COMPND   8 ENGINEERED: YES;
	 * COMPND   9 MOL_ID: 3;
	 * COMPND  10 MOLECULE: RNA (5'-(*AP*U)-3');
	 * COMPND  11 CHAIN: E;
	 * COMPND  12 ENGINEERED: YES
	 * <p/>
	 * COMPND    MOL_ID: 1;
	 * COMPND   2 MOLECULE: HEVAMINE A;
	 * COMPND   3 CHAIN: NULL;
	 * COMPND   4 EC: 3.2.1.14, 3.2.1.17;
	 * COMPND   5 OTHER_DETAILS: PLANT ENDOCHITINASE/LYSOZYME
	 */

	private void pdb_COMPND_Handler(String line) {
//		System.out.println("PDBFileParser.pdb_COMPND_Handler: BEGIN");
//		System.out.println("PDBFileParser.pdb_COMPND_Handler: Parsing: " + line);

		/* do we need the continuation number?
        if (line.substring(9, 10).contains(" ")) 
            continuationNo = 0;
        else 
            continuationNo = Integer.valueOf(line.substring(9, 10));
		 */

		if (DEBUG) {
			//System.out.println("current continuationNo     is " + continuationNo);
			System.out.println("current continuationField  is " + continuationField);
			System.out.println("current continuationString is " + continuationString);
			System.out.println("current compount           is " + current_compound);
		}
		StringTokenizer compndTokens = new StringTokenizer(line.substring(10,70));

		while (compndTokens.hasMoreTokens()) {
			String token = compndTokens.nextToken();



			if (compndFieldValues.contains(token)) {

				if (continuationString.equals("")) {
					continuationField = token;
				} else {

					compndValueSetter(continuationField, continuationString);
					continuationField = token;
					continuationString = "";
				}
			} else if ( ignoreCompndFieldValues.contains(token)){
				// this field shall be ignored
				continuationField = token;
			} else {         

				if (DEBUG)
					System.out.println("Still in field " + continuationField);

				continuationString = continuationString.concat(token + " ");
				if (DEBUG)
					System.out.println("continuationString = " + continuationString);
			}
//			System.out.println("PDBFileParser.pdb_COMPND_Handler: END");
		}
	}


	/** set the value in the currrent molId object
	 * 
	 * @param field
	 * @param value
	 */
	private void compndValueSetter(String field, String value) {

		value = value.trim().replace(";", "");
		if (field.equals("MOL_ID:")) {

			//todo: find out why an extra mol or chain gets added  and why 1H1J, 1J1H ATOM records are missing, but not 1H1H....
			if ( DEBUG )
				System.out.println("molTypeCounter " + molTypeCounter+ " "+ value);
			if (molTypeCounter != Integer.valueOf(value)) {
				molTypeCounter++;

				compounds.add(current_compound);
				current_compound = null;
				current_compound = new Compound();

			}

			current_compound.setMolId(value);
		}
		if (field.equals("MOLECULE:")) {
			current_compound.setMolName(value);

		}
		if (field.equals("CHAIN:")) {
			//System.out.println(value);
			StringTokenizer chainTokens = new StringTokenizer(value, ",");
			List<String> chains = new ArrayList<String>();

			while (chainTokens.hasMoreTokens()) {
				chains.add(chainTokens.nextToken().trim());
			}
			current_compound.setChainId(chains);            


		}
		if (field.equals("SYNONYM:")) {

			StringTokenizer synonyms = new StringTokenizer(value, ",");
			List<String> names = new ArrayList<String>();

			while (synonyms.hasMoreTokens()) {
				names.add(synonyms.nextToken());

				current_compound.setSynonyms(names);
			}

		}

		if (field.equals("EC:")) {

			StringTokenizer ecNumTokens = new StringTokenizer(value, ",");
			List<String> ecNums = new ArrayList<String>();

			while (ecNumTokens.hasMoreTokens()) {
				ecNums.add(ecNumTokens.nextToken());

				current_compound.setEcNums(ecNums);
			}

		}
		if (field.equals("FRAGMENT:")) {

			current_compound.setFragment(value);

		}
		if (field.equals("ENGINEERED:")) {

			current_compound.setEngineered(value);

		}
		if (field.equals("MUTATION:")) {

			current_compound.setMutation(value);

		}
		if (field.equals("BIOLOGICAL_UNIT:")) {

			current_compound.setBiologicalUnit(value);

		}
		if (field.equals("OTHER_DETAILS:")) {

			current_compound.setDetails(value);

		}



	}



	/** Handler for
	 * SOURCE Record format
	 * 
	 * The SOURCE record specifies the biological and/or chemical source of each biological molecule in the entry. Sources are described by both the common name and the scientific name, e.g., genus and species. Strain and/or cell-line for immortalized cells are given when they help to uniquely identify the biological entity studied.
Record Format

COLUMNS   DATA TYPE         FIELD          DEFINITION                        
-------------------------------------------------------------------------------
 1 -  6   Record name       "SOURCE"                                         
 9 - 10   Continuation      continuation   Allows concatenation of multiple records.                         
11 - 70   Specification     srcName        Identifies the source of the macromolecule in 
           list                            a token: value format.                        
	 * @param line the line to be parsed

	 */
	private void pdb_SOURCE_Handler(String line) {


		// this is horrible! The order of the handlers have to be maintained and 
		//this method actually finishes off the
		//  job of the COMPND_Handler. :(
		try {


			if (compounds.size() < Integer.valueOf(current_compound.getMolId())) {
//				System.out.println("Finishing off final MolID header.");
				compndValueSetter(continuationField, continuationString);
				continuationString = "";   
				compounds.add(current_compound.clone());
			}

			if ( ! continuationString.equals("")){
				if (DEBUG) {
					System.out.println("continuationString is not empty. " + current_compound);
				}
				// looks like an old style PDB file header
				if ( current_compound.getMolId().equals("0") ) {
					if (DEBUG){
						System.out.println("looks like an old PDB file header");
					}
					current_compound.setMolName(continuationString);
					compounds.add(current_compound);
				}
				continuationString = "";   
			}

		} catch (CloneNotSupportedException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		int molTypeCounter = 0;

		// ignore trailing bits of data in PDB file format < v3.0      
		String data = line.substring(0,70);

		//System.out.println("Parsing: "+ line);
		StringTokenizer sourceTokens = new StringTokenizer(data);
		while (sourceTokens.hasMoreTokens()) {
			String code = sourceTokens.nextToken();
			String[] values = data.split(":");
			int valueLength = values.length;
			String value = values[valueLength - 1].trim().replace(";", "");
			//System.out.println(lineTokens.countTokens()  + " " + value);

			if (code.equals("MOL_ID:")) {
				if (molTypeCounter != Integer.valueOf(value)) {

					//System.out.println("New MOL!!!");

					for (Compound molId : compounds) {
						if (molId.getMolId().equals(value)) {
							current_compound = molId;
						}
					}
					//System.out.println("Setting current_molId to :" + current_molId.getMolId());
					molTypeCounter++;

				}

				//current_molId.showSource();
			}
			if (code.equals("SYNTHETIC:")) {
				current_compound.setSynthetic(value);
			} else if (code.equals("FRAGMENT:")) {
				current_compound.setFragment(value);
			} else if (code.equals("ORGANISM_SCIENTIFIC:")) {
				current_compound.setOrganismScientific(value);
			} else if (code.equals("ORGANISM_COMMON:")) {
				current_compound.setOrganismCommon(value);
			} else if (code.equals("STRAIN:")) {  
				current_compound.setStrain(value);
			} else if (code.equals("VARIANT:")) {
				current_compound.setVariant(value);
			} else if (code.equals("CELL_LINE:")) {
				current_compound.setCellLine(value);
			} else if (code.equals("ATCC:")) {
				current_compound.setAtcc(value);
			} else if (code.equals("ORGAN:")) {
				current_compound.setOrgan(value);
			} else if (code.equals("TISSUE:")) {
				current_compound.setTissue(value);
			} else if (code.equals("CELL:")) {
				current_compound.setCell(value);
			} else if (code.equals("ORGANELLE:")) {
				current_compound.setOrganelle(value);
			} else if (code.equals("SECRETION:")) {
				current_compound.setSecretion(value);
			} else if (code.equals("GENE:")) {
				current_compound.setGene(value);
			} else if (code.equals("CELLULAR_LOCATION:")) {
				current_compound.setCellularLocation(value);
			} else if (code.equals("EXPRESSION_SYSTEM:")) {
				current_compound.setExpressionSystem(value);
			} else if (code.equals("EXPRESSION_SYSTEM_STRAIN:")) {
				current_compound.setExpressionSystemStrain(value);
			} else if (code.equals("EXPRESSION_SYSTEM_VARIANT:")) {
				current_compound.setExpressionSystemVariant(value);
			} else if (code.equals("EXPRESSION_SYSTEM_CELL_LINE:")) {
				current_compound.setExpressionSystemCellLine(value);
			} else if (code.equals("EXPRESSION_SYSTEM_ATCC_NUMBER:")) {
				current_compound.setExpressionSystemAtccNumber(value);
			} else if (code.equals("EXPRESSION_SYSTEM_ORGAN:")) {
				current_compound.setExpressionSystemOrgan(value);
			} else if (code.equals("EXPRESSION_SYSTEM_TISSUE:")) {
				current_compound.setExpressionSystemTissue(value);
			} else if (code.equals("EXPRESSION_SYSTEM_CELL:")) {
				current_compound.setExpressionSystemCell(value);
			} else if (code.equals("EXPRESSION_SYSTEM_ORGANELLE:")) {
				current_compound.setExpressionSystemOrganelle(value);
			} else if (code.equals("EXPRESSION_SYSTEM_CELLULAR_LOCATION:")) {
				current_compound.setExpressionSystemCellularLocation(value);
			} else if (code.equals("EXPRESSION_SYSTEM_VECTOR_TYPE:")) {
				current_compound.setExpressionSystemVectorType(value);
			} else if (code.equals("EXPRESSION_SYSTEM_VECTOR:")) {
				current_compound.setExpressionSystemVector(value);
			} else if (code.equals("EXPRESSION_SYSTEM_PLASMID:")) {
				current_compound.setExpressionSystemPlasmid(value);
			} else if (code.equals("EXPRESSION_SYSTEM_GENE:")) {
				current_compound.setExpressionSystemGene(value);
			} else if (code.equals("OTHER_DETAILS:")) {
				current_compound.setExpressionSystemOtherDetails(value);
			}

		}
//		System.out.println("PDBFileParser.pdb_SOURCE_Handler: END");
	}


	/** Handler for
	 REMARK  2 

	 * For diffraction experiments:

	 COLUMNS        DATA TYPE       FIELD               DEFINITION
	 --------------------------------------------------------------------------------
	 1 -  6        Record name     "REMARK"
	 10             LString(1)      "2"
	 12 - 22        LString(11)     "RESOLUTION."
	 23 - 27        Real(5.2)       resolution          Resolution.
	 29 - 38        LString(10)     "ANGSTROMS."
	 */

	private void pdb_REMARK_2_Handler(String line) {

		int i = line.indexOf("ANGSTROM");
		if ( i != -1) {
			// line contains ANGSTROM info...
			String resolution = line.substring(22,27).trim();
			// convert string to float
			float res = 99 ;
			try {
				res = Float.parseFloat(resolution);
			} catch (NumberFormatException e) {
				System.err.println(e.getMessage());
				System.err.println("could not parse resolution from line and ignoring it " + line);
				return ;
			}
			header.put("resolution",new Float(res));
			pdbHeader.setResolution(res);
		}

	}


	/** Handler for REMARK lines 
	 */
	private void pdb_REMARK_Handler(String line) {
		// finish off the compound handler!



		String l = line.substring(0,11).trim();
		if (l.equals("REMARK   2"))pdb_REMARK_2_Handler(line);

	}


	/** Handler for
	 EXPDTA Record Format

	 COLUMNS       DATA TYPE      FIELD         DEFINITION
	 -------------------------------------------------------------------------------
	 1 -  6       Record name    "EXPDTA"
	 9 - 10       Continuation   continuation  Allows concatenation of multiple
	 records.
	 11 - 70       SList          technique     The experimental technique(s) with
	 optional comment describing the
	 sample or experiment.

	 allowed techniques are:
	 ELECTRON DIFFRACTION
	 FIBER DIFFRACTION
	 FLUORESCENCE TRANSFER
	 NEUTRON DIFFRACTION
	 NMR
	 THEORETICAL MODEL
	 X-RAY DIFFRACTION

	 */

	private void pdb_EXPDTA_Handler(String line) {

		String technique  = line.substring (10, 70).trim() ;

		String t =(String) header.get("technique");
		t += technique +" ";
		header.put("technique",t);
		pdbHeader.setTechnique(t);

		int nmr = technique.indexOf("NMR");
		if ( nmr != -1 ) structure.setNmr(true);  ;

	}




	/**
	 Handler for
	 ATOM Record Format 
	 <pre>
	 COLUMNS        DATA TYPE       FIELD         DEFINITION
	 ---------------------------------------------------------------------------------
	 1 -  6        Record name     "ATOM  "
	 7 - 11        Integer         serial        Atom serial number.
	 13 - 16        Atom            name          Atom name.
	 17             Character       altLoc        Alternate location indicator.
	 18 - 20        Residue name    resName       Residue name.
	 22             Character       chainID       Chain identifier.
	 23 - 26        Integer         resSeq        Residue sequence number.
	 27             AChar           iCode         Code for insertion of residues.
	 31 - 38        Real(8.3)       x             Orthogonal coordinates for X in
	 Angstroms.
	 39 - 46        Real(8.3)       y             Orthogonal coordinates for Y in
	 Angstroms.
	 47 - 54        Real(8.3)       z             Orthogonal coordinates for Z in
	 Angstroms.
	 55 - 60        Real(6.2)       occupancy     Occupancy.
	 61 - 66        Real(6.2)       tempFactor    Temperature factor.
	 73 - 76        LString(4)      segID         Segment identifier, left-justified.
	 77 - 78        LString(2)      element       Element symbol, right-justified.
	 79 - 80        LString(2)      charge        Charge on the atom.
	 </pre>
	 */
	private void  pdb_ATOM_Handler(String line) 
	throws PDBParseException
	{

		atomCount++;

		if ( atomCount == ATOM_CA_THRESHOLD ) {
			// throw away the SEQRES lines - too much to deal with...
			System.err.println("more than " + ATOM_CA_THRESHOLD + " atoms in this structure, ignoring the SEQRES lines");
			seqResChains.clear();

			switchCAOnly();


		}

		if ( atomCount == MAX_ATOMS){
			System.err.println("too many atoms (>"+MAX_ATOMS+"in this protein structure.");
			System.err.println("ignoring lines after: " + line);
			return;
		}
		if ( atomCount > MAX_ATOMS){
			//System.out.println("too many atoms in this protein structure.");
			//System.out.println("ignoring line: " + line);
			return;
		}



		//TODO: treat the following residues as amino acids?
		/*
		MSE Selenomethionine
		CSE Selenocysteine
		PTR Phosphotyrosine
		SEP Phosphoserine
		TPO Phosphothreonine
		HYP 4-hydroxyproline
		5HP Pyroglutamic acid; 5-hydroxyproline
		PCA Pyroglutamic Acid
		LYZ 5-hydroxylysine
		GLX Glu or Gln
		ASX Asp or Asn
		GLA gamma-carboxy-glutamic acid
		 */
		//          1         2         3         4         5         6
		//012345678901234567890123456789012345678901234567890123456789
		//ATOM      1  N   MET     1      20.154  29.699   5.276   1.0
		//ATOM    112  CA  ASP   112      41.017  33.527  28.371  1.00  0.00
		//ATOM     53  CA  MET     7      23.772  33.989 -21.600  1.00  0.00           C  
		//ATOM    112  CA  ASP   112      37.613  26.621  33.571     0     0


		String fullname = line.substring (12, 16);

		// check for CA only if requested
		if ( parseCAOnly){
			// yes , user wants to get CA only
			// only parse CA atoms...
			if (! fullname.equals(" CA ")){
				//System.out.println("ignoring " + line);
				atomCount--;
				return;
			}
		}
		// create new atom
		String recordName = line.substring (0, 6).trim ();
		int pdbnumber = Integer.parseInt (line.substring (6, 11).trim ());
		AtomImpl atom = new AtomImpl() ;
		atom.setPDBserial(pdbnumber) ;

		Character altLoc   = new Character(line.substring (16, 17).charAt(0));

		atom.setAltLoc(altLoc);
		atom.setFullName(fullname) ;
		atom.setName(fullname.trim());

		double x = Double.parseDouble (line.substring (30, 38).trim());
		double y = Double.parseDouble (line.substring (38, 46).trim());
		double z = Double.parseDouble (line.substring (46, 54).trim());

		double[] coords = new double[3];       
		coords[0] = x ;
		coords[1] = y ;
		coords[2] = z ;
		atom.setCoords(coords);

		double occu  = 1.0;
		if ( line.length() > 59 ) {
			try {
				// occu and tempf are sometimes not used :-/
				occu = Double.parseDouble (line.substring (54, 60).trim());
			}  catch (NumberFormatException e){}
		}

		double tempf = 0.0;
		if ( line.length() > 65)  
			try {
				tempf = Double.parseDouble (line.substring (60, 66).trim());
			}  catch (NumberFormatException e){}

			atom.setOccupancy(  occu  );
			atom.setTempFactor( tempf );		


			String chain_id      = line.substring(21,22);
			String residueNumber = line.substring(22,27).trim();
			String groupCode3     = line.substring(17,20);

			Character aminoCode1 = null;
			if ( recordName.equals("ATOM") ){

				aminoCode1 = get1LetterCode(groupCode3);

			}




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


			current_group.addAtom(atom);
			//System.out.println(current_group);


	}


	private void switchCAOnly(){
		parseCAOnly = true;

		current_model = CAConverter.getCAOnly(current_model);

		for ( int i =0; i< structure.nrModels() ; i++){
			//  iterate over all known models ...
			List<Chain> model = structure.getModel(i);
			model = CAConverter.getCAOnly(model);            
			structure.setModel(i,model);
		}

		current_chain = CAConverter.getCAOnly(current_chain);

	}


	/** safes repeating a few lines ... */
	private Integer conect_helper (String line,int start,int end) {
		String sbond = line.substring(start,end).trim();
		int bond  = -1 ;
		Integer b = null ;

		if ( ! sbond.equals("")) {
			bond = Integer.parseInt(sbond);
			b = new Integer(bond);
		}

		return b ;
	}

	/** 
	 Handler for
	 CONECT Record Format 

	 COLUMNS         DATA TYPE        FIELD           DEFINITION
	 ---------------------------------------------------------------------------------
	 1 -  6         Record name      "CONECT"
	 7 - 11         Integer          serial          Atom serial number
	 12 - 16         Integer          serial          Serial number of bonded atom
	 17 - 21         Integer          serial          Serial number of bonded atom
	 22 - 26         Integer          serial          Serial number of bonded atom
	 27 - 31         Integer          serial          Serial number of bonded atom
	 32 - 36         Integer          serial          Serial number of hydrogen bonded
	 atom
	 37 - 41         Integer          serial          Serial number of hydrogen bonded
	 atom
	 42 - 46         Integer          serial          Serial number of salt bridged
	 atom
	 47 - 51         Integer          serial          Serial number of hydrogen bonded
	 atom
	 52 - 56         Integer          serial          Serial number of hydrogen bonded
	 atom
	 57 - 61         Integer          serial          Serial number of salt bridged
	 atom
	 */  
	private void pdb_CONECT_Handler(String line) {
		//System.out.println(line);
		// this try .. catch is e.g. to catch 1gte which has wrongly formatted lines...
		if ( atomOverflow) {
			return ;
		}
		try {
			int atomserial = Integer.parseInt (line.substring(6 ,11).trim());
			Integer bond1      = conect_helper(line,11,16);
			Integer bond2      = conect_helper(line,16,21);
			Integer bond3      = conect_helper(line,21,26);
			Integer bond4      = conect_helper(line,26,31);
			Integer hyd1       = conect_helper(line,31,36);
			Integer hyd2       = conect_helper(line,36,41);
			Integer salt1      = conect_helper(line,41,46);
			Integer hyd3       = conect_helper(line,46,51);
			Integer hyd4       = conect_helper(line,51,56);
			Integer salt2      = conect_helper(line,56,61);

			//System.out.println(atomserial+ " "+ bond1 +" "+bond2+ " " +bond3+" "+bond4+" "+
					//		   hyd1+" "+hyd2 +" "+salt1+" "+hyd3+" "+hyd4+" "+salt2);
			HashMap<String, Integer> cons = new HashMap<String, Integer>();
			cons.put("atomserial",new Integer(atomserial));

			if ( bond1 != null) cons.put("bond1",bond1);
			if ( bond2 != null) cons.put("bond2",bond2);
			if ( bond3 != null) cons.put("bond3",bond3);
			if ( bond4 != null) cons.put("bond4",bond4);
			if ( hyd1  != null) cons.put("hydrogen1",hyd1);
			if ( hyd2  != null) cons.put("hydrogen2",hyd2);
			if ( salt1 != null) cons.put("salt1",salt1);
			if ( hyd3  != null) cons.put("hydrogen3",hyd3);
			if ( hyd4  != null) cons.put("hydrogen4",hyd4);
			if ( salt2 != null) cons.put("salt2",salt2);

			connects.add(cons);
		} catch (Exception e){
			System.err.println("could not parse CONECT line correctly.");
			System.err.println(e.getMessage() + " at line " + line);
			return;
		}
	}

	/*
	 Handler for
	 MODEL Record Format 

	 COLUMNS       DATA TYPE      FIELD         DEFINITION
	 ----------------------------------------------------------------------
	 1 -  6       Record name    "MODEL "
	 11 - 14       Integer        serial        Model serial number.
	 */

	private void pdb_MODEL_Handler(String line) {
		// check beginning of file ...
		if (current_chain != null) {
			if (current_group != null) {
				current_chain.addGroup(current_group);
			}
			//System.out.println("starting new model "+(structure.nrModels()+1));

			Chain ch = isKnownChain(current_chain.getName(),current_model) ;
			if ( ch == null ) {
				current_model.add(current_chain);
			}
			structure.addModel(current_model);
			current_model = new ArrayList<Chain>();
			current_chain = null;
			current_group = null;
		}

	}


	/**
    COLUMNS       DATA TYPE          FIELD          DEFINITION
    ----------------------------------------------------------------
     1 - 6        Record name        "DBREF "
     8 - 11       IDcode             idCode         ID code of this entry.
    13            Character          chainID        Chain identifier.
    15 - 18       Integer            seqBegin       Initial sequence number 
                                                    of the PDB sequence segment.
    19            AChar              insertBegin    Initial insertion code 
                                                    of the PDB sequence segment.
    21 - 24       Integer            seqEnd         Ending sequence number 
                                                    of the PDB sequence segment.
    25            AChar              insertEnd      Ending insertion code 
                                                    of the PDB sequence segment.
    27 - 32       LString            database       Sequence database name. 
    34 - 41       LString            dbAccession    Sequence database accession code.
    43 - 54      LString            dbIdCode        Sequence database 
                                                    identification code.
    56 - 60      Integer            dbseqBegin      Initial sequence number of the
                                                    database seqment.
    61           AChar              idbnsBeg        Insertion code of initial residue
                                                    of the segment, if PDB is the
                                                    reference.
    63 - 67      Integer            dbseqEnd        Ending sequence number of the
                                                    database segment.
    68           AChar              dbinsEnd        Insertion code of the ending
                                                    residue of the segment, if PDB is
                                                    the reference.
	 */
	private void pdb_DBREF_Handler(String line){
		DBRef dbref = new DBRef();
		String idCode      = line.substring(7,11);
		String chainId     = line.substring(12,13);                      
		String seqBegin    = line.substring(14,18);        
		String insertBegin = line.substring(18,19);
		String seqEnd      = line.substring(20,24);
		String insertEnd   = line.substring(24,25);
		String database    = line.substring(26,32);
		String dbAccession = line.substring(33,41);
		String dbIdCode    = line.substring(42,54);
		String dbseqBegin  = line.substring(55,60);
		String idbnsBeg    = line.substring(60,61);
		String dbseqEnd    = line.substring(62,67);
		String dbinsEnd    = line.substring(67,68);

		dbref.setIdCode(idCode);
		dbref.setChainId(chainId.charAt(0));
		dbref.setSeqbegin(intFromString(seqBegin));        
		dbref.setInsertBegin(insertBegin.charAt(0));
		dbref.setSeqEnd(intFromString(seqEnd));
		dbref.setInsertEnd(insertEnd.charAt(0));
		dbref.setDatabase(database.trim());
		dbref.setDbAcession(dbAccession.trim());
		dbref.setDbIdCode(dbIdCode.trim());
		dbref.setDbSeqBegin(intFromString(dbseqBegin));
		dbref.setIdbnsBegin(idbnsBeg.charAt(0));
		dbref.setDbSeqEnd(intFromString(dbseqEnd));
		dbref.setIdbnsEnd(dbinsEnd.charAt(0));

		//System.out.println(dbref.toPDB());
		dbrefs.add(dbref);
	}

	private int intFromString(String intString){
		int val = Integer.MIN_VALUE; 
		try {
			val = Integer.parseInt(intString.trim());
		} catch (NumberFormatException ex){
			ex.printStackTrace();
		}
		return val;
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



	private BufferedReader getBufferedReader(InputStream inStream) 
	throws IOException {

		BufferedReader buf ;
		if (inStream == null) {
			throw new IOException ("input stream is null!");
		}

		buf = new BufferedReader (new InputStreamReader (inStream));
		return buf ;

	}



	/** parse a PDB file and return a datastructure implementing
	 * PDBStructure interface.
	 *
	 * @param inStream  an InputStream object
	 * @return a Structure object
	 * @throws IOException     
	 */
	public Structure parsePDBFile(InputStream inStream) 
	throws IOException
	{

		//System.out.println("preparing buffer");
		BufferedReader buf ;
		try {
			buf = getBufferedReader(inStream);

		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException ("error initializing BufferedReader");
		}
		//System.out.println("done");

		return parsePDBFile(buf);

	}

	/** parse a PDB file and return a datastructure implementing
	 * PDBStructure interface.
	 *
	 * @param buf  a BufferedReader object
	 * @return the Structure object
	 * @throws IOException ...
	 */

	public Structure parsePDBFile(BufferedReader buf) 
	throws IOException 
	{      
		// (re)set structure 

		structure     = new StructureImpl() ;
		current_model = new ArrayList<Chain>();
		seqResChains  = new ArrayList<Chain>();
		current_chain = null           ;
		current_group = null           ;
		header        = init_header();
		pdbHeader     = new PDBHeader();
		connects      = new ArrayList<Map<String,Integer>>();
		continuationField = "";
		continuationString = "";
		current_compound = new Compound();
		molTypeCounter = 1;
		compounds.clear();
		helixList.clear();
		strandList.clear();
		turnList.clear();
		lengthCheck = -1;
		atomCount = 0;
		atomOverflow = false;
		String line = null;
		try {

			line = buf.readLine ();
			String recordName = "";

			// if line is null already for the first time, the buffered Reader had a problem
			if ( line == null ) {
				throw new IOException ("could not parse PDB File, BufferedReader returns null!");
			}



			while (line != null) {

				// System.out.println (">"+line+"<");

				// ignore empty lines     
				if ( line.equals("") || 
						(line.equals(NEWLINE))){

					line = buf.readLine (); 
					continue;
				}


				// ignore short TER and END lines 
				if ( (line.startsWith("TER")) || 
						(line.startsWith("END"))) {

					line = buf.readLine ();
					continue;
				}

				if ( line.length() < 6) {
					System.err.println("found line length < 6. ignoring it. >" + line +"<" );
					line = buf.readLine ();
					continue;
				}

				try {
					recordName = line.substring (0, 6).trim ();

				} catch (StringIndexOutOfBoundsException e){

					System.err.println("StringIndexOutOfBoundsException at line >" + line + "<" + NEWLINE +
					"this does not look like an expected PDB file") ;
					e.printStackTrace();
					throw new StringIndexOutOfBoundsException(e.getMessage());

				}

				//System.out.println(recordName);

				try {
					if      ( recordName.equals("ATOM")  ) pdb_ATOM_Handler  ( line ) ;

					else if ( recordName.equals("SEQRES")) pdb_SEQRES_Handler(line);
					else if ( recordName.equals("HETATM")) pdb_ATOM_Handler  ( line );
					else if ( recordName.equals("MODEL") ) pdb_MODEL_Handler ( line );
					else if ( recordName.equals("HEADER")) pdb_HEADER_Handler( line );
					else if ( recordName.equals("TITLE") ) pdb_TITLE_Handler ( line );
					else if ( recordName.equals("SOURCE")) pdb_SOURCE_Handler( line );
					else if ( recordName.equals("COMPND")) pdb_COMPND_Handler( line );
					else if ( recordName.equals("EXPDTA")) pdb_EXPDTA_Handler( line );
					else if ( recordName.equals("REMARK")) pdb_REMARK_Handler( line );
					else if ( recordName.equals("CONECT")) pdb_CONECT_Handler( line );
					else if ( recordName.equals("REVDAT")) pdb_REVDAT_Handler( line );
					else if ( recordName.equals("DBREF" )) pdb_DBREF_Handler ( line );
					else if ( parseSecStruc) {
						if ( recordName.equals("HELIX") ) pdb_HELIX_Handler (  line ) ;
						else if (recordName.equals("SHEET")) pdb_SHEET_Handler(line ) ;
						else if (recordName.equals("TURN")) pdb_TURN_Handler(   line ) ;
					}
					else {
						// this line type is not supported, yet.
						// we ignore it
					}
				} catch (Exception e){
					// the line is badly formatted, ignore it!
					e.printStackTrace();					
					System.err.println("badly formatted line ... " + line);
				}
				line = buf.readLine ();
			}

			triggerEndFileChecks();

		} catch (Exception e) {
			System.err.println(line);
			e.printStackTrace();
			throw new IOException ("Error parsing PDB file");
		}

		if ( parseSecStruc) 
			setSecStruc();


		return structure;

	}

	private void triggerEndFileChecks(){

		// finish and add ...

		String modDate = (String) header.get("modDate");
		if ( modDate.equals("0000-00-00") ) {
			// modification date = deposition date
			String depositionDate = (String) header.get("depDate");
			header.put("modDate",depositionDate) ;

			if (! depositionDate.equals(modDate)){
				// depDate is 0000-00-00

				try {
					Date dep = dateFormat.parse(depositionDate);
					pdbHeader.setDepDate(dep);
				} catch (ParseException e){
					e.printStackTrace();
				}
			}

		}

		// a problem occured earlier so current_chain = null ...
		// most likely the buffered reader did not provide data ...
		if ( current_chain != null ) {
			current_chain.addGroup(current_group);
			if (isKnownChain(current_chain.getName(),current_model) == null) {
				current_model.add(current_chain);
			}
		}

		structure.addModel(current_model);
		structure.setHeader(header);
		structure.setPDBHeader(pdbHeader);
		structure.setConnections(connects);
		structure.setCompounds(compounds);
		structure.setDBRefs(dbrefs);

		if ( alignSeqRes ){

			SeqRes2AtomAligner aligner = new SeqRes2AtomAligner();
			aligner.align(structure,seqResChains);
		}

		linkChains2Compound(structure);

	}


	private void setSecStruc(){

		setSecElement(helixList,  PDB_AUTHOR_ASSIGNMENT, HELIX  );
		setSecElement(strandList, PDB_AUTHOR_ASSIGNMENT, STRAND );
		setSecElement(turnList,   PDB_AUTHOR_ASSIGNMENT, TURN   );

	}

	private void setSecElement(List<Map<String,String>> secList, String assignment, String type){


		Iterator<Map<String,String>> iter = secList.iterator();
		nextElement:
			while (iter.hasNext()){
				Map<String,String> m = iter.next();

				// assign all residues in this range to this secondary structure type
				// String initResName = (String)m.get("initResName");
				String initChainId = (String)m.get("initChainId");
				String initSeqNum  = (String)m.get("initSeqNum" );
				String initICode   = (String)m.get("initICode" );
				// String endResName  = (String)m.get("endResName" );
				String endChainId  = (String)m.get("endChainId" );
				String endSeqNum   = (String)m.get("endSeqNum");
				String endICode    = (String)m.get("endICode");

				if (initICode.equals(" "))
					initICode = "";
				if (endICode.equals(" "))
					endICode = "";



				GroupIterator gi = new GroupIterator(structure);
				boolean inRange = false;
				while (gi.hasNext()){
					Group g = (Group)gi.next();
					Chain c = g.getParent();

					if (c.getName().equals(initChainId)){

						String pdbCode = initSeqNum + initICode;
						if ( g.getPDBCode().equals(pdbCode)  ) {
							inRange = true;
						}
					}
					if ( inRange){
						if ( g instanceof AminoAcid) {
							AminoAcid aa = (AminoAcid)g;

							Map<String,String> assignmentMap = new HashMap<String,String>();
							assignmentMap.put(assignment,type);
							aa.setSecStruc(assignmentMap);                        
						}

					}
					if ( c.getName().equals(endChainId)){
						String pdbCode = endSeqNum + endICode;
						if (pdbCode.equals(g.getPDBCode())){
							inRange = false;
							continue nextElement;
						}
					}

				}

			}
	}

	public void linkChains2Compound(Structure s){
		List<Compound> compounds = s.getCompounds();

		for(Compound comp : compounds){
			List<Chain> chains = new ArrayList<Chain>();
			List<String> chainIds = comp.getChainId();
			if ( chainIds == null)
				continue;
			for ( String chainId : chainIds) {
				if ( chainId.equals("NULL"))
					chainId = " ";
				try {

					Chain c = s.findChain(chainId);
					chains.add(c);

				} catch (StructureException e){
					// usually if this happens something is wrong with the PDB header
					// e.g. 2brd - there is no Chain A, although it is specified in the header
					e.printStackTrace();
				}
			}
			comp.setChains(chains);
		}

		if ( compounds.size() == 1) {
			Compound comp = compounds.get(0);
			if ( comp.getChainId() == null){
				List<Chain> chains = s.getChains(0);
				if ( chains.size() == 1) {
					// this is an old style PDB file - add the ChainI
					Chain ch = chains.get(0);
					List <String> chainIds = new ArrayList<String>();
					chainIds.add(ch.getName());
					comp.setChainId(chainIds);
					comp.addChain(ch);
				} 
			}
		}

		for (Compound comp: compounds){
			if ( comp.getChainId() == null) {
				// could not link to chain 
				// TODO: should this be allowed to happen?
				continue;
			}
			for ( String chainId : comp.getChainId()){
				try {
					Chain c = s.getChainByPDB(chainId);
					c.setHeader(comp);
				} catch (StructureException e){
					e.printStackTrace();
				}
			}
		}

	}


}
