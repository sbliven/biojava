/*
 *  
 */
package org.ensembl.idmapping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author arne
 * 
 * Objects of this class should be able to take pairs of exons and their
 * associated score. Access functions should enable to get sorted lists and
 * pairs out by various criteria. This will be used to store Exon and Transcript
 * scores.
 */
public class ScoredMappingMatrix implements Serializable {

	//private TreeSet sourceTree, targetTree;

	private static final long serialVersionUID = 1L;

	private HashMap combinedMap;
	private Entry initialEntry;

	public static void main(String[] args) {

	}

	public ScoredMappingMatrix() {
		this( 10 );
	}
	
	public ScoredMappingMatrix( int initialSize ) {

		combinedMap = new HashMap( initialSize );
		initialEntry = new Entry( -1l, -1l, 0.0f );
		initialEntry.nextSourceForTarget = -1;
		initialEntry.nextTargetForSource = -1;
		CombinedKey key = new CombinedKey( initialEntry );
		combinedMap.put( key, initialEntry );
	}

	/**
	 * Build a scored mapping matrix from a list of entries.
	 * The Entries will have modified newTargetForSource and nextSourceForTarget
	 * Attributes 
	 */
	public ScoredMappingMatrix(List entries) {

		this();
		Iterator it = entries.iterator();
		while (it.hasNext()) {
			Entry e = (Entry) it.next();
			this.addEntry( e );
		}
	}

	/**
	 * Use an Entry struct to contain all scored pair information.
	 * 
	 * @param source
	 * @param target
	 * @return an Entry object with source,target and score
	 */
	public Entry getEntry(long source, long target) {

		CombinedKey key = new CombinedKey(source, target);
		if (combinedMap.containsKey(key)) {
			return (Entry) combinedMap.get(key);
		} else {
			return null;
		}
	}

	/**
	 * Find if there was a score defined between given source and target object.
	 * 
	 * @param source
	 * @param target
	 * @return whether there was a score defined between given source and target
	 *         object.
	 */
	public boolean hasScore(long source, long target) {

		return (getEntry(source, target) != null);
	}

	/**
	 * Return a score between the given objects or 0.0f if there is no score.
	 * 
	 * @param source
	 * @param target
	 * @return score between the given objects or 0.0f if there is no score.
	 */
	public float getScore(long source, long target) {

		Entry entry = getEntry(source, target);
		if (entry != null) {
			return entry.score;
		} else {
			return 0.0f;
		}
	}

	
	private void removeFromSourcesList( Entry e ) {
		CombinedKey key = new CombinedKey( -1l, e.target );
		Entry iter, listHead;
		iter = (Entry) combinedMap.get( key );
		listHead = iter;
		while( iter.nextSourceForTarget != e.source ) {
			key = new CombinedKey( iter.nextSourceForTarget, e.target );
			iter = ( Entry ) combinedMap.get( key );
			if( iter == null ) {
				throw new InternalError( "Matrix lists broken");
			}
		}
		
		iter.nextSourceForTarget = e.nextSourceForTarget;
		if( listHead.nextSourceForTarget == -1 ) {
			// no interactions left with this target
			iter = initialEntry;
			while( iter.nextTargetForSource != listHead.target ) {
				key = new CombinedKey( -1l, iter.nextTargetForSource );
				iter = ( Entry ) combinedMap.get( key );
				if( iter == null ) {
					throw new InternalError( "Matrix lists broken");
				}
			}
			iter.nextTargetForSource = listHead.nextTargetForSource;
		}
	}
	
	private void removeFromTargetsList( Entry e ) {
		CombinedKey key = new CombinedKey( e.source, -1l );
		Entry iter, listHead;
		iter = (Entry) combinedMap.get( key );
		listHead = iter;
		while( iter.nextTargetForSource != e.target ) {
			key = new CombinedKey( iter.nextTargetForSource, e.source );
			iter = ( Entry ) combinedMap.get( key );
			if( iter == null ) {
				throw new InternalError( "Matrix lists broken");
			}
		}		
		iter.nextTargetForSource = e.nextTargetForSource;
		
		if( listHead.nextTargetForSource == -1 ) {
			// no interactions left with this target
			iter = initialEntry;
			while( iter.nextSourceForTarget != listHead.source ) {
				key = new CombinedKey( iter.nextSourceForTarget, -1l );
				iter = ( Entry ) combinedMap.get( key );
				if( iter == null ) {
					throw new InternalError( "Matrix lists broken");
				}
			}
			iter.nextSourceForTarget = listHead.nextSourceForTarget;
		}
		
	}
	
	
	public void remove( long source, long target ) {
		CombinedKey k = new CombinedKey( source, target );
		Entry e = ( Entry ) combinedMap.get( k );
		if( e != null ) {
			removeFromSourcesList( e );
			removeFromTargetsList( e );
			combinedMap.remove( k );
		}
	}
	
