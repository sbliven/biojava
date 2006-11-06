/*
  Copyright (C) 2003 EBI, GRL

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
package org.ensembl.driver;

import java.util.Properties;
import java.util.logging.Logger;

import org.ensembl.util.PropertiesUtil;
import org.ensembl.variation.driver.VariationDriver;

/**
 * Utility 
 * for creating drivers from a Properties format configuration files.
 * 
 * <ul>The files can be specifed as:
 *  
 *   <li>Filepaths. Relative to current working directory or absolute.
 * 
 *   <li>Resources. File paths relative to the classpath.
 * 
 *   <li>URLs.
 * 
 * </ul> 
 * @see java.util.Properties
 * <!--deprecated Since version 29.3, use Factory classes instead e.g. org.ensembl.driver.CoreDriverImpl
 * and org.ensembl.variation.driver.VariationDriverImpl.-->
 * */
public class DriverManager {

  //  static {
  //    // Guarantee Logging system configured before we use it.
  //    if (!LoggingManager.isConfigured())
  //      LoggingManager.configure();
  //  }

  private static final Logger logger =
    Logger.getLogger(DriverManager.class.getName());

  /**
   * Prevent default public constructor being created.
   *
   */
  private DriverManager() {
  }

  /**
   * Creates an instance of the driver specified by the ensembl_driver
   * parameter in the configuration and calls driver.initialise(configuration).
   *
   * <p>If the property "ensembl_driver" is ommitted the default
   *  "org.ensembl.driver.impl.CoreDriverImpl".
   * 
   * @param driverConfig configuration for a specific driver.
   * @see org.ensembl.driver.impl.CoreDriverImpl
   * @see org.ensembl.driver.CoreDriver
   * @throws ConfigurationException if it fails to create and instance of the
   * driver or the driver can not be configured, for example due to missing parameters.
   * 
   */
  public static CoreDriver loadDriver(Properties driverConfig)
    throws ConfigurationException {

    String driverClassName = driverConfig.getProperty("ensembl_driver");
    if (driverClassName == null) {
      driverClassName = "org.ensembl.driver.impl.CoreDriverImpl";
      driverConfig.put(
        "ensembl_driver",
        "org.ensembl.driver.impl.CoreDriverImpl");
      logger.fine("Defaulting to driver " + driverClassName);
    }

    return (CoreDriver) loadEnsemblDriver(driverConfig);
  }

  /**
   * Creates an instance of the driver specified by the ensembl_driver
   * parameter in the configuration and calls driver.initialise(configuration).
   *
   * <p>If the property "ensembl_driver" is ommitted the default
   *  "org.ensembl.variation.driver.impl.VariationDriverImpl" is used.
   *  
   * @param driverConfig configuration for a specific driver.
   * @see org.ensembl.variation.driver.impl.VariationDriverImpl
   * @see org.ensembl.driver.CoreDriver
   * @throws ConfigurationException if it fails to create and instance of the
   * driver or the driver can not be configured, for example due to missing parameters.
   * 
   */
  public static VariationDriver loadVariationDriver(Properties driverConfig)
    throws ConfigurationException {

    String driverClassName = driverConfig.getProperty("ensembl_driver");
    if (driverClassName == null) {
      driverClassName =
        "org.ensembl.variation.driver.impl.VariationDriverImpl";
      driverConfig.put("ensembl_driver", driverClassName);
      logger.fine("Defaulting to driver " + driverClassName);
    }

    return (VariationDriver) loadEnsemblDriver(driverConfig);
  }

  /**
   * Creates an instance of the driver specified by the ensembl_driver
   * parameter in the configuration and calls driver.initialise(configuration).
   *
   * <p>If the property "ensembl_driver" is ommitted the default
   *  "org.ensembl.variation.driver.impl.MySQLVariationDriver" is used.
   *  
   * @param fileReference filepath or url, url can be relative to classpath
   * @see org.ensembl.variation.driver.impl.VariationDriverImpl
   * @see org.ensembl.driver.CoreDriver
   * @throws ConfigurationException if it fails to create and instance of the
   * driver or the driver can not be configured, for example due to missing parameters.
   * 
   */
  public static VariationDriver loadVariationDriver(String fileReference)
    throws ConfigurationException {
    return (VariationDriver) loadVariationDriver(
      PropertiesUtil.createProperties(fileReference));
  }

