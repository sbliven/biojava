/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.driver.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.CoordinateSystemMapping;
import org.ensembl.datamodel.InvalidLocationException;
import org.ensembl.datamodel.Location;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoordinateSystemAdaptor;
import org.ensembl.driver.EnsemblDriver;

/**
 * Implementation of CoordinateSystemAdaptor interface.
 *  
 */
// TODO - Implement store/delete
public class CoordinateSystemAdaptorImpl extends BaseAdaptor implements
		CoordinateSystemAdaptor {

	private class MaxLengthMap extends HashMap {

		private static final long serialVersionUID = 1L;

		private void put(CoordinateSystem cs, String tableName, int maxLength) {
			put(maxLengthKey(cs, tableName), new Integer(maxLength));
		}

		private int get(CoordinateSystem cs, String tableName) {
			Object o = get(maxLengthKey(cs, tableName));
			if (o == null)
				return -1;
			else
				return ((Integer) o).intValue();
		}

		private String maxLengthKey(CoordinateSystem cs, String tableName) {
			return cs.getName() + "." + cs.getVersion() + "." + tableName;
		}

	}

	private static final Logger logger = Logger
			.getLogger(CoordinateSystemAdaptorImpl.class.getName());

	private List additionalDrivers = new ArrayList();

	private ArrayList mappings = null;

	private Map coordSystemCache = null;

	private HashMap featureTableCache = null;

	private MaxLengthMap maxLengthCache = null;

	public CoordinateSystemAdaptorImpl(CoreDriverImpl driver)
			throws AdaptorException {
		super(driver);
	}

	public CoordinateSystem fetch(long internalID) throws AdaptorException {

		if (coordSystemCache == null) {
			buildCaches();
		}
		return (CoordinateSystem) coordSystemCache.get(new Long(internalID));

	}

	public CoordinateSystem fetch(String name, String version)
			throws AdaptorException {

		if (coordSystemCache == null)
			buildCaches();

		return fetch(name, version, coordSystemCache);
	}

	public CoordinateSystem[] fetchAll() throws AdaptorException {

		if (coordSystemCache == null) {
			buildCaches();
		}
		ArrayList result = new ArrayList(coordSystemCache.values());
		// you have to sort the result by rank
		Collections.sort(result, new Comparator() {
			public int compare(Object a, Object b) {
				int rankA = ((CoordinateSystem) a).getRank();
				int rankB = ((CoordinateSystem) b).getRank();
				if (rankA == rankB) {
					return 0;
				}
				if (rankA < rankB) {
					return -1;
				} else {
					return 1;
				}
			}
		});

		return (CoordinateSystem[]) result.toArray(new CoordinateSystem[result
				.size()]);
	}

	public CoordinateSystem fetchSequenceLevel() throws AdaptorException {

		if (coordSystemCache == null) {
			buildCaches();
		}
		Iterator it = coordSystemCache.keySet().iterator();
		while (it.hasNext()) {
			Long id = (Long) it.next();
			CoordinateSystem cs = (CoordinateSystem) coordSystemCache.get(id);
			if (cs.isSequenceLevel()) {
				return cs;
			}
		}
		return null;
	}

	public long store(CoordinateSystem cs) throws AdaptorException {

		return -1;
	}

	/**
	 * Check if a string containing one or more comma-separated attributes
	 * contains a particular attribute.
	 * 
	 * @param attribStr
	 *            The string to check.
	 * @param attrib
	 *            The attribute to look for,.
	 * @return True if attrib appears in attribStr
	 */
	private boolean hasAttrib(String attribStr, String attrib) {

		if (attribStr == null)
			return false;

		String[] attribs = attribStr.split(",");
		for (int i = 0; i < attribs.length; i++) {
			if (attribs[i].equalsIgnoreCase(attrib)) {
				return true;
			}
		}

		return false;
	}

	private CoordinateSystem createCoordSystemFromResultSetRow(ResultSet rs)
			throws SQLException {

		String attribStr = rs.getString("attrib");
		CoordinateSystem result = new CoordinateSystem(
				rs.getString("name")
				, rs.getString("version")
				, hasAttrib(attribStr, "default_version")
				, rs.getLong("coord_system_id")
				, Integer.parseInt(rs.getString("rank"))
				, hasAttrib(attribStr, "sequence_level"));
		
		return result;

	}

	public String getType() throws AdaptorException {

		return TYPE;
	}

	public CoordinateSystem[] getMappingPath(CoordinateSystem cs1,
			CoordinateSystem cs2) throws AdaptorException {

		// load and cache mapping info from meta table if required
		// mappings holds a list of CoordinateSystemMapping objects
		if (mappings == null) 
			buildMappingCache();

		// get the required mapping path from the list of mappings
		for (Iterator it = mappings.iterator(); it.hasNext();) {

			CoordinateSystemMapping mapping = (CoordinateSystemMapping) it
					.next();
			if (mapping.getFirst().equals(cs1) && mapping.getLast().equals(cs2)
					|| mapping.getFirst().equals(cs2)
					&& mapping.getLast().equals(cs1)) {

				return mapping.getPath();
			}
		}

		return null;
	}

  private void buildMappingCache() throws AdaptorException {

    mappings = new ArrayList();

    Connection conn = null;
    String sql = "SELECT meta_value FROM meta WHERE meta_key='assembly.mapping'";
    try {

      Pattern p = Pattern.compile("([^\\||#]+)([\\||#])?");

      conn = getConnection();
      ResultSet rs = executeQuery(conn, sql);

      while (rs.next()) {

        String path = rs.getString("meta_value");
        if (path == null)
          continue;

        // break path into coordinate systems and separators
        Matcher m = p.matcher(path);
        ArrayList buf = new ArrayList();
        while (m.find())
          for (int i = 1; i < m.groupCount() + 1; i++)
            if (m.group(i) != null)
              buf.add(m.group(i));
        String[] items = (String[]) buf.toArray(new String[buf.size()]); 
        
        // create mappingPath for pair of coordinate systems
        if (items.length==3) {
          String cs1 = items[0];
          String sep = items[1];
          String cs2 = items[2];
          if ("#".equals(sep)) 
            mappings.add(createMapping(cs1,cs2,true));
          else if ("|".equals(sep))
            mappings.add(createMapping(cs1,cs2,false));
          else
            throw new AdaptorException("Unsupported meta entry assembly.mapping="+path);
        } else if (items.length==5) {
        	// XXX This is a quick hack as there wasn't
        	// time to implement correct support for a#b#c mappings.
					// For now treat a#b#c as a|b|c. This means we
					// might have problems with 1 to many mappings
					// e.g. 6 ensembl human 39 contigs map to both
					// default chromosomes and haplotypes.
//          if (!"|".equals(items[1]) || !"|".equals(items[3])) {
//            // TODO support paths such as "a # b | c"
//            System.err.println("WARNING: ignoring unsupported coordinate system mapping: " + path);
//            continue;
//          }
          CoordinateSystem[] css = new CoordinateSystem[] {
              mappingName2CoordSystem(items[0]),
              mappingName2CoordSystem(items[2]),
              mappingName2CoordSystem(items[4])
          };
          mappings.add(new CoordinateSystemMapping(css));
        }
      }
//      for (int i = 0; i < mappings.size(); i++) 
//        System.out.println(""+i+"\t"+mappings.get(i));
      

    } catch (SQLException e) {
      throw new AdaptorException(
          "Failed to load mapping paths from meta table (meta_key='assembly.mapping')",
          e);
    } finally {
      close(conn);
    }
  }

	private CoordinateSystemMapping createMapping(String cs1Label, String cs2Label, boolean chainedMapper) throws AdaptorException {
    CoordinateSystem cs1 = mappingName2CoordSystem(cs1Label);
    CoordinateSystem cs2 = mappingName2CoordSystem(cs2Label);
    if (chainedMapper)
      return new CoordinateSystemMapping(new CoordinateSystem[] {cs1, null, cs2});
    else
      return new CoordinateSystemMapping(new CoordinateSystem[] {cs1, cs2});
  }

  private CoordinateSystem mappingName2CoordSystem(String mappingLabel) throws AdaptorException {
    String[] parts = mappingLabel.split(":");
    String name = parts[0];
    // version is optional
    String version = parts.length > 1 ? parts[1] : "";
    return fetch(name, version);
  }

  /**
   * Store all the entries in the database in a HashMap of CoordinateSystem
   * objects. The HashMap is keyed on internal ID, so retrieval by internal ID
   * will be very fast. Other retrieval methods will be slower but this should
   * not be a problem as the coord_system table is always likely to be small.
   * Also stores the feature table name / coordinate system mappings in
   * featureTableCache.
   */
	private void buildCaches() throws AdaptorException {

		Connection con = null;
		coordSystemCache = new HashMap();
		featureTableCache = new HashMap();
		maxLengthCache = new MaxLengthMap();

		buildCoordSystemCache();
		addMetaCoordEntriesToCache(driver.getDatasource());
		if (driver.getVariationDriver() != null)
			addMetaCoordEntriesToCache(driver.getVariationDriver()
					.getDatasource());

	}

	private void buildCoordSystemCache() throws AdaptorException {
		Connection con = null;
		try {
			con = getConnection();
			String sql = "SELECT * FROM coord_system";
			ResultSet rs = executeQuery(con, sql);
			while (rs.next()) {
				CoordinateSystem cs = createCoordSystemFromResultSetRow(rs);
				coordSystemCache.put(new Long(cs.getInternalID()), cs);
			}
		} catch (SQLException e) {
			throw new AdaptorException("Failed to load coordinate system", e);
		} finally {
			close(con);
		}
	}

	/**
	 * @param ds
	 * @return
	 * @throws SQLException
	 * @throws AdaptorException
	 */
	private void addMetaCoordEntriesToCache(DataSource ds)
			throws AdaptorException {
		Connection conn = null;
		;

		try {
			// feature tables
			// note key for featureTableCache = tablename,
			// value = ArrayList of CoordinateSystem objects
			// deliberately do "SELECT *" because the code needs to
			// handle older schema (2 columns) and newer schema
			// (3 columns)
			String sql = "SELECT * FROM meta_coord";
			conn = ds.getConnection();
			ResultSet rs = executeQuery(conn, sql);
			int nCols = 0;
			while (rs.next()) {
				String tableName = rs.getString("table_name").toLowerCase();
				// note case insensitive
				CoordinateSystem cs = fetch(rs.getLong("coord_system_id"));
				if (cs == null) {
					throw new AdaptorException(
							"meta_coord table refers to non-existant co-ordinate system with ID "
									+ rs.getLong("coord_system_id"));
				}
				ArrayList csList = (ArrayList) featureTableCache.get(tableName);
				if (csList == null) {
					csList = new ArrayList();
				}
				csList.add(cs);
				featureTableCache.put(tableName, csList);

				if (nCols == 0)
					nCols = rs.getMetaData().getColumnCount();
				if (nCols > 2)
					maxLengthCache.put(cs, tableName, rs.getInt("max_length"));

			}
		} catch (SQLException e) {
			throw new AdaptorException("Failed to load meta_coord table", e);
		} finally {
			close(conn);
		}

	}

	public CoordinateSystem[] fetchAllByFeatureTable(String featureTableName)
			throws AdaptorException {

		if (featureTableCache == null) {
			buildCaches();
		}
		String tableName = featureTableName.toLowerCase();
		ArrayList csList = (ArrayList) featureTableCache.get(tableName);
		if (csList==null) {
		  logger.warning("Coordinate system for type \""
		      + tableName + "\" not specified in \"meta_coord\" table.");
		  return new CoordinateSystem[]{};
		}

		return (CoordinateSystem[]) csList.toArray(new CoordinateSystem[csList
				.size()]);

	}

	/**
	 * This function supports old style Locations with Maps. Maps are
	 * essentially CoordinateSystems so we retrieve one by mapname
	 * 
	 * @param mapName
	 *            a Map identifier
	 * @return the equivalent CoordinateSystem object
	 * @throws AdaptorException
	 */

	public CoordinateSystem fetchByMap(String mapName) throws AdaptorException {
		return fetch(mapName, "");
	}

	public CoordinateSystem fetchComplete(CoordinateSystem cs)
			throws AdaptorException {

		return (cs.isComplete()) ? cs : fetch(cs.getName(), cs.getVersion());

	}

	public List fetchTopLevelLocations() throws AdaptorException {

		List locs = new ArrayList();

		String sql = "SELECT coord_system_id, sr.name, length "
				+ "FROM seq_region sr,seq_region_attrib sra, attrib_type at "
				+ "WHERE sr.seq_region_id=sra.seq_region_id and sra.attrib_type_id=at.attrib_type_id and code='toplevel'";

		Connection conn = null;
		CoordinateSystemAdaptor csAdaptor = driver.getCoordinateSystemAdaptor();
		conn = getConnection();
		ResultSet rs = executeQuery(conn, sql);
		try {
			while (rs.next()) {
				locs.add(new Location(csAdaptor.fetch(rs.getLong(1)), rs
						.getString(2), 1, rs.getInt(3)));
			}
		} catch (InvalidLocationException e) {
			throw new AdaptorException(
					"Problem constructing top level location", e);
		} catch (SQLException e) {
			throw new AdaptorException(
					"Problem constructing top level location", e);
		}

		return locs;
	}

	/**
	 * Returns the maximum length of the feature in the coordinate system or -1
	 * if no max length is found.
	 * 
	 * @param cs
	 *            coordinate system feature is in.
	 * @param tableName
	 *            table name feature is stored in.
	 * @return max length, or -1 if no max length is available.
	 */
	public int fetchMaxLength(CoordinateSystem cs, String tableName) {
		return maxLengthCache.get(cs, tableName);
	}

	/**
	 * Clears this adaptors caches.
	 */
	public void clearCache() {
		coordSystemCache = null;
		featureTableCache = null;
		mappings = null;
		maxLengthCache = null;
	}

	public void addEnsemblDriver(EnsemblDriver driver) {
		additionalDrivers.add(driver);
		// must clear cache because the new datasource might contain new
		// meta coord entries we need to know about.
		featureTableCache = null;
	}

	public boolean removeEnsemblDriver(EnsemblDriver driver) {
		return additionalDrivers.remove(driver);
	}

	public List getEnsemblDrivers() throws AdaptorException {
		List dss = new ArrayList(additionalDrivers);
		dss.add(0, getDataSource());
		return dss;
	}

	/**
	 * @param coordinateSystem
	 * @param coordSysCache
	 * @return
	 */
	static CoordinateSystem fetch(String name, String version, Map cache) {
		if ("".equals(version))
			version = null;

		CoordinateSystem r = null;
		for (Iterator iter = cache.values().iterator(); r == null
				&& iter.hasNext();) {
			CoordinateSystem cs = (CoordinateSystem) iter.next();
			String csName = cs.getName();
			if (csName.equals(name)) {
				String csVersion = cs.getVersion();
				if (csVersion == version // incase version=NULL in db
						|| (csVersion != null && csVersion.equals(version))
						|| (version == null && cs.isDefault()))
					r = cs;
			}
		}

		return r;
	}
}