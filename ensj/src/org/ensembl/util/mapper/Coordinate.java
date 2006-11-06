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

package org.ensembl.util.mapper;


/**
 * This class converts coordinates from one reference system to another.
 * It uses pairs of aligned ungapped coordinates.
 */

public class Coordinate {
  public int start, end, strand;
  public String id;

  public Coordinate( int start, int end ) {
    this.start = start;
    this.end = end;
    id = null;
    strand = 0;
  }

  public Coordinate( String id, int start, int end, int strand ) {
    this.id = id;
    this.start = start;
    this.end = end;
    this.strand = strand;
  }

  public boolean isGap() {
    return (id==null);
  }

  public String toString() {
    return id+":"+start+"-"+end+","+strand;
  }
  
  public int length() {
  	return end - start + 1;
  }
}