	/**
	 * Put a score to the two given objects.
	 * 
	 * @param source
	 * @param target
	 * @param score
	 */
	public void addScore(long source, long target, float score) {

		CombinedKey key = new CombinedKey(source, target);

		if (combinedMap.containsKey(key)) {
			Entry oldEntry = (Entry) combinedMap.get(key);
			oldEntry.score = score;
		} else {
			Entry newEntry = new Entry(source, target, score);
			addEntry( newEntry );
		}
	}

	/**
	 * Careful, each Entry can only be part of one ScoredMappingMatrix as it contains
	 * the pointers to the source and target lists. If you take an Entry from a Matrix
	 * and put it into another, you imediatly break the original Matrix! 
	 * This awkward design is done to save memory, but does it really?
	 * 
	 */
	public void addEntry( Entry e ) {
		CombinedKey targetListKey = new CombinedKey(e.source, -1l);
		CombinedKey sourceListKey = new CombinedKey(-1l, e.target);

		if (combinedMap.containsKey(sourceListKey)) {
			Entry sourceListEntry = (Entry) combinedMap.get(sourceListKey);
			e.nextSourceForTarget = sourceListEntry.nextSourceForTarget;
			sourceListEntry.nextSourceForTarget = e.source;
		} else {
			Entry sourceListEntry = new Entry(-1l, e.target, 0.0f);
			sourceListEntry.nextSourceForTarget = e.source;
			sourceListEntry.nextTargetForSource = initialEntry.nextTargetForSource;
			initialEntry.nextTargetForSource = e.target;
			combinedMap.put(sourceListKey, sourceListEntry);
			e.nextSourceForTarget = -1l;
		}

		if (combinedMap.containsKey(targetListKey)) {
			Entry targetListEntry = (Entry) combinedMap.get(targetListKey);
			e.nextTargetForSource = targetListEntry.nextTargetForSource;
			targetListEntry.nextTargetForSource = e.target;
		} else {
			Entry targetListEntry = new Entry(e.source, -1l, 0.0f);
			targetListEntry.nextTargetForSource = e.target;
			targetListEntry.nextSourceForTarget = initialEntry.nextSourceForTarget;
			initialEntry.nextSourceForTarget = e.source;
			combinedMap.put(targetListKey, targetListEntry);				
			e.nextTargetForSource = -1l;
		}
		combinedMap.put( new CombinedKey( e ), e );
	}

	
	
	/**
	 * Gives back a List of Entry objects that have that source. If no Entry
	 * objects have that source, an empty list (rather than null) is returned.
	 */
	public List sourceEntries(long source) {
		
		ArrayList list = new ArrayList();
		Entry entry;
		CombinedKey key;
		
		CombinedKey targetListKey = new CombinedKey(source, -1l);
		if (combinedMap.containsKey(targetListKey)) {
			entry = (Entry) combinedMap.get( targetListKey );
			long nextTarget = entry.nextTargetForSource;
			while( nextTarget != -1  ) {
				key = new CombinedKey( source, nextTarget );
				entry = (Entry ) combinedMap.get( key );
				if( entry == null ) {
					throw new InternalError( "scoring Matrix list broken ");
				}
				list.add( entry );
				nextTarget = entry.nextTargetForSource;
			}
		} 

		return list;
}

	/**
	 * Gives back a List of Entry objects that have that target. If no Entry
	 * objects have that target, an empty list (rather than null) is returned.
	 */
	public List targetEntries(long target) {

		ArrayList list = new ArrayList();
		Entry entry;
		CombinedKey key;
		
		CombinedKey sourceListKey = new CombinedKey(-1l, target);
		if (combinedMap.containsKey(sourceListKey)) {
			entry = (Entry) combinedMap.get( sourceListKey );
			long nextSource = entry.nextSourceForTarget;
			while( nextSource != -1  ) {
				key = new CombinedKey( nextSource, target );
				entry = (Entry ) combinedMap.get( key );
				if( entry == null ) {
					throw new InternalError( "scoring Matrix list broken ");
				}
				list.add( entry );
				nextSource = entry.nextSourceForTarget;
			}
		} 


		return list;
	}

