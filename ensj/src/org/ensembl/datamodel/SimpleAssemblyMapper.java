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

import java.util.HashMap;
import java.util.HashSet;

import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.AssemblyMapperAdaptor;
import org.ensembl.driver.CoreDriver;
import org.ensembl.util.mapper.Coordinate;
import org.ensembl.util.mapper.Mapper;
import org.ensembl.util.mapper.Pair;

public class SimpleAssemblyMapper implements AssemblyMapper {
	private CoordinateSystem componentCoordinateSystem, assembledCoordinateSystem;
	private CoreDriver driver;
	public Mapper mapper;
	private HashSet componentRegistry;
	private HashMap assembledRegistry;

	public SimpleAssemblyMapper(
		CoreDriver driver,
		CoordinateSystem assembledCoordinateSystem,
		CoordinateSystem componentCoordinateSystem) {
		this.componentCoordinateSystem = componentCoordinateSystem;
		this.assembledCoordinateSystem = assembledCoordinateSystem;
		this.driver = driver;
		mapper = new Mapper("component", "assembled");
		componentRegistry = new HashSet();
		assembledRegistry = new HashMap();
	}

	public Coordinate[] map(Location loc) throws AdaptorException {

		// register and check on which end of this mapper
		if (registerLocation(loc)) {
			return mapper.mapCoordinate(
				loc.getSeqRegionName(),
				loc.getStart(),
				loc.getEnd(),
				loc.getStrand(),
				"component");
		} else {
			return mapper.mapCoordinate(
				loc.getSeqRegionName(),
				loc.getStart(),
				loc.getEnd(),
				loc.getStrand(),
				"assembled");
		}
	}

	public void flush() {
		assembledRegistry.clear();
		componentRegistry.clear();
		mapper.flush();
	}

	public int getSize() {
		return mapper.getSize();
	}

	public Coordinate fastmap(Location loc) throws AdaptorException {
		if (registerLocation(loc)) {
			return mapper.fastmap(
				loc.getSeqRegionName(),
				loc.getStart(),
				loc.getEnd(),
				loc.getStrand(),
				"component");
		} else {
			return mapper.fastmap(
				loc.getSeqRegionName(),
				loc.getStart(),
				loc.getEnd(),
				loc.getStrand(),
				"assembled");
		}
	}

	/**
	 * Registers the Location if necessary. 
	 * This triggers the loading in the AssemblyMapperAdaptor.
	 * @param loc
	 * @return true on component coordinate system, false on Assembly coordinate system
	 * @throws Exception
	 */
	private boolean registerLocation(Location loc) throws AdaptorException {
		if ((loc.getCoordinateSystem() == componentCoordinateSystem)
			|| (loc.getCoordinateSystem().equals(componentCoordinateSystem))) {

			if (!componentRegistry.contains(loc.getSeqRegionName())) {
				AssemblyMapperAdaptor aa =
					(AssemblyMapperAdaptor) driver.getAdaptor(AssemblyMapperAdaptor.TYPE);
				aa.registerComponent(this, loc.getSeqRegionName());
			}
			return true;
		} else if (
			(loc.getCoordinateSystem() == assembledCoordinateSystem)
				|| (loc.getCoordinateSystem().equals(assembledCoordinateSystem))) {

			AssemblyMapperAdaptor aa =
				(AssemblyMapperAdaptor) driver.getAdaptor(AssemblyMapperAdaptor.TYPE);
				if( ! loc.isEndSet() || !loc.isStartSet()) {
					throw new IllegalArgumentException( "Need location with start, end for mapping" );
				}
			aa.registerAssembled(
				this,
				loc.getSeqRegionName(),
				loc.getStart(),
				loc.getEnd());
			return false;
		} else {
			throw (new IllegalArgumentException("Coordinate system in argument location not known: "+ loc.getCoordinateSystem()));
		}
	}

	public String[] listSeqRegionNames(Location loc) throws AdaptorException {
		String[] result;
		Pair[] pairs;

		boolean componentCoord = registerLocation(loc);

		if (componentCoord) {
			pairs =
				mapper.listPairs(
					loc.getSeqRegionName(),
					loc.getStart(),
					loc.getEnd(),
					"component");
		} else {
			pairs =
				mapper.listPairs(
					loc.getSeqRegionName(),
					loc.getStart(),
					loc.getEnd(),
					"assembled");
		}

		result = new String[pairs.length];
		for (int i = 0; i < pairs.length; i++) {
			if (componentCoord) {
				result[i] = pairs[i].fromId;
			} else {
				result[i] = pairs[i].toId;
			}
		}
		return result;
	}

	public boolean haveRegisteredComponent(String seqRegionName) {
		return (componentRegistry.contains(seqRegionName));
	}

	public boolean haveRegisteredAssembled(String seqRegionName, int chunk) {
		if (assembledRegistry.containsKey(seqRegionName)) {
			HashSet chunkIdSet;
			chunkIdSet = (HashSet) assembledRegistry.get(seqRegionName);
			return (chunkIdSet.contains(new Integer(chunk)));
		}
		return false;
	}

	public void registerComponent(String seqRegionName) {
		componentRegistry.add(seqRegionName);
	}
	/**
	 * Mark the given assembly chunk as registered in this AssemblyMapper. 
	 * This doesnt actually load anything. Its merely used by the loading process.
	 * @param seqRegionName 
	 * @param chunk
	 */
	public void registerAssembled(String seqRegionName, int chunk) {
		HashSet chunkIdSet;
		if (assembledRegistry.containsKey(seqRegionName)) {
			chunkIdSet = (HashSet) assembledRegistry.get(seqRegionName);
		} else {
			chunkIdSet = new HashSet();
			assembledRegistry.put(seqRegionName, chunkIdSet);
		}
		chunkIdSet.add(new Integer(chunk));
	}

	public CoordinateSystem getComponentCoordinateSystem() {
		return componentCoordinateSystem;
	}

	public CoordinateSystem getAssembledCoordinateSystem() {
		return assembledCoordinateSystem;
	}
}
