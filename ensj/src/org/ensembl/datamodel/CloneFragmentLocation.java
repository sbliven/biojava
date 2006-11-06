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
  License along with this library; if not, write to the Free Sofnetware
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.ensembl.datamodel;

import java.text.ParseException;
import java.util.Comparator;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.ensembl.util.StringUtil;
import org.ensembl.util.Warnings;

/**
 * Location on a Contig (Clone Fragment). 
 */
public class CloneFragmentLocation extends Location implements Cloneable {

  /**
   * Used by the (de)serialization system to determine if the data 
   * in a serialized instance is compatible with this class.
   *
   * It's presence allows for compatible serialized objects to be loaded when
   * the class is compatible with the serialized instance, even if:
   *
   * <ul>
   * <li> the compiler used to compile the "serializing" version of the class
   * differs from the one used to compile the "deserialising" version of the
   * class.</li>
   *
   * <li> the methods of the class changes but the attributes remain the same.</li>
   * </ul>
   *
   * Maintainers must change this value if and only if the new version of
   * this class is not compatible with old versions. e.g. attributes
   * change. See Sun docs for <a
   * href="http://java.sun.com/j2se/1.4.2/docs/guide/serialization/">
   * details. </a>
   *
   */
  private static final long serialVersionUID = 1L;



	private static final Logger logger = Logger.getLogger(CloneFragmentLocation.class.getName());

	public final static CoordinateSystem DEFAULT_CS = new CoordinateSystem("contig");
	public final static CloneFragmentLocation DEFAULT = new CloneFragmentLocation();

  /**
   * @return new Location("contig:"+s);
   */
  public static Location valueOf(String s) throws ParseException {
    return new Location("contig:"+s);
  }

	/**
	 * Returns a comparator that orders <code>CloneFragmentLocation</code> by the
	 * following fields in this order: cloneFragmentInternalID, start, end and strand.
	 */
	public final static Comparator ASCENDING_ORDER = new AscendingOrder();

	long cloneFragmentInternalID;

	private static class AscendingOrder implements Comparator {
		public int compare(Object o1, Object o2) {
			CloneFragmentLocation l1 = (CloneFragmentLocation)o1;
			CloneFragmentLocation l2 = (CloneFragmentLocation)o2;

			if (l1.cloneFragmentInternalID > l2.cloneFragmentInternalID)
				return 1;
			if (l1.cloneFragmentInternalID < l2.cloneFragmentInternalID)
				return -1;

			if (l1.getStart() > l2.getStart())
				return 1;
			if (l1.getStart() < l2.getStart())
				return -1;

			if (l1.getEnd() > l2.getEnd())
				return 1;
			if (l1.getEnd() < l2.getEnd())
				return -1;

			if (l1.getStrand() > l2.getStrand())
				return 1;
			if (l1.getStrand() < l2.getStrand())
				return -1;

			return 0;
		}
	}

	/**
	 * Constructs a location with coordinate system = CloneFragmentLocation.DEFAULT_MAP,
	 * start=1, end=length+1, strand=unset and whether it is a gap.
	 */
	public CloneFragmentLocation(boolean gap, int length) {
		super(CloneFragmentLocation.DEFAULT_CS, gap, length);
		Warnings.deprecated("Instantiating CloneFragmentLocation directly is no longer advisable - use Location instead.");
	}

	/**
	 * Constructs a location with specified coordinate system, start=1, end=length+1,
	 * strand=unset and whether it is a gap.
	 */
	public CloneFragmentLocation(CoordinateSystem cs, boolean gap, int length) {
		super(cs, gap, length);
		Warnings.deprecated("Instantiating CloneFragmentLocation directly is no longer advisable - use Location instead.");

	}

	/**
	 * Sets coordinate system to CloneFragmentLocation.DEFAULT_CS.
	 */
	public CloneFragmentLocation() {
		super(CloneFragmentLocation.DEFAULT_CS);
		Warnings.deprecated("Instantiating CloneFragmentLocation directly is no longer advisable - use Location instead.");

	}

	/**
	 * Sets coordinate system to CloneFragmentLocation.DEFAULT_CS.
	 */
	public CloneFragmentLocation(String seqRegionName) {
		super(CloneFragmentLocation.DEFAULT_CS, seqRegionName);
		Warnings.deprecated("Instantiating CloneFragmentLocation directly is no longer advisable - use Location instead.");

	}



	/**
	 * Extracts optional start, end and strand from _parts_ and sets them on
	 * the location.  */
	static Location setStartEndStrand(StringTokenizer parts, Location loc) throws ParseException {
		if (parts.hasMoreTokens()) {
			StringTokenizer startEnd = new StringTokenizer(parts.nextToken(), "-", true);
			String next = startEnd.nextToken();
			if (next.equals("-")) {
				// no start specified, did they include an end?
				if (startEnd.hasMoreTokens()) {
					next = startEnd.nextToken();
					loc.setEnd(StringUtil.parseInt(next));
				}
			} else {
				// start was specified ...
				loc.setStart(StringUtil.parseInt(next));
				if (startEnd.hasMoreTokens()) {
					next = startEnd.nextToken();
					if (next.equals("-")) {
						// no start specified, did they include an end?
						if (startEnd.hasMoreTokens()) {
							next = startEnd.nextToken();
							loc.setEnd(StringUtil.parseInt(next));
						}
					}
				}
			}

		}
		if (parts.hasMoreTokens()) {

			int nextInt = StringUtil.parseInt(parts.nextToken());
			if (nextInt < -1 || nextInt > 1) {
				throw new ParseException("Strand must be -1, 0, or 1", 0);
			}
			loc.setStrand(nextInt);
		}

		return loc;
	}

