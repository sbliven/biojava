package org.biojava.bio.structure.align.util;

import java.io.IOException;


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biojava.bio.structure.Atom;

import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;

import org.biojava.bio.structure.StructureTools;
import org.biojava.bio.structure.io.FileParsingParameters;
import org.biojava.bio.structure.io.LocalCacheStructureProvider;
import org.biojava.bio.structure.io.PDBFileReader;
import org.biojava.bio.structure.io.StructureProvider;
import org.biojava.bio.structure.scop.ScopDomain;
import org.biojava.bio.structure.scop.ScopInstallation;
import org.biojava3.core.util.InputStreamProvider;



/** A utility class that provides easy access to Structure objects. If you are running a
 *  script that is frequently re-using the same PDB structures, the AtomCache keeps an 
 *  in-memory cache of the files for quicker access. The cache is a soft-cache, this 
 *  means it won't cause out of memory exceptions, but garbage collects the data if the 
 *  Java virtual machine needs to free up space. The AtomCache is thread-safe.
 * 
 * @author Andreas Prlic
 * @since 3.0
 */
public class AtomCache {

	public static final String CHAIN_NR_SYMBOL = ":";
	
	public static final String CHAIN_SPLIT_SYMBOL = ".";

	private static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	String path;


	// make sure IDs are loaded uniquely
	Collection<String> currentlyLoading = Collections.synchronizedCollection(new TreeSet<String>());

	private static ScopInstallation scopInstallation ;
	boolean autoFetch;
	boolean isSplit;
	boolean strictSCOP;
	FileParsingParameters params;

	/** Creates an instance of an AtomCache that is pointed to the a particular
	 * path in the file system.
	 * 
	 * @param pdbFilePath a directory in the file system to use as a location to cache files.
	 * @param isSplit a flag to indicate if the directory organisation is "split" as on the PDB ftp servers, or if all files are contained in one directory.
	 */
	public AtomCache(String pdbFilePath, boolean isSplit){

		
		if ( ! pdbFilePath.endsWith(FILE_SEPARATOR))
			pdbFilePath += FILE_SEPARATOR;
		
		// we are caching the binary files that contain the PDBs gzipped
		// that is the most memory efficient way of caching...
		// set the input stream provider to caching mode
		System.setProperty(InputStreamProvider.CACHE_PROPERTY, "true");

		path = pdbFilePath;


		//this.cache = cache;
		this.isSplit = isSplit;
		
		autoFetch = true;
		currentlyLoading.clear();
		params = new FileParsingParameters();

		// we don't need this here
		params.setAlignSeqRes(false);
		// no secstruc either 
		params.setParseSecStruc(false);
		// 

		this.strictSCOP = true;
		
		scopInstallation = null;
	}

	/** Creates a new AtomCache object based on the provided UserConfiguration.
	 * 
	 * @param config the UserConfiguration to use for this cache.
	 */
	public AtomCache(UserConfiguration config){
		this(config.getPdbFilePath(),config.isSplit());
		autoFetch = config.getAutoFetch();
	}


	/** Get the path that is used to cache PDB files.
	 * 
	 * @return path to a directory
	 */
	public String getPath() {
		return path;
	}

	/** Set the path that is used to cache PDB files.
	 * 
	 * @param path to a directory
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/** Is the organization of files within the directory split, as on the PDB FTP servers,
	 * or are all files contained in one directory.
	 * @return flag 
	 */
	public boolean isSplit() {
		return isSplit;
	}

	/** Is the organization of files within the directory split, as on the PDB FTP servers,
	 * or are all files contained in one directory.
	 * @param isSplit flag 
	 */
	public void setSplit(boolean isSplit) {
		this.isSplit = isSplit;
	}

	/** Does the cache automatically download files that are missing from the local installation from the PDB FTP site?
	 * 
	 * @return flag
	 */
	public boolean isAutoFetch() {
		return autoFetch;
	}

