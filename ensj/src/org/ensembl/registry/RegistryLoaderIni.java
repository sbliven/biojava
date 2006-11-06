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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.ConfigurationException;
import org.ensembl.driver.ServerDriver;
import org.ensembl.driver.ServerDriverFactory;
import org.ensembl.util.PropertiesUtil;

/**
 * Loads a registry with DriverGroups defined in "ini" style configuration files.
 * 
 * The loader understands ".ini" style configuration files which define
 * blocks that contain key-value pairs. The blocks define either default values
 * or DriverGroups. For example:
 * 
 * <pre><code>
 *        # default values that will be used by all subsequent groups unless they are overriden.
 *        [default]
 *        host localhost
 *        port 6000
 *        user anonymous
 *        connection_pool_size 4
 *        type species
 *       
 *        [ensembl_compara]
 *        aliases compara
 *        type compara
 *       
 *        # define a 'homo_sapiens' species group
 *        [homo_sapiens]
 *        aliases human
 *        ensembl_prefix ENS
 *       
 *        [cat]
 *        database_prefix tmp_cat_db
 *       
 *        [kangaroo]
 *        core.database kanga_core
 *        variation.database kanga_variation
 *       
 *        # define a rat group that only has a core database but no variation database
 *        [rat]
 *        core.host ensembldb.ensembl.org
 *        core.user anonymous
 *        core.database_prefix rattus_norvegicus_core
 *        user bob
 *        password bobspassword
 *      
 * </code></pre>
 * 
 * Description:
 * <ul>
 * 
 * <li>[block] - if block is "default" then the following properties are used
 * as default values in the following groups. If block is not "default" then it
 * defines a group which should be accessable from the registry. This example
 * defines the groups ensembl_compara, homo_sapiens, cat, kangaroo and rat.
 * <li>type - type of driver group, should be "species" or "compara". Default
 * is "species" if property is ommitted.</li>
 * <li>aliases - list names, separated by white spaces, that can be passed to
 * <code>getDriverGroup(String)</code> to retrieve a DriverGroup. In the
 * example above we can use both "homo_sapiens" and "human" to retrieve the
 * homo_sapiens group.</li>
 * <li>host,port,user,connection_pool_size - values used to specify a database.
 * </li>
 * <li>database_prefix - the prefix used to match the database. If this isn't
 * specified the group name is used instead.</li>
 * <li>core.database_prefix, variation.database_prefix - database name prefixes
 * for the specified EnsemblDrivers. The highest versioned database name
 * matching the prefixes will be used by the respective driver.</li>
 * <li>core.database, variation.database - specific database names to use for
 * the driver.</li>
 * <li>ensembl_prefix - this is the Ensembl stable id prefix for the species
 * <li>schema_version - specifies the version of the schema the database is stored
 * in. This is not needed for released ensembl databases as the version can be determined
 * from the database name. However, if you use ensj with a database that does not follow the
 * ensembl database naming convention of SPECIES_SCHEMA-VERSION_RELEASE you must  
 * tell ensj which schema version it is.
 * </li>
 * 
 * </ul>
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class RegistryLoaderIni implements RegistryLoader {

  private static final Logger logger = Logger.getLogger(RegistryLoaderIni.class
      .getName());

  private Map groups2Names;

  /**
   * Optimisation: We use a ServerDriverFactory to reduce the number of database
   * connections required and the number of fetch database name calls.
   */
  private ServerDriverFactory serverDriverFactory;

  /**
   * Loads DriverGroups for configuration defined in _srcConfig_.
   * 
   * @param config
   *          configuration.
   * @param configDescription
   *          description of configuration.
   * @throws ConfigurationException
   * @throws IOException
   */
  public RegistryLoaderIni(InputStream config, String configDescription)
      throws ConfigurationException, IOException {
    this.serverDriverFactory = new ServerDriverFactory();
    readConfig(config, configDescription);
  }

  /**
   * Loads DriverGroups for configuration stored in byte array.
   * 
   * This can be used for loading configuration values from a a String like
   * this: <code>new RegistryLoaderIni(string.getBytes(), "my config")</code>.
   * 
   * @param config
   *          configuration as byte array.
   * @param configDescription
   *          description of configuration.
   * @throws ConfigurationException
   * @throws IOException
   */
  public RegistryLoaderIni(byte[] config, String configDescription)
      throws ConfigurationException, IOException {
    this.serverDriverFactory = new ServerDriverFactory();
    readConfig(new ByteArrayInputStream(config), configDescription);
  }

  /**
   * Loads DriverGroups for configuration at _url_ 
   * 
   * Providing a serverDriverFactory that is used in other parts of your
   * code can lead to faster performance as the caches only need to be populated
   * once.
   * 
   * @param url
   *          url of configuration file.
   * @param serverDriverFactory
   *          serverDriver factory.
   * @throws IOException
   */
  public RegistryLoaderIni(String url,
      ServerDriverFactory serverDriverFactory) throws IOException {
    this(new String[]{url}, serverDriverFactory, false);
  }

  /**
   * Load driver groups defined in the  _urls_.
   * 
   * <p>The configuration files are concatenated in the order they are provided and the 
   * optional default groups will be appended at the end if included.
   * </p>
   * 
   * <p>Providing a serverDriverFactory that is used in other parts of your
   * code can lead to faster performance as the caches only need to be populated
   * once and connection pooling may be possible between drivers.
   * </p>
   * 
   * <p>One use case for this method is to automatically load all the groups 
   * defined on a database server (based on ensembl database naming conventions).
   * To do this:
   * <ol>
   * 
   * <li>Create a configuration file such as "myconfig.ini" that defines the server
   * parameters and any other 'default' parameters for the drivers. e.g.
   * <pre><code>
 *        [default]
 *        host localhost
 *        port 6000
 *        user anonymous
 *        connection_pool_size 4
 *        type species
   * </code></pre>
   * </li>
   * 
   * <li>Use this constructor <code>new RegistryLoaderIni(new String[]{"myconfig.ini"}, aServerDriverFactory, true)</code>
   * </li>
   * </ol>
   * </p>
   * 
   * @param urls
   *          urls of (possibly partial) configuration files.
   * @param serverDriverFactory
   *          factory of ServerDrivers.  
   * @param includeDefaultGroups whether to include default groups in addition to any 
   * defined in the _urls_.
   * @throws IOException
   */
