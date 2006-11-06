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

package org.ensembl.datamodel;

import java.util.List;

import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.util.mapper.Coordinate;
import org.ensembl.util.mapper.Mapper;
import org.ensembl.util.mapper.Pair;
import org.ensembl.util.mapper.RangeRegistry;

/**
 * @author arne
 *
 */
public class ChainedAssemblyMapper implements AssemblyMapper {

	public static void main(String[] args) {
	}

	private static final int CHUNKFACTOR = 20;
	private static final int MAXMAPPERSIZE = 6000;

	private RangeRegistry firstReg, lastReg;
	private CoordinateSystem csFirst, csMiddle, csLast;
	private Mapper firstMiddleMapper, lastMiddleMapper, firstLastMapper;
	private CoreDriver driver;

	public ChainedAssemblyMapper(
		CoreDriver driver,
		CoordinateSystem cs1,
		CoordinateSystem cs2,
		CoordinateSystem cs3) {

		this.csFirst = cs1;
		this.csMiddle = cs2;
		this.csLast = cs3;

		this.driver = driver;
		this.firstMiddleMapper = new Mapper("first", "middle");
		this.lastMiddleMapper = new Mapper("last", "middle");

		this.firstLastMapper = new Mapper("first", "last");
		this.firstReg = new RangeRegistry();
		this.lastReg = new RangeRegistry();
	}

	public int getSize() {
		return firstLastMapper.getSize()
			+ firstMiddleMapper.getSize()
			+ lastMiddleMapper.getSize();
	}

	public void flush() {
		firstReg.flush();
		lastReg.flush();
		firstMiddleMapper.flush();
		lastMiddleMapper.flush();
		firstLastMapper.flush();
	}
	/**
	 * 
	 * @param loc All assembly table entries for this Location are retrieved ans
	 * stored in the contained mapper
	 * @return The boolean indicates wether the Location is from the beginning or from
	 * the end of the chain, so this doesnt have to be checked again
	 * @throws AdaptorException
	 */
	public boolean registerLocation(Location loc) throws AdaptorException {
		RangeRegistry rr;
		boolean fromFirst;

		CoordinateSystem locCoord = loc.getCoordinateSystem();
		if (locCoord.equals(csFirst)) {
			rr = firstReg;
			fromFirst = true;
		} else if (locCoord.equals(csLast)) {
			rr = lastReg;
			fromFirst = false;
		} else {
			throw new AdaptorException("unknown coordinate system in location");
		}

		int minStart, minEnd;
		minStart = (loc.getStart() >> CHUNKFACTOR) << CHUNKFACTOR;
		minEnd = (((loc.getEnd() >> CHUNKFACTOR) + 1) << CHUNKFACTOR ) - 1;
		try {

			List ranges =
				rr.checkAndRegister(
					loc.getSeqRegionName(),
					loc.getStart(),
					loc.getEnd(),
					minStart,
					minEnd);

			if (ranges != null) {
				if (getSize() > MAXMAPPERSIZE) {
					flush();
					ranges =
						rr.checkAndRegister(loc.getSeqRegionName(), minStart, minEnd);
				}
				String tag = (fromFirst) ? "first" : "last";
				driver.getAssemblyMapperAdaptor().registerChained(this, tag, loc.getSeqRegionName(), ranges);
			}
		} catch (IllegalArgumentException e) {
			throw new AdaptorException("start end in Location illegal", e);
		}

		return fromFirst;
	}

	public Coordinate[] map(Location loc) throws AdaptorException {
		boolean fromFirst = registerLocation(loc);
		String coordinateName;

		if (fromFirst) {
			coordinateName = "first";
		} else {
			coordinateName = "last";
		}
		try {
			return firstLastMapper.mapCoordinate(
				loc.getSeqRegionName(),
				loc.getStart(),
				loc.getEnd(),
				loc.getStrand(),
				coordinateName);
		} catch (IllegalArgumentException e) {
			throw new AdaptorException(
				"Location with bad coordinates" + loc.toString(),
				e);
		}
	}

	public Coordinate fastmap(Location loc) throws AdaptorException {
		boolean fromFirst = registerLocation(loc);
		String coordinateName;

		if (fromFirst) {
			coordinateName = "first";
		} else {
			coordinateName = "last";
		}
		try {
			return firstLastMapper.fastmap(
				loc.getSeqRegionName(),
				loc.getStart(),
				loc.getEnd(),
				loc.getStrand(),
				coordinateName);

		} catch (IllegalArgumentException e) {
			throw new AdaptorException(
				"Location with bad coordinates" + loc.toString(),
				e);
		}
	}

	public String[] listSeqRegionNames(Location loc) throws AdaptorException {
		boolean fromFirst = registerLocation(loc);
		Pair[] pairs;
		try {

			if (fromFirst) {
				pairs =
					firstLastMapper.listPairs(
						loc.getSeqRegionName(),
						loc.getStart(),
						loc.getEnd(),
						"first");
			} else {
				pairs =
					firstLastMapper.listPairs(
						loc.getSeqRegionName(),
						loc.getStart(),
						loc.getEnd(),
						"last");
			}
		} catch (IllegalArgumentException e) {
			throw new AdaptorException(
				"Location with bad coordinates" + loc.toString(),
				e);
		}
		String[] result = new String[pairs.length];
		for (int i = 0; i < pairs.length; i++) {
			if (fromFirst) {
				result[i] = pairs[i].toId;
			} else {
				result[i] = pairs[i].fromId;
			}
		}

		return result;
	}


	public CoordinateSystem getCsFirst() {
		return csFirst;
	}

	public CoordinateSystem getCsLast() {
		return csLast;
	}

	public CoordinateSystem getCsMiddle() {
		return csMiddle;
	}

	public CoreDriver getDriver() {
		return driver;
	}

	public Mapper getFirstLastMapper() {
		return firstLastMapper;
	}

	public Mapper getFirstMiddleMapper() {
		return firstMiddleMapper;
	}

	public RangeRegistry getFirstReg() {
		return firstReg;
	}

	public Mapper getLastMiddleMapper() {
		return lastMiddleMapper;
	}

	public RangeRegistry getLastReg() {
		return lastReg;
	}

	public void setDriver(CoreDriver driver) {
		this.driver = driver;
	}

}
