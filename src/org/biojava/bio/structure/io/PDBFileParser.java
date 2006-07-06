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
 * Created on 16.03.2004
 * @author Andreas Prlic
 *
 *
 * some interesting PDB files ...
 * /nfs/disk100/pdb/pdbent/pdb1dw9.ent
 * chain a first group -> selenomethionine
 */
package org.biojava.bio.structure.io;

import org.biojava.bio.structure.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.io.SymbolTokenization ;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Calendar;



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
    ArrayList extensions            ;
    
    // required for parsing:
    StructureImpl structure      ;
    ArrayList     current_model  ;
    ChainImpl     current_chain  ;
    Group         current_group  ;
    
    // for conversion 3code 1code
    SymbolTokenization threeLetter ;
    SymbolTokenization oneLetter ;
    
    String nucleotides[] ;
    String NEWLINE;
    HashMap   header ;
    ArrayList connects ;
    
    public PDBFileParser() {
        extensions    = new ArrayList();
        structure     = null           ;
        current_model = new ArrayList();
        current_chain = null           ;
        current_group = null           ;
        header = init_header() ;
        connects = new ArrayList() ;
        
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
    
    
    
    
    
    /** initialize the header. */
    private HashMap init_header(){
        
        
        HashMap header = new HashMap ();
        header.put ("idCode","");		
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
    
    /* test if the threlettercode of an ATOM entry corresponds to a
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
        String idCode          = line.substring (62, 66).trim() ;       
        
        header.put("idCode",idCode);
        structure.setPDBCode(idCode);
        header.put("classification",classification);
        header.put("depDate",deposition_date);
        
        
        structure.setPDBCode(idCode);
        //setId(idCode);
        
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
                e.printStackTrace() ;
                return ;
            }
            header.put("resolution",new Float(res));
        }
        
    }
    
    
    /** Handler for REMARK lines 
     */
    private void pdb_REMARK_Handler(String line) {
        String l = line.substring(0,10).trim();
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
     
     */
    private void  pdb_ATOM_Handler(String line) 
    throws PDBParseException
    {
        //System.out.println(line);
        
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
        
        
        double occu  = Double.parseDouble (line.substring (54, 60).trim());
        double tempf = Double.parseDouble (line.substring (60, 66).trim());
        
        atom.setOccupancy(  occu  );
        atom.setTempFactor( tempf );
        
        //System.out.println(atom);
        
        // add the atom to the structure
        //structure.addAtom(atom,line);
        
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
                    System.out.println("unknown amino acid"+groupCode3 );
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
            // stupid to re-convert the ints to string, but hashmap does not allow to put ints ...
            // shout they be converted to Integer ?
            if ( bond1 != null) cons.put("bond1",bond1);
            if ( bond2 != null) cons.put("bond2",bond2);
            if ( bond3 != null) cons.put("bond3",bond3);
            if ( bond4 != null) cons.put("bond4",bond4);
            if ( hyd1  != null) cons.put("hydrogen1",hyd1);
            if ( hyd2  != null) cons.put("hydrogen2",hyd2);
            if ( salt1 != null) cons.put("salt1",salt1);
            if ( hyd3  != null) cons.put("hydrogen3",hyd3);
            if ( hyd4  != null) cons.put("hydrogen4",hyd4);
            if ( salt1 != null) cons.put("salt2",salt2);
            
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
     * @throws IOException ...
     * @throws PDBParseException ...
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
     * @throws PDBParseException ...
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
        
        String line = null;
        try {
            
            line = buf.readLine ();
            String recordName = "";
            
            // if line is null already for the first time, the bufferede Reader had a problem
            if ( line == null ) {
                throw new IOException ("could not parse PDB File, BufferedReader returns null!");
            }
            
       
            
            while (line != null) {
//              System.out.println (">"+line+"<");
                if ( line.equals("") || (line.equals(NEWLINE))){
                    // ignore empty lines that some people have in their (local) PDB files
                    
                    //System.out.println("ignoring");
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
                if      ( recordName.equals("ATOM")  ) pdb_ATOM_Handler  ( line ) ;
                else if ( recordName.equals("HETATM")) pdb_ATOM_Handler  ( line ) ;
                else if ( recordName.equals("MODEL") ) pdb_MODEL_Handler ( line ) ;
                else if ( recordName.equals("HEADER")) pdb_HEADER_Handler( line ) ;
                else if ( recordName.equals("TITLE") ) pdb_TITLE_Handler ( line ) ;
                else if ( recordName.equals("EXPDTA")) pdb_EXPDTA_Handler( line ) ;
                else if ( recordName.equals("REMARK")) pdb_REMARK_Handler( line ) ;
                else if ( recordName.equals("CONECT")) pdb_CONECT_Handler( line ) ;
                else if ( recordName.equals("REVDAT")) pdb_REVDAT_Handler( line ) ;
                else {
                    // this line type is not supported, yet.
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
        
        
        return structure;
        
    }
    
    
    
    
}
