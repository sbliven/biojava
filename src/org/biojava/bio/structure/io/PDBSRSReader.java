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
import java.net.Socket ;

/** reads a PDB file from a local SRS installation using getz Actually
 * is the same as PDBFileReader, but instead of reading from a file stream, reads from a
 * buffered stream.
 *
 * @author Andreas Prlic
 *
*/

public class PDBSRSReader extends PDBFileReader {  
    

    private  BufferedReader getBufferedReader() 
	throws IOException
    {
	// getz -view PDBdata '[pdb:5pti] 
	Socket          client  = null;
	DataInputStream input   = null;
	PrintStream     output  = null;
	String          machine = ""  ;
	int             port    = 0   ;

	String message = "please set System properties PFETCH_machine and PFETCH_port !" ;

	try {
	    machine     = System.getProperty("PFETCH_host");
	    String p    = System.getProperty("PFETCH_port");
	    port        = Integer.parseInt(p);

	} catch ( NullPointerException e) {
	    System.err.println(message);
	    e.printStackTrace();
	    throw new IOException() ;
	} catch (IllegalArgumentException  e) {
	    System.err.println(message);
	    e.printStackTrace(); 
	    throw new IOException() ;
	}

	if (port  == 0 ) {
	    throw new IOException(message);
	}
	if ( (machine.equals(""))) {	   
	    throw new IOException(message); 
	}
	System.out.println("contacting: " + machine + " " + port);
	//Process proc = Runtime.getRuntime().exec(GETZSTRING+argument);
	client = new Socket(machine , port);
	client.setSoTimeout(10000) ; // 10 seconds
	System.out.println("socket o.k.");
	input  = new DataInputStream(client.getInputStream());
	BufferedReader buf = new BufferedReader (new InputStreamReader (input));
	
	System.out.println("sending: --pdb " + pdb_code.toLowerCase());
	output = new PrintStream(client.getOutputStream());	  
	output.println("--pdb "+ pdb_code.toLowerCase());
	output.flush();
	
	// check if return is O.K.
	buf.mark(100);
	String line = buf.readLine();

	buf.reset();
	if ( line.equals("no match")) {
	    System.out.println("first line: " + line );
	    throw new IOException("no pdb with code "+pdb_code.toLowerCase() +" found");	    
	}
	
	return buf ;


    }


     /** load a structure from from SRS installation using wgetz
     * requires pdb_code to be set earlier...
     */
    public synchronized Structure getStructure() 
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
	Structure s = null ;
	try{	    
	    //System.out.println("Starting to parse PDB file " + getTimeStamp());
	    s = parsePDBFile(buf) ;
	    //System.out.println("Done parsing PDB file " + getTimeStamp());
	} catch(Exception ex){
	    ex.printStackTrace();
	}

	notifyAll();
	return s ;
	
    }

    /** open filename and returns
     * a PDBStructure object. 
     * Overrides PDBFileReader.getStructure.
     */
    public synchronized Structure getStructure(String filename) 
	throws IOException
    {
	
	pdb_code = filename ;
	Structure s = null ;
	try {
	    s = getStructure();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new IOException();
	}

	notifyAll();
	return s ;


    }



}