	/** Does the cache automatically download files that are missing from the local installation from the PDB FTP site?
	 * 
	 * @param autoFetch flag
	 */
	public void setAutoFetch(boolean autoFetch) {
		this.autoFetch = autoFetch;
	}

	
	/**
	 * Reports whether strict scop naming will be enforced, or whether this AtomCache
	 * should try to guess some simple variants on scop domains.
	 * @return true if scop names should be used strictly with no guessing
	 */
	public boolean isStrictSCOP() {
		return strictSCOP;
	}

	/**
	 * When strictSCOP is enabled, SCOP domain identifiers (eg 'd1gbga_') are
	 * matched literally to the SCOP database.
	 * 
	 * When disabled, some simple mistakes are corrected automatically.
	 * For instance, the invalid identifier 'd1gbg__' would be corrected to 'd1gbga_' automatically.
	 * @param strictSCOP Indicates whether strict scop names should be used.
	 */
	public void setStrictSCOP(boolean strictSCOP) {
		this.strictSCOP = strictSCOP;
	}

	/** Returns the representation of a ScopDomain as a BioJava Structure object
	 * 
	 * @param domain a scop domain
	 * @return a Structure object.
	 * @throws IOException
	 * @throws StructureException
	 */

	public Structure getStructureForDomain(ScopDomain domain) throws IOException, StructureException{


		Structure s = null;

		String pdbId = domain.getPdbId();

		try {
			s = getStructure(pdbId);

		} catch (StructureException ex){
			System.err.println("error getting Structure for " + pdbId);

			throw new StructureException(ex);
		}


		String range = "(";
		int rangePos = 0;
		for ( String r : domain.getRanges()) {
			rangePos++;
			range+= r;
			if ( ( domain.getRanges().size()> 1) && (rangePos < domain.getRanges().size())){
				range+=",";
			}

		}
		range+=")";
		//System.out.println("getting range for "+ pdbId + " " + range);

		Structure n = StructureTools.getSubRanges(s, range);

		// get free ligands of first chain...
		if ( n.getChains().size()> 0) {
			Chain c1 = n.getChains().get(0);
			for ( Chain c : s.getChains()) {
				if ( c1.getChainID().equals(c.getChainID())) {
					List<Group> ligands = c.getAtomLigands();

					for(Group g: ligands){
						if ( ! c1.getAtomGroups().contains(g)) {
							c1.addGroup(g);
						}
					}
				}

			}
		}
		n.setName(domain.getScopId());
		n.setPDBCode(domain.getScopId());

		return n;
	}


	/** Returns the CA atoms for the provided name. See {@link #getStructure(String)} for supported naming conventions.
	 * 
	 * @param name
	 * @return an array of Atoms. 
	 * @throws IOException
	 * @throws StructureException
	 */
	public  Atom[] getAtoms(String name) throws IOException,StructureException{
		// synchronizing the whole method now to prevent the same PDB file to be loaded multiple times

		Atom[] atoms = null;

		//System.out.println("loading " + name);
		Structure s = null;
		try {
			s = getStructure(name);

		} catch (StructureException ex){
			System.err.println("error getting Structure for " + name);
			throw new StructureException(ex);
		}

		atoms =  StructureTools.getAtomCAArray(s);

		/*synchronized (cache){
			cache.put(name, atoms);
		}*/


		return atoms;
	}


	/** Returns the CA atoms for the provided name. See {@link #getStructure(String)} for supported naming conventions.
	 * 
	 * @param name
	 * @param clone flag to make  sure that the atoms are getting coned
	 * @return an array of Atoms. 
	 * @throws IOException
	 * @throws StructureException
	 * @deprecated does the same as {@link #getAtoms(String)} ;
	 */
	public  Atom[] getAtoms(String name, boolean clone)throws IOException,StructureException{
		Atom[] atoms =  getAtoms(name);

		if ( clone)
			return StructureTools.cloneCAArray(atoms);
		return atoms; 

	}




