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
 * Created on 21.05.2004
 * @author Andreas Prlic
 *
 *
 */

package org.biojava.bio.structure.io;

import org.biojava.bio.structure.* ;
import java.io.*;

/** reads a PDB file from a local SRS installation using getz Actually
 * is the same as PDBFileReader, but instead of reading from a file stream, reads from a
 * buffered stream.
*/

public class PDBSRSReader extends PDBFileReader {  
    
    static String GETZSTRING = "/nfs/team71/phd/ap3/BIN/getz -view PDBdata";
    

    private BufferedReader getBufferedReader() 
	throws IOException
    {
	// e.g. getz -view PDBdata '[pdb:5pti] 

	//String shellcommand = GETZSTRING+pdb_code+"]'" ;
	String argument = " [pdb:" + pdb_code+"]" ;
	//GETZSTRING += argument ;
	System.out.println(GETZSTRING + argument);
	Process proc = Runtime.getRuntime().exec(GETZSTRING+argument);
	System.out.println("command executed");
	// get its output (your input) stream
	
	//DataInputStream instr = new DataInputStream(proc.getInputStream());
	InputStream instr = proc.getInputStream() ;
	BufferedReader buf = new BufferedReader (new InputStreamReader (instr));
	return buf ;


    }


     /** load a structure from from SRS installation using wgetz
     * requires pdb_code to be set earlier...
     */
    public Structure getStructure() 
	throws IOException
    {
	
	BufferedReader buf ;
	//inStream = getInputStream();
	buf = getBufferedReader() ;
	/*String line = buf.readLine ();	
	while (line != null) {
	    System.out.println (line);
	    line = buf.readLine ();

	}
	return null ;
	*/
	try{	    
	    System.out.println("Starting to parse PDB file " + getTimeStamp());
	    parsePDBFile(buf) ;
	    System.out.println("Done parsing PDB file " + getTimeStamp());
	} catch(Exception ex){
	    ex.printStackTrace();
	}

	return structure ;
	
    }

    /** open filename (does not support compressed files!) and returns
     * a PDBStructure object 
     */
    public Structure getStructure(String filename) 
	throws IOException
    {
	
	pdb_code = filename ;

	return getStructure() ;


    }



}
