/*
 * Copyright (C) 2002 EBI, GRL
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

package org.ensembl.datamodel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.ensembl.util.StringUtil;

/**
 * A Location is an 'address' of regions on sequences.
 * 
 * <p>
 * Each Location is defined by a coordinate system (e.g. "chromosome", "contig",
 * "supercontig"), sequence region (e.g. "X") and a region (start, end and strand).
 * </p>
 * 
 * <p>
 * Ensembl supports multiple coordinate systems; if a Location's coordinate
 * system is "chromosome" then it's sequence region is a chromosome such as "X"
 * or "21". Similarly if the coordinate system is "contig" then it's sequence
 * region is a contig. It is often possible to convert locations from one
 * coordinate system to another using a <code>LocationConverter</code>. e.g.
 * <code>driver.getLocationConverter().convert(loc, "chromosome")</code>
 * </p>
 * 
 * <p>
 * Locations can exist as a single instance or as a node in a linked list of
 * Locations but most users will only deal explicitly with the single instance
 * flavour of locations. There are various cases where list behaviour is
 * desirable because the thing being represented jumps parts of a sequence
 * region. For example the translation coding location represents the coding
 * part of exons and "jumps" introns and the non-coding parts of exons on a
 * sequence. List related methods include <a href="#next()">next() </a>, <a
 * href="#setNext(org.ensembl.datamodel.Location)">setNext(Location) </a>, <a
 * href="#last()">last() </a> and <a
 * href="#append(org.ensembl.datamodel.Location)">append(Location) </a>. <a
 * href="#getNodeLength()">getNodeLength() </a> returns the length of <b>this
 * </b> node and <a href="#getLength()">getLength() </a> returns the combined length
 * of <b>all </b> nodes in the list from <b>this </b> one to the last. If this
 * node is the head of a list then length() returns the length for the whole
 * list. <a href="#containsGapNodes()">containsGapNodes()</a> and 
 * <a href="#removeGapNodes()">removeGapNodes()</a> are also list aware methods for
 * confirming the existence of gap nodes in a list and removing them respectively. 
 * </p>
 *
 * <p>
 * Useful methods:
 * <ul>
 * 
 * <li><b>Equality:</b> Use <code>loc1.compareTo(loc2)==0</code>  
 * to test for value equality. Use equals() to test for object
 * identity. (The equals() and hashCode() methods are
 * not overidden for performance reasons.)</li>
 * 
 * <li><b>Change region:</b> <a href="#transform(int, int)">transform(int, int) </a> is useful for
 * resizing locations and finding flanking, explanded and cropped locations.
 * </li>
 * 
 * <li><b>Overlapping Locations:</b> Whether locations overlap and the size of any overlap can be determined
 * using <a href="#overlaps(org.ensembl.datamodel.Location)">overlaps(Location)
 * </a>, <a href="#overlaps(org.ensembl.datamodel.Location, boolean)">overlaps(Location, boolean) </a>, <a
 * href="#overlapSize(org.ensembl.datamodel.Location)">overlapSize(Location)
 * </a> and <a href="#overlapSize(org.ensembl.datamodel.Location, boolean)">overlapSize(Location, boolean) </a>. Note that strand is ignored by
 * default. If you want to take strand into account you should use the boolean
 * flag version of the methods.</li>
 * 
 * <li><b>Sorting:</b> Arrays and collections of locations can be easily sorted because Location
 * implements the Comparable interface. e.g.
 * <code>Collections.sort(listOfLocation)</code> and
 * <code>java.util.Arrays.sort(arrayOfLocations)</code>
 * </li>
 *
 * </ul>
 * </p>
 *
 * 
 * @see CoordinateSystem
 * @see SequenceRegion
 * @see org.ensembl.driver.LocationConverter
 */
public class Location implements Cloneable, java.io.Serializable, Comparable {

	/**
	 * Used by the (de)serialization system to determine if the data in a
	 * serialized instance is compatible with this class.
	 * 
	 * It's presence allows for compatible serialized objects to be loaded when
	 * the class is compatible with the serialized instance, even if:
	 * 
	 * <ul>
	 * <li>the compiler used to compile the "serializing" version of the class
	 * differs from the one used to compile the "deserialising" version of the
	 * class.</li>
	 * 
	 * <li>the methods of the class changes but the attributes remain the same.
	 * </li>
	 * </ul>
	 * 
	 * Maintainers must change this value if and only if the new version of this
	 * class is not compatible with old versions. e.g. attributes change. See
	 * Sun docs for <a
	 * href="http://java.sun.com/j2se/1.4.2/docs/guide/serialization/"> details.
	 * </a>
	 *  
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Use this to format long numbers in strings. e.g. start, end. Keep a
	 * static reference to avoid repeated NumberFormat.getXXXInstance() calls.
	 */
	private transient static NumberFormat numberFormatter = null;

	private final static Logger logger = Logger.getLogger(Location.class
			.getName());

	private CoordinateSystem cs;

	private int start;

	private int end;

	private int strand;

	private boolean gap;

	private Location next;

	// 3 ways to specify the sequence region.
	private String seqRegionName;
	private long segRegionID;
	private SequenceRegion sequenceRegion;
	

	/**
	 * Private constructor used by other methods.
	 */
	private Location() {
	}

	/**
	 * Default values: strand = 0, gap = false.
	 * 
	 * @param cs
	 * @param seqRegionName
	 * @param start
	 * @param end
	 * @throws InvalidLocationException
	 */
	public Location(CoordinateSystem cs, String seqRegionName, int start,
			int end) throws InvalidLocationException {
		this(cs, seqRegionName, start, end, 0, false);
	}

