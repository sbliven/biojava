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
import java.util.*;


/**
 * This class encapsulates all the parsing of the registry configuration 
 * file
 *
 * @author Brian Gilman
 * @author Thomas Down
 * @version $Revision$
 */

public class OBDARegistryParser {
    public static RegistryConfiguration parseRegistry(BufferedReader in,
						      String locator)
        throws IOException, RegistryException
    {
	String line = "";
	String dbName = "";
	String key = "";
	String value = "";
	Map config = new HashMap();
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
	return new RegistryConfiguration.Impl(locator, Collections.unmodifiableMap(config));
    }
}
