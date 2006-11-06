/*
    Copyright (C) 2001 EBI, GRL

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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.LogManager;

/**
 * Utility class to simplify logging system configuration.
 *
 * <p>Setting the environment variable verbose (e.g. java -Dverbose ... )
 * causes the logging file loaded to be shown.
 *
 *
 *
*/
public class LoggingManager {

  public static final String DEFAULT_LOGGING_CONF_FILE =
    "resources/data/logging_info_level.properties";

  private static boolean configured = false;

  /**
   * Stop any instances being created.
   */
  private LoggingManager() {
  }

  /**
   * Initialise logging system with file DEFAULT_LOGGING_CONF_FILE.
   */
  public static void configure() {
    configure(DEFAULT_LOGGING_CONF_FILE);
  }

  /**
   * Configures logging system with contents of the file <i>filepath</i>. It
   * tries to load the file as a 'normal' file first relative to the current
   * working directory. A 'slash' at the beginning of the filepath makes it
   * absolute. If that fails it attempts to load the file relative to the
   * classpath. This means the config file can be in a jar.
   * 
   * @param filepath path relative to current working directory OR the
   * classpath.
   *
   * @throws RuntimeException if filpath not found.
   **/
  public static void configure(String filepath) {

    URL url = null;

    // is it a try it as a file path first
    File f = new File(filepath);
    if (f.exists()) {
      try {
        url = f.toURL();
        configure(url);
      } catch (MalformedURLException e) {
        url = null;
      }
    }

    // try as path relative to classpath second
    if (url == null) {
      url = getResource(filepath);
    }

    if (url != null)
      configure(url);
    else {
      System.out.println("No logging?");
      // throw new RuntimeException("Failed to configure logging system; file not found : "
      //                            + filepath);
    }
  }

  /**
   * Configures logging system with contents of URL.
   * Note that the call is ignored if the logging system has
   * already been configured via the system property
   * java.util.logging.config.file.
   */
  public static void configure(URL file) {

    String environOption = System.getProperty("java.util.logging.config.file");
    if (environOption == null) {
      try {
        LogManager.getLogManager().readConfiguration(file.openStream());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else if (System.getProperty("ensj.debug")!=null) {
      System.err.println(
        "WARNING: ignoring logging config file "
          + file
          + " because logging already configured from "
          + "environment property java.util.logging.config.file to "
          + environOption);
      ;
    }
    configured = true;
  }

  public static boolean isConfigured() {
    return configured;
  }

  private static URL getResource(String resourcePath) {
    return LoggingManager.class.getClassLoader().getResource(resourcePath);
  }

} // LoggingManager
