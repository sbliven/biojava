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
 * <p>
 * A registry that loads up the standard biodirectory files.
 * </p>
 *
 * <p>
 * This class will search for the following classes in turn:
 * <ol>
 * <li>~/.bioinformatics/seqdatabase.ini where ~ is the JAVA user home system
 * property</li>
 * <li>/etc/bioinformatics/seqdatabase.ini</li>
 * <li>"http://www.open-bio.net/bioinformatics/seqdatabase.ini</li>
 * </ol>
 * </p>
 *
 * <p>
 * The first file found will be loaded and used as the configuration. There is
 * currently no support for cascading these so that local setting over-ride
 * more general ones.
 * </p>
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @author Keith James
 */

public class SystemRegistry {
  private static Registry systemRegistry;
  
  /**
   * Get the singleton Registry instance representing the system-wide default
   * registry.
   *
   * @return the system-wide Registry object
   */
  public static Registry instance() {
    if (systemRegistry == null) {
      RegistryConfiguration.Composite regConfig
        = new RegistryConfiguration.Composite();
      Iterator i = getRegistryPath().iterator();
      
      while (i.hasNext()) {
        try {
          String locator = (String) i.next();
          BufferedReader stream = new BufferedReader(
            new InputStreamReader(
              new URL(locator).openStream()
            )
          );
          if (stream != null) {
            try {
              RegistryConfiguration cfg
                = OBDARegistryParser.parseRegistry(stream, locator);
              regConfig.addBottomConfig(cfg);
            } catch (Exception ex) {
              ex.printStackTrace(); // FIXME: we should log this or something
            }
          }
        } catch (Exception ex) {} // FIXME: logging?
      }
      
      systemRegistry = new Registry(regConfig);
    }
    
    return systemRegistry;
  }
  
  /**
   * Get the list of places that will be searched for registry files.
   *
   * @return a List of strings that are URLs to bioregistry files
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
