package org.biojava.directory;
import java.io.*;
import java.net.URL;
import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.gff.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.db.biosql.*;

/**
 * SequsnceDBFactory is a factory which gets implementations
 * of the biojava SequenceDB interface
 *
 * @author Brian Gilman
 * @version $Revision$
 */


public class SequenceDBFactory {

    /**
     * SequenceDB handle
     */
    private SequenceDB seqDB = null;
    /**
     * Private Instance to satisfy singleton paradigm
     */
    private static SequenceDBFactory instance = null;
    
    /**
     * Registry Configuration instance
     */
    private RegistryConfiguration regConfig = null;

    
    private SequenceDBFactory(){
	
    }
    
    public static SequenceDBFactory getInstance(){
	if(instance == null){
	    instance = new SequenceDBFactory();
	}
	
	return instance;
    }

    public SequenceDB getDatabase(String dbName) throws RegistryException, BioException{
	
	Map dbConfig = null;
	String providerName = "";
	
	try{
	    
	    dbConfig = (Map) getRegistryConfiguration().getConfiguration().get(dbName);
	    
	    if (dbConfig == null) {
		throw new RegistryException("Couldn't find a configuration for database: " +dbName);
	    }
	    
	    providerName = (String) dbConfig.get("protocol");
	    
	}catch(Exception e){
	    throw new RegistryException("File for configuration cannot be found: " + e.toString());
	}
	    return getProvider(providerName).getSequenceDB(dbConfig);
	
	
    }
    
    private  SequenceDBProvider getProvider(String providerName) throws RegistryException{
	
	try{
	    
	    //BufferedReader to load classes
	    BufferedReader reader = null;
	    Class clazz = null;
	    ClassLoader loader = this.getClass().getClassLoader();
	    
	    Enumeration services = getClass().getClassLoader().getResources("META-INF/services/org.biojava.directory.SequenceDBProvider");
	    
	    SequenceDBProvider seqDB = null;
	    
	    while (services.hasMoreElements()) {
		//need to look for this implementation on the classpath and 
		//load it with ClassLoader
		
		URL provider = (URL)services.nextElement();
		
		reader  = new BufferedReader(new InputStreamReader(provider.openStream()));
		
		String className = "";
		
		while((className= reader.readLine()) != null){
		    System.out.println(className);
		    clazz = loader.loadClass(className);
		    seqDB = (SequenceDBProvider) clazz.newInstance();
		    if(seqDB.getName().equals(providerName)){
			return seqDB;
		    }
		}
		
	    }
	    
	    throw new ProviderNotFoundException("No such provider exists: " + providerName);
	    
	}catch(Exception e){
	    throw new RegistryException(e.toString());
	}
    }
    
    
    public void setRegistryConfiguration(RegistryConfiguration regConfig){
	this.regConfig = regConfig;
    }

    public RegistryConfiguration getRegistryConfiguration(){
	return this.regConfig;
    }

}
