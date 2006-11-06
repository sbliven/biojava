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

package org.ensembl.driver;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.ensembl.util.PropertiesUtil;

/**
 * Factory for producing, or retrieving from a cached, ServerDrivers.
 * 
 * Sharing ServerDrivers amongst EnsemblDrivers that wrap databases
 * on the same server can significantly reduce the number of connections
 * and made to the server.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 * @see org.ensembl.driver.ServerDriver
 */
public class ServerDriverFactory {

  private Map serverDrivers = new HashMap();
  
  public ServerDriverFactory() {
  }
  
  public synchronized void clear() {
    serverDrivers.clear();
  }
  
  /**
   * Returns a ServerDriver corresponding to the _config_.
   * 
   * If an appropriatte ServerDriver is cached then that is returned.
   * Otherwise a new instance is created, cached, and returned.
   * 
   * @param config database connection parameters for a ServerDriver. Should contain
   * at least "connection_url", or  "host" and "user", otherwise no ServerDriver will be returned.
   * @return ServerDriver created from database specific parameters in _config_, or null if
   * prerequisite parameters are missing.
   */
  public synchronized ServerDriver get(Properties config) {
    
    Properties serverConfig = new Properties();
    PropertiesUtil.copyProperty(config, "host",serverConfig);
    PropertiesUtil.copyProperty(config, "port",serverConfig);
    PropertiesUtil.copyProperty(config, "user",serverConfig);
    PropertiesUtil.copyProperty(config, "password",serverConfig);
    PropertiesUtil.copyProperty(config, "connection_string",serverConfig);
    PropertiesUtil.copyProperty(config, "connection_parameters",serverConfig);
    PropertiesUtil.copyProperty(config, "connection_url",serverConfig);
    PropertiesUtil.copyProperty(config, "jdbc_driver",serverConfig);
    PropertiesUtil.copyProperty(config, "connection_pool_size",serverConfig);
    
    // No ServerDriver if incomplete connection parameters
    if (!serverConfig.containsKey("connection_url") &&
        !(serverConfig.containsKey("host") && serverConfig.containsKey("user")) )
    return null;
    
    // Connection parameters are also used as key in caches.
    ServerDriver serverDriver = (ServerDriver) serverDrivers.get(serverConfig);
    if (serverDriver == null) 
      serverDrivers.put(serverConfig, serverDriver = new ServerDriver(serverConfig));
    
    return serverDriver;
  }


  

  
}