public RegistryLoaderIni(String[] urls, ServerDriverFactory serverDriverFactory, boolean includeDefaultGroups) throws IOException {
    
  	this.serverDriverFactory = serverDriverFactory;
  	
  	URL[] realURLs = new URL[includeDefaultGroups ? urls.length+1 : urls.length];
  	for (int i = 0; i < urls.length; i++) {
      realURLs[i] = PropertiesUtil.stringToURL(urls[i]);
      if (realURLs[i]==null)
        throw new IllegalArgumentException("Can not find registry file:" + urls[i]);
    }
  	if (includeDefaultGroups) {
  	  String url = "resources/data/default_registry_groups.ini";
  	  realURLs[urls.length] = PropertiesUtil.stringToURL(url);
  	  if (realURLs[urls.length]==null)
  	    throw new IllegalArgumentException("Can not find registry file:" + url);
  	}
  	
  	
  	// concatenate config files in order provided
    
  	StringBuffer srcDescription = new StringBuffer();
    ByteArrayOutputStream config = new ByteArrayOutputStream();
    for (int i = 0; i < realURLs.length; i++) {
      
      if (i>0)
        srcDescription.append(",");
      
      srcDescription.append(realURLs[i].toExternalForm());
      
      InputStream is = new BufferedInputStream(realURLs[i].openStream());
      int b = 0;
      while((b=is.read())!=-1)
        config.write(b);
      is.close();
      config.write('\n');
      
    }
    
    
    // load concatenated config
    InputStream is = new ByteArrayInputStream(config.toByteArray());
    readConfig(is, srcDescription.toString());
    is.close();
    
    this.serverDriverFactory = serverDriverFactory;
  }
  /**
   * Loads DriverGroups for configuration at _url_.
   * 
   * @param url
   *          url of configuration file.
   * @throws IOException
   */
  public RegistryLoaderIni(String url) throws IOException {
    this(new String[]{url}, new ServerDriverFactory(), false);
  }

  /**
   * Loads DriverGroups defined in the configuration file _url_.
   * 
   * @param url
   *          url of configuration file.
   */
  public RegistryLoaderIni(URL url) throws IOException {
    this(new String[]{url.toExternalForm()}, new ServerDriverFactory(), false);
  }

  private void readConfig(InputStream srcConfig, String srcDescription)
      throws ConfigurationException, IOException {
    Map name2Properties = readNames2Properties(
        new InputStreamReader(srcConfig), srcDescription);
    groups2Names = convert2Groups2Names(name2Properties);
  }

  /**
   * Adds a copy of all loaded DriverGroups keyed on name and alias to the
   * _registry_.
   */
  public synchronized void load(Registry registry) {

    for (Iterator iter = groups2Names.keySet().iterator(); iter.hasNext();) {

      DriverGroup dg = (DriverGroup) iter.next();
      String[] names = (String[]) groups2Names.get(dg);

      // prevent the same driver being shared by multiple registries.
      dg = dg.copy();

      // add all the name:group and alias:group mappings to the registry.
      for (int i = 0; i < names.length; i++)
        registry.add(names[i], dg);
    }
  }

  /**
   * Attempt to derive specific database name for _groupName_ from _p_.
   * 
   * @param groupName
   * @param databaseType
   * @param p
   * @param groupConfig
   * @return specific database name if found, otherwise null.
   * @throws AdaptorException
   */
  private String deriveDatabaseFromType(String groupName, String databaseType,
      Properties p, Properties groupConfig) throws AdaptorException {

    ServerDriver serverDriver = serverDriverFactory.get(groupConfig);

    // No ServerDriver can be constructed from the groupConfig
    if (serverDriver==null) return null;
    
    // Determine possible database name prefix
    String dbPrefix = p.getProperty(databaseType + ".database_prefix");
    if (dbPrefix == null) {
      if ("compara".equals(databaseType)) {

        // special case for compara, no need to include _databaseType_
        // database
        // name.
        if (dbPrefix == null)
          dbPrefix = p.getProperty("database_prefix");
        if (dbPrefix == null)
          dbPrefix = groupName;

      } else {

        // General case, try to construct database name prefix using
        // _databaseType_.
        if (dbPrefix == null && p.containsKey("database_prefix"))
          dbPrefix = p.getProperty("database_prefix") + "_" + databaseType;
        if (dbPrefix == null)
          dbPrefix = groupName + "_" + databaseType;
      }
    }
    return serverDriver.highestVersionedDatabaseName(dbPrefix);
  }

  /**
   * Map each group, e.g. "homo_sapiens", to a properties object.
   * 
   * @param r
   *          reader providing contents of configuration file.
   * @param streamLabel
   *          label to be included in debugging output, e.g. configuration file
   *          name.
   * @return map containing zero or more group:properties pairs.
   * @throws IOException
   * @throws ConfigurationException
   */
  private static Map readNames2Properties(Reader r, String streamLabel)
      throws IOException, ConfigurationException {

    Map group2Properties = new HashMap();

    // Iteratate over lines constructing/modifying groups.

    BufferedReader br = new BufferedReader(r);

    // We share a DatabaseNameCacheManager amongst DriverGroups
    // to th minimise number of database connections and queries necessary
    // to determine database names.

    Properties defaultProperties = new Properties();

    Pattern groupRegexp = Pattern.compile("\\[(.*)\\]");
    Pattern nameValueRegexp = Pattern.compile("(\\S+)[\\s\\=]+(\\S+)");

    Properties p = null; // current properties object
    DriverGroup g = null; // current group
    String line = null;
    int lineNum = 0;
    while ((line = br.readLine()) != null) {

      lineNum++;
      line = line.trim();

      if (line.length() == 0 || line.charAt(0) == '#')
        continue;

      String debugDetails = line + " (line " + lineNum + ") " + " in source "
          + streamLabel;

      Matcher m = groupRegexp.matcher(line);
      if (m.matches()) {

        String group = m.group(1);
        if ("default".equals(group)) {

          p = defaultProperties;

        } else {

          p = (Properties) group2Properties.get(group);

          if (p == null) {

            p = new Properties();
            group2Properties.put(group, p);
            // load the default values assigned so far
            p.putAll(defaultProperties);

          }
        }
      } else {

        if (p == null)
          throw new ConfigurationException(
              "Must specify group before property: " + debugDetails);

        Matcher nvm = nameValueRegexp.matcher(line);

        if (nvm.matches()) {

          String name = nvm.group(1);
          String value = nvm.group(2);
          p.put(name, value);

        } else {
          throw new ConfigurationException(
              "Must specify \"name value\" for property: " + debugDetails);
        }

      }
    }

    return group2Properties;
  }

  /**
   * Creates an EnsemblDriver configuration from a DriverGroup configuration.
   * 
   * Supports autoload and auto delete databases. All autoload databases that do
   * not have a "database" paramter have one automatically assigned and are
   * marked for automatic deletion by the addition of a parameter
   * "autoload.temporary" = "true".
   * 
   * 
   * @param groupName
   *          name of the group e.g. "homo_sapiens".
   * @param databaseType
   *          type of database e.g. "core", "variation", "compara".
   * @param groupConfig
   *          configuration partially defining a DriverGroup.
   * @return configuration for an EnsemblDriver, or null if one cannot be
   *         created from the parameters.
   * @throws AdaptorException
   * @see org.ensembl.driver.CoreDriver
   * @see org.ensembl.variation.driver.VariationDriver
   * @see org.ensembl.compara.driver.ComparaDriver
   */
  private Properties createEnsemblDriverConfig(String groupName,
      String databaseType, Properties groupConfig,
      ServerDriverFactory serverDriverFactory) throws AdaptorException {

    Properties modifiedGroupConfig = PropertiesUtil.removePrefixFromKeys(groupConfig,
        databaseType);

    String databaseName = modifiedGroupConfig.getProperty("database");
    if (databaseName != null)
      return modifiedGroupConfig;

    databaseName = deriveDatabaseNameFromAutoload(modifiedGroupConfig);
    if (databaseName != null) {
      modifiedGroupConfig.put("database", databaseName);
      // mark database for automatic deletion unless this set in config file
      modifiedGroupConfig.put("autoload.permanent", modifiedGroupConfig.getProperty("autoload.permanent", "false"));
      return modifiedGroupConfig;
    }

    databaseName = deriveDatabaseFromType(groupName, databaseType, groupConfig,
        modifiedGroupConfig);
    if (databaseName != null) {
      modifiedGroupConfig.put("database", databaseName);
      return modifiedGroupConfig;
    }

    return null;

  }

  /**
   * Attempt to derive database name from "autoload" parameters.
   * 
   * @param config
   *          driver configuration.
   * @return database name, or null if "autoload" parameter not in config.
   */
  private static String deriveDatabaseNameFromAutoload(Properties config) {

    String autoload = config.getProperty("autoload");
    if (autoload == null)
      return null;

    String user = System.getProperty("user.name");
    if (user == null)
      user = "unkown_user";

    autoload = autoload.replace('/', '_');

    return user + "_testdb_" + autoload;
  }

  /**
   * Converts map containing name:property pairs into group:names pairs.
   * 
   * @param groups2Properties
   *          zero or more name:Properties pairs.
   * @return
   * @throws AdaptorException
   */
  private Map convert2Groups2Names(Map groups2Properties)
      throws AdaptorException {

    Map g2n = new HashMap();

    for (Iterator iter = groups2Properties.entrySet().iterator(); iter
        .hasNext();) {

      Map.Entry entry = (Map.Entry) iter.next();
      String name = (String) entry.getKey();
      Properties p = (Properties) entry.getValue();

      // create coreConfig, variationContig, comparaConfig
      DriverGroup g = new DriverGroup();
      String type = p.getProperty("type");

      if ("compara".equals(type)) {

        g.setComparaConfig(createEnsemblDriverConfig(name, "compara", p,
            serverDriverFactory));
      } else {

        g.setCoreConfig(createEnsemblDriverConfig(name, "core", p,
            serverDriverFactory));
        g.setVariationConfig(createEnsemblDriverConfig(name, "variation", p,
            serverDriverFactory));
      }

      if (g.hasConfigs()) {

        String[] names = null;
        String aliasesStr = p.getProperty("aliases");
        if (aliasesStr != null) {
          String[] aliases = aliasesStr.split("\\s+");
          names = new String[aliases.length + 1];
          names[0] = name;
          System.arraycopy(aliases, 0, names, 1, aliases.length);
        } else {
          names = new String[] { name };
        }
        g2n.put(g, names);

      } else {

        logger.warning("No drivers for " + name + " = " + p);
      }

    }

    return g2n;
  }

}