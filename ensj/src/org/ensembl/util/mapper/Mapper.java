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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * This class converts coordinates from one reference system to another.
 * It uses pairs of aligned ungapped coordinates.
 */

public class Mapper {
	private String fromTag, toTag;
	private HashMap fromMap, toMap;
	private int size;

	public static void main(String args[]) throws Exception {
	}

	public Mapper(String fromTag, String toTag) {
		this.fromTag = fromTag;
		this.toTag = toTag;
		fromMap = new HashMap();
		toMap = new HashMap();
		size = 0;
	}

	public void flush() {
		size = 0;
		fromMap.clear();
		toMap.clear();
	}
	
	public Coordinate[] mapCoordinate(
		String id,
		int start,
		int end,
		int strand,
		String tag)
		throws IllegalArgumentException {

		HashMap map;
		LinkedList ll;
		ListIterator li;
		Coordinate[] resultCoords;
		Coordinate coord;
		Pair pair;

		if (tag.equals(fromTag)) {
			map = fromMap;
		} else if (tag.equals(toTag)) {
			map = toMap;
		} else {
			throw (
				new IllegalArgumentException(
					"Unknown tag value " + tag + "\nUse " + fromTag + " or " + toTag));
		}

		if (!map.containsKey(id)) {
			coord = new Coordinate(start, end);
			resultCoords = new Coordinate[1];
			resultCoords[0] = coord;
			return resultCoords;
		}

		ll = (LinkedList) map.get(id);
		li = ll.listIterator();

		int selfStart, selfEnd, targetStart, targetEnd, targetStrand, lastSelfEnd;
		String targetId;
		LinkedList resultList = new LinkedList();
		lastSelfEnd = 0;

		while (li.hasNext()) {
			pair = (Pair) li.next();
			if (map == fromMap) {
				selfStart = pair.fromStart;
				selfEnd = pair.fromEnd;
				targetStart = pair.toStart;
				targetEnd = pair.toEnd;
				targetId = pair.toId;
			} else {
				selfStart = pair.toStart;
				selfEnd = pair.toEnd;
				targetStart = pair.fromStart;
				targetEnd = pair.fromEnd;
				targetId = pair.fromId;
			}

			if (selfEnd < start) {
				continue;
			}
			if (selfStart > end) {
				break;
			}
			if (start < selfStart) {
				// gap detected
				coord = new Coordinate(start, selfStart - 1);
				resultList.add(coord);
				start = selfStart;
			}

			int coordStart, coordEnd, coordOri;
			// java wants it initialized ...
			coordStart = 0;
			coordEnd = 0;
			coordOri = 0;

			if (pair.ori == 1) {
				coordStart = targetStart + start - selfStart;
			} else {
				coordEnd = targetEnd - start + selfStart;
			}

			// either we are enveloping this map or not. If yes, then end
			// point (self perspective) is determined solely by target. If not
			// we need to adjust

			if (end > selfEnd) {
				// enveloped
				if (pair.ori == 1) {
					coordEnd = targetEnd;
				} else {
					coordStart = targetStart;
				}
			} else {
				// need to adjust end 
				if (pair.ori == 1) {
					coordEnd = targetStart + end - selfStart;
				} else {
					coordStart = targetEnd - end + selfStart;
				}
			}

			coord = new Coordinate(targetId, coordStart, coordEnd, pair.ori * strand);
			resultList.add(coord);
			lastSelfEnd = selfEnd;
			start = selfEnd + 1;
		}

		// if list was empty or just didnt produce anything
		// we need to make big gap
		if (resultList.size() == 0) {
			coord = new Coordinate(start, end);
			resultList.add(coord);
		} else {

			// if we havnt treated everything we have to append a gap for the rest
			if (lastSelfEnd < end) {
				coord = new Coordinate(lastSelfEnd + 1, end);
				resultList.add(coord);
			}
		}

		// reverse the list for output on strand == -1
		if (strand == -1) {
			li = resultList.listIterator(resultList.size());
		} else {
			li = resultList.listIterator();
		}

		resultCoords = new Coordinate[resultList.size()];
		for (int i = 0; i < resultCoords.length; i++) {
			if (strand == -1) {
				coord = (Coordinate) li.previous();
			} else {
				coord = (Coordinate) li.next();
			}

			resultCoords[i] = coord;
		}

		return resultCoords;
	}