	/** Request a Structure based on a <i>name</i>.
	 * The following rules are applied to this name:
	 *  If only a PDB code is provided, the whole structure will be used for the alignment.
	 *  <ul>
	 *	<li>To specify a particular chain write as: 4hhb.A (chain IDs are case sensitive, PDB ids are not)</li>
	 * 	<li>To specify that the 1st chain in a structure should be used write: 4hhb:0 .</li>
	 *  <li>To specify a SCOP domain write a scopId e.g. d2bq6a1. Some flexibility can be allowed in SCOP domain names, see {@link #setStrictSCOP(boolean)}</li>
	 *  </ul>
	 *  
	 * @param name
	 * @return a Structure object
	 * @throws IOException
	 * @throws StructureException
	 */

	public Structure getStructure(String name) throws IOException, StructureException{

		if ( name.length() < 4)
			throw new IllegalArgumentException("Can't interpred IDs that are shorter than 4 residues!");


		//loading.set(true);


		Structure n = null;

		boolean useChainNr = false;
		boolean useDomainInfo = false;
		String range = null;
		int chainNr = -1;

		try {


			String pdbId   = null;
			String chainId = null;

			if ( name.length() == 4){

				pdbId = name; 

			} else if ( name.startsWith("d")){


				// looks like a SCOP domain!
				ScopDomain domain;
				if( this.strictSCOP) {
					domain = getScopDomain(name);
				} else {
					domain = guessScopDomain(name);
				}
				if ( domain != null){
					Structure s = getStructureForDomain(domain);
					return s;
				}
			
				if( !this.strictSCOP) {
					Matcher scopMatch = scopIDregex.matcher(name);
					if( scopMatch.matches() ) {
						String pdbID = scopMatch.group(1);
						String chainID = scopMatch.group(2);

						// None of the actual SCOP domains match. Guess that '_' means 'whole chain'
						if( !chainID.equals("_") ) {
							//Add chain identifier
							pdbID += "."+scopMatch.group(2);
						}
						// Fetch the structure by pdb id
						return getStructure(pdbID);
					}
				}
									
				throw new StructureException("Unable to get structure for SCOP domain: "+name);
			
			} else if (name.length() == 6){
				pdbId = name.substring(0,4);
				if ( name.substring(4,5).equals(CHAIN_SPLIT_SYMBOL)) {
					chainId = name.substring(5,6);
				} else if ( name.substring(4,5).equals(CHAIN_NR_SYMBOL)) {

					useChainNr = true;	
					chainNr = Integer.parseInt(name.substring(5,6));
				}
			} else if ( (name.length() > 6) &&  
					(name.contains(CHAIN_NR_SYMBOL))) {
				pdbId = name.substring(0,4);
				// this ID has domain split information...
				useDomainInfo = true;
				range = name.substring(5);
			}

			//System.out.println("got: " + name + " " + pdbId + " " + chainId + " useChainNr:" + useChainNr + " " +chainNr + " useDomainInfo:" + useDomainInfo + " " + range);

			if (pdbId == null) {

				return null;
			}

			while ( checkLoading(pdbId) ){
				// waiting for loading to be finished...

				try {
					Thread.sleep(100);
				} catch (InterruptedException e){
					System.err.println(e.getMessage());
				}

			}


			//long start  = System.currentTimeMillis();

			Structure s;




			flagLoading(pdbId);
			try {
				PDBFileReader reader = new PDBFileReader();
				reader.setPath(path);
				reader.setPdbDirectorySplit(isSplit);
				reader.setAutoFetch(autoFetch);

				reader.setFileParsingParameters(params);

				s = reader.getStructureById(pdbId.toLowerCase());

			} catch (Exception e){
				flagLoadingFinished(pdbId);
				throw new StructureException(e.getMessage() + " while parsing " + pdbId,e);
			}
			flagLoadingFinished(pdbId);

			//long end  = System.currentTimeMillis();
			//System.out.println("time to load " + pdbId + " " + (end-start) + "\t  size :" + StructureTools.getNrAtoms(s) + "\t cached: " + cache.size());
			if ( chainId == null && chainNr < 0 && range == null) {								
				// we only want the 1st model in this case
				return StructureTools.getReducedStructure(s,-1);

			}


			if ( useChainNr) {
				//System.out.println("using ChainNr");
				n = StructureTools.getReducedStructure(s, chainNr);
			} else if ( useDomainInfo) {
				//System.out.println("calling getSubRanges");
				n = StructureTools.getSubRanges(s, range);
			} else  {
				//System.out.println("reducing Chain Id " + chainId);
				n = StructureTools.getReducedStructure(s, chainId);
			}


		} catch (Exception e){

			e.printStackTrace();

			throw new StructureException(e.getMessage() + " while parsing " + name,e);

		}

		return n;


	}

