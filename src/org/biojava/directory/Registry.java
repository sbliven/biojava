package org.biojava.directory;


import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.gff.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.db.biosql.*;


/**
 * Class which provides an instance of a registry
 * This is a singleton
 * Notice this is implementing the Registry Document provided
 * by Ewan Birney 
 *@author Brian Gilman
 *@version $Revision$
 */

public class Registry {

  
    
    /**
     * Configuration for this registry
     */
    private RegistryConfiguration rCongif = null;
    
    /**
     * Instance of this registry to pass around
     */
    private static Registry instance  = null;
 
    /**
     * Instance of a DBFactory which gives back Database objects
     * Backed by particular SequenceDB
     */
    private SequenceDBFactory dbFactory = null;
    
   
    /**
     * Default Constructor
     * Private to satisfy the singleton design pattern 
     */
    private Registry(RegistryConfig rConfig) throws NullPointerException{
	
	if(rConfig == null){
	    throw new NullPointerException("A registry configuration cannot be null");
	}
	
	this.rConfig = rConfig;
    }

    /** 
     * This is a singleton pattern so we use the private constructor
     * public getInstance paradigm
     *@param rConfig a registry configuration object
     *@return An instance of the Registry
     */
    public static Registry getInstance(RegistryConfiguration rConfig) {
	
	if(instance == null){
	    this.instance = new Registry(rConfig);
	}

	return instance;
    }


    /**
     * getDatabase method provides database objects 
     * given a String constrained by the BioDirect Specification
     *@param registryName the database type you are interested in (I wish I had enums!)
     *@return an array of database objects
     */

    public SequenceDB getDatabase(String dbName) throws NoSuchElementException{
	
	dbFactory = SequenceDBFactory.getInstance();
	
	SequenceDB db = null;
	
	try{
	    db = dbFactory.getDatabase(dbName);
	}catch(NullPointerException ne){
	    throw new RegistryException("no database provider by that name");
	}
	
	return db;
    }

    /**
     * TODO:
     * Implement ability to add databases to repository
     *
     * setDatabase(SequenceDB seqDB);
     */
    

}
    
    