	/**
	 * Get the targets that have a score with a given source. Note this only
	 * returns the target IDs, not Entry objects, so no scores are returned.
	 */
	public long[] getTargetsForSource(long source) {

		List sourceEntries = sourceEntries(source);
		long[] result = new long[sourceEntries.size()];
		Iterator it = sourceEntries.iterator();
		int i = 0;
		while (it.hasNext()) {
			result[i++] = ((Entry) it.next()).getTarget();
		}

		return result;

	}

	/**
	 * Get the sources that have a score with a given target. Note this only
	 * returns the source IDs, not Entry objects, so no scores are returned.
	 */
	public long[] getSourcesForTarget(long target) {

		List targetEntries = targetEntries(target);
		long[] result = new long[targetEntries.size()];
		Iterator it = targetEntries.iterator();
		int i = 0;
		while (it.hasNext()) {
			result[i++] = ((Entry) it.next()).getSource();
		}

		return result;

	}

	/**
	 * @return A list of all the sources which have entries in this matrix.
	 */
	public long[] getAllSources() {

		ArrayList list = new ArrayList();
		// ahh need to support this
		Entry e = initialEntry;
		long source = e.nextSourceForTarget;
		while( source != -1 ) {
			CombinedKey k = new CombinedKey( source, -1l );
			list.add( new Long( source));
			e = (Entry) combinedMap.get( k );
			if( e == null) {
				throw new InternalError( "Matrix linked list broken");
			}
			source = e.nextSourceForTarget;
		}
		long[] res = new long[ list.size()];
		for( int i=list.size(); i-->0; ) {
			res[i] = ((Long)list.get(i)).longValue();
		}
		return res;
	}
	
	public int getSourceCount() {
		Entry e = initialEntry;
		long source = e.nextSourceForTarget;
		int count = 0;
		while( source != -1 ) {
			CombinedKey k = new CombinedKey( source, -1l );
			e = (Entry) combinedMap.get( k );
			count++;
			source = e.nextSourceForTarget;
		}
		return count;
	}
	
	public int getTargetCount() {
		Entry e = initialEntry;
		long target = e.nextTargetForSource;
		int count = 0;
		while( target != -1 ) {
			CombinedKey k = new CombinedKey( -1l, target );
			e = (Entry) combinedMap.get( k );
			count++;
			target = e.nextTargetForSource;
		}
		return count;
	}
	
	/**
	 * @return A list of all the sources which have entries in this matrix.
	 */
	public long[] getAllTargets() {

		ArrayList list = new ArrayList();
		Entry e = initialEntry;

		long target = e.nextTargetForSource;
		while( target != -1 ) {
			CombinedKey k = new CombinedKey( -1l, target );
			list.add( new Long( target ));
			e = (Entry) combinedMap.get( k );
			if( e == null) {
				throw new InternalError( "Matrix linked list broken");
			}
			target = e.nextTargetForSource;
		}
		long[] res = new long[ list.size()];
		for( int i=list.size(); i-->0; ) {
			res[i] = ((Long)list.get(i)).longValue();
		}
		return res;
	}

	public int getEntryCount() {
		return combinedMap.size();
	}

	/**
	 * Get the minimum and maximum scores for this matrix.
	 * 
	 * @return A 2-element array, the first element being the lowest score, the
	 *         second the highest.
	 */
	public float[] getMinMaxScores() {

		float[] result = { Float.MAX_VALUE, Float.MIN_VALUE };
		Collection values = combinedMap.values();
		Iterator it = values.iterator();
		while (it.hasNext()) {
			Entry e = (Entry) it.next();
			result[0] = Math.min(result[0], e.getScore());
			result[1] = Math.max(result[1], e.getScore());
		}

		return result;

	}

	/**
	 * Get the average scores for this matrix.
	 * 
	 * @return The average score.
	 */
	public float getAverageScore() {

		float total = 0.0f;
		Collection values = combinedMap.values();
		Iterator it = values.iterator();
		int size = 0;
		while (it.hasNext()) {
			Entry e = (Entry) it.next();
			if( e.source == -1 || e.target==-1 ) {
				continue;
			}
			total += e.getScore();
			size++;
		}
		
		if( size > 0 ) {
		    return total / size;
		} else {
			return 0f;			
		}

	}