	/**
	 * Both strands of the sequence region from _start_ to _end_ in the default version of the specified coordinate system.
	 * @param coordinateSystemName
	 *            The name of the CoordinateSystem this location is expressed in.
	 * @param seqRegionName
	 *            The name of the sequence region.
	 * @param start
	 *            Start position, >=0.
	 * @param end
	 *            End position, end>start.
	 * @throws InvalidLocationException
	 *             if location invalid.
	 */
	public Location(String coordinateSystemName, String seqRegionName, int start,
			int end)
			throws InvalidLocationException {
		this(new CoordinateSystem(coordinateSystemName), seqRegionName, start, end, 0, false);
	}
	
	/**
	 * The strand of the sequence region from _start_ to _end_ in the default version of the specified coordinate system.
	 * @param coordinateSystemName
	 *            The name of the CoordinateSystem this location is expressed in.
	 * @param seqRegionName
	 *            The name of the sequence region.
	 * @param start
	 *            Start position, >=0.
	 * @param end
	 *            End position, end>start.
	 * @param strand
	 *            Strand -1,0 (unstranded) or +1.
	 * @throws InvalidLocationException
	 *             if location invalid.
	 */
	public Location(String coordinateSystemName, String seqRegionName, int start,
			int end, int strand)
			throws InvalidLocationException {
		this(new CoordinateSystem(coordinateSystemName), seqRegionName, start, end, strand, false);
	}
	
