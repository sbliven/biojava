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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * <p><code>SystemRegistry</code> is used to retrieve a reference to
 * the system OBDA registry.</p>
 *
 * @author Keith James
 */
public class SystemRegistry {
    private static Registry systemRegistry;

    /**
     * <p><code>instance</code> retrieves a registry reference. The
     * registry path is searched in the order specified in the OBDA
     * standard and the first instance found is returned. The path
     * is</p>
     *
     * <pre>
     *    $HOME/.bioinformatics/seqdatabase.ini
     *    /etc/bioinformatics/seqdatabase.ini
     *    http://www.open-bio.org/registry/seqdatabase.ini
     * </pre>
     *
     * @return a <code>Registry</code>.
     */
    public static Registry instance() {
	if (systemRegistry == null) {
	    String locator = null;
	    BufferedReader stream = null;
	    Iterator i = getRegistryPath().iterator();

	    while (stream == null && i.hasNext()) {
		try {
		    locator = (String) i.next();
		    stream = new BufferedReader(new InputStreamReader(new URL(locator).openStream()));
		} catch (Exception ex) {
                    ex.printStackTrace();
                }
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

    /**
     * <code>getRegistryPath</code> returns a <code>List</code> of URL
     * <code>String</code>s.
     *
     * @return a <code>List</code> URL <code>String</code>s.
     */
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