	/**
	 * Get a list of all the entries in this matrix.
	 *  
	 */
	public List getAllEntries() {
		ArrayList list = new ArrayList( combinedMap.size() );

		Collection values = combinedMap.values();
		Iterator it = values.iterator();
		while (it.hasNext()) {
			Entry e = (Entry) it.next();
			if( e.source == -1 || e.target==-1 ) {
				continue;
			}
			list.add( e );
		}
		
		return list;
	}

	
	public List getSortedEntries() {
		ArrayList entries = (ArrayList) getAllEntries();
		Collections.sort( entries, new Comparator() 
				{
				// reverse sorted by score
				public int compare( Object o1, Object o2 ) {
					Entry e1 = (Entry) o1;
					Entry e2 = (Entry) o2;
					if( e1.score == e2.score ) {
						return 0;
					}
					if( e1.score < e2.score ) {
						return 1;
					}
					return -1;
				}	
				});
		return entries;
	}
	
	public void dump() {
		Set entries = combinedMap.entrySet();
		Iterator it = entries.iterator();
		while (it.hasNext()) {

			Map.Entry e = (Map.Entry) it.next();
			CombinedKey key = (CombinedKey) e.getKey();
			Entry entry = (Entry) e.getValue();
			if( entry.source == -1 || entry.target==-1 ) {
				continue;
			}
			System.out.println("Key: " + key.source + "," + key.target
					+ " Entry: " + entry.getSource() + "," + entry.getTarget()
					+ "," + entry.getScore());
		}
	}

	public void dumpToFile(String rootDir, String outputFileName) {

		try {

			Set entries = combinedMap.entrySet();
			Iterator it = entries.iterator();

			OutputStreamWriter writer = new OutputStreamWriter(
					new FileOutputStream(rootDir + File.separator
							+ outputFileName));

			while (it.hasNext()) {

				Map.Entry e = (Map.Entry) it.next();
				CombinedKey key = (CombinedKey) e.getKey();
				Entry entry = (Entry) e.getValue();
				if( entry.source == -1 || entry.target==-1 ) {
					continue;
				}
				writer.write(entry.getSource() + "\t" + entry.getTarget()
						+ "\t" + entry.getScore() + "\n");

			}

			writer.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	
	//  -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	/**
	 * Combine this matrix with another to get the union. If a source/target
	 * pair has different scores in each, the highest score is used.
	 */
	public void combineWith(ScoredMappingMatrix smm) {

		List smmEntries = smm.getAllEntries();
		Iterator it = smmEntries.iterator();
		while (it.hasNext()) {
			Entry e = (Entry) it.next();
			Entry myEntry = getEntry(e.getSource(), e.getTarget());

			// if it's not in our matrix, add it
			if (myEntry == null) {
				addScore(e.getSource(), e.getTarget(), e.getScore());
			} else {
				// if it is in our matrix, set the score if required
				if (e.getScore() > myEntry.getScore()) {
					myEntry.score = e.score;
				}
			}
		}

	}

	// -------------------------------------------------------------------------

	public String toString() {

		float[] minMax = getMinMaxScores();
		return ("ScoredMappingMatrix: Size: " + getEntryCount()
				+ " Min score: " + minMax[0] + " Max score: " + minMax[1]
				+ " Average score: " + getAverageScore() +
				" Source count " + getSourceCount() + 
				" Target count " + getTargetCount());

	}

	// -------------------------------------------------------------------------

}


/**
 * Object containing a source and target object that can
 */

class CombinedKey implements Serializable {

	private static final long serialVersionUID = 1L;

	public long source, target;

	public CombinedKey(long source, long target) {

		this.source = source;
		this.target = target;
	}

	public CombinedKey(Entry e) {

		this.source = e.source;
		this.target = e.target;

	}

	// need to override equals and hashcode to allow sensible comparisons
	public boolean equals(Object o) {

		CombinedKey ck = (CombinedKey) o;

		return (ck.source == source && ck.target == target);

	}

	public int hashCode() {

		int hash = 17;

		hash = 37 * hash * (int) source;

		hash = 37 * hash * (int) target;

		return hash;
	}

}
// -------------------------------------------------------------------------

