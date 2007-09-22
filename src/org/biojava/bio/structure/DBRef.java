/*
 *                  BioJava development code
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
 * Created on Sep 3, 2007
 *
 */
package org.biojava.bio.structure;

import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.Locale;


/** A class to represent database cross references. This is just a simple bean that contains the infor from one 
 * DBREF line
 * 
 * @author Andreas Prlic
 * @since 4:56:14 PM
 * @version %I% %G%
 */
public class DBRef {

	Structure parent;
	String idCode;
    Character chainId;
    int seqbegin;
    char insertBegin;
    int seqEnd;
    char insertEnd;

    String database;
    String dbAcession;
    String dbIdCode;

    int dbSeqBegin;
    char idbnsBegin;
    int dbSeqEnd;
    char idbnsEnd;

    private Long id;
    
    public DBRef() {

    }
    
    /** get the ID used by Hibernate
     * 
     * @return the ID used by Hibernate
     */
    public Long getId() {
        return id;
    }

    /** set the ID used by Hibernate
     * 
     * @param id
     */ 
    private void setId(Long id) {
        this.id = id;
    }

    public void setParent(Structure s){
    	parent = s;
    
    }
    public Structure getParent(){
    	return parent;
    }
    
    public String toPDB(){
        
        StringBuilder build = new StringBuilder();
        Formatter form = new Formatter(build,Locale.UK);
        
         form.format("DBREF %4s %1s %4d%1s %4d%1s %6s %8s %12s %5d%1c %5d%1c ", 
                 idCode, chainId,seqbegin,insertBegin,seqEnd,insertEnd,
                 database,dbAcession,dbIdCode,
                 dbSeqBegin,idbnsBegin,dbSeqEnd,idbnsEnd
                 );
        
         return build.toString();
        
    }

    public String toString(){
        StringBuffer buf = new StringBuffer();
        
        try {
            
            Class c = Class.forName("org.biojava.bio.structure.DBRef");
            Method[] methods  = c.getMethods();
            
            for (int i = 0; i < methods.length; i++) {
                Method m = methods[i];     
                
                String name = m.getName();
                
                if ( name.substring(0,3).equals("get")) {                   
                    if (name.equals("getClass"))
                            continue;
                    Object o  = m.invoke(this, new Object[]{});
                    if ( o != null){
                        buf.append(name.substring(3,name.length()));
                        buf.append(": " + o + " ");
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        
        return buf.toString();
    }
    
    
    /** get the idCode for this entry
     * 
     * @return the idCode
     */
    public String getIdCode() {
		return idCode;
	}

    /** set the idCode for this entry
     * 
     * @param idCode
     */
	public void setIdCode(String idCode) {
		this.idCode = idCode;
	}

	/** the chain ID of the corresponding chain
     * 
     * @return chainId
     */
    public Character getChainId() {
        return chainId;
    }


    public void setChainId(Character chainId) {
        this.chainId = chainId;
    }


    /** the database of the db-ref. 
     * uses the abbreviation as provided in the PDB files:
     * 
     *<pre>   Database name                         database 
                                     (code in columns 27 - 32)
    ----------------------------------------------------------
    GenBank                               GB
    Protein Data Bank                     PDB
    Protein Identification Resource       PIR
    SWISS-PROT                            SWS
    TREMBL                                TREMBL
    UNIPROT                               UNP
    </pre>
     * @return name of database of this DBRef
     */
    public String getDatabase() {
        return database;
    }


    public void setDatabase(String database) {
        this.database = database;
    }

    /* Sequence database accession code */
    public String getDbAcession() {
        return dbAcession;
    }


    public void setDbAcession(String dbAcession) {
        this.dbAcession = dbAcession;
    }


    /** Sequence database          identification code.
     * 
     * @return the dbIdCode
     */
    public String getDbIdCode() {
        return dbIdCode;
    }


    public void setDbIdCode(String dbIdCode) {
        this.dbIdCode = dbIdCode;
    }

    /** Initial sequence number of the
    database seqment.
     * @return position
     */
    public int getDbSeqBegin() {
        return dbSeqBegin;
    }


    public void setDbSeqBegin(int dbSeqBegin) {
        this.dbSeqBegin = dbSeqBegin;
    }


    /** Ending sequence number of the database segment.
     * @return dbSeqEnd
     */
    public int getDbSeqEnd() {
        return dbSeqEnd;
    }


    public void setDbSeqEnd(int dbSeqEnd) {
        this.dbSeqEnd = dbSeqEnd;
    }

    /** Insertion code of initial residue of the segment, if PDB is the
    reference. 
     * @return idbnsBegin*/
    public char getIdbnsBegin() {
        return idbnsBegin;
    }


    public void setIdbnsBegin(char idbnsBegin) {
        this.idbnsBegin = idbnsBegin;
    }

    /** Insertion code of the ending
    residue of the segment, if PDB is
    the reference.
     * @return idbnsEnd
     */
    public char getIdbnsEnd() {    	
        return idbnsEnd;
    }


    public void setIdbnsEnd(char idbnsEnd) {
        this.idbnsEnd = idbnsEnd;
    }

    /** Initial insertion code of the PDB sequence segment.
     * 
     * @return insertBegin
     */

    public char getInsertBegin() {
        return insertBegin;
    }


    public void setInsertBegin(char insertBegin) {
        this.insertBegin = insertBegin;
    }

    /** Ending insertion code of the PDB sequence segment.
     * 
     * @return insertEnd
     */
    public char getInsertEnd() {    
        return insertEnd;
    }


    public void setInsertEnd(char insertEnd) {
        this.insertEnd = insertEnd;
    }

    /**   Initial sequence number of the PDB sequence segment.
     * 
     * @return start seq. position
     */
    public int getSeqbegin() {
        return seqbegin;
    }


    public void setSeqbegin(int seqbegin) {
        this.seqbegin = seqbegin;
    }

    /**Ending sequence number   of the PDB sequence segment.
     * 
     * @return sequence end position
     */
    public int getSeqEnd() {
        return seqEnd;
    }


    public void setSeqEnd(int seqEnd) {
        this.seqEnd = seqEnd;
    }






}
