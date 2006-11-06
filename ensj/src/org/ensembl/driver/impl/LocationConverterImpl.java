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
package org.ensembl.driver.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ensembl.datamodel.AssemblyMapper;
import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Persistent;
import org.ensembl.datamodel.SequenceRegion;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.AssemblyMapperAdaptor;
import org.ensembl.driver.CoordinateSystemAdaptor;
import org.ensembl.driver.LocationConverter;
import org.ensembl.util.IDMap;
import org.ensembl.util.LruCache;
import org.ensembl.util.mapper.Coordinate;

/**
 * Location converter which satisfies convert() calls by querying the database.
 * Fast to initialise because no 'data loading' stage. Individual calls to
 * convert however require separate database calls.
 * 
 * <p>
 * Possible optimisations based on cacing assembly:
 * <ul>
 * <li>Include 'gaps' in cache rather than dynamically generate them.
 * <li>Hold 2 versions of assembly in memory for fast access:
 * assemblyMap->cloneFragmentMap and cloneFragmentMap->assemblyMap.
 * <li>To speed loading consider writing/readinf serialised version of cache.
 * </ul>
 * 
 * @version $Revision$
 */
public class LocationConverterImpl extends BaseAdaptor implements
		LocationConverter {
	private static final Logger logger = Logger
			.getLogger(LocationConverterImpl.class.getName());

	public static final int SEQ_REGION_CACHE_SIZE = 200000;

	private LruCache seqRegionCache = new LruCache(SEQ_REGION_CACHE_SIZE);
	private IDMap coordSysCache = new IDMap();

	public LocationConverterImpl(CoreDriverImpl driver) {
		super(driver);
	}

	
	
	/**
	 * @see org.ensembl.driver.Adaptor#clearCache()
	 */
	public void clearCache() {
		super.clearCache();
		seqRegionCache.clear();
		coordSysCache.clear();
	}
	
	
	public void cacheSeqRegion(String regionName, CoordinateSystem cs,
			long regionId, int regionLength) {
		cacheSeqRegion2(regionName, cs, regionId, regionLength);
	}

	// returning the RegionCacheElement enables certain
	// optimisations BUT cacheSeqRegion does not and RegionCacheElement
	// is an implementation specific type so we implement this shadow
	// method.
	private RegionCacheElement cacheSeqRegion2(String regionName,
			CoordinateSystem cs, long regionId, int regionLength) {

		RegionCacheElement rce = new RegionCacheElement();
		rce.cs = cs;
		rce.id = regionId;
		rce.seqRegionName = regionName;
		rce.regionLength = regionLength;

		seqRegionCache.put(rce, (regionName + ":" + cs.getInternalID()),
				new Long(regionId));

		return rce;
	}

	public long[] namesToIds(String[] names, CoordinateSystem cs)
			throws AdaptorException {
		long[] result = new long[names.length];
		try {
			for (int i = 0; i < names.length; i++) {
				result[i] = nameToId(names[i], cs);
			}
		} catch (Exception e) {
			// rethrow
			throw new AdaptorException("rethrow", e);
		}
		return result;
	}

	/**
	 * Extract the internalIds from the given Location Ignore gap bits.
	 */
	public long[] locationToIds(Location loc) throws AdaptorException {
		Set result = new HashSet();
		for (Location node = loc; node != null; node = node.next()) {
			if (node.isGap() && logger.isLoggable(Level.FINE)) {
				logger
						.fine("MySQLLocatinConvertor.locationToIds ignoring gap for "
								+ node.toString());
			}
			if (!node.isGap()) {
				long id = nameToId(node.getSeqRegionName(), node
						.getCoordinateSystem());
				result.add(new Long(id));
			}
		}

		long[] ids = new long[result.size()];
		int i = 0;
		Iterator iter = result.iterator();
		while (iter.hasNext())
			ids[i++] = ((Long) iter.next()).longValue();

		return ids;
	}

	public int getLengthByLocation(Location loc) throws AdaptorException {
		RegionCacheElement rce = (RegionCacheElement) seqRegionCache.get(loc
				.getSeqRegionName()
				+ ":" + loc.getCoordinateSystem().getInternalID());
		if (rce != null) {
			return rce.regionLength;
		}

		String sql = " SELECT " + "    seq_region_id, length " + " FROM  "
				+ "    seq_region " + " WHERE  " + "    name = ? AND "
				+ "    coord_system_id = ? ";
		logger.fine(sql);
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, loc.getSeqRegionName());
			ps.setLong(2, loc.getCoordinateSystem().getInternalID());
			ResultSet result = ps.executeQuery();
			if (!result.next()) {
				return -1;
			}
			long seqRegionId = result.getLong(1);
			int regionLength = result.getInt(2);
			cacheSeqRegion(loc.getSeqRegionName(), loc.getCoordinateSystem(),
					seqRegionId, regionLength);
			return regionLength;
		} catch (Exception e) {
			throw new AdaptorException("rethrow", e);
		} finally {
			close(conn);
		}
	}

	public long nameToId(String seqRegionName, CoordinateSystem cs)
			throws AdaptorException {

		// XXX use cache & CSA impl
		if (!cs.isComplete()) 
			cs = driver.getCoordinateSystemAdaptor().fetchComplete(cs);

		RegionCacheElement rce = (RegionCacheElement) seqRegionCache
				.get(seqRegionName + ":" + cs.getInternalID());
		if (rce != null) {
			return rce.id;
		}

		String sql = " SELECT " + "    seq_region_id, length " + " FROM  "
				+ "    seq_region " + " WHERE  " + "    name = ? AND "
				+ "    coord_system_id = ? ";
		logger.fine(sql);
		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, seqRegionName);
			ps.setLong(2, cs.getInternalID());
			ResultSet result = ps.executeQuery();
			if (!result.next()) {
				return -1;
			}
			long seqRegionId = result.getLong(1);
			int regionLength = result.getInt(2);
			cacheSeqRegion(seqRegionName, cs, seqRegionId, regionLength);
			return seqRegionId;
		} catch (Exception e) {
			throw new AdaptorException("rethrow", e);
		} finally {
			close(conn);
		}
	}

	public Location idToLocation(long id) throws AdaptorException {
		return idToLocation(id, 0, 0, 0);
	}

	public Location idToLocation(long id, int start, int end, int strand)
			throws AdaptorException {

		RegionCacheElement rce = idToRegion(id);
		//		default values if necessary
		if (start == 0)
			start = 1;
		if (end == 0)
			end = rce.regionLength;

		return new Location(rce.cs, rce.seqRegionName, start, end, strand,
				false);

	}

	RegionCacheElement idToRegion(long id)
			throws AdaptorException {
		
		// Optimisation: we maintain a local cache of id2coordsys to
		// avoid the risk of CoordinateSystemAdaptor connecting to the
		// database while this method has an open connection. If it did that
		// then >1 connection could be open to the same database.
		if (coordSysCache.isEmpty()) 
			coordSysCache.putAll((Persistent[])driver.getCoordinateSystemAdaptor().fetchAll());
		
		
		RegionCacheElement rce = (RegionCacheElement) seqRegionCache
				.get(new Long(id));

		if (rce != null)
			return rce;

		String sql = " SELECT coord_system_id, name, length "
				+ " FROM seq_region " 
				+ " WHERE seq_region_id = ?";

		Connection conn = null;
		try {
			conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setLong(1, id);
			ResultSet result = ps.executeQuery();
			if (!result.next()) {
				return null;
			}
			long coordSystemId = result.getLong(1);
			String regionName = result.getString(2);
			int regionLength = result.getInt(3);
			CoordinateSystem cs = (CoordinateSystem)coordSysCache.get(coordSystemId);
			
			return cacheSeqRegion2(regionName, cs, id, regionLength);

		} catch (Exception e) {
			throw new AdaptorException(e);
		} finally {
			close(conn);
		}

	}

	/**
	 * @see org.ensembl.driver.LocationConverter#assignSeqRegionNameAndCoordinateSystem(org.ensembl.datamodel.Location)
	 */
	public Location assignSeqRegionNameAndCoordinateSystem(Location loc)
			throws AdaptorException {

		return loc;
	}

	/**
	 * Note: converting an incomplete sourceLocation (missing either or both
	 * start and end) will result in a new location with a start and end based
	 * on where the sourceLocation overlaps the target coordinate system.
	 * Consequently, converting an incomplete location into another coordinate
	 * system and back again will not produce exactly the same location as the
	 * original.
	 */
	public Location convert(Location sourceLocation, CoordinateSystem targetCS,
			boolean includeGaps, boolean allList, boolean setSequenceRegion)
			throws AdaptorException {

		if (targetCS == null) {
			throw new IllegalArgumentException(
					"Need target coordinate system to be set");
		}

		CoordinateSystem sourceCS = sourceLocation.getCoordinateSystem();
		if (sourceCS == null) {
			throw new IllegalArgumentException(
					"Location needs valid coordinate system");
		}

		if (!sourceLocation.isSeqRegionNameSet())
			return new Location(targetCS);

		CoordinateSystem cs = sourceLocation.getCoordinateSystem();
		if (cs!=null && !cs.isComplete())
			cs = driver.getCoordinateSystemAdaptor().fetchComplete(cs);
		if (cs == null)
			throw new AdaptorException(
					"No CoordinateSystem in database corresponding to: "
							+ sourceLocation.getCoordinateSystem());
		sourceLocation = sourceLocation.copy();
		sourceLocation.setCoordinateSystem(cs);
		
		if (!targetCS.isComplete())
			targetCS = driver.getCoordinateSystemAdaptor().fetchComplete(targetCS);

		// we need to know the start and end before we can do the conversion
		if (!sourceLocation.isStartSet() || !sourceLocation.isEndSet())
			sourceLocation = fetchComplete(sourceLocation);

		// can't convert the location if it doesn not correspond to entries
		// in the database
		if (sourceLocation == null)
			return null;

		if (sourceLocation.getCoordinateSystem().equals(targetCS)) {

			// not 100 percent correct yet
			// should remove gaps when includeGpas is false
			// maybe should make a copy
			return sourceLocation;

		}

		AssemblyMapperAdaptor ama = driver.getAssemblyMapperAdaptor();
		AssemblyMapper am = ama.fetchByCoordSystems(sourceLocation
				.getCoordinateSystem(), targetCS);
		Coordinate coords[];
		Location cLoc = sourceLocation;
		Location resultLocation = null;
		Location loc;

		do {
			coords = am.map(cLoc);
			for (int i = 0; i < coords.length; i++) {
				loc = null;
				if (!coords[i].isGap()) {
					loc = new Location(targetCS, coords[i].id, coords[i].start,
							coords[i].end, coords[i].strand);

				} else if (includeGaps) {
					loc = new Location(targetCS, coords[i].id, coords[i].start,
							coords[i].end, coords[i].strand, true);

				}

				if (loc != null) {

					if (setSequenceRegion) {
						loc.setSequenceRegion(driver.getSequenceRegionAdaptor()
								.fetch(loc.getSeqRegionName(),
										loc.getCoordinateSystem()));
					}

					if (resultLocation == null) {
						resultLocation = loc;
					} else {
						resultLocation.append(loc);
					}
				}
			}
			cLoc = cLoc.next();
			loc = null;
		} while (allList && cLoc != null);

		// load the sequence region attribute of resultLocation if required
		if (setSequenceRegion) {

			SequenceRegion sr = driver.getSequenceRegionAdaptor().fetch(
					resultLocation.getSeqRegionName(),
					resultLocation.getCoordinateSystem());
			resultLocation.setSequenceRegion(sr);

		}

		return resultLocation;
	}

	/**
	 * Delegates conversion to appropriate sister method.
	 * 
	 * @return location translated into map coordinates, or null if no
	 *         equivalent exists.
	 * 
	 * @throws AdaptorException
	 *             if the location type is unsupported.
	 */
	public Location convert(Location sourceLocation, String targetMap,
			boolean includeGaps, boolean allList, boolean setSequenceRegion)
			throws AdaptorException {
		Location newLoc = null;
		CoordinateSystemAdaptor csa = driver.getCoordinateSystemAdaptor();
		CoordinateSystem tcs = csa.fetchByMap(targetMap);
		return convert(sourceLocation, tcs, includeGaps, allList,
				setSequenceRegion);
	}

	/**
	 * Converts location and returned location does not have gaps.
	 * 
	 * @return location translated into map coordinates, or null if no
	 *         equivalent exists.
	 */
	public Location convert(Location sourceLocation, String targetMap)
			throws AdaptorException {
		return convert(sourceLocation, targetMap, false, true, false);
	}

	/**
	 * Standard conversion with gaps and all Location is converted.
	 * SequenceRegion attrib in returned location is NOT filled.
	 * 
	 * @param loc
	 * @param cs
	 * @return location translated into map coordinates, or null if no
	 *         equivalent exists.
	 * @throws AdaptorException
	 */

	public Location convert(Location loc, CoordinateSystem cs)
			throws AdaptorException {
		return convert(loc, cs, true, true, false);
	}

	/**
	 * Perform an in place edit where necessary. Does nothing if the location is
	 * complete or can not be made complete.
	 */
	public Location fetchComplete(Location location) throws AdaptorException {

		if (location.isComplete())
			return location;

		// Note: we use recursive implementation rather than a loop so that we
		// can rollback
		// changes (by not making them) if a problem occurs trying to edit
		// either one of
		// the values in this location node or one of the later ones in a
		// location list.

		if (coordSysCache.isEmpty())
			coordSysCache.putAll(driver.getCoordinateSystemAdaptor().fetchAll());

		// TODO test cs.isComplete()
    
    long srID = location.getSegRegionID();
    SequenceRegion sr = null;
    if (srID>0 && (location.getCoordinateSystem()==null || location.getSeqRegionName()==null)) 
      sr = driver.getSequenceRegionAdaptor().fetch(srID);
    
    CoordinateSystem cs = location.getCoordinateSystem();
    if (cs==null) {
      if (sr!=null) 
        cs = sr.getCoordinateSystem();
    }else {
      cs = CoordinateSystemAdaptorImpl.fetch(cs.getName(), cs.getVersion(), coordSysCache);
    }
		if (cs == null)
			return null;
		location.setCoordinateSystem(cs);
		
		String srName = location.getSeqRegionName();
		if (srName==null && sr!=null) {
		  srName = sr.getName();
      location.setSeqRegionName(srName);
    }
		
		if (srID<1 && srName!=null)
			location.setSegRegionID(srID = nameToId(srName, cs));

		// If only the coordinate system is specified then we need to
		// create a location list that includes all of the sequence regions
		// in the coordinate system.
		if (srID<1 && srName==null && !location.isGap()) {

			SequenceRegion[] srs = driver.getSequenceRegionAdaptor()
					.fetchAllByCoordinateSystem(cs);
			Location l = location;
			for (int i = 0; i < srs.length; ++i) {
				sr = srs[i];
				l.setSeqRegionName(sr.getName());
				l.setSegRegionID(sr.getInternalID());
				l.setStart(1);
				l.setEnd((int) sr.getLength());
				l.setStrand(0);
				// only add "next" node if there is another seq reqion
				if (i + 1 < srs.length) {
					l.setNext(new Location(cs));
					l = l.next();
				}
			}

			return location;
		}

		int start = -1;
		int end = -1;
		if (srName != null || srID>0) {

			start = location.getStart();
			if (start < 1)
				start = 1;

			end = location.getEnd();
			if (end < 1) {
				// XXX avoid call that could open a new connection.
				sr = driver.getSequenceRegionAdaptor().fetch(
						location);
				// if sequence region is not in db we can't construct the
				// complete
				// location
				if (sr == null)
					return null;
				if (end < 1)
					end = (int) sr.getLength();
			}
		}

		// update next node if necessary, return null if problem occured
		// updating it
		Location next = location.next();
		if (next != null)
			if (fetchComplete(next) == null)
				return null;

		// store cs, sr, start, end if we get this far
		location.setCoordinateSystem(cs);
		if (srName != null) {
			location.setSeqRegionName(srName);
			location.setSegRegionID(srID);
			location.setStart(start);
			location.setEnd(end);
		}

		return location;
	}

	public String getType() {
		return TYPE;
	}

	/**
	 * Assemblies in dataset.
	 * 
	 * @return list of zero or more assemblies.
	 * @deprecated should use CoordinateSystemAdaptor
	 */
	public String[] fetchAssemblyNames() throws AdaptorException {
		CoordinateSystemAdaptor csa = driver.getCoordinateSystemAdaptor();
		CoordinateSystem cs[] = csa.fetchAll();
		ArrayList resultList = new ArrayList();
		for (int i = 0; i < cs.length; i++) {
			if (cs[i].getName().equals("Chromosome")) {
				resultList.add(cs[i].getVersion());
			}
		}
		String[] assembliesArr = null;
		assembliesArr = (String[]) resultList.toArray(assembliesArr);

		return assembliesArr;
	}

	/**
	 * 
	 * @deprecated please use locationToIds( Location loc) after you converted
	 *             to your target coordinate system
	 * @throws AdaptorException
	 */
	public long[] listSeqRegionIds(Location loc,
			CoordinateSystem targetCoordinateSystem) throws AdaptorException {
		return new long[0];
	}

	/**
	 * @see org.ensembl.driver.LocationConverter#convertToTopLevel(org.ensembl.datamodel.Location)
	 */
	public Location convertToTopLevel(Location location)
			throws AdaptorException {

		Location loc = null;

		CoordinateSystem[] css = driver.getCoordinateSystemAdaptor().fetchAll();
		for (int i = 0; loc == null && i < css.length; i++) {
			CoordinateSystem cs = css[i];
			loc = convert(location, cs);
		}

		return loc;
	}

	/**
	 * @see org.ensembl.driver.LocationConverter#dereference(org.ensembl.datamodel.Location)
	 */
	public Location dereference(Location loc) throws AdaptorException {
		return driver.getAssemblyExceptionAdaptor().dereference(loc);
	}

	/**
	 * @throws AdaptorException
	 * @see org.ensembl.driver.LocationConverter#rereference(Location,
	 *      org.ensembl.datamodel.SequenceRegion)
	 */
	public Location rereference(Location loc, SequenceRegion seqRegion)
			throws AdaptorException {
		return driver.getAssemblyExceptionAdaptor().rereference(loc, seqRegion);
	}

} // LocationConverterImpl
