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

/**
 * Store information about the mapping between a pair of co-ordinate systems.
 * Mapping may be direct (e.g. chromosome-contig) or indirect (e.g. chromosome-contig-clone)
 * Arbitrary-length mappings are supported but in practice the length
 * will be 2 or maybe 3.
 */
public class CoordinateSystemMapping {

	CoordinateSystem[] path = null;

	/**
	 * Create a new mapping.
	 * @param path Array of components of this mapping path.
	 */
	public CoordinateSystemMapping(CoordinateSystem[] path) {

		this.path = path;

	}

	public CoordinateSystem[] getPath() {

		return path;

	}

	public CoordinateSystem getFirst() {

		return path[0];

	}

	public CoordinateSystem getLast() {

		return path[path.length - 1];

	}

	public String toString() {

		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < path.length; i++) {
      if (i>0) buf.append(" <-> ");
			buf.append(path[i].getName()+"_"+path[i].getVersion());
		}
    buf.append("]");

		return buf.toString();
	}

}