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

import java.util.*;
import java.io.*;
import java.net.*;

public class SystemRegistry {
    private static Registry systemRegistry;

    public static Registry instance() {
	if (systemRegistry == null) {
	    String locator = null;
	    BufferedReader stream = null;
	    Iterator i = getRegistryPath().iterator();
	    
	    while (stream == null && i.hasNext()) {
		try {
		    locator = (String) i.next();
		    stream = new BufferedReader(new InputStreamReader(new URL(locator).openStream()));
		} catch (Exception ex) {}
	    }

	    if (stream != null) {
		try {
		    systemRegistry = new Registry(OBDARegistryParser.parseRegistry(stream, locator));
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    } else {
		systemRegistry = new Registry(new RegistryConfiguration.Impl("<none>", Collections.EMPTY_MAP));
	    }
	}

	return systemRegistry;
    }

    public static List getRegistryPath() {
	List registryPath = new ArrayList();
	String userHome = System.getProperty("user.home");
	if (userHome != null) {
	    registryPath.add("file:///" + userHome + "/.bioinformatics/seqdatabase.ini");
	}
	registryPath.add("file:///etc/bioinformatics/seqdatabase.ini");
	registryPath.add("http://www.open-bio.net/bioinformatics/seqdatabase.ini");
	return registryPath;
    }
}
