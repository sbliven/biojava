/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.idmapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.CoreDriverFactory;
import org.ensembl.driver.impl.BaseAdaptor;
import org.ensembl.util.PropertiesUtil;
import org.ensembl.util.Util;

public class Config {

	private CoreDriver sourceDriver, targetDriver;

	public String rootDir;

	public String debugDir;

	public boolean debug = false;

	private static String DRIVER_CLASS = "org.gjt.mm.mysql.Driver";

	// private static String DRIVER_CLASS = "com.p6spy.engine.spy.P6SpyDriver";

	public Config(String configFile) {

		readPropertiesFileIntoSystem(configFile);
		rootDir = createWorkingDirectory();
		this.debugDir = rootDir + File.separator + "debug";

	}

	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------

	/**
	 * Read the a properties file into the System properties
	 * 
	 * @param propertiesFileName
	 *          The properties file to read.
	 */
	public void readPropertiesFileIntoSystem(final String propertiesFileName) {

		String propsFile = propertiesFileName;
		System.out.println("Reading properties from " + propsFile);
		Properties dbProps = readSimplePropertiesFile(propsFile);
		Enumeration e = dbProps.propertyNames();
		String name, value;
		while (e.hasMoreElements()) {

			name = (String) e.nextElement();
			value = dbProps.getProperty(name);
			System.setProperty(name, value);

		}

	} // readPropertiesFile

	// -------------------------------------------------------------------------
	/**
	 * Read a properties file.
	 * 
	 * @param propertiesFileName
	 *          The name of the properties file to use.
	 * @return The Properties hashtable.
	 */
	public Properties readSimplePropertiesFile(String propertiesFileName) {

		Properties props = new Properties();

		try {

			FileInputStream in = new FileInputStream(propertiesFileName);
			props.load(in);
			in.close();

		} catch (Exception e) {

			e.printStackTrace();
			System.exit(1);

		}

		return props;

	} // readPropertiesFile

	// -------------------------------------------------------------------------