	public Coordinate fastmap(
		String id,
		int start,
		int end,
		int strand,
		String tag)
		throws IllegalArgumentException {

		Coordinate result;
		LinkedList ll;
		ListIterator li;
		Pair pair;

		result = null;

		if (tag.equals(fromTag)) {
			ll = (LinkedList) fromMap.get(id);
			li = ll.listIterator();
		} else if (tag.equals(toTag)) {
			ll = (LinkedList) toMap.get(id);
			li = ll.listIterator();
		} else {
			throw (new IllegalArgumentException("unknown tag value"));
		}

		int selfStart, selfEnd, targetStart, targetEnd, targetStrand, lastSelfEnd;
		String targetId;

		while (li.hasNext()) {
			pair = (Pair) li.next();
			if (tag.equals(fromTag)) {
				selfStart = pair.fromStart;
				selfEnd = pair.fromEnd;
				targetStart = pair.toStart;
				targetEnd = pair.toEnd;
				targetId = pair.toId;
			} else {
				selfStart = pair.toStart;
				selfEnd = pair.toEnd;
				targetStart = pair.fromStart;
				targetEnd = pair.fromEnd;
				targetId = pair.fromId;
			}

			if (start < selfStart || end > selfEnd) {
				continue;
			}
			if (selfStart > end) {
				break;
			}

			if (pair.ori == 1) {
				result =
					new Coordinate(
						targetId,
						targetStart + start - selfStart,
						targetStart + end - selfStart,
						strand);
				break;
			} else {
				result =
					new Coordinate(
						targetId,
						targetEnd - end + selfStart,
						targetEnd - start + selfStart,
						-strand);
				break;
			}
		}

		return result;
	}

