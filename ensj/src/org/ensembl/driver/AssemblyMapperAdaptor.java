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

package org.ensembl.driver;

import java.util.List;

import org.ensembl.datamodel.AssemblyMapper;
import org.ensembl.datamodel.ChainedAssemblyMapper;
import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.SimpleAssemblyMapper;

/**
 * @author Arne Stabenau
 *
 */

public interface AssemblyMapperAdaptor extends Adaptor {
	
  final static String TYPE = "assembly_mapper";

  AssemblyMapper fetchByCoordSystems(
			CoordinateSystem cs1,
			CoordinateSystem cs2) throws AdaptorException ;
	
	void registerAssembled(
		SimpleAssemblyMapper asmMapper,
		String seqRegionName,
		int start,
		int end)
		throws AdaptorException; 

		
	void registerComponent(
		SimpleAssemblyMapper asmMapper,
		String componentSeqRegionName) throws AdaptorException;

	void registerChained(
		ChainedAssemblyMapper cam,
		String startTag,
		String seqRegionName,
		List ranges) throws AdaptorException;		
}

	