	private static final Pattern scopIDregex = Pattern.compile("d(....)(.)(.)" );
	/**
	 * <p>Guess a scop domain. If an exact match is found, return that.
	 * 
	 * <p>Otherwise, return the first scop domain found for the specified protein
	 * such that<ul>
	 *   <li>The chains match, or one of the chains is '_' or '.'.
	 *   <li>The domains match, or one of the domains is '_'.
	 * </ul>
	 *   
	 *   
	 * @param name
	 * @return
	 * @throws IOException
	 * @throws StructureException
	 */
	private ScopDomain guessScopDomain(String name) throws IOException, StructureException {

		// Try exact match first
		ScopDomain domain = getScopDomain(name);
		if ( domain != null){
			return domain;
		}

		// Didn't work. Guess it!
		System.err.println("Warning, could not find SCOP domain: " + name);

		Matcher scopMatch = scopIDregex.matcher(name);
		if( scopMatch.matches() ) {
			String pdbID = scopMatch.group(1);
			String chainID = scopMatch.group(2);
			String domainID = scopMatch.group(3);

			if ( scopInstallation == null) {
				scopInstallation = new ScopInstallation(path);
			}

			for( ScopDomain potentialSCOP : scopInstallation.getDomainsForPDB(pdbID) ) {
				Matcher potMatch = scopIDregex.matcher(potentialSCOP.getScopId());
				if(potMatch.matches()) {
					if( chainID.equals(potMatch.group(2)) ||
							chainID.equals("_") || chainID.equals(".") ||
							potMatch.group(2).equals("_") || potMatch.group(2).equals(".") ) {
						if( domainID.equals(potMatch.group(3)) || domainID.equals("_") || potMatch.group(3).equals("_") ) {
							// Match, or near match
							System.err.println("Trying domain "+potentialSCOP.getScopId());
							return potentialSCOP;
						}
					}
				}
			}
		}

		// Give up.
		return null;
	}

	private  boolean checkLoading(String name) {
		return  currentlyLoading.contains(name);

	}

	private  void flagLoading(String name){
		if ( ! currentlyLoading.contains(name))	
			currentlyLoading.add(name);
	}

	private  void flagLoadingFinished(String name){
		currentlyLoading.remove(name);   
	}

	private ScopDomain getScopDomain(String scopId)
	{

		if ( scopInstallation == null) {
			scopInstallation = new ScopInstallation(path);
		}

		return scopInstallation.getDomainByScopID(scopId);
	}
	public ScopInstallation getScopInstallation() {
		if ( scopInstallation == null) {
			scopInstallation = new ScopInstallation(path);
		}
		
		return scopInstallation;
	}

	public FileParsingParameters getFileParsingParams()
	{
		return params;
	}

	public void setFileParsingParams(FileParsingParameters params)
	{
		this.params = params;
	}

	
	/** Loads the biological unit
	 * 
	 * @param pdbId the PDB ID
	 * @return a structure object
	 * @throws IOException  
	 * @throws StructureException 
	 */
	public Structure getBiologicalUnit(String pdbId) throws StructureException, IOException{
		StructureProvider provider = new LocalCacheStructureProvider();
		return provider.getBiologicalUnit(pdbId);
	}






}