  /**
     * Creates an instance of the driver specified by the ensembl_driver
     * parameter in the configuration and calls driver.initialise(configuration).
     * 
     * @param driverConfig filepath or url, url can be relative to classpath
     * @return driver configured with parameters in file.
     * @throws ConfigurationException
     */

  public static EnsemblDriver loadEnsemblDriver(Properties driverConfig)
    throws ConfigurationException {

    EnsemblDriver driver = null;

    try {

      String driverClassName = driverConfig.getProperty("ensembl_driver");
      logger.fine("About to create driver : " + driverClassName);
      driver = (EnsemblDriver) Class.forName(driverClassName).newInstance();
      logger.fine("Initialising " + driverClassName + "...");
      driver.initialise(driverConfig);
      logger.fine("Initialised.");

      // CoreDriver assigns it's own path.
      String driverName = driver.getConfiguration().getProperty("name");
      if (driverName == null) {
        throw new ConfigurationException(
          "property 'name' " + "not set in config file(s).");
      }

      logger.fine(
        "Configured driver : "
          + driverClassName
          + ", with parameters "
          + driverConfig);

    } catch (AdaptorException e) {
      throw new ConfigurationException(
        "Couldn't create driver from properties : " + driverConfig,
        e);
    } catch (InstantiationException e) {
      throw new ConfigurationException(
        "Couldn't create driver from properties : " + driverConfig,
        e);
    } catch (IllegalAccessException e) {
      throw new ConfigurationException(
        "Couldn't create driver from properties : " + driverConfig,
        e);
    } catch (ClassNotFoundException e) {
      throw new ConfigurationException(
        "Couldn't create driver from properties : " + driverConfig,
        e);
    }

    logger.fine("Finsihed");
    return driver;

  }

  /**
   * Creates an instance of the driver specified by the ensembl_driver
   * parameter in the configuration and calls driver.initialise(configuration).
   *
   * <p>If the property "ensembl_driver" is ommitted the default of
   *  "org.ensembl.driver.impl.CoreDriverImpl" is used.
   * 
   * This is a synonym for loadDriver(Properties) and is kept for backwards
   * compatibility.
   * 
   * @param driverConfig configuration parameters specifiying the driver
   * @return driver configured with parameters in file.
   * @throws ConfigurationException
   * @see #loadDriver(Properties)
   */
  public static CoreDriver load(Properties driverConfig)
    throws ConfigurationException {
    return loadDriver(driverConfig);
  }

  /**
  * Creates an instance of the driver specified by the ensembl_driver
  * parameter in the configuration and calls driver.initialise(configuration).
  *
  * <p>If the property "ensembl_driver" is ommitted the default 
  *  "org.ensembl.driver.impl.CoreDriverImpl" is used.
  * 
  * @param fileReference filepath or url, url can be relative to classpath
  * @return driver configured with parameters in file.
  * @throws ConfigurationException
  */
  public static CoreDriver loadDriver(String fileReference)
    throws ConfigurationException {
    return (CoreDriver) loadDriver(PropertiesUtil.createProperties(fileReference));
  }

  /**
   * Creates an instance of the driver specified by the ensembl_driver
   * parameter in the configuration and calls driver.initialise(configuration).
   *
   * <p>If the property "ensembl_driver" is ommitted it defaults to
   *  "org.ensembl.driver.impl.CoreDriverImpl".
   * 
   * This is a synonym for loadDriver(String) and is kept for backwards
   * compatibility.
   * 
   * @param fileReference filepath or url, url can be relative to classpath
   * @return driver configured with parameters in file.
   * @throws ConfigurationException
   * @see #loadDriver(String)
   */
  public static CoreDriver load(String fileReference)
    throws ConfigurationException {
    return loadDriver(fileReference);
  }

  /**
   * Creates an instance of the driver specified by the ensembl_driver
   * parameter in the configuration and calls driver.initialise(configuration).
   *
   * @param fileReference filepath or url, url can be relative to classpath
   * @return driver configured with parameters in file.
   * @throws ConfigurationException
   */
  public static EnsemblDriver loadEnsemblDriver(String fileReference)
    throws ConfigurationException {
    return loadEnsemblDriver(PropertiesUtil.createProperties(fileReference));
  }

}