	public boolean validateConfig() {

		boolean result = true;

		Set validModes = new HashSet();
		validModes.add("normal");
		validModes.add("similarity");
    validModes.add("results");
    validModes.add("archive");
		if (!validModes.contains(getMode())) {
			System.err.println("Invalid idmapping.mode value: '" + getMode() + "' should be one of " + validModes);
			result = false;
		}

    if ("similarity".equals(getMode())) {
      long retrofitMappingSessionID = Long.parseLong(System.getProperty("idmapping.retrofit_mapping_session_id","-1"));
      if (retrofitMappingSessionID==-1) {
        System.err.println("Invalid/missing idmapping.retrofit_mapping_id");
        result=false;
      }
    }
    
    String workingDir = workingDirectory();
    File f = new File(workingDir).getParentFile();
    if (f!=null && !f.exists()) {
      System.err.println("idmapping.base_directory does not exist: '" + f + "'");
      result = false;
    }
      
    
		// ------------------------------------------------
		// check source and target permissions

		try {
			Class.forName(DRIVER_CLASS);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		if (debug) {
			System.out.println("Loaded database driver");
		}

		// source
		Connection sourceCon = buildConnection(System.getProperty("idmapping.source.host"),
				System.getProperty("idmapping.source.port"), "", System.getProperty("idmapping.source.user"), System
						.getProperty("idmapping.source.password"));

		if (sourceCon == null) {

			System.err.println("Cannot open connection to source database");
			result = false;

		} else {

			debug("Source connection OK");

			// check that we have read permission for this database
			if (!checkPermission(sourceCon, System.getProperty("idmapping.source.user"), "SELECT")) {

				System.err.println("Cannot obtain read permission for source database");
				result = false;

			} else {

				debug("Can read source");

			}

		} // if sourceCon

		// target
		Connection targetCon = buildConnection(System.getProperty("idmapping.target.host"),
				System.getProperty("idmapping.target.port"), "", System.getProperty("idmapping.target.user"), System
						.getProperty("idmapping.target.password"));

		if (targetCon == null) {

			System.err.println("Cannot open connection to target database");
			result = false;

		} else {

			debug("Target connection OK");

			// check that we have read AND write permission for this database
			if (!checkPermission(targetCon, System.getProperty("idmapping.target.user"), "SELECT")) {

				System.err.println("Cannot obtain read permission for target database");
				result = false;

			} else {

				debug("Can read target");

			}

			boolean writeAccessRequired = booleanFromProperty("idmapping.upload.events")
					|| booleanFromProperty("idmapping.upload.stableids") || booleanFromProperty("idmapping.upload.archive");

			if (writeAccessRequired && !checkPermission(targetCon, System.getProperty("idmapping.target.user"), "INSERT")) {

				System.err.println("Cannot obtain write permission for target database");
				result = false;

			} else {

				debug("Can write target");

			}

		} // if targetCon

		// check target sequence connection, if applicable
		Connection targetSequenceCon = null;
		if (System.getProperty("idmapping.target.sequence.host") != null) {

			targetSequenceCon = buildConnection(System.getProperty("idmapping.target.sequence.host"), System
					.getProperty("idmapping.target.sequence.port"), "", System.getProperty("idmapping.target.sequence.user"), System
					.getProperty("idmapping.target.sequence.password"));

			if (targetSequenceCon == null) {

				System.err.println("Cannot open connection to target sequence database");
				result = false;

			} else {

				debug("Target connection OK");

			}
		}

		// ------------------------------------------------
		// check drivers
		CoreDriver sd = getSourceDriver();
		if (sd == null) {
			System.err.println("Cannot create source driver");
			result = false;
		} else {

			debug("Source driver OK");

		}
		CoreDriver td = getTargetDriver();
		if (td == null) {
			System.err.println("Cannot create target driver");
			result = false;
		} else {
			debug("Target driver OK");
		}

		// ------------------------------------------------
		// check working directory is writeable
		File tmp = new File(System.getProperty("user.dir") + File.separator + System.currentTimeMillis() + ".tmp");
		boolean canWrite = true;
		try {

			FileOutputStream fos = new FileOutputStream(tmp);
			fos.write(1);
			fos.close();
			tmp.delete();

		} catch (IOException e1) {

			System.err.println("Cannot write in " + System.getProperty("user.dir"));
			e1.printStackTrace();
			canWrite = false;

		}

		if (canWrite) {
			debug("Can write to " + System.getProperty("user.dir"));
		}

		result &= canWrite;

		// ------------------------------------------------
		// Check source and target databases have sequence

		if (sourceCon != null) {

			if (Integer.parseInt(getRowColumnValue(sourceCon, "SELECT COUNT(*) FROM " + System.getProperty("idmapping.source.database")
					+ ".dna")) == 0) {
				System.err.println("Source database has no sequence");
				result = false;
			} else {
				debug("Source database has sequence");
			}

		}

		if (targetCon != null) {

			if (Integer.parseInt(getRowColumnValue(targetCon, "SELECT COUNT(*) FROM " + System.getProperty("idmapping.target.database")
					+ " .dna")) == 0) {
				System.err.println("Target database has no sequence");

				// check target sequence database
				if (targetSequenceCon != null) {
				    if (Integer.parseInt(getRowColumnValue(targetSequenceCon, "SELECT COUNT(*) FROM "
									   + System.getProperty("idmapping.target.sequence.database") + " .dna")) == 0) {
					System.err.println("Target sequence database has no sequence");
					result = false;
				    }
				} else {
				    result = false;
				}

			} else {

				debug("target database has sequence");

			}
		}

		// ------------------------------------------------
		// Check that target database's stable ID tables etc are empty
		String[] tables = { "gene_stable_id", "transcript_stable_id", "translation_stable_id", "exon_stable_id", "mapping_session",
				"stable_id_event", "gene_archive", "peptide_archive" };
		for (int i = 0; i < tables.length; i++) {

			String tableName = tables[i];
			int rows = getRowCount(targetCon, "SELECT COUNT(*) FROM " + System.getProperty("idmapping.target.database") + "." + tableName);
			if (rows > 0) {
				System.err.println(tableName + " in target database " + System.getProperty("idmapping.target.database")
						+ " is not empty (has " + rows + " rows)");
			} else {
				debug(tableName + " in target database " + System.getProperty("idmapping.target.database") + " is empty");
			}
		}

		// ------------------------------------------------
		// Check that the genebuild.version value in the meta table is different in
		// both databases
		String sgbSQL = "SELECT meta_value FROM " + System.getProperty("idmapping.source.database")
				+ ".meta WHERE meta_key='genebuild.version'";
		String tgbSQL = "SELECT meta_value FROM " + System.getProperty("idmapping.target.database")
				+ ".meta WHERE meta_key='genebuild.version'";
		String sourceGenebuild = getRowColumnValue(sourceCon, sgbSQL);
		String targetGenebuild = getRowColumnValue(targetCon, tgbSQL);
		if (sourceGenebuild.equals(targetGenebuild)) {
			System.err.println("WARNING: Meta table has same genebuild.version value (" + sourceGenebuild
					+ ") in source and target databases");
		} else {
			debug("Meta table has different genebuild.version values (" + sourceGenebuild + " and " + targetGenebuild
					+ ") in source & target databases");
		}

		// ------------------------------------------------

		return result;

	}

	// -------------------------------------------------------------------------
	private String createWorkingDirectory() {

		String dirName = workingDirectory();

		File f = new File(dirName);
		f.mkdir();

		File debug = new File(dirName + File.separator + "debug");
		debug.mkdir();

		return dirName;

	}

  private String workingDirectory() {
    String dirName = "";
		if (System.getProperty("idmapping.base_directory") != null) {
			dirName = System.getProperty("idmapping.base_directory");
		}
		dirName += File.separator + System.getProperty("idmapping.source.database") + "_"
				+ System.getProperty("idmapping.target.database");
    return dirName;
  }

	private void debug(String s) {
		if (debug) {
			System.out.println(s);
		}

	}

	// -------------------------------------------------------------------------

	public Connection buildConnection(String host, String port, String database, String user, String password) {

		Connection con = null;

		String url = host;
		if (port != null && port.length() > 0) {
			url += ":" + port;
		}

		url += "/" + database;

		try {
			con = java.sql.DriverManager.getConnection("jdbc:mysql://" + url, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return con;

	}

	// -------------------------------------------------------------------------

	private boolean checkPermission(Connection con, String user, String priv) {

		List grants = new ArrayList();

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SHOW GRANTS FOR " + user);
			while (rs.next()) {

				grants.addAll(parseGrants(rs.getString(1)));

			}
		} catch (SQLException e) {

			System.err.println("Can't check " + priv + " permission for " + user);
			e.printStackTrace();

		}

		// if ALL PRIVILEGES is specified, anything goes!
		return grants.contains(priv) || grants.contains("ALL PRIVILEGES");

	}

	// -------------------------------------------------------------------------

	private List parseGrants(String str) {

		List grants = new ArrayList();

		// str will have format GRANT SELECT, INSERT, ... ON ...
		str = str.substring(6, str.indexOf(" ON "));
		String[] privs = str.split(",");
		for (int i = 0; i < privs.length; i++) {
			grants.add(privs[i].trim());
		}

		return grants;

	}

	// -------------------------------------------------------------------------}

	public CoreDriver getSourceDriver() {

		if (sourceDriver != null) {
			return sourceDriver;
		}

		return getDriver("idmapping.source");

	}

	// -------------------------------------------------------------------------

	public CoreDriver getTargetDriver() {

		if (targetDriver != null) {
			return targetDriver;
		}

		return getDriver("idmapping.target");

	}

	// -------------------------------------------------------------------------

	private CoreDriver getDriver(String prefix) {

		CoreDriver driver = null;

		Properties prop = new Properties();
		prop.putAll(PropertiesUtil.removePrefixFromKeys(System.getProperties(), prefix));
		prop.put("path", prefix); // driver name == prefix, as good as any

		try {

			driver = CoreDriverFactory.createCoreDriver(prop);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return driver;
	}

	// -------------------------------------------------------------------------
	/**
	 * Return a connection to the source database.
	 */
	public Connection getSourceConnection() {

		return buildConnection(System.getProperty("idmapping.source.host"), System.getProperty("idmapping.source.port"), System
				.getProperty("idmapping.source.database"), System.getProperty("idmapping.source.user"), System
				.getProperty("idmapping.source.password"));

	}

	// -------------------------------------------------------------------------
	/**
	 * Return a connection to the target database.
	 */
	public Connection getTargetConnection() {

		return buildConnection(System.getProperty("idmapping.target.host"), System.getProperty("idmapping.target.port"), System
				.getProperty("idmapping.target.database"), System.getProperty("idmapping.target.user"), System
				.getProperty("idmapping.target.password"));

	}

	// -------------------------------------------------------------------------
	/**
	 * Execute a SQL statement and return the value of one column of one row. Only
	 * the FIRST row matched is returned.
	 * 
	 * @param con
	 *          The Connection to use.
	 * @param sql
	 *          The SQL to check; should return ONE value.
	 * @return The value returned by the SQL.
	 */
	private String getRowColumnValue(Connection con, String sql) {

		String result = "";

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs != null && rs.first()) {
				result = rs.getString(1);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	} // getRowColumnValue

	// -------------------------------------------------------------------------

	private int getRowCount(Connection con, String sql) {

		int result = -1;

		try {
			Statement stmt = con.createStatement();
			// System.out.println("Executing " + sql);
			ResultSet rs = stmt.executeQuery(sql);
			if (rs != null) {
				if (rs.first()) {
					result = rs.getInt(1);
				} else {
					result = -1; // probably signifies an empty ResultSet
				}
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;

	} // getRowCount

	// -------------------------------------------------------------------------
	/**
	 * Load data from a tab-delimited file into a MySQL table.
	 * 
	 * @param fileName
	 *          The file to load from.
	 * @param table
	 *          The table to load data into.
	 * @param con
	 *          The database connection to use.
	 * @param ignore
	 *          If true, duplicates are ignored. This should be set to false
	 *          unless you specifically want to ignore duplicates.
	 */

	public static void uploadFromFile(String fileName, String table, Connection con, boolean ignore) {

		File f = new File(fileName);
		if (!f.exists() || !f.canRead()) {
			System.err.println("Cannot read " + fileName + " - nothing uploaded to " + table);
			return;
		}

		String ignoreStr = "";
		String ignoreMsg = "";

		if (ignore) {

			ignoreStr = "IGNORE";
			ignoreMsg = " Ignoring rows that duplicate an existing row on a unique key.";
		}

		System.out.println("Uploading data in " + fileName + " to " + System.getProperty("idmapping.target.database") + "." + table
				+ ignoreMsg);

		try {

			BaseAdaptor.executeUpdate(con, "LOAD DATA LOCAL INFILE '" + fileName + "' " + ignoreStr + " INTO TABLE " + table);

		} catch (AdaptorException e) {

			System.err.println("Error uploading data from " + fileName + " to " + System.getProperty("idmapping.target.database") + "."
					+ table);
			e.printStackTrace();
		}

	}

	// -------------------------------------------------------------------------

	/**
	 * Build a map of pre-defined choices for config settings.
	 * 
	 * @return Map of Lists, keyed on property name, value is list of possible
	 *         settings.
	 */
	public Map buildConfigChoices() {

		Map choices = new HashMap();

		Util.addToMapList(choices, "idmapping.source.host", "ecs2:3364");
		Util.addToMapList(choices, "idmapping.source.host", "ecs2:3363");
		Util.addToMapList(choices, "idmapping.source.host", "ecs1g:3306");
		Util.addToMapList(choices, "idmapping.source.host", "127.0.0.1:5000");
		Util.addToMapList(choices, "idmapping.source.host", "127.0.0.1:5001");
		Util.addToMapList(choices, "idmapping.source.user", "ensro");
		Util.addToMapList(choices, "idmapping.source.user", "ensadmin");
		Util.addToMapList(choices, "idmapping.source.user", "anonymous");

		Util.addToMapList(choices, "idmapping.target.host", "ecs2:3364");
		Util.addToMapList(choices, "idmapping.target.host", "ecs2:3363");
		Util.addToMapList(choices, "idmapping.target.host", "ecs1g:3306");
		Util.addToMapList(choices, "idmapping.target.host", "127.0.0.1:5000");
		Util.addToMapList(choices, "idmapping.target.host", "127.0.0.1:5001");
		Util.addToMapList(choices, "idmapping.target.user", "ensro");
		Util.addToMapList(choices, "idmapping.target.user", "ensadmin");
		Util.addToMapList(choices, "idmapping.target.user", "anonymous");

		Util.addToMapList(choices, "idmapping.base_directory", "c:\\work");

		Util.addToMapList(choices, "idmapping.exonerate.path", "/usr/local/ensembl/bin/exonerate-0.8.1");

		Util.addToMapList(choices, "idmapping.email", "glenn@ebi.ac.uk");
		Util.addToMapList(choices, "idmapping.smtp_server", "mailserv.ebi.ac.uk");
		Util.addToMapList(choices, "idmapping.smtp_server", "mailsrv1.internal.sanger.ac.uk");

		// Now add those from idmapping.properties if they're not there
		// Note this will only work if config file has already been read
		Properties props = PropertiesUtil.filterOnPrefix("idmapping.", System.getProperties());
		Enumeration names = props.propertyNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			if (!name.endsWith(".host") && !name.endsWith(".port")) {
				if (choices.containsKey(name)) {
					List list = (List) choices.get(name);
					if (!list.contains(System.getProperty(name))) {
						Util.addToMapList(choices, name, System.getProperty(name));
					}
				}
			}

		}

		// .host property has port as well
		Util.addToMapList(choices, "idmapping.source.host", System.getProperty("idmapping.source.host") + ":"
				+ System.getProperty("idmapping.source.port"));
		Util.addToMapList(choices, "idmapping.target.host", System.getProperty("idmapping.target.host") + ":"
				+ System.getProperty("idmapping.target.port"));

		return choices;

	}

	// -------------------------------------------------------------------------

	public static boolean booleanFromProperty(String property, String defaultValue) {

		String val = System.getProperty(property, defaultValue);
		val = val.toLowerCase();
		return (val.equals("true") || val.equals("yes"));

	}

	public static boolean booleanFromProperty(String property) {

		return booleanFromProperty(property, "no");

	}

	// --------------------------------------------------------------------------

	public String getMode() {
		return System.getProperty("idmapping.mode", "normal").toLowerCase();
	}

}
