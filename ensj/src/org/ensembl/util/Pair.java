/*
  Copyright (C) 2002 EBI, GRL

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

package org.ensembl.util;

/**
 * A simple "record" data structure representing a pair of strings. An int
 * can be associated with each string. Instances of this class have a natural
 * ordering making them easy to sort (Arrays.sort(), Collections.sort()). hashCode() and equals
 * are also implemented making it suitable for use in hashes where hash by value rather than
 * identity is required. 
 */
public class Pair implements Comparable {

	private int hash = -1;
	public final String left;
	public final String right;
	public final int leftInt;
	public final int rightInt;
	public final String type;

	/**
	 * leftInt and rightInt default to 0.
	 * @param left
	 * @param right
	 */
	public Pair(String left, String right) {
		this(left, 0, right, 0, null);
	}

	public Pair(String left, 
							int leftInt, 
							String right, 
							int rightInt,
							String type) {
								
		this.left = left;
		this.leftInt = leftInt;
		this.right = right;
		this.rightInt = rightInt;
		this.type = type;
	}

	public String toString() {
		return "[left="
			+ left
			+ ", leftInt="
			+ leftInt
			+ ", rightInt="
			+ rightInt
			+ ", right="
			+ right
			+ "]";
	}

	public boolean equals(Object o) {
		return o instanceof Pair && o.hashCode() == hashCode();
	}

	public int hashCode() {
		if (hash == -1) {
			hash = 17;
			hash = hash * 37 + ((left == null) ? 0 : left.hashCode());
			hash = hash * 37 + ((right == null) ? 0 : right.hashCode());
			hash = hash * 37 + ((leftInt > -1) ? leftInt : leftInt * -7);
			hash = hash * 37 + ((rightInt > -1) ? rightInt : rightInt * -7);
			hash = hash * 37 + ((type == null) ? 0 : type.hashCode());
		}
		return hash;
	}

	public int compareTo(Object o) {
		Pair other = (Pair) o;

		int tmp = left.compareTo(other.left);
		if (tmp != 0)
			return tmp;

		tmp = leftInt - other.leftInt;
		if (tmp != 0)
			return tmp;

		tmp = right.compareTo(other.right);
		if (tmp != 0)
			return tmp;

		return rightInt - other.rightInt;
	}
}
