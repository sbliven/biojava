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


package org.acedb;

import java.util.*;
import java.net.URL;

/**
 * The universal port of call for accessing ace urls.
 * <P>
 * ACeDB urls should be of the form <code>ace://machine:port</code>.
 * <P>
 * This object is responsible for making sure that there is only one
 * database open for each unique database URL.
 *
 * @author Matthew Pocock
 */
public class DatabaseManager {
  static Set drivers;
  static Map databases;
  
  static {
    drivers = new HashSet();
    databases = new HashMap();

    Properties sysProp = System.getProperties();
    String pkgs = (String) sysProp.get("java.protocol.handler.pkgs");
    String myPkg = "bio";
    if(pkgs != null)
      pkgs = pkgs + "|" + myPkg;
    else
      pkgs = myPkg;
    
    sysProp.put("java.protocol.handler.pkgs", pkgs);
  }
  
    /**
     * Nobody should be creating one of these!
     */

    private DatabaseManager() {
    }

  /**
   * Register a driver with the manager.
   * <P>
   * The driver will be asked if it can accept a particular
   * ace url, and if so, it may be used to create a
   * Database reprenting that url.
   */
  public static void registerDriver(Driver driver) {
    drivers.add(driver);
  }
  
  /**
   * Retrieves the database associated with this url.
   */
  public static Database getDatabase(URL url) throws AceException {
    Database db = (Database) databases.get(url);
    if(db == null) {
      for(Iterator i = drivers.iterator(); i.hasNext(); ) {
        Driver d = (Driver) i.next();
        if(d.accept(url)) {
          db = d.connect(url);
          databases.put(url, db);
          break;
        }
      }
    }
    return db;
  }
}