	/**
	 * Location object is initialized to the value of the specified string.
	 * Below are examples demonstrating the supported format for the string.
	 * 
	 * <p>
	 * <ul>
	 * Examples:
	 * <li>"chromosome:20:50000000-51000000:0" Bases from both strands of
	 * chromosome 20 in the range 50m to 51m using the default version of the
	 * "chromosome" coordinate system.
	 * <li>"chromosome:20" All of chromosome 20.
	 * <li>"chromosome:20:33" All of Chromosome 20 from the 33rd base onwards.
	 * <li>"chromosome_2:20:33" All of Chromosome 20 from the 33rd base onwards
	 * from coordinate system "chromosome" version "2".
	 * <li>"chromosome:20:33-" Same.
	 * <li>"chromosome:20:33-40" All of Chromosome 20 from the 33rd to the 40th
	 * base inclusive.
	 * <li>"chromosome:20:33-40:1" The positive strand of chromosome 20 from
	 * the 33rd to the 40 base inclusive.
	 * <li>"chromosome:20:-40" The first 40 bases of chromosome 20.
	 * <li>"chromosome:20:-:1" The positive strand of chromosome 20.
	 * <li>"chromosome:x:1000000-2000000" X chromsome from 10m to 20m bases.
	 * <li>"chromosome:x:1m-2m" Same.
	 * <li>"chromosome:x:1,000,000-2,000,000:1" Same.
	 * <li>"chromosome:x:1k-2k" X chromsome from 1,000 to 2,000 bases.
	 * </ul>
	 * </p>
	 * 
	 * @param s
	 *            The string to be parsed.
	 * @throws ParseException
	 *             if the string can not be parsed as an AssemblyLocation.
	 */
	public Location(String s) throws ParseException {

		//  initialise this node with the values from the fist location string
		Location loc = this;

		String[] locationNodeString = s.split("->");
		for (int i = 0; i < locationNodeString.length; i++) {

			if (i > 0) {
				Location tmp = new Location();
				loc.setNext(tmp);
				loc = tmp;
			}

			StringTokenizer parts = new StringTokenizer(locationNodeString[i],
					":");
			if (!parts.hasMoreTokens())
				throw new ParseException(
						"Invalid location, must contain at least Coordinate system name: "
								+ s, 0);

			StringTokenizer coord = new StringTokenizer(parts.nextToken(), "_");
			String n = coord.nextToken();
			String v = (coord.hasMoreTokens()) ? coord.nextToken() : null;
			loc.setCoordinateSystem(new CoordinateSystem(n,v));

			if (parts.hasMoreTokens()) {
			  String seqRegionName = parts.nextToken();
			  // handle special case where seqRegion is null,
			  // this occurs when the location is a gap.
			  if ("unset".equals(seqRegionName)) {
			    loc.setGap(true);
			    seqRegionName = null;
			  }
				loc.setSeqRegionName(seqRegionName);
			}
			if (parts.hasMoreTokens()) {
				StringTokenizer startEnd = new StringTokenizer(parts
						.nextToken(), "-", true);
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

		}
	}

	/**
	 * @param cs
	 *            The CoordinateSystem that this location is expressed in.
	 * @param start
	 *            Start position, >=0.
	 * @param end
	 *            End position, end>start.
	 * @param strand
	 *            Strand -1,0 (unstranded) or +1.
	 * @param gap whether the location is a gap.
	 * @throws InvalidLocationException
	 *             if location invalid.
	 */
	public Location(CoordinateSystem cs, String seqRegionName, int start,
			int end, int strand, boolean gap) throws InvalidLocationException {
		if (cs == null)
			throw new InvalidLocationException(
					"CoordinateSystem must not be null");
		// end!=start-1 allows for variation insertions
		if (end != 0 && end < start && end != start - 1)
			throw new InvalidLocationException(
					"end<start but should be end>start");
		if (strand < -1 || strand > 1)
			throw new InvalidLocationException("Strand should be -1,0,+1, not "
					+ strand);

		this.cs = cs;
		this.gap = gap;
		setSeqRegionName(seqRegionName);
		setStart(start);
		setEnd(end);
		setStrand(strand);

	}

	public Location(CoordinateSystem cs, String seqRegionName, int start,
			int end, int strand) throws InvalidLocationException {
		this(cs, seqRegionName, start, end, strand, false);
	}

	
	/**
	 * Create a location as a range on a sequence region defined by it's
	 * internalID.
	 * 
	 * The method LocationConverter.completeSeqRegionNameAndCoordinateSystem(...)
	 * can 'fill in' the seq region name and coordinate system using this
	 * information. The use of that method and this class allows for certain
	 * optimisations. 
	 * 
	 * @param seqRegionInternalID internal ID of sequence region location is on.
	 * @param start start position on the sequence region. start<=end.
	 * @param end end position on the sequence region. end>=start.
	 * @param strand strand on sequence region. -1,0,+1
	 * @throws InvalidLocationException
	 * @see org.ensembl.driver.LocationConverter#assignSeqRegionNameAndCoordinateSystem(Location)
	 */
	public Location(long seqRegionInternalID, int start,
			int end, int strand) throws InvalidLocationException {
		
		if (end != 0 && end < start && end != start - 1)
			throw new InvalidLocationException(
					"end<start but should be end>start");
		if (strand < -1 || strand > 1)
			throw new InvalidLocationException("Strand should be -1,0,+1, not "
					+ strand);
		this.segRegionID = seqRegionInternalID;
		setStart(start);
		setEnd(end);
		setStrand(strand);		
	}
	
	/**
	 * Location representing the whole coordinate system.
	 * @param cs
	 *            The CoordinateSystem that this location is expressed in.
	 * @throws InvalidLocationException
	 *             if location invalid.
	 */
	public Location(CoordinateSystem cs) throws InvalidLocationException {

		this(cs, null, 0, 0, 0, false);
	}

	/**
	 * The whole of the sequence region in the default version of the specified coordinate system.
	 * @param cs
	 *            The CoordinateSystem that this location is expressed in.
	 * @param seqRegionName
	 *            The name of the sequence region.
	 * @throws InvalidLocationException
	 *             if location invalid.
	 */
	public Location(CoordinateSystem cs, String seqRegionName)
			throws InvalidLocationException {
		this(cs, seqRegionName, 0, 0, 0, false);
	}

	/**
	 * The whole of the sequence region in the default version of the specified coordinate system.
	 * @param coordinateSystemName
	 *            The name of the CoordinateSystem this location is expressed in.
	 * @param seqRegionName
	 *            The name of the sequence region.
	 * @throws InvalidLocationException
	 *             if location invalid.
	 */
	public Location(String coordinateSystemName, String seqRegionName) throws InvalidLocationException {
		this(new CoordinateSystem(coordinateSystemName), seqRegionName, 0, 0, 0, false);
	}
	
	/**
	 * @param cs
	 *            The CoordinateSystem that this location is expressed in.
	 * @param seqRegion
	 *            The sequence region.
	 * @throws InvalidLocationException
	 *             if location invalid.
	 */
	public Location(CoordinateSystem cs, SequenceRegion seqRegion)
			throws InvalidLocationException {
		this(cs, seqRegion.getName(), 0, 0, 0, false);
	}

	/**
	 * Constructs a location with map=CloneFragmentLocation.DEFAULT_CS, start=1,
	 * end=length+1, strand=unset and whether it is a gap.
	 * 
	 * @param cs
	 *            The CoordinateSystem that this location is expressed in.
	 * @param gap
	 *            Whether this Location is a gap.
	 * @param length
	 *            The length of the location.
	 * @throws InvalidLocationException
	 *             if location invalid.
	 */
	public Location(CoordinateSystem cs, boolean gap, int length)
			throws InvalidLocationException {

		this(cs, null, 1, length + 1, -1, gap);

	}

	/**
	 * Create a list location by appending copies of the items in _locations_.
	 * 
	 * @param locations
	 *            list of 1 or more locations
	 */
	public Location(Location[] locations) throws IllegalArgumentException {

		if (locations == null || locations.length == 0)
			throw new IllegalArgumentException(
					"locations should be an array of 1 or more locations.");

		Location first = locations[0];
		this.cs = first.cs;
		this.end = first.end;
		this.gap = first.gap;
		this.seqRegionName = first.seqRegionName;
		this.sequenceRegion = first.sequenceRegion;
		this.start = first.start;
		this.strand = first.strand;
		Location n = first.next;
		if (n != null)
			this.next = n.copy();

		// implementation doesn't use append() because that would be
		// inefficient.
		Location cur = last();
		for (int i = 1; i < locations.length; i++) {
			Location tmp = locations[i].copy();
			cur.last().next = tmp;
			cur = tmp.last();
		}
	}

	/**
	 * The location is complete if it species a region unambiguously.
	 * 
	 * The region is specified unambiguously if, for all nodes, start>=1,
	 * end>=1, seqName!=null and the coordinate system is complete.
	 * 
	 * @return true if the location is complete, otherwise false.
	 */
	public boolean isComplete() {
		boolean complete = true;
		for (Location node = this; complete && node != null; node = node.next)
			if (node.seqRegionName == null || node.segRegionID<1
					|| node.start < 1 || node.end < 1
					|| !node.cs.isComplete())
				complete = false;
		return complete;
	}

	/**
	 * End of this location node on the region.
	 * 
	 * @return end position.
	 */
	public int getEnd() {
		return end;
	}

	/**
	 * Formats end by inserting commas every 3 decimal places. Can make large
	 * numbers easier to read. e.g. 1003298 -> 1,003,298.
	 * 
	 * @return end with commas every 3 decimal places.
	 */
	public String getEndAsFormattedString() {
		return format(end);
	}

	/**
	 * Sets the end attribute. Can only be called when seqRegionName is not
	 * null.
	 * 
	 * @param end
	 *            end position. end>start. end>=0. Where 0 means unset.
	 * @throws InvalidLocationException
	 *             if end>start.
	 * @throws IllegalStateException
	 *             if seqRegionName is null.
	 */
	public final void setEnd(int end) {

		if (!gap && seqRegionName == null && segRegionID<1 && end != 0)
			throw new IllegalStateException(
					"Can not set end unless sequence region specified or gap.");
		if (end < 0)
			throw new InvalidLocationException("End should be >=0.");
		//  end!=start-1 allows for variation insertions
		if (end != 0 && end < start && end != start - 1)
			throw new InvalidLocationException(
					"end<start but should be end>start");
		this.end = end;

	}

	public boolean isEndSet() {
		return end != 0;
	}

	public int getStrand() {
		return strand;
	}

	/**
	 * Sets the strand. Can only be called when seqRegionName is not null.
	 * 
	 * @param strand
	 *            -1,0 (unstranded) or +1
	 * @throws InvalidLocationException
	 *             if strand invalid.
	 * @throws IllegalStateException
	 *             if seqRegionName is null.
	 */
	public void setStrand(int strand) {

		if (!gap && seqRegionName == null && segRegionID<1 && strand != 0)
			throw new IllegalStateException(
					"Can not set strand unless sequence region specified.");
		if (strand < -1 || strand > 1)
			throw new InvalidLocationException("Strand should be -1,0,+1, not "
					+ strand);
		this.strand = strand;

	}

	/**
	 * @return true if strand is +1 or -1, false if strand is 0.
	 */
	public boolean isStrandSet() {
		return strand != 0;
	}

	/**
	 * Start of this location node on the region.
	 * 
	 * @return start position.
	 */

	public int getStart() {
		return start;
	}

	/**
	 * Formats start by inserting commas every 3 decimal places. Can make large
	 * numbers easier to read. e.g. 1003298 -> 1,003,298.
	 * 
	 * @return start with commas every 3 decimal places.
	 */
	public String getStartAsFormattedString() {
		return format(start);
	}

	/**
	 * Sets the start. Can only be called when seqRegionName is not null.
	 * 
	 * @param start
	 *            Start position, >=0. Where 0 means unset.
	 * @throws InvalidLocationException
	 *             if start <0.
	 */
	public final void setStart(int start) {
		if (!gap && seqRegionName == null && segRegionID <1 && start != 0)
			throw new IllegalStateException(
					"Can not set start unless sequence region specified or gap.");
		if (start < 0)
			throw new InvalidLocationException("Start should be >=0.");
		this.start = start;

	}

	public boolean isStartSet() {
		return start != 0;
	}

	public CoordinateSystem getCoordinateSystem() {
		return cs;
	}

	public void setCoordinateSystem(CoordinateSystem cs) {
		if (cs == null && (start != 0 || end != 0 || strand != 0))
			throw new IllegalStateException(
					"Can not set coordinate system to strand while start, end or strand are set. Unset them first.");
		this.cs = cs;
	}

	public boolean isCoordinateSystemSet() {
		return cs != null;
	}

	/**
	 * Length is calculated by end - start + 1. If start is not set length
	 * equals end (end = end - 1 + 1).
	 * 
	 * @return length if end is set, otherwise -1.
	 * @see #getNodeLengthAsFormattedString()
	 */
	public int getNodeLength() {

		if (!isEndSet() || !isStartSet()) {
			return -1;
		} else {
			return end - start + 1;
		}
	}

	/**
	 * Formats nodeLength by inserting commas every 3 decimal places. Can make
	 * large numbers easier to read. e.g. 1003298 -> 1,003,298.
	 * 
	 * @return nodeLength with commas every 3 decimal places.
	 * @see #getNodeLength()
	 */
	public String getNodeLengthAsFormattedString() {
		return format(getNodeLength());
	}

	/**
	 * Transforms this location into another by changing the start and end
	 * positions. The new location will have start and end >=1;
	 * 
	 * <pre>
	 * 
	 *  
	 *   
	 *     if strand==0 or strand==+1: start += startDiff end += endDiff elseif strand==-1 start -= endDiff end -= startDiff
	 *    
	 *   
	 *  
	 * </pre>
	 * 
	 * 
	 * <p>
	 * <i>Example- expand location: </i> Add 1000 bases to the upstream side of
	 * the location, and none to the downstream side.
	 * <code>loc.transform( -1000, 0);</code>
	 * </p>
	 * 
	 * <p>
	 * <i>Example- flanking location: </i> Represent the 1000 bases downstream
	 * of the location. <code>loc.transform( loc.getLength(), 1000+1);</code>
	 * </p>
	 * 
	 * <p>
	 * <i>Example- crop location: </i> Represent the location minus the first
	 * 100 and last 50 bases. <code>loc.transform( 100, -50);</code>
	 * </p>*
	 * 
	 * @param startDiff
	 *            difference to make to start position.
	 * @param endDiff
	 *            difference to make to end position.
	 * @return a new location representing a transformed version of this
	 *         location.
	 * @throws InvalidLocationException
	 *             if start>end
	 */

	public Location transform(int startDiff, int endDiff)
			throws InvalidLocationException {

		//System.out.println("Before: " + this);

		// head of the location list to return.
		Location head = null;

		// Start and end (base) positions in the location list.
		final int startPoint = startDiff;
		final int endPoint = getLength() + endDiff;

		// book keeping variables so we know what to do with each node in the
		// list.
		boolean last = false;
		boolean first = false;
		boolean relevant = false;
		int total = 0;
		int prevTotal = 0;
		Location prevDerived = null;
		Location derived = null;

		// Loop over nodes in the original list. Make copies of the relevant
		// ones
		// and resize where necessary.
		for (Location original = this; original != null && !last; original = original
				.next()) {

			total += original.getNodeLength();

			if (!relevant && total > startPoint) {
				first = true;
				relevant = true;
			}
			if (total >= endPoint || (relevant && original.next() == null)) // last
				// element
				// in
				// list,
				// might
				// need to stretch it.
				last = true;

			final int cropStart = startPoint - prevTotal;
			final int cropEnd = endPoint - total;

			if (first && last) {
				// crop/stretch first and last nodes
				//System.out.println("crop both: " + cropStart+ " , " +
				// cropEnd);
				derived = original.transformNode(cropStart, cropEnd);
			} else if (first) {
				// crop/stretch first node
				//System.out.println("(first) crop start: " + cropStart);
				derived = original.transformNode(cropStart, 0);
			} else if (last) {
				// crop/stretch last node
				//System.out.println("(first) crop end: " + cropEnd);
				derived = original.transformNode(0, cropEnd);
			} else if (relevant) {
				// copy 'unchanged' location from middle of list

				derived = original.copy();

			}

			// remove end of list if present.
			if (last)
				derived.setNext(null);

			// Make derived be the next element of the previous derived node.
			if (prevDerived != null)
				prevDerived.setNext(derived);

			// remember the head of the location list so we can return it.
			if (first)
				head = derived;

			first = false; // only one first location
			prevTotal = total;
			prevDerived = derived;
		}

		// new location must lie beyond one end of this location. e.g. a
		// flanking region
		if (head == null) {

			Location headNode = this;
			Location lastNode = last();

			int newStart, newEnd;
			if (strand == -1) {
				newStart = lastNode.getStart() - endDiff;
				;
				newEnd = headNode.getEnd() - startDiff;
			} else {
				newStart = headNode.getStart() + startDiff;
				newEnd = lastNode.getEnd() + endDiff;
			}

			head = cloneNode();
			head.setStart(newStart);
			head.setEnd(newEnd);

		}
		//System.out.println("after: " + head);

		return head;
	}

	/**
	 * Resizes location node taking into account the strand and ignoring rest of
	 * location list if present. The new location will have start and end >=1;
	 * 
	 * Only use this method if you want operate on a single node, most use cases
	 * should use transform(int, int).
	 * 
	 * @param startDiff
	 *            number of bases by which to change the start.
	 * @param endDiff
	 *            number of bases which to change the end of the location.
	 * 
	 * @return a new location representing a transformed version of this
	 *         location.
	 * @throws InvalidLocationException
	 *             if changes start>end
	 */

	public Location transformNode(int startDiff, int endDiff) {

		Location copy = (Location) this.copy();

		//System.out.println("b4 resizeNode" + copy);

		if (copy.strand == -1) {
			copy.start -= endDiff;
			copy.end -= startDiff;
		} else {
			copy.start += startDiff;
			copy.end += endDiff;
		}

		if (copy.start < 1) {
			logger.fine("Location.start was invalid: start=" + copy.start
					+ " <1" + " (startDiff=" + startDiff + "endDiff=" + endDiff
					+ ")");
			copy.start = 1;

		}
		if (copy.end < 1) {
			logger.fine("Location.end was invalid: end=" + copy.end + " <1"
					+ " (startDiff=" + startDiff + "endDiff=" + endDiff + ")");
			copy.end = 1;

		}

		if (copy.start > copy.end)
			throw new InvalidLocationException("Location now invalid: start="
					+ copy.start + " > end=" + copy.end + " (startDiff="
					+ startDiff + "endDiff=" + endDiff + ")");

		//System.out.println("after resizeNode" + copy);

		return copy;
	}

	/**
	 * "Complements" strand. Converts +1 to -1 and -1 to +1. Does copy and flip
	 * order of pieces if location is unstranded (strand is 0).
	 * 
	 * @return new location that is the complement of this one
	 */
	public Location complement() {

		if (next == null) {

			Location newLoc = cloneNode();
			newLoc.strand = strand * -1;
			return newLoc;

		} else {

			Location curr = this.copy(); // copy of list
			Location prev = null;
			Location nxt = curr.next();

			// move through list changing next values.
			do {
				curr.setNext(prev);
				prev = curr;
				curr = nxt;
				nxt = nxt.next();
			} while (nxt != null);
			curr.setNext(prev);

			return curr;

		}
	}

	/**
	 * Converts all nodes into a string.
	 * 
	 * @return string representation of the location including all nodes.
	 * @see #toString(boolean,boolean)  
	 * @see #toString(boolean,boolean,boolean)
	 */
	public String toString() {
		return toString(true, false);
	}

	/**
	 * Converts this node into a string. If _all_ is true then all nodes in the
	 * linked list are appended to the string.
	 * 
	 * @param all
	 *            whether to include the nodes other than just this one in the
	 *            string
	 * @return string representation of the location.
	 */
	public String toString(boolean all, boolean extraInformation) {
		return toString(all, extraInformation, false);
	}

	/**
	 * Converts this node into a string. If _all_ is true then all nodes in the
	 * linked list are appended to the string.
	 * 
	 * @param all
	 *            whether to include the nodes other than just this one in the
	 *            string
	 * @return string representation of the location.
	 */
	public String toString(boolean all, boolean extraInformation,
			boolean formatBigNumbers) {
		StringBuffer buf = new StringBuffer();
		Location loc = this;
		while (loc != null) {
			loc.nodeToString(buf, extraInformation, formatBigNumbers);
			loc = loc.next;
			if (loc != null)
				buf.append("->");
		}
		return buf.toString();
	}

	private void nodeToString(StringBuffer buf, boolean extraInformation,
			boolean formatBigNumber) {

		if (cs!=null) {
			buf.append(cs.getName());
			if (cs.getVersion() != null)
				buf.append("_").append(cs.getVersion());
		
		} else {
			buf.append("unset");
		}
		buf.append(":");
		
		buf.append(StringUtil.stringOrUnset(seqRegionName)).append(":");
		if (isStartSet())
			if (formatBigNumber)
				buf.append(format(start));
			else
				buf.append(start);

		buf.append("-");

		if (isEndSet())
			if (formatBigNumber)
				buf.append(format(end));
			else
				buf.append(end);

		if (isStrandSet())
			buf.append(":").append(strand);

		if (extraInformation) {

			buf.append("[");

			buf.append("nodeLength=");
			int nl = getNodeLength();
			if (nl == -1)
				buf.append("unkown");
			else
				buf.append(formatBigNumber ? format(nl) : Integer.toString(nl));

			int l = getLength();
			buf.append(", length=");
			if (l == -1)
				buf.append("unkown");
			else
				buf.append(formatBigNumber ? format(l) : Integer.toString(l));

			buf.append(", seqRegionID=").append( segRegionID>0 ? Long.toString(segRegionID) : "unset");
			
			
			buf.append(", gap=").append(isGap());

			buf.append("]");
		}
	}

	private Location cloneNode() {

		try {
			return (Location) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("This shouldn't happen.", e);
		}
	}

	/**
	 * Performs a deep copy. 
	 * 
	 * It is easier to use copy() which prevents the need
	 * for a caste to Location in many cases.
	 * 
	 * @return deep copy of this instance.
	 * @see #copy()
	 */
	public Object clone() {
		// Implement this method otherwise the default clone() clone
		// implementation will return a shallow copy
		// of this instance. This could result in hard to debug problems.
		return copy();
	}

	/**
	 * Performs a deep copy.
	 * 
	 * @return deep copy of this instance.
	 */
	public Location copy() {
		Location head = cloneNode();

		Location prev = head;
		for (Location x = next; x != null; x = x.next) {
			Location last = x.cloneNode();
			prev.next = last;
			prev = last;
		}

		return head;
	}

	/**
	 * Performs a deep of the head node.
	 * 
	 * The next pointer is set to null in the returned
	 * node, i.e. <code>loc.copyNode().next()==null</code>.
	 *  
	 * @return deep copy of this node instance.
	 */
	public Location copyNode() {
	  Location node = cloneNode();
	  node.next = null;
	  return node;
	}
	
	/**
	 * Creates a new 1 base pair location relative to this location where
	 * newLocation.start = newLocation.end = this.start + offset.
	 * 
	 * @param offset
	 *            number of bases from the start current start
	 * @return new location at specified position
	 * @throws InvalidLocationException
	 *             if start not set or offset is invalid
	 */
	public Location relative(int offset) throws InvalidLocationException {
		return relative(offset, 1);
	}

	/**
	 * Creates a new location relative to this location where
	 * start=this.start+offset, and end=this.start+length-1.
	 * 
	 * @param offset
	 *            number of bases from the start of this location, any int is
	 *            valid
	 * @param length
	 *            length of the new location, >=1
	 * @return new location at specified position
	 * @throws InvalidLocationException
	 *             if start not set, length is <1 or the new location is invalid
	 */

	public Location relative(int offset, int length)
			throws InvalidLocationException {

		if (!isStartSet())
			throw new InvalidLocationException("Start not set.");
		if (length < 1)
			throw new InvalidLocationException("length should be >=1:" + length);

		return transform(offset, (offset + length) - getLength());
	}

	/**
	 * Convenience method to find the difference between to locations. Works on
	 * first node in list.
	 * 
	 * @return otherLocation.start - location.start
	 */
	public int diff(Location otherLocation) {
		return otherLocation.getStart() - start;
	}

	/**
	 * Sum of this nodeLength and any other ones after it in location list.
	 * 
	 * @return total length of this location (list) or -1 if this node or one of
	 *         it's subsequent locations has nodeLength=-1.
	 * @see #getLengthAsFormattedString()
	 */
	public int getLength() {

		int totalLength = 0;

		for (Location loc = this; totalLength != -1 && loc != null; loc = loc
				.next()) {
			int nodeLength = loc.getNodeLength();
			if (nodeLength == -1)
				totalLength = -1;
			else
				totalLength += nodeLength;
		}

		return totalLength;
	}

	/**
	 * Formats length by inserting commas every 3 decimal places. Can make large
	 * numbers easier to read. e.g. 1003298 -> 1,003,298.
	 * 
	 * @return length with commas every 3 decimal places.
	 * @see #getLength()
	 */
	public String getLengthAsFormattedString() {
		return format(getLength());
	}

	/**
	 * Last node in location list. If only one element it returns this one.
	 * 
	 * @return last node in list.
	 */
	public Location last() {
		Location node = this;
		while (node.hasNext()) {
			node = node.next();
		}

		return node;
	}

	/**
	 * Return whether this node is a gap.
	 * @return true if this node is a gap.
	 */
	public boolean isGap() {
		return gap;
	}

	public final void setGap(boolean gap) {
		this.gap = gap;
	}

	/**
	 * Returns length of gap.
	 * 
	 * @deprecated Since ensj28.3. Use getLength() instead.
	 * @return length;
	 * @see #getLength()
	 */
	public int getGap() {
		return getLength();
	}

	/**
	 * Returns number of elements in location list.
	 */
	public int size() {
		int size = 0;
		for (Location loc = this; loc != null; loc = loc.next())
			size++;
		return size;
	}

	/**
	 * @return next node in list.
	 */

	public Location next() {
		return next;
	}

	public final void setNext(Location next) {
		this.next = next;
	}

	/**
	 * @return true if next!=null.
	 */
	public boolean hasNext() {
		return next != null;
	}

	public Location append(Location location) {
		last().next = location;
		return this;
	}

	/**
	 * Checks if locations overlap each other.
	 * 
	 * @param other
	 *            other location
	 * @param requireStrandsOverlap
	 *            whether the strands should overlap.
	 * @return true if locations overlap.
	 */
	public boolean overlaps(Location other, boolean requireStrandsOverlap) {

		for (Location x = this; x != null; x = x.next()) {

			for (Location y = other; y != null; y = y.next()) {
				
				if (
						(x.segRegionID>0 && x.segRegionID==y.segRegionID
								|| ((x.seqRegionName == null 
										|| y.seqRegionName == null 
										|| x.seqRegionName.equals(y.seqRegionName) )
										&& x.cs.equals(y.cs)))
						
						&& (!requireStrandsOverlap || x.strand == y.strand
								|| x.strand == 0 || y.strand == 0)
						
						&& (x.start <= y.end || !y.isEndSet() || !x
								.isStartSet())
						
						&& (x.end >= y.start || !x.isEndSet() || !y
								.isStartSet())) {
					return true;
				}
			}

		}

		return false;

	}

	/**
	 * Calls <code>overlaps(location,false)</code>.
	 * 
	 * @param other
	 *            other location
	 * @return whether this location overlaps with the other location
	 * @see #overlaps(Location,boolean)
	 */
	public boolean overlaps(Location other) {
		return overlaps(other, false);
	}

	/**
	 * Finds the number of bases this location shares with the other location.
	 * 
	 * <p>
	 * <b>Possible problem: </b> The coordinate system name AND version need to
	 * be the same for the locations to overlap. If either of these is unset
	 * then the the locations are not considered to overlap. For example, even
	 * if the gene with internal ID = 111 IS inside the first 1m bases of
	 * chromosome 1 then this use of overlapSize() will return 0 which is
	 * probably not what is expected.
	 * 
	 * <pre>
	 * <code>
	 * 
	 *  
	 *   
	 *    Location loc = new Location(&quot;chromosome:1:1-1m&quot;)
	 *    // get the location for a gene
	 *    Location loc2 = geneAdaptor.fetch(111).getLocation()
	 *    int overlap = loc.overlapSize(loc2);
	 *    
	 *   
	 *  
	 * </code>
	 * </pre>
	 * 
	 * and then call loc.overlaps(loc2) where
	 * 
	 * This is because the gene.location.coordinateSystem.version is set by the
	 * geneAdaptor but loc.coordinateSystem.version is null. To ensure that the
	 * code works as intended <code>loc</code> should be <i>complete </i>.
	 * i.e. all fields should be set. This can be achieved like this:
	 * 
	 * <pre>
	 * <code>
	 * 
	 *  
	 *   
	 *    Location loc = locationConverter.fetchComplete(new Location(&quot;chromosome:1:1-1m&quot;));
	 *    // get the location for a gene
	 *    Location loc2 = geneAdaptor.fetch(111).getLocation()
	 *    int overlap = loc.overlapSize(loc2);
	 *    
	 *   
	 *  
	 * </code>
	 * </pre>
	 * 
	 * and then call loc.overlaps(loc2) where
	 * 
	 * </p>
	 * 
	 * @param other
	 *            other location.
	 * @param requireStrandsOverlap
	 *            whether the strands should overlap.
	 * @return number of bases this location shares with other. Between 0 and
	 *         getLength().
	 * @throws IllegalArgumentException
	 *             if "start" or "end" is not set on any of the location nodes
	 *             in either this instance or other.
	 */
	public int overlapSize(Location other, boolean requireStrandsOverlap) {
		int total = 0;

		for (Location x = this; x != null; x = x.next()) {

			if (x.start < 1 || x.end < 1)
				throw new IllegalArgumentException(
						"Can't calculate overlap for location where location start or end is missing: "
								+ this);

			for (Location y = other; y != null; y = y.next()) {

				if (y.start < 1 || y.end < 1)
					throw new IllegalArgumentException(
							"Can't calculate overlap for location where location start or end is missing: "
									+ other);

				//      TODO use requireStrandsOverlap

				if ((x.cs == y.cs || x.cs != null && x.cs.equals(y.cs))
						&& (x.seqRegionName == y.seqRegionName || (x.seqRegionName != null && x.seqRegionName
								.equals(y.seqRegionName)))
						&& (!requireStrandsOverlap || x.strand == y.strand
								|| x.strand == 0 || y.strand == 0)
						&& x.start <= y.end && x.end >= y.start) {
					total += Math.min(x.end, y.end)
							- Math.max(x.start, y.start) + 1;
				}
			}

		}

		return total;

	}

	/**
	 * Calls <code>overlapSize(location,true)</code>.
	 * 
	 * @param other
	 *            other location
	 * @return number of bases the locations overlap by
	 * @see #overlapSize(Location,boolean)
	 */
	public int overlapSize(Location other) {
		return overlapSize(other, false);
	}

	/**
	 * Iterate through this location and the other one comparing them. The order
	 * of the attributes used for the ordering is: coordinateSystem,
	 * seqRegionName, start, end, strand.
	 * 
	 * @return -1 if this location is before other, 0 if it is equivalent, 1 if
	 *         it is after it.
	 */
	public int compareTo(Object other) {

		if (other == null)
			return 1;

		Location nxt = this;
		Location nxt2 = (Location) other;

		while (nxt != null) {

			final CoordinateSystem nxtcs = nxt.cs;
			final CoordinateSystem nxt2cs = nxt2.cs;
			// nxt.cs==nxt2.cs is a fast way to check for same coord systems
			int tmp = (nxtcs == nxt2cs) ? 0 : nxtcs.compareTo(nxt2cs);
			if (tmp != 0)
				return tmp;

			final String nxtSeqRegionName = nxt.seqRegionName;
			final String nxt2SeqRegionName = nxt2.seqRegionName;
			tmp = (nxtSeqRegionName == nxt2SeqRegionName) ? 0 : StringUtil
					.compare(nxtSeqRegionName, nxt2SeqRegionName);
			if (tmp != 0)
				return tmp;

			final int nxtStart = nxt.start;
			final int nxt2Start = nxt2.start;
			if (nxtStart > nxt2Start)
				return 1;
			if (nxtStart < nxt2Start)
				return -1;

			final int nxtEnd = nxt.end;
			final int nxt2End = nxt2.end;
			if (nxtEnd > nxt2End)
				return 1;
			if (nxtEnd < nxt2End)
				return -1;

			final int nxtStrand = nxt.strand;
			final int nxt2Strand = nxt2.strand;
			if (nxtStrand > nxt2Strand)
				return 1;
			if (nxtStrand < nxt2Strand)
				return -1;

			nxt = nxt.next();
			nxt2 = nxt2.next();

			if (nxt == null && nxt2 != null)
				return -1;
			if (nxt2 == null && nxt != null)
				return 1;
		}

		return 0;
	}

	/**
	 * @return sequence region name.
	 */
	public String getSeqRegionName() {
		return seqRegionName;
	}

	public boolean isSeqRegionNameSet() {
		return seqRegionName != null;
	}

	/**
	 * @param seqRegionName
	 * @throws IllegalStateException
	 *             if coordinate system is null.
	 */
	public void setSeqRegionName(String seqRegionName) {
		if (cs == null)
			throw new IllegalStateException(
					"Can not set seqRegionName when coordinateSystem is null.");

		this.seqRegionName = (seqRegionName == null) ? null : seqRegionName
				.intern();
	}

	/**
	 * @return sequenceRegion if set, otherwise null.
	 */
	public SequenceRegion getSequenceRegion() {
		return sequenceRegion;
	}

	/**
	 * @param sequenceRegion
	 *            sequence region for this location. null if you want to remove
	 *            a previously set sequence region.
	 */
	public void setSequenceRegion(SequenceRegion sequenceRegion) {
		this.sequenceRegion = sequenceRegion;
	}
	
	/**
	 * Set the sequence region internal ID this location node is on.
	 * @return set the sequence region internal ID. Set it to -1 to 'unset' it.
	 */
	public void setSegRegionID(long sequenceRegionInternalID) {
		this.segRegionID = sequenceRegionInternalID;
	}
	
	/**
	 * Return the sequence region internal ID this location node is on.
	 * @return sequence region internal ID, or -1 if not set.
	 */
	public long getSegRegionID() {
		return segRegionID;
	}
	
	private synchronized String format(int n) {
		if (numberFormatter == null)
			numberFormatter = NumberFormat.getIntegerInstance();
		return numberFormatter.format(n);
	}

	/**
	 * Merges nodes in this location that are adjacent on a sequence.
	 * 
	 * Does nothing if this is a single node location or the nodes are not
	 * adjacent.
	 * 
	 * Two nodes are merged when node.end+1 == node.next().start. In this case
	 * node.setEnd(node.next().end) and node.setNext(node.next()).
	 * 
	 * @return this node.
	 */
	public Location mergeAdjacentNodes() {
		Location node = this;
		while (node != null) {
			Location x = node.next;
			if (x != null && node.end + 1 == x.start && x.end > 0
					&& node.seqRegionName.equals(x.seqRegionName)
					&& node.cs.equals(x.cs)) {
				node.end = x.end;
				node.next = x.next;
			} else {
				node = node.next;
			}
		}
		return this;
	}

  /**
   * Number of nodes in list.
   * @return number of nodes in list, 1 if node.next is null.
   */
  public int getNodeCount() {
    int count = 1;
    for(Location node = this; node.next!=null; node=node.next)
      count++;
    return count;
  }
  
  /**
   * Returns whether this location contains a gap node.
   * 
   * Returns true if at least one node in the location is a gap.
   * 
   * @return true if this location contains a gap node, otherwise false.
   * @see #isGap()
   */
  public boolean containsGapNodes() {
    for(Location node = this; node!=null; node=node.next)
      if (node.gap) return true;
    return false;
  }
  
  /**
   * Returns a location derived by removing all gap nodes from this location, null
   * if only gaps present.
   * 
   * If this location contains no gaps then this location is returned unmodified. 
   * 
   * If the location consists only of gaps then null is returned.
   * 
   * If the location contains at least one gap and at least one none gap then a 
   * new node is returned.
   * 
   * @return location derived by removing all gaps from this location, null
   * if only gaps present and this node if it contains no gaps.
   * @see #isGap()
   */
  public Location removeGapNodes() {
    
    // optimisation: skip processing below if this is a one node list.
    if (next==null)
      if (gap) return null;
      else return this;
    
    // optimisation: most locations do not contain gaps and we don't
    // want to waste resources creating copies of nodes in these cases. 
    if (!containsGapNodes()) return this;
    
    Location head = null;
    Location tail = null;
    
    for(Location node = this; node!=null; node=node.next)       
      if (!node.gap) 
        if (head==null) 
          head = tail = node.copyNode();
        else 
          tail.next = node.copyNode();
          
    return head;
  }
}
