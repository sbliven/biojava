/*
 Copyright (C) 2005 EBI, GRL

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.ensembl.registry;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.ConfigurationException;
import org.ensembl.driver.ServerDriverFactory;
import org.ensembl.util.StringUtil;

/**
 * A Registry provide convenient programatic access to multiple ensembl
 * databases through DriverGroups.
 * 
 * 
 * <p>
 * <b>Accessing ensembldb.ensembl.org. </b>
 * </p>
 * A default registry providing access to the latest databases on
 * ensembldb.ensembl.org can be created by
 * <code>Registry.createDefaultDriver()</code>. This example shows how to
 * access the latest human and compara databases on that database:
 * 
 * <pre><code>
 * Registry r = Registry.createDefaultDriver();
 * 
 * DriverGroup human = r.getGroupDriver(&quot;human&quot;);
 *       
 *   CoreDriver humanCore = human.getCoreDriver(); 
 *   Iterator genesIter = humanCore.getGeneAdaptor().fetchIterator(new Location(&quot;chromosome:10&quot;));
 *       
 *       
 *   VariationDriver humanVar = human.getVariationDriver();
 *   Iterator vfIter = humanVar.getVariationFeatureAdaptor().fetchIterator(new Location(&quot;chromosome:10&quot;));
 *        
 *   ComparaDriver compara = r.getDriverGroup(&quot;compara&quot;).getComparaDriver();
 *   List dnaDnaAlignFeatures = compara.getDnaDnaAlignFeatureAdaptor().fetch(
 *   &quot;Homo sapiens&quot;, new Location(&quot;chromosome:1:1000000-1200000:0&quot;),
 *   &quot;Mus musculus&quot;, &quot;BLASTZ_NET&quot;); 
 *    
 *   
 *  
 * </code></pre>
 * 
 * <p>
 * <b>Default user registry</b>
 * </p>
 * Databases added to the default user registry file
 * are automatically available via the 
 * default user registry. For example <code>Registry.createDefaultUserRegistry().getGroup("some_group")</code>
 * retrieves the "some_group" DriverGroup defined in the config file. See <a href="#createDefaultUserRegistry()">createDefaultUserRegistry()</a>, 
 * <a href="#getDefaultUserRegistryURL()">getDefaultUserRegistryURL()</a> and
 * <a href="#isDefaultUserRegistryAvailable()">isDefaultUserRegistryAvailable()</a>
 * for more information.
 *
 * <p>
 * <b>Adding registries</b>
 * </p>
 * Users can create additional Registry instances by using registry
 * loaders. For example 
 * <code>new Registry(new RegistryLoaderIni("my_config.ini"))</code>.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 * @see org.ensembl.registry.DriverGroup
 * @see org.ensembl.registry.RegistryLoaderIni
 */
public class Registry {

  private static final Logger logger = Logger.getLogger(Registry.class
      .getName());

  private Map names2GroupDrivers = new HashMap();

  /**
   * Create an empty registry.
   */
  public Registry() {
  }

  /**
   * Create a registry with DriverGroups loaded from loader.
   */
  public Registry(RegistryLoader loader) {
    loader.load(this);
  }

  /**
   * Create a registry pointing at the latest databases on
   * ensembldb.ensembl.org.
   * 
   * It is possible to disable/override the configuration. See
   * getDefaultRegistry() for more information.
   * 
   * @return registry pointing at the latest databases on ensembldb.ensembl.org.
   * @throws IOException
   * @see #getDefaultRegistryURLs()
   */
  public static Registry createDefaultRegistry() throws AdaptorException {
    try {
      return new Registry(new RegistryLoaderIni(getDefaultRegistryURLs(),new ServerDriverFactory(),false));
    } catch (Exception e) {
    throw new AdaptorException(e);
    }
  }

   /**
   * Create a registry from the default user registry file returned by getDefaultUserRegistryFile().
   * 
   * See RegistryLoaderIni for information on the file format.
   *
   * @return registry pointing at the latest databases on ensembldb.ensembl.org.
   * @throws AdaptorException
   * @throws IOException
   * @see RegistryLoaderIni
   * @see #getDefaultUserRegistryURL()
   */
  public static Registry createDefaultUserRegistry() throws AdaptorException{
    
    try {
      return new Registry(new RegistryLoaderIni(getDefaultUserRegistryURL()));
    } catch (Exception e) {
      throw new AdaptorException(e);
    } 
  }
  
