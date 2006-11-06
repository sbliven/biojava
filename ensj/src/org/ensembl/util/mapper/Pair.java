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

public class Pair {
  public int fromStart, fromEnd, toStart, toEnd, ori;
  public String fromId, toId;

    public Pair( String fromId, int fromStart, int fromEnd,
		 String toId, int toStart, int toEnd, int ori ) {
      this.fromId = fromId;
      this.fromStart = fromStart;
      this.fromEnd = fromEnd;
      this.toId = toId;
      this.toStart = toStart;
      this.toEnd = toEnd;
      this.ori = ori;
    }
}