	/**
	 * Checks if locations overlap each other, ignores strand.
	 * @return true if locations overlap.
	 */
	public boolean overlaps(Location other) {

		//if ( other.getType()!=TYPE ) return false;

		for (CloneFragmentLocation x = this; x != null; x = x.nextCFL()) {

			for (CloneFragmentLocation y = (CloneFragmentLocation)other; y != null; y = y.nextCFL()) {

				if ((x.getStart() <= y.getEnd() || !x.isStartSet() || !y.isEndSet())
					&& (x.getEnd() >= y.getStart() || !x.isEndSet() || !y.isStartSet())
					&& (x.cloneFragmentInternalID == 0
						|| y.cloneFragmentInternalID == 0
						|| x.cloneFragmentInternalID == y.cloneFragmentInternalID)
					&& x.getCoordinateSystem().equals(y.getCoordinateSystem()))
					return true;

			}
		}

		return false;

	}

	/** 
	 *@throws UnsupportedOperationException  
	 */
	public String getChromosome() {
		throw new UnsupportedOperationException();
	}

	/** 
	 *@throws UnsupportedOperationException  
	 */
	public void setChromosome(String chromosomse) {
		throw new UnsupportedOperationException();
	}

	/** 
	 *@return false 
	 */
	public boolean isChromosomeSet() {
		return false;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		buf.append(super.toString());
		buf.append(", cloneFragmentInternalID=").append(cloneFragmentInternalID);
		buf.append(", next=").append(next);
		buf.append("]");

		return buf.toString();
	}

	public boolean isCloneFragmentInternalIDSet() {
		return cloneFragmentInternalID > 0;
	}

	public void clearChromosome() {
		throw new org.ensembl.util.MethodNotSupportedException();
	}

	private CloneFragmentLocation next;

	public Location next() {
		return next;
	}

	/**
	 * @return next location as a CloneFragmentLocation, or null if not set. 
	 */
	public CloneFragmentLocation nextCFL() {
		return next;
	}

	public void setNext(CloneFragmentLocation next) {
		this.next = next;
	}


	public boolean hasNext() {
		return next != null;
	}

	public Location append(Location location) {
		return append((CloneFragmentLocation)location);
	}

	/**
	 * Adds _location_ to the end of the location list, this is the same as
	 * using setNext(location) if next==null.
	 */
	public CloneFragmentLocation append(CloneFragmentLocation location) throws InvalidLocationException {
		if (location == this)
			throw new InvalidLocationException("Location is already at front of location list: " + location);

		CloneFragmentLocation node = null;
		for (node = this; node.next != null; node = node.next) {
			if (node == node.next)
				throw new InvalidLocationException("Location is already in location list: " + location);
		}
		node.next = location;

		return this;
	}

	/**
	 * Iterate through this location and the other one comparing them. The
	 * order of the attributes used for the ordering is: cloneFragmentInternalID,
	 * start, end, strand.
	 * @return -1 if this location is before other, 0 if it is equivalent, 1 if
	 * it is after it.
	 */
	public int compareTo(Object other) {

		if (other == null)
			return 1;

		CloneFragmentLocation nxt = this;
		CloneFragmentLocation nxt2 = (CloneFragmentLocation)other;

		while (nxt != null) {

			if (nxt.cloneFragmentInternalID > nxt2.cloneFragmentInternalID)
				return 1;
			if (nxt.cloneFragmentInternalID < nxt2.cloneFragmentInternalID)
				return -1;

			if (nxt.getStart() > nxt2.getStart())
				return 1;
			if (nxt.getStart() < nxt2.getStart())
				return -1;

			if (nxt.getEnd() > nxt2.getEnd())
				return 1;
			if (nxt.getEnd() < nxt2.getEnd())
				return -1;

			if (nxt.getStrand() > nxt2.getStrand())
				return 1;
			if (nxt.getStrand() < nxt2.getStrand())
				return -1;

			nxt = nxt.nextCFL();
			nxt2 = nxt2.nextCFL();

			if (nxt == null && nxt2 != null)
				return -1;
			if (nxt2 == null && nxt != null)
				return 1;
		}

		return 0;
	}

  /**
  * @deprecated use complement, reverse is not biological meaningful
  * @see Location#complement()
  */
	public Location reverse() {

		CloneFragmentLocation nxt = next;
		if (nxt == null)
			return this;

		CloneFragmentLocation prev = null;
		CloneFragmentLocation curr = this;

		// move through list changing next values.
		do {
			curr.next = prev;

			prev = curr;
			curr = nxt;
			nxt = nxt.next;
		} while (nxt != null);
		curr.next = prev;

		return curr;
	}

}