	public boolean addMapCoordinates(
		String fromId,
		int fromStart,
		int fromEnd,
		int ori,
		String toId,
		int toStart,
		int toEnd) {
		Pair newPair =
			new Pair(fromId, fromStart, fromEnd, toId, toStart, toEnd, ori);

		HashMap map = toMap;
		LinkedList pairs;
		if (map.containsKey(toId)) {
			pairs = (LinkedList) map.get(toId);
			Pair currentPair, lastPair;
			ListIterator li;

			li = pairs.listIterator();
			boolean newPairInserted = false;

			while (li.hasNext()) {
				currentPair = (Pair) li.next();

				// possible merge directly after current element 
				if ((newPair.toStart - 1 == currentPair.toEnd)
					&& (newPair.ori == currentPair.ori)
					&& (newPair.fromId.equals(currentPair.fromId))) {

					if (newPair.ori == 1) {

						// check forward strand merge
						if (newPair.fromStart - 1 == currentPair.fromEnd) {
							// yes its a merge

							currentPair.toEnd = newPair.toEnd;
							currentPair.fromEnd = newPair.fromEnd;

							// is it a threesome ?

							if (li.hasNext()) {
								lastPair = currentPair;
								currentPair = (Pair) li.next();

								if ((currentPair.ori == lastPair.ori)
									&& (lastPair.toEnd + 1 == currentPair.toStart)
									&& (lastPair.fromId.equals(currentPair.fromId))) {

									// thats a three_some :-

									lastPair.toEnd = currentPair.toEnd;
									lastPair.fromEnd = currentPair.fromEnd;

									li.remove();
									size--;
									// remove currentpair from the from list
									pairs = (LinkedList) fromMap.get(fromId);
									pairs.remove(currentPair);
								}
							}
							return true;
						}
					} else {
						// check backward strand merge

						if (newPair.fromEnd + 1 == currentPair.fromStart) {

							// yes its a merge
							currentPair.toEnd = newPair.toEnd;
							currentPair.fromStart = newPair.fromStart;

							// possible merge with next element?
							if (li.hasNext()) {
								lastPair = currentPair;
								currentPair = (Pair) li.next();

								if ((currentPair.ori == lastPair.ori)
									&& (currentPair.toStart == lastPair.toEnd + 1)
									&& (currentPair.fromId.equals(lastPair.fromId))) {

									// thats a three_some :-
									lastPair.toEnd = currentPair.toEnd;
									lastPair.fromStart = currentPair.fromStart;

									li.remove();
									size--;
									// remove from the other list
									pairs = (LinkedList) fromMap.get(fromId);
									pairs.remove(currentPair);
								}
							}

							return true;
						}
					}
				}


				// check a merge with directly after,
				// now check a merge pair directly before $lr->[$i]
				// no threesome check necessary !
				if ((newPair.ori == currentPair.ori)
					&& (newPair.toEnd + 1 == currentPair.toStart)
					&& (newPair.fromId.equals(currentPair.fromId))) {

					if (newPair.ori == 1) {
						// check forward strand merge

						if (newPair.fromEnd + 1 == currentPair.fromStart) {
							// yes its a merge
							currentPair.toStart = newPair.toStart;
							currentPair.fromStart = newPair.fromStart;

							return true;
						}
					} else {
						// check backward strand merge
						if (newPair.fromStart - 1 == currentPair.fromEnd) {

							// yes its a merge
							currentPair.toStart = newPair.toStart;
							currentPair.fromStart = newPair.fromStart;

							return true;
						}
					}
				}

				if (newPair.toStart < currentPair.toStart) {
					// insert here
					li.previous();
					li.add(newPair);
					size++;
					newPairInserted = true;
					break;
				} else if (newPair.toStart == currentPair.toStart) {
					// reject duplicates
					return false;
				}

			} // end of insertion sort while loop

			if (!newPairInserted) {
				li.add(newPair);
			}
		} // end of the toId was known before 

		else {
			pairs = new LinkedList();
			pairs.add(newPair);
			map.put(toId, pairs);
			size++;
		}

		map = fromMap;

		if (map.containsKey(fromId)) {
			pairs = (LinkedList) map.get(fromId);
			Pair currentPair;
			ListIterator li;

			li = pairs.listIterator();
			while (li.hasNext()) {
				currentPair = (Pair) li.next();
				if (newPair.fromStart < currentPair.fromStart) {
					li.previous();
					li.add(newPair);
					return true;
				} else if (newPair.fromStart == currentPair.fromStart) {
					// duplicate which shouldnt appear here any more
					// should be caught by duplicate case in the previous loop
					return false;
				}
			}
			li.add(newPair);
			return true;
		} else {
			pairs = new LinkedList();
			pairs.add(newPair);
			map.put(fromId, pairs);
			return true;
		}

	}

	public Pair[] listPairs(String id, int start, int end, String tag)
		throws IllegalArgumentException {

		HashMap map;
		LinkedList result = new LinkedList();
		ListIterator li;
		LinkedList ll;
		Pair pair;

		if (tag.equals(fromTag)) {
			map = fromMap;
		} else if (tag.equals(toTag)) {
			map = toMap;
		} else {
			throw (new IllegalArgumentException("Unknown coordinate system tag"));
		}

		if (!map.containsKey(id)) {
			return new Pair[0];
		} else {
			ll = (LinkedList) map.get(id);
			li = ll.listIterator();

			while (li.hasNext()) {
				pair = (Pair) li.next();
				if (map == fromMap) {
					if (pair.fromEnd < start) {
						continue;
					}
					if (pair.fromStart > end) {
						break;
					}
				} else {
					if (pair.toEnd < start) {
						continue;
					}
					if (pair.toStart > end) {
						break;
					}
				}
				result.add(pair);
			}
		}

		return (Pair[]) ll.toArray(new Pair[ll.size()]);
	}

	public String getFromTag() {
		return fromTag;
	}

	public String getToTag() {
		return toTag;
	}

	public int getSize() {
		return size;
	}

}
