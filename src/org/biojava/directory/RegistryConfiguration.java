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
 * @author Brian Gilman
 * @version $Revision$
 */


public interface RegistryConfiguration {
    public Map getConfiguration() throws RegistryException;
    public String getConfigLocator();

    public static class Impl implements RegistryConfiguration {
	private String configFileLocation = null;
	private Map config = null;
	
	public Impl(String configFileLocation, Map config){
	    this.configFileLocation = configFileLocation;
	    this.config = config;
	}

	public Map getConfiguration() {
	    return config;
	}

	public String getConfigLocator() {
	    return configFileLocation;
	}
    }
}
