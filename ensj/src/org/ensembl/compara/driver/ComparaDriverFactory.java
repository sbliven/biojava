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

package org.ensembl.compara.driver;

import java.util.Properties;
import java.util.logging.Logger;

import org.ensembl.compara.driver.impl.ComparaDriverImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.util.PropertiesUtil;

/**
 * Factory class that creates ensembl compara driver implementation instances.
 * 
 * <p>
 * Setting the environment variable "ensj.debug" (e.g. java -Densj.debug ... )
 * causes the database connection string to be displayed when the driver first
 * attempts to connect to the database. This is useful when you want to check
 * which database is being connected to.
 * </p>
 * 
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 * @see <a href="../../../ensembl/Example.java.html">Example.java source </a>
 * @see CoreDriver
 */
public class ComparaDriverFactory {

  private static final Logger logger = Logger
      .getLogger(ComparaDriverFactory.class.getName());

  private ComparaDriverFactory() {
  }

  /**
   * Creates a core driver connected to the specified database.
   * 
   * e.g. <code>
   * <pre>
   * CoreDriver coreDriver = ComparaDriverFactory(&quot;ensembldb.ensembl.org&quot;, 3306,
   *     &quot;homo_sapiens_core_24_34e&quot;, &quot;anonymous&quot;, null);
   * </pre>
   * </code>
   * 
   * @param host
   *          computer hosting database server.
   * @param port
   *          port database server is running on.
   * @param database
   *          database name.
   * @param user
   *          user name.
   * @param password
   *          password, null if not needed.
   */
  public static ComparaDriver createComparaDriver(String host, int port,
      String database, String user, String password) throws AdaptorException {
    
    Properties config = new Properties();
    config.put("host", host);
    config.put("database", database);
    config.put("user", user);
    config.put("password", password);
    config.put("port", Integer.toString(port));
    
    return new ComparaDriverImpl(config);

  }

  /**
   * Create a core driver connected to the newest database matching the prefix.
   * 
   * This is useful if you always want the 'latest' of a dataset such as
   * homo_sapiens_core.
   * 
   * e.g. <code>
   * <pre>
   * 
   * ComparaDriver latestDriver = ComparaDriverFactory
   *     .createComparaDriverWithPrefix(&quot;ensembldb.ensembl.org&quot;, 3306,
   *         &quot;homo_sapiens_core&quot;, &quot;anonymous&quot;, null);
   * 
   * 
   * </pre>
   * </code>
   * 
   * @param host
   *          computer hosting database server.
   * @param port
   *          port database server is running on.
   * @param databasePrefix
   *          database prefix. The database name matching the database prefix
   *          with the highest version is selected.
   * @param user
   *          user name.
   * @param password
   *          password, null if not needed..
   */
  public static ComparaDriver createComparaDriverUsingDatabasePrefix(
      String host, int port, String databasePrefix, String user, String password)
      throws AdaptorException {
    
    Properties config = new Properties();
    config.put("host", host);
    config.put("database_prefix", databasePrefix);
    config.put("user", user);
    config.put("password", password);
    config.put("port", Integer.toString(port));
    
    return new ComparaDriverImpl(config);
  }

  /**
   * Create a core driver and configure it with the properties object.
   * 
   * e.g. <code>
   * <pre>
   * Properties config = new Properties();
   * config.put(&quot;host&quot;, &quot;ensembldb.ensembl.org&quot;);
   * config.put(&quot;database&quot;, &quot;SOME_core_DATABASE&quot;);
   * config.put(&quot;user&quot;, &quot;USER_NAME&quot;);
   * ComparaDriver driver = ComparaDriverFactory.createComparaDriver(config);
   * </pre>
   * </code>
   * 
   * <p>
   * These are the necessary and optional parameters for configuring a
   * ComparaDriver instance:
   * 
   * <ul>
   * <b>Necessary configuration parameters with example values: </b>
   * <li>host = ensembldb.ensembl.org
   * <li>either (a)database = SOME_core_DATABASE or (b)database_prefix =
   * SOME_DATABASE_PREFIX
   * <li>user = USER_NAME
   * </ul>
   * 
   * <ul>
   * <b>Optional (advanced) configuration parameters with default values: </b>
   * 
   * <li>port = 3306
   * <li>password = ""
   * <li>jdbc_driver = com.mysql.jdbc.Driver
   * <li>connection_pool_size = 4 (min is 2)
   * 
   * <li>It is possible to specify per adaptor database configurations so each
   * adaptor can connect to a different databases. Adaptor specific parameters
   * begin with the adaptors type (<code>someAdaptor.getType()</code>)
   * followed by a "." then the connection parameter. For example, if you wish
   * to retrieve sequence from the SEQUENCE_DATABASE on the same host as the
   * rest of the adaptors you would add this property: sequence.database =
   * SEQUENCE_DATABASE. If it is on a different host then add these parameters:
   * sequence.host = SEQUENCE_HOST, sequence.user = SEQUENCE_USER
   * 
   * </ul>
   * 
   * @param config
   *          configuration parameters.
   * @throws AdaptorException
   */
  public static ComparaDriver createComparaDriver(Properties config)
      throws AdaptorException {
    return new ComparaDriverImpl(config);
  }

  /**
   * Create a core driver configured with the parameters in the specified
   * configuration properties file.
   * 
   * The configuration file should match the java.util.Properties format which
   * defines name value pairs separated by white space or "=". Each pair is on a
   * separate line.
   * 
   * For example if we have a file called "driver.properties": <code> 
   * <pre>
   * 
   *  
   *         host ensembldb.ensembl.org 
   *         database SOME_core_DATABASE 
   *         user USER_NAME
   *   
   *  
   * </pre>
   * </code>
   * 
   * Then we could create a driver instance from it like this: <code>
   * <pre>
   * ComparaDriver driver = ComparaDriverFactory
   *     .createComparaDriver(&quot;driver.properties&quot;);
   * </pre>
   * </code>
   * 
   * <p>
   * The file is loaded as a Properties object and passed to <a
   * href="#createComparaDriver(java.util.Properties)">createComparaDriver(java.util.Properties)
   * </a>. See which details the possible parameters.
   * </p>
   * 
   * <p>
   * The advantage of this approach over hard coding the constructor parameters
   * or properties into the code is flexibility. It makes it possible to change
   * the datasource an application uses without changing and recompiling the
   * code.
   * </p>
   * 
   * @param filepath
   *          filepath to a properties file.
   * @throws AdaptorException
   * @see #createComparaDriver(Properties)
   */
  public static ComparaDriver createComparaDriver(String filepath)
      throws AdaptorException {
    Properties config = PropertiesUtil.createProperties(filepath);
    if (filepath == null)
      throw new AdaptorException("Couldn't open configuration file: "
          + filepath);
    return createComparaDriver(config);
  }

}