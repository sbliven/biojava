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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.ensembl.compara.driver.ComparaDriver;
import org.ensembl.compara.driver.ComparaDriverFactory;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.CoreDriverFactory;
import org.ensembl.driver.EnsemblDriver;
import org.ensembl.variation.driver.VariationDriver;
import org.ensembl.variation.driver.VariationDriverFactory;

/**
 * All the EnsemblDrivers for a species-release or a compara driver for a release.
 * 
 * Configurations for EnsemblDrivers are set via the setXXXConfig() methods. The
 * drivers they define are lazy loaded on demand and then cached.
 * 
 * DriverGroups can be retrieved from Registry instances.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 * @see org.ensembl.registry.Registry
 */
public class DriverGroup {

  private static final Logger logger = Logger.getLogger(DriverGroup.class
      .getName());

  private Properties coreConfig = null;

  private CoreDriver coreDriver = null;

  private Properties variationConfig = null;

  private VariationDriver variationDriver = null;

  private Properties comparaConfig = null;

  private ComparaDriver comparaDriver = null;

  /**
   * Create a DriverGroup.
   */
  public DriverGroup() {
  }

  /**
   * Returns the core driver if a core configuration is set.
   * 
   * Delegates to <code>CoreDriverFactory.createCoreDriver(coreConfig)</code>
   * and caches the result.
   * 
   * @return core driver if available, otherwise null.
   * @throws AdaptorException
   *           if a CoreDriver cannot be created from _coreConfig_.
   * @see CoreDriverFactory
   */
  public CoreDriver getCoreDriver() throws AdaptorException {

    if (coreDriver == null && coreConfig != null)
        coreDriver = CoreDriverFactory.createCoreDriver(coreConfig);

    return coreDriver;
  }

  /**
   * Returns the variation driver if available.
   * 
   * Lazy creates via 
   * <code>VariationDriverFactory.createVariationDriver(variationConfig)</code>
   * and caches the result.
   * 
   * Also sets variationDriver.coreDriver = getCoreDriver().
   * 
   * @return variation driver if available, otherwise null.
   * @throws AdaptorException
   *           if a VariationDriver cannot be created from _variationConfig_.
   */
  public VariationDriver getVariationDriver() throws AdaptorException {

    if (variationDriver == null && variationConfig != null) {

      variationDriver = VariationDriverFactory
          .createVariationDriver(variationConfig);

      if (variationDriver != null) {
        CoreDriver cd = getCoreDriver();
        if (cd != null)
          variationDriver.setCoreDriver(cd);
        else
          logger.warning("No core driver for variation driver: "
              + variationDriver);
      }
    }

    return variationDriver;
  }

  /**
   * Returns the compara driver if a compara configuration is set.
   * 
   * Delegates to
   * <code>ComparaDriverFactory.createComparaDriver(comparaConfig)</code> and
   * caches the result.
   * 
   * 
   * @return compara driver if available, otherwise null.
   * @throws AdaptorException
   *           if a ComparaDriver cannot be created from _comparaConfig_.
   */
  public ComparaDriver getComparaDriver() throws AdaptorException {

    if (comparaDriver == null && comparaConfig != null)
      comparaDriver = ComparaDriverFactory.createComparaDriver(comparaConfig);
    
    return comparaDriver;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();

    buf.append("[");

    buf.append("coreConfig=").append(coreConfig);
    buf.append(", variationConfig=").append(variationConfig);
    buf.append(", comparaConfig=").append(comparaConfig);

    buf.append("]");

    return buf.toString();
  }

  /**
   * Configuration for the compara driver.
   * 
   * @return Returns the compara driver configuration.
   */
  public Properties getComparaConfig() {
    return comparaConfig;
  }

  /**
   * Configuration for the compara driver.
   * 
   * @param comparaConfig
   *          new compara configuration.
   */
  public void setComparaConfig(Properties comparaConfig) {
    this.comparaConfig = comparaConfig;
  }

  /**
   * Configuration for the core driver.
   * 
   * @return Returns the core driver configuration.
   */
  public Properties getCoreConfig() {
    return coreConfig;
  }

  /**
   * Configuration for the core driver.
   * 
   * @param coreConfig
   *          The core driver configuration to set.
   */
  public void setCoreConfig(Properties coreConfig) {
    this.coreConfig = coreConfig;
  }

  /**
   * Configuration for the variation driver.
   * 
   * @return Returns the variation configuration.
   */
  public Properties getVariationConfig() {
    return variationConfig;
  }

  /**
   * Configuration for the variation driver.
   * 
   * @param variationConfig
   *          The variation configuration to set.
   */
  public void setVariationConfig(Properties variationConfig) {
    this.variationConfig = variationConfig;
  }

  /**
   * Closes open database connections on all drivers.
   * 
   * @throws AdaptorException
   */
  public void closeAllConnections() throws AdaptorException {

    if (coreDriver != null)
      coreDriver.closeAllConnections();

    if (variationDriver != null)
      variationDriver.closeAllConnections();

    if (comparaDriver != null)
      comparaDriver.closeAllConnections();
  }

  /**
   * Convenience method that returns whether at least one of the _configs_ is
   * set.
   * 
   * @return true if at least one of the _configs_ is not null.
   */
  public boolean hasConfigs() {
    return coreConfig != null || variationConfig != null
        || comparaConfig != null;
  }

  /**
   * Creates a deep copy of this DriverGroup.
   * 
   * @see java.lang.Object#clone()
   */
  public DriverGroup copy() {
    DriverGroup dg = new DriverGroup();
    dg.coreConfig = copy(coreConfig);
    dg.variationConfig = copy(variationConfig);
    dg.comparaConfig = copy(comparaConfig);
    return dg;
  }

  /**
   * Checks if _obj_ is equal to this instance.
   * 
   * @return true if _obj_ is a DriverGroup and has the same configurations.
   */
  public boolean equals(Object obj) {
    return obj instanceof DriverGroup && obj.hashCode() == hashCode();
  }

  /**
   * Hash code for this instance.
   * 
   * The hashcode takes into account the configs.
   */
  public int hashCode() {
    // calculate hash code every time because underlying
    // configs might change.
    int result = 17;
    result = 37 * result + hashCode(coreConfig);
    result = 37 * result + hashCode(variationConfig);
    result = 37 * result + hashCode(comparaConfig);
    return result;

  }

  /**
   * Convenience method returning all drivers in the group.
   * 
   * @return zero or more EnsemblDrivers.
   * @throws AdaptorException
   */
  public List getDrivers() throws AdaptorException {

    List r = new ArrayList();

    EnsemblDriver d = getCoreDriver();
    if (d != null)
      r.add(d);

    d = getVariationDriver();
    if (d != null)
      r.add(d);

    d = getComparaDriver();
    if (d != null)
      r.add(d);

    return r;
  }

  private final int hashCode(Properties p) {
    return (p == null) ? 0 : p.hashCode();
  }

  private final Properties copy(Properties p) {
    if (p == null)
      return null;
    else
      return (Properties) p.clone();
  }

}