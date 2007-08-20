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
 */
package org.biojava.bio.structure.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.biojava.bio.structure.AminoAcid;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.GroupIterator;
import org.biojava.bio.structure.Structure;
import org.biojava.utils.io.InputStreamProvider;


/**
 * A PDB file parser.
 * @author Andreas Prlic
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
public class PDBFileReader implements StructureIOFile {

	String path                     ;
	List<String> extensions            ;
	boolean parseSecStruc;
	boolean autoFetch;

	public static void main(String[] args){
		String filename =  "/path/to/5pti.pdb" ;

		PDBFileReader pdbreader = new PDBFileReader();
		pdbreader.setParseSecStruc(true);

		try{
			Structure struc = pdbreader.getStructure(filename);
			System.out.println(struc);

			GroupIterator gi = new GroupIterator(struc);
			while (gi.hasNext()){
				Group g = (Group) gi.next();
				Chain  c = g.getParent();
				if ( g instanceof AminoAcid ){
					AminoAcid aa = (AminoAcid)g;
					Map<String,String> sec = aa.getSecStruc();
					System.out.println(c.getName() + " " + g + " " + sec);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public PDBFileReader() {
		extensions    = new ArrayList<String>();
		path = "" ;
		extensions.add(".ent");
		extensions.add(".pdb");
		extensions.add(".ent.gz");
		extensions.add(".pdb.gz");
		extensions.add(".ent.Z");
		extensions.add(".pdb.Z");
		parseSecStruc = false;
		autoFetch     = false;
	}


	/** should the parser to fetch missing PDB files from the EBI FTP server automatically?
	 *  default is false
	 * @return flag
	 */
	public boolean isAutoFetch() {
		return autoFetch;
	}

	/** tell the parser to fetch missing PDB files from the EBI FTP server automatically.
	 * 
	 * default is false. If true, new PDB files will be automatically stored in the Path and gzip compressed.
	 * 
	 * @param autoFetch
	 */
	public void setAutoFetch(boolean autoFetch) {
		this.autoFetch = autoFetch;
	}


	public boolean isParseSecStruc() {
		return parseSecStruc;
	}


	public void setParseSecStruc(boolean parseSecStruc) {
		this.parseSecStruc = parseSecStruc;
	}


	/** directory where to find PDB files */
	public void setPath(String p){
		path = p ;
	}

	/**
	 * Returns the path value.
	 * @return a String representing the path value
	 * @see #setPath
	 *
	 */
	public String getPath() {
		return path ;
	}

	/** define supported file extensions 
	 * compressed extensions .Z,.gz do not need to be specified
	 * they are dealt with automatically.

	 */
	public void addExtension(String s){
		//System.out.println("add Extension "+s);
		extensions.add(s);
	}




	/** try to find the file in the filesystem and return a filestream in order to parse it 
	 * rules how to find file
	 * - first check: if file is in path specified by PDBpath
	 * - secnd check: if not found check in PDBpath/xy/ where xy is second and third char of PDBcode.
	 * if autoFetch is set it will try to download missing PDB files automatically.
	 */

	private InputStream getInputStream(String pdbId) 
	throws IOException
	{
		//System.out.println("checking file");

		// compression formats supported
		// this has been moved to InputStreamProvider ...

		//String[] str = {".gz",".zip",".Z"};
		//ArrayList  compressions = new ArrayList( Arrays.asList( str ) ); 

		InputStream inputStream =null;

		String pdbFile = null ;
		File f = null ;

		// this are the possible PDB file names...
		String fpath = path+"/"+pdbId;
		String ppath = path +"/pdb"+pdbId;

		String[] paths = new String[]{fpath,ppath};

		for ( int p=0;p<paths.length;p++ ){
			String testpath = paths[p];
			//System.out.println(testpath);
			for (int i=0 ; i<extensions.size();i++){
				String ex = (String)extensions.get(i) ;
				//System.out.println("PDBFileReader testing: "+testpath+ex);
				f = new File(testpath+ex) ;

				if ( f.exists()) {
					//System.out.println("found!");
					pdbFile = testpath+ex ;

					InputStreamProvider isp = new InputStreamProvider();

					inputStream = isp.getInputStream(pdbFile);
					break;
				}

				if ( pdbFile != null) break;        
			}
		}

		if ( pdbFile == null ) {
			if ( autoFetch) 
				return downloadAndGetInputStream(pdbId);

			String message = "no structure with PDB code " + pdbId + " found!" ;
			throw new IOException (message);
		}

		return inputStream ;
	}


	public void gzipCompress(Structure s){

	}


	private File downloadPDB(String pdbId){
	
		File tempFile = new File(path+"/"+pdbId+".pdb.gz");		
		File pdbHome = new File(path);
		
		if ( ! pdbHome.canWrite() ){
			System.err.println("can not write to " + pdbHome);
			return null;
		}
		
		String ftp = String.format("ftp://ftp.ebi.ac.uk/pub/databases/msd/pdb_uncompressed/pdb%s.ent", pdbId.toLowerCase()); 

		System.out.println("Fetching " + ftp); 
		try {
			URL url = new URL(ftp);
			InputStream conn = url.openStream();			

			// prepare destination
			System.out.println("writing to " + tempFile);
			
		
			FileOutputStream outPut = new FileOutputStream(tempFile);
			GZIPOutputStream gzOutPut = new GZIPOutputStream(outPut);
			PrintWriter pw = new PrintWriter(gzOutPut);
			
			BufferedReader fileBuffer = new BufferedReader(new InputStreamReader(conn));
			String line;
			while ((line = fileBuffer.readLine()) != null) {				
				pw.println(line);
			}
			pw.flush();
			pw.close();
			outPut.close();
			conn.close();
		} catch (Exception e){
			e.printStackTrace();
			return null; 
		}
		return tempFile;
	}

	private InputStream downloadAndGetInputStream(String pdbId) 
	throws IOException{
		//PDBURLReader reader = new PDBURLReader();
		//Structure s = reader.getStructureById(pdbId);
		File tmp = downloadPDB(pdbId);
		if ( tmp != null ) {
			InputStreamProvider prov = new InputStreamProvider();
			return prov.getInputStream(tmp);
			

		} else {
			throw new IOException("could not find PDB " + pdbId + " in file system and also could not download");
		}


	}




	/** load a structure from local file system and return a PDBStructure object 

	 * @param pdbId  a String specifying the id value (PDB code)
	 * @return the Structure object
	 * @throws IOException ...
	 */
	public Structure getStructureById(String pdbId) 
	throws IOException
	{


		InputStream inStream = getInputStream(pdbId);

		PDBFileParser pdbpars = new PDBFileParser();
		pdbpars.setParseSecStruc(parseSecStruc);
		Structure struc = pdbpars.parsePDBFile(inStream) ;
		return struc ;
	}

	/** opens filename, parses it and returns
	 * aStructure object .
	 * @param filename  a String
	 * @return the Structure object
	 * @throws IOException ...
	 */
	public Structure getStructure(String filename) 
	throws IOException
	{
		File f = new File(filename);
		return getStructure(f);

	}

	/** opens filename, parses it and returns a Structure object
	 * 
	 * @param filename a File object
	 * @return the Structure object
	 * @throws IOException ...
	 */
	public Structure getStructure(File filename) throws IOException {

		InputStreamProvider isp = new InputStreamProvider();

		InputStream inStream = isp.getInputStream(filename);

		PDBFileParser pdbpars = new PDBFileParser();
		pdbpars.setParseSecStruc(parseSecStruc);
		Structure struc = pdbpars.parsePDBFile(inStream) ;
		return struc ;

	}

}
