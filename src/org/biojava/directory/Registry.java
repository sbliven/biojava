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
 */

package org.biojava.directory;

import java.io.*;
import java.net.URL;
import java.util.*;
import org.biojava.utils.*;
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
 * @author Thomas Down
 * @version $Revision$
 */


public class Registry {
    /**
     * Registry Configuration instance
     */
    private RegistryConfiguration regConfig = null;

    public Registry(RegistryConfiguration regConfig) {
	this.regConfig = regConfig;
    }

    public SequenceDBLite getDatabase(String dbName) throws RegistryException, BioException{
	
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
	    ClassLoader loader = getClass().getClassLoader();
	    Iterator implNames = Services.getImplementationNames(SequenceDBProvider.class, loader).iterator();
	    while (implNames.hasNext()) {
		String className = (String) implNames.next();
		Class clazz = loader.loadClass(className);
		SequenceDBProvider seqDB = (SequenceDBProvider) clazz.newInstance();
		if(seqDB.getName().equals(providerName)){
		    return seqDB;
		}
	    }
	    
	    throw new ProviderNotFoundException("No such provider exists: " + providerName);
	}catch(Exception e){
	    throw new RegistryException(e, "Error accessing SequenceDBProvider services");
	}
    }
    
    public RegistryConfiguration getRegistryConfiguration(){
	return this.regConfig;
    }

}
