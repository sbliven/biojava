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
	String key = "";
	String value = "";
	config = new HashMap();
	Map currentDB = null;
	
	while((line = in.readLine()) != null){
	    
	    //System.out.println(line);
	    if(line.trim().length() > 0){
		if(line.indexOf("[") > -1){
		    dbName = line.substring(1, line.indexOf("]"));
		    currentDB = new HashMap();
		    config.put(dbName, currentDB); //instantiate new hashtable 
		    //for this tag
		    
		}else{
		    StringTokenizer strTok = new StringTokenizer(line, "=");
		    //here we assume that there are only key = value pairs in the
		    //config file
		    key = strTok.nextToken();
		    if(strTok.hasMoreTokens()){
			value = strTok.nextToken();
		    }else{
			value = "";
		    }
		    
		    currentDB.put(key.trim(), value.trim());    
		}
	    }
	}
	return config;
    }
}	
    

