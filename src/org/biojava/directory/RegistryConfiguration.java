package org.biojava.directory;

import java.io.*;
import java.util.*;


/**
 * This class encapsulates all the parsing of the registry configuration 
 * file
 *@author Brian Gilman
 *@version $Revision$
 */


public class RegistryConfiguration{

    private String configFileLocation = null;
    private HashMap config = null;
    
    public RegistryConfiguration(String configFileLocation){

	this.configFileLocation = configFileLocation;
    }

    public HashMap getConfiguration() throws FileNotFoundException, IOException{
	
	BufferedReader in = new BufferedReader(new FileReader(configFileLocation));
	String line = "";
	String dbName = "";
	HashMap params = null;
	config = new HashMap();
	
	while((line = in.readLine()) != null){
	    
	    if(line.indexOf("[") > -1){
		dbName = line.substring(1, line.indexOf("]"));
		
		System.out.println(dbName);
		config.put(dbName, new HashMap()); //instantiate new hashtable 
		//for this tag
	    }else{
		StringTokenizer strTok = new StringTokenizer(line, "=");
		//here we assume that there are only key = value pairs in the
		//config file
		String key = strTok.nextToken();
		String value = strTok.nextToken();
		System.out.println(key + " " + value);
		
		if(config.containsKey(dbName)){
		    config.put(dbName, params.put(key, value));
		}else{
		    params = new HashMap();
		}

	    }
	}
	return config;
    }
}	
    

