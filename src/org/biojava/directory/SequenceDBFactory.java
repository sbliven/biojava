package org.biojava.directory;
import java.io.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.program.gff.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.db.biosql.*;

/**
 * SequsnceDBFactory is a factory which gets implementations
 * of the biojava SequenceDB interface
 *@author Brian Gilman
 @Design Pattern Singleton, Factory
 @version $Revision$
*/


public class SequenceDBFactory {

    /**
     * SequenceDB handle
     */
    private SequenceDB seqDB = null;
    /**
     * Private Instance to satisfy singleton paradigm
     */
    private SequenceDBFactory instance = null;
    
    /**
     * Registry Configuration instance
     */
    private RegistryConfiguration regConfig = null;

    
    private SequenceDBFactory(RegistryConfiguration regConfig){
	this.regConfig = regConfig;
    }
    
    public SequenceDBFactory getInstance(RegistryConfiguration regConfig){
	if(instance == null){
	    instance = new SequenceDBFactory(regConfig);
	}
	
	return instance;
    }

    public SequenceDB getDatabase(String providerName){
	// TODO:
	// make this handle multiple different providers
	// Call Thomas' provider interface
	// Below is for testing purposes only
	SequenceDB seqDB = new BioSQLSequenceDB("jdbc:mysql://192.168.0.95/biosql_test";, 
						"root", 
						"", 
						gbvrl, 
						false, 
						new MySQLDBHelper());
	return seqDB;
	
    }
}
