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

package org.ensembl.test;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.ensembl.driver.EnsemblDriver;
import org.ensembl.driver.LoggingManager;
import org.ensembl.driver.ServerDriverFactory;
import org.ensembl.registry.DriverGroup;
import org.ensembl.registry.Registry;
import org.ensembl.registry.RegistryLoaderIni;

/**
 * Base class for all ensj tests that connect to a database.
 * 
 * The class creates a registry of drivers that the tests can use. The default
 * configuration file for these databases is resource/data/unit_test.ini. Users
 * can override this file to specify different databases by creating their own
 * config file $HOME/.ensembl/unit_test.ini. See RegistryLoaderIni for details
 * about the file format.
 * 
 * The logging system is configured by default with the file
 * resources/data/unit_test_logging.properties. Users can override this file by
 * creating their own file $HOME/.ensembl/unit_test_logging.properties.
 * 
 * We create drivers in setUp() and close connections in tearDown() to avoid
 * opening too many connections to the db server: If we simply create the driver
 * in the constructor then it's connections will stay open until the drivers are
 * garbage collected or it's connection closer thread closes them. This means
 * that classes with many test methods can have many connections open
 * simultaneously and this causes the tests to hang if the mysql server limits
 * connections.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 * @see org.ensembl.registry.RegistryLoaderIni
 */
public abstract class Base extends TestCase {

  private static final Logger logger = Logger.getLogger(Base.class.getName());

  protected static Registry registry = null;

  private static int testCaseCounter = 0;

  private static boolean first = true;

  public final static String ASSEMBLY_MAP_NAME = "DEFAULT_ASSEMBLY";

  public final static String DRIVER_CONFIG_FILENAME = "unit_test.ini";

  public final static String DEFAULT_CONFIG_DIR = "resources/data";

  public final static String DEFAULT_LOGGING_CONFIG = "unit_test_logging.properties";

  public final static String UNLIKELY_ASSEMBLY_MAP_NAME = "asdjakl12asdas";

  public final static String LATEST_HUMAN_CHROMOSOME_VERSION = "NCBI36";

  public final static String LATEST_MOUSE_CHROMOSOME_VERSION = "NCBIM35";

  public Base(String name) {
    super(name);

    LoggingManager.configure(resolveFilename(DEFAULT_LOGGING_CONFIG));

  }

  public static String resolveFilename(String filename) {

    String userDir = System.getProperty("user.home") + File.separator
        + ".ensembl";
    File userSpecificFile = new File(userDir + File.separator + filename);

    if (userSpecificFile.exists())
      return userSpecificFile.getAbsolutePath();
    else
      return DEFAULT_CONFIG_DIR + File.separator + filename;
  }

  protected void setUp() throws Exception {

    if (registry == null) {

      // Optimisation: providing shared ServerDrivers to drivers
      // enables them to share lists of database names (requires fewer queries)
      // and share connection pools.
      
      ServerDriverFactory cacheManager = new ServerDriverFactory();
      RegistryLoaderIni loader = new RegistryLoaderIni(
          resolveFilename(DRIVER_CONFIG_FILENAME), cacheManager);
      registry = new Registry(loader);
      assertNotNull(registry);

      List groups = registry.getGroups();
      for (int i = 0; i < groups.size(); i++) {
        DriverGroup group = (DriverGroup) groups.get(i);
        List drivers = group.getDrivers();
        for (int j = 0; j < drivers.size(); j++) {
          EnsemblDriver driver = (EnsemblDriver) drivers.get(j);
          driver.setServerDriverFactory(cacheManager);
        }
      }
    

   
    }

  }

  protected void tearDown() throws Exception {
    registry.closeAllConnections();
  }

  
}