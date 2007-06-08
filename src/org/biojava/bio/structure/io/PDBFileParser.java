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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.structure.AminoAcid;
import org.biojava.bio.structure.AminoAcidImpl;
import org.biojava.bio.structure.AtomImpl;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.ChainImpl;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.GroupIterator;
import org.biojava.bio.structure.HetatomImpl;
import org.biojava.bio.structure.NucleotideImpl;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureImpl;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;




/**
 * A PDB file parser.
 * @author Andreas Prlic
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

    String path                     ;
    List extensions            ;

    // required for parsing:
    StructureImpl structure      ;
    List     current_model  ;
    ChainImpl     current_chain  ;
    Group         current_group  ;

    // for conversion 3code 1code
    SymbolTokenization threeLetter ;
    SymbolTokenization oneLetter ;

    String nucleotides[] ;
    String NEWLINE;
    Map   header ;
    List connects ;
    List helixList;
    List strandList;
    List turnList;
    
    boolean parseSecStruc;
    
    public static String idCode = "idCode";

    public static final String PDB_AUTHOR_ASSIGNMENT = "PDB_AUTHOR_ASSIGNMENT";
    public static final String HELIX  = "HELIX";
    public static final String STRAND = "STRAND";
    public static final String TURN   = "TURN";
    
    public PDBFileParser() {
        extensions    = new ArrayList();
        structure     = null           ;
        current_model = new ArrayList();
        current_chain = null           ;
        current_group = null           ;
        header        = init_header() ;
        connects      = new ArrayList() ;
        parseSecStruc = false;
        helixList     = new ArrayList();
        strandList    = new ArrayList();
        turnList      = new ArrayList();
        
        NEWLINE = System.getProperty("line.separator");

        Alphabet alpha_prot = ProteinTools.getAlphabet();

        try {
            threeLetter = alpha_prot.getTokenization("name");
            oneLetter  = alpha_prot.getTokenization("token");
        } catch (Exception e) {
            e.printStackTrace() ;
        }

        // store nucleic acids (C, G, A, T, U, and I), and 
        // the modified versions of nucleic acids (+C, +G, +A, +T, +U, and +I), and 
        nucleotides = new String[]{"C","G","A","T","U","I","+C","+G","+A","+T","+U","+I"};


    }
    
    /** is secondary structure assignment being parsed from the file?
     * defauls is null
     * @return boolean if HELIX STRAND and TURN fields are being parsed
     */
    public boolean isParseSecStruc() {
        return parseSecStruc;
    }

    /** a flag to tell the parser to parse the Author's secondary structure assignment from the file
     * defaul is set to false, i.e. do NOT parse.
     * @param parseSecStruc
     */
    public void setParseSecStruc(boolean parseSecStruc) {
        this.parseSecStruc = parseSecStruc;
    }


    /** initialize the header. */
    private HashMap init_header(){


        HashMap header = new HashMap ();
        header.put (idCode,"");		
        header.put ("classification","")         ;
        header.put ("depDate","0000-00-00");
        header.put ("title","");
        header.put ("technique","");
        header.put ("resolution","");
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

        for (int i=0;i<nucleotides.length;i++){
            String n=nucleotides[i];
            //System.out.print("isNucleotide?"+groupCode3.trim()+" "+n);
            if (groupCode3.trim().equals(n))	{
                //System.out.println("yes!") ;
                return true ;
            }
            //System.out.println("no...") ;
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

        header.put(idCode,pdbCode);
        structure.setPDBCode(pdbCode);
        header.put("classification",classification);
        header.put("depDate",deposition_date);
      
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
        
        Map m = new HashMap();
        
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
        
        Map m = new HashMap();
        
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
        
        Map m = new HashMap();
        
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
        }
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
        }

    }


    /** Handler for REMARK lines 
     */
    private void pdb_REMARK_Handler(String line) {
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
        //System.out.println(line);


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
        String recordName = line.substring (0, 6).trim ();
        // create new atom
        AtomImpl atom = new AtomImpl() ;

        int pdbnumber = Integer.parseInt (line.substring (6, 11).trim ());
        atom.setPDBserial(pdbnumber) ;

        String fullname = line.substring (12, 16);

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
                testchain = isKnownChain(current_chain.getName());
                if ( testchain == null) {
                    current_model.add(current_chain);		
                }


                //see if chain_id of new residue is one of the previous chains ...
                testchain = isKnownChain(chain_id);
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
            HashMap cons = new HashMap();
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
            e.printStackTrace();
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

            Chain ch = isKnownChain(current_chain.getName()) ;
            if ( ch == null ) {
                current_model.add(current_chain);
            }
            structure.addModel(current_model);
            current_model = new ArrayList();
            current_chain = null;
            current_group = null;
        }

    }

    /** test if the chain is already known (is in current_model
     * ArrayList) and if yes, returns the chain 
     * if no -> returns null
     */
    private Chain isKnownChain(String chainID){
        Chain testchain =null;
        Chain retchain =null;
        //System.out.println("isKnownCHain: >"+chainID+"< current_chains:"+current_model.size());

        for (int i = 0; i< current_model.size();i++){
            testchain = (Chain) current_model.get(i);
            //System.out.println("comparing chainID >"+chainID+"< against testchain " + i+" >" +testchain.getName()+"<");
            if (chainID.equals(testchain.getName())) {
                //System.out.println("chain "+ chainID+" already known ...");
                retchain = testchain;
                break ;
            }
        }
        //if (retchain == null) {
        //    System.out.println("unknownCHain!");
        //}
        return retchain;
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
        current_model = new ArrayList();
        current_chain = null           ;
        current_group = null           ;
        header        = init_header();
        connects      = new ArrayList();
        helixList.clear();
        strandList.clear();
        turnList.clear();
        
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
                    else if ( recordName.equals("HETATM")) pdb_ATOM_Handler  ( line ) ;
                    else if ( recordName.equals("MODEL") ) pdb_MODEL_Handler ( line ) ;
                    else if ( recordName.equals("HEADER")) pdb_HEADER_Handler( line ) ;
                    else if ( recordName.equals("TITLE") ) pdb_TITLE_Handler ( line ) ;
                    else if ( recordName.equals("EXPDTA")) pdb_EXPDTA_Handler( line ) ;
                    else if ( recordName.equals("REMARK")) pdb_REMARK_Handler( line ) ;
                    else if ( recordName.equals("CONECT")) pdb_CONECT_Handler( line ) ;
                    else if ( recordName.equals("REVDAT")) pdb_REVDAT_Handler( line ) ;
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

            // finish and add ...

            String modDate = (String) header.get("modDate");
            if ( modDate.equals("0000-00-00") ) {
                // modification date = deposition date
                String depositionDate = (String) header.get("depDate");
                header.put("modDate",depositionDate) ;
            }

            // a problem occured earlier so current_chain = null ...
            // most likely the buffered reader did not provide data ...
            if ( current_chain != null ) {
                current_chain.addGroup(current_group);
                if (isKnownChain(current_chain.getName()) == null) {
                    current_model.add(current_chain);
                }
            }


            structure.addModel(current_model);
            structure.setHeader(header);
            structure.setConnections(connects);
        } catch (Exception e) {
            System.err.println(line);
            e.printStackTrace();
            throw new IOException ("Error parsing PDB file");
        }

        if ( parseSecStruc) 
            setSecStruc();
        

        return structure;

    }


    private void setSecStruc(){
        
        setSecElement(helixList,  PDB_AUTHOR_ASSIGNMENT, HELIX  );
        setSecElement(strandList, PDB_AUTHOR_ASSIGNMENT, STRAND );
        setSecElement(turnList,   PDB_AUTHOR_ASSIGNMENT, TURN   );
        
    }
    
    private void setSecElement(List secList, String assignment, String type){
        
        
        Iterator iter = secList.iterator();
        nextElement:
        while (iter.hasNext()){
            Map m = (Map) iter.next();
            
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
                       
                        Map assignmentMap = new HashMap();
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


}