  /**
   * Returns the URLs of the configuration files used to initialise the default registry.
   * 
   * The normal registry config files are "resources/data/default_registry_server.ini"
   * and "resources/data/default_registry_groups.ini"
   * which are found on the classpath (usually in ensj.jar). 
   * 
   * These can be overridden by a user created file 
   * USER_HOME_DIRECTORY/.ensembl/user_registry.ini. Although this
   * is generally not necessary (you should use the default user registry to configure
   * access to your own databases) one case where it is useful is if you have trouble
   * connecting to ensembldb.ensembl.org (e.g.due to access/firewall restrictions) 
   * and your program does so automatically. In this case you can 'disable' access by
   * creating an empty USER_HOME_DIRECTORY/.ensembl/user_registry.ini file or  
   * one that defines groups on another database server.
   * 
   * @return urls of the configuration files that will be used initialise the default
   * registry.
   * @throws MalformedURLException
   */
  public static String[] getDefaultRegistryURLs() throws MalformedURLException {
    
    
    String homedir = System.getProperty("user.home");
    if (homedir!=null) {
      String url = homedir + File.separator + ".ensembl" + File.separator + "default_registry.ini"; 
      File file = new File(url);
      if (file.exists())
        return new String[] {file.toURI().toURL().toExternalForm()};
    } else {
      logger.fine("WARNING: Failed to find users home directory. This means that if you have" +
      		"a .ensembl/default_registry.ini file it will be ignored. " +
      		"You can solve this problem by setting JVM parameter user.home=USER_HOME_DIR.");
    }
    
    return new String[] {"resources/data/default_registry_server.ini",
        "resources/data/default_registry_groups.ini"};
    
  }
  
  /**
   * Returns the URL of the default user registry file.
   * 
   * By default this is "file:USER_HOME_DIRECTORY_PATH/.ensembl/user_registry.ini"
   * but this can be overridden by specifying an alternative URL 
   * via the JVM parameter USER_REGISTRY_URL. 
   * e.g. <code>java -DUSER_REGISTRY_URL=file:/tmp/my_registry.ini MyProgram</code>.
   * 
   * @return URL of the default user registry file.
   * @throws MalformedURLException
   */
  public static String getDefaultUserRegistryURL() throws MalformedURLException {
    
    String url = System.getProperty("USER_REGISTRY_URL");
    if (url!=null)
      return url;
    
    // default registry file location.
    String homedir = System.getProperty("user.home");
    if (homedir!=null) { 
      url = homedir + File.separator + ".ensembl" + File.separator + "user_registry.ini";
      File file = new File(url);
      if (file.exists())
        return file.toURL().toExternalForm();
      
    }
    else
      logger.warning("User's home directory is unkown: we might not be able to load user's default registry file. " +
      		"Try setting -Duser.home=SOME_FILE explicitly or -DUSER_REGISTRY_FILE=SOME_FILE.");
    
    return null;
  }
  
  /**
   * Convenience method that checks whether the default user registry is
   * available.
   * @return true if the URL returned by getDefaultUserRegistryURL() can
   * be opened, otherwise false.
   * @see #getDefaultUserRegistryURL()
   */
  public static boolean isDefaultUserRegistryAvailable() {
    try {
      String url = getDefaultUserRegistryURL();
      if (url!=null) {
        URL u = new URL(url);
        u.openStream().close();
        return true;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }
  
  /**
   * Returns the specified DriverGroup.
   * 
   * @param name
   *          group name or alias for a DriverGroup.
   * @return specified DriverGroup, or null if none available.
   * @throws ConfigurationException
   */
  public DriverGroup getGroup(String name) throws ConfigurationException {

    return (DriverGroup) names2GroupDrivers.get(name);
  }

  /**
   * Returns all GroupDrivers.
   * 
   * Note that it is possible to have more groupNames than groups
   * (getGroups().size() <getGroupNames().size()) because a group may have
   * additional aliases.
   * 
   * @return all (zero or more) GroupDrivers.
   * @throws ConfigurationException
   */
  public List getGroups() throws ConfigurationException {

    return new ArrayList(new HashSet(names2GroupDrivers.values()));
  }

  public String toString() {

    StringBuffer buf = new StringBuffer();

    buf.append("[");
    buf.append("nGroupDrivers=").append(names2GroupDrivers.values().size());
    buf.append(", groupDrivers=").append(StringUtil.toString(names2GroupDrivers.keySet()));
    buf.append("]");

    return buf.toString();
  }

  /**
   * Returns the names for all MetaDrivers.
   * 
   * @return zero or more MetaDriverNames.
   */
  public String[] getGroupNames() {
    Set names = names2GroupDrivers.keySet();
    return (String[]) names.toArray(new String[names.size()]);
  }

  /**
   * Close all the connections on all driver groups.
   * 
   * @throws AdaptorException
   */
  public void closeAllConnections() throws AdaptorException {

    for (Iterator iter = names2GroupDrivers.values().iterator(); iter.hasNext();)
      ((DriverGroup) iter.next()).closeAllConnections();

  }

  /**
   * Adds the named group to the registry.
   * 
   * @param name
   *          name of group.
   * @param group
   *          group.
   * @return previous DriverGroup associatted with _name_, can be null.
   */
  public DriverGroup add(String name, DriverGroup group) {
    return (DriverGroup) names2GroupDrivers.put(name, group);
  }

  /**
   * Add the DriverGroups from the loader.
   * 
   * @param loader
   *          RegistryLoader providing zero or more DriverGroups.
   */
  public void add(RegistryLoader loader) {
    loader.load(this);
  }

  /**
   * Removes the mapping from _name_ to it's associated DriverGroup.
   * 
   * @param name
   *          name of the DriverGroup.
   * @return DriverGroup removed, or null if nothing was associated with the
   *         name.
   */
  public DriverGroup remove(String name) {
    return (DriverGroup) names2GroupDrivers.remove(name);
  }

  /**
   * Removes all name to DriverGroup mappings.
   */
  public void clear() {
    names2GroupDrivers.clear();
  }

}