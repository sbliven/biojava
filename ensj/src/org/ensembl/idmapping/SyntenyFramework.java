/*
 * Created on Aug 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.ensembl.idmapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Location;


/**
 * @author arne
 * 
 * This class is to be filled with pairs of locations very analoguous to the
 * information in the assembly table. The locations dont have to be the same
 * length though. Once filled, use this Framework to score pairs of genes on the
 * source location side and target location side whether they are similar.
 * 
 * The Framework will be build from genes that map uniquely between two
 * genebuilds.
 *  
 */
public class SyntenyFramework {
	private Config conf;
	private Cache cache;
	private ArrayList  mergedSyntenies;

	private boolean debug = true;
	
	public SyntenyFramework( Config conf, Cache cache ) {
		this.conf = conf;
		this.cache = cache;
		mergedSyntenies = new ArrayList();
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer();
		Iterator i = mergedSyntenies.iterator();
		result.append( "SyntenyFramework\n" );
		while( i.hasNext() ) {
			result.append( i.next().toString());
		}
		return result.toString();
	}
	
	/**
	 * Use this synteny framework to rescore all entries in the Matrix.
	 * Retain 70% of the old score and build the last 30% from how well
	 * source and target gene co locate with well mapped genes.
	 * 
	 * @param geneScores
	 */
	public void rescoreGeneMatrix( ScoredMappingMatrix geneScores ) {
		Iterator it = geneScores.getAllEntries().iterator();
		while( it.hasNext()) {
			Entry e = (Entry) it.next();
			rescoreGeneMapping( e, 0.7f );
		}
	}
	
	/** 
	 * Set a new score on given Entry by finding the location
	 * in this framework. Score it against all the relevant SyntenyRegions
	 * and take the highest score.
	 * 
	 * Then do oldscore*retainFactor+highestScore*(1-retainFactor)
	 * 
	 * @param e
	 * @param retainFactor
	 */
	public void rescoreGeneMapping( Entry e, float retainFactor ) {
		float highestMapScore = 0.0f;
		
		Gene sourceGene = (Gene) cache.getSourceGenesByInternalID().get( e.source );
		Gene targetGene = (Gene) cache.getTargetGenesByInternalID().get( e.target );
		
		Iterator i = mergedSyntenies.iterator();
		SyntenyRegion highestSr = null;
		while( i.hasNext() ) {
			SyntenyRegion sr = (SyntenyRegion) i.next();
			float score = sr.scoreLocationRelationship( sourceGene.getLocation(), targetGene.getLocation());
			if( score>highestMapScore) {
				highestMapScore = score;
				highestSr = sr;
			}
		}
		if( highestMapScore > 0.5f ) {
		    //System.out.println( "\nRescore( SYN, SG, TG )\n" + highestSr + 
		    //		"\nSource Gene " + sourceGene +
		    //		"\nTarget Gene " + targetGene +
		    //		"\nMap Score " + highestMapScore );
		}
		e.score = (float) (e.score*retainFactor+(1.0-retainFactor)*highestMapScore);
	}
	
	/**
	 * I think what I want to do here is build many SyntenyRegion blocks,
	 * which are allowed to overlap and when somebody wants the score for a gene,
	 * the system goes through all of them and takes he best score.
	 * 
	 * At most we merge two SyntenyRegions together. Mapped genes with no suitable neighbour
	 * for a region merge can still merit a slightly enlarged SyntenyRegion.
	 * 
	 * @param geneMappings
	 */
	public void buildSyntenyFromGeneMappings( List geneMappings ) {

		List syntenyList = simpleSyntenyList( geneMappings );
		if( syntenyList.size() == 0 ) {
			// no framework can be build
			System.out.println( "Couldn't build framework" );
			return;
		}
		
		SyntenyRegion lastRegion, mergedRegion, sr;
		lastRegion = null;
		
		Iterator i = syntenyList.iterator();
		boolean lastMerged = false;
		while( i.hasNext() ) {
			sr = (SyntenyRegion) i.next();
			if( lastRegion == null ) {
				lastRegion = sr;
				continue;
			}

			mergedRegion = lastRegion.merge( sr );
			if( mergedRegion == null ) {
				if( ! lastMerged ) {
					SyntenyRegion stretchedRegion = lastRegion.stretch( 3.0 );
					mergedSyntenies.add( stretchedRegion );
					lastMerged = false;
				}
			} else {
				SyntenyRegion stretchedRegion = mergedRegion.stretch( 3.0 );
				mergedSyntenies.add( stretchedRegion );
				lastMerged = true;
			}
			
			lastRegion = sr;
 		}
		if( !lastMerged ) {
			SyntenyRegion stretchedRegion = lastRegion.stretch( 3.0 );
			mergedSyntenies.add( stretchedRegion );
		}
	}
	

	/**
	 * Take the list of gene mappings and convert them into SyntenyRegions.
	 * Sort those by source Location.
	 * @param geneMappings
	 * @return
	 */
	private List simpleSyntenyList( List geneMappings ) {
		ArrayList syntenyList = new ArrayList();
		Iterator i = geneMappings.iterator();
		while( i.hasNext()) {
			Entry e = (Entry) i.next();
			Gene sourceGene = (Gene) cache.getSourceGenesByInternalID().get( e.source );
			Gene targetGene = (Gene) cache.getTargetGenesByInternalID().get( e.target );
		
			SyntenyRegion sr = new SyntenyRegion( sourceGene.getLocation(), targetGene.getLocation(), e.score );
			syntenyList.add( sr );
		}
		
		Collections.sort( syntenyList, new Comparator() { 
			public int compare( Object o1, Object o2 ) {
				SyntenyRegion sr1, sr2;
				sr1 = (SyntenyRegion) o1;
				sr2 = (SyntenyRegion) o2;
				return sr1.sourceLocationCompare( sr2.sourceLocation );
			}
		} );
		
		return syntenyList;
	}
	
	
	
	
	private void debug(String s) {
		if( debug ) 
			System.out.println( s );
	}
	
	
	// should contain lists of non overlapping SyntenyRegions
	public class SyntenyRegion {

		Location sourceLocation;
		Location targetLocation;
		float score;
		private int targetOffset;
		/**
		 * How well are source location and target location in accordance with
		 * this SyntenyRegion?
		 * 
		 * The score is the distance from the interpolated target to the real
		 * target. The distance is divided by half the target size and
		 * substracted from 1.0. If its smaller than 0.0 its set to 0.
		 * 
		 * @param sLoc
		 * @param tLoc
		 * @return
		 */
		public SyntenyRegion( Location sl, Location tl, float score ) {
			if( sl.getStrand() == 1 ) {
				sourceLocation = sl;
				targetLocation = tl;
			} else {
				sourceLocation = sl.complement();
				targetLocation = tl.complement();
			}
			this.score = score;
		}
		

		public String toString() {
			return "SL: " + sourceLocation + "\nTL: " + targetLocation + "\nScore: " + score;
		}
		/** 
		 * Create a new SyntenyRegion which covers (factor-1) * score times more area in source
		 * and target.
		 * @param factor
		 * @return
		 */
		public SyntenyRegion stretch( double factor ) {
			Location stretchedSource, stretchedTarget;
			stretchedSource = sourceLocation.copy();
			stretchedTarget = targetLocation.copy();
			factor -= 1.0;
			if( factor > 0 ) {
				int sourceExpand = (int) (stretchedSource.getLength()*factor*score);
				stretchedSource.transform( -sourceExpand, sourceExpand );
				int targetExpand = (int) (stretchedTarget.getLength()*factor*score);
				stretchedTarget.transform( -targetExpand, targetExpand );
			}
			
			SyntenyRegion result = new SyntenyRegion( stretchedSource, stretchedTarget, score );
			return result;
		}
		
		/**
		 * Does given location overlap with this SyntenyRegion? Return 0 if so,
		 * return -1 if given region is too small and 1 if its too big.
		 * 
		 * @param sl
		 * @return
		 */
		public int sourceLocationCompare( Location sl ) {
			if( sl.getSegRegionID() < sourceLocation.getSegRegionID()) 
				return -1;
			if( sl.getSegRegionID() > sourceLocation.getSegRegionID()) 
				return 1;
			if( sl.getEnd() < sourceLocation.getStart() ) return -1;
			if( sl.getStart() > sourceLocation.getEnd() ) return 1;
			return 0;
		}
		
		/**
		 * This function calculates how well the given source location
		 * interpolates on given target location inside this SyntenyRegion. It
		 * assumes the locations overlap so check with sourceLocationCompare
		 * before calling it.
		 * 
		 * Scoring is done the following way: Source and target location are
		 * normalized with respect to this Regions source and target. Source
		 * range will then be somewhere close to 0.0-1.0 and target range
		 * anything around that.
		 * 
		 * The extend of the covered area between source and target range is a
		 * measurement of how well they agree. (Smaller extend is better). The
		 * extend (actually 2*extend) is reduced by the size of the regions.
		 * This will result in 0.0 if they overlap perfectly and bigger values
		 * if they dont.
		 * 
		 * This is substracted from 1.0 to give the score. The score is likely
		 * to be below zero, but is cut off at 0.0f. Voila. (Slightly smug about
		 * this scoring)
		 * 
		 * Finally the score is multiplied with the score of the synteny itself.
		 * 
		 * @param sLoc
		 * @param tLoc
		 * @return
		 */
		public float scoreLocationRelationship( Location sLoc, Location tLoc ) {
			float result = 0f;
			if( tLoc.getSegRegionID() != targetLocation.getSegRegionID() ||
				sLoc.getSegRegionID() != sourceLocation.getSegRegionID()) return 0f;

			// clever xor for wrong strand
			if( sLoc.getStrand() == sourceLocation.getStrand() ^ tLoc.getStrand() == targetLocation.getStrand() )
				return 0f;

			// we first normalize the 2 locations
			float sLocRelStart, sLocRelEnd, tLocRelStart, tLocRelEnd;
			
			sLocRelStart =((float) (sLoc.getStart() - sourceLocation.getStart())) / sourceLocation.getLength();
			sLocRelEnd = ((float)(sLoc.getEnd() - sourceLocation.getStart() + 1 )) / sourceLocation.getLength();

			// cut off if the source location is completely outside
			if( sLocRelStart > 1.1 || sLocRelEnd < -0.1 ) {
				return 0f;
			}
			if( targetLocation.getStrand() == 1 ) {
				tLocRelStart =  ((float) ( tLoc.getStart() - targetLocation.getStart())) / targetLocation.getLength();
				tLocRelEnd =  ((float) ( tLoc.getEnd() - targetLocation.getStart() + 1)) / targetLocation.getLength();
			} else {
				tLocRelStart =  ((float) ( targetLocation.getEnd() - tLoc.getEnd())) / targetLocation.getLength();
				tLocRelEnd =  ((float) ( targetLocation.getEnd() - tLoc.getStart() + 1 )) / targetLocation.getLength();
			}

			float addedRange = (tLocRelEnd>sLocRelEnd?tLocRelEnd:sLocRelEnd)
			- (tLocRelStart<sLocRelStart?tLocRelStart:sLocRelStart);
			float distance = 2*addedRange - tLocRelEnd - sLocRelEnd + tLocRelStart + sLocRelStart;
			result = 1.0f - distance;
			
			if( result < 0 ) result = 0f;
			if( result > 1.0f ) { 
				System.out.println( "Scoring problem in " + sLoc + tLoc + " result " + result );
			}
			result *= score;
			return result;
		}
	
		/**
		 * If sr and this region are mergeable, return a merged SyntenRegion,
		 * otherwise return null If they are mergeable, the scores are combined.
		 * 
		 * @param sr
		 * @return
		 */
		public SyntenyRegion merge( SyntenyRegion sr ) {
			SyntenyRegion result = null;
			
			// source and targets need to be on same seqRegion
			if( sourceLocation.getSegRegionID() != sr.sourceLocation.getSegRegionID() ||
					targetLocation.getSegRegionID() != sr.targetLocation.getSegRegionID()) {
				return null;
			}
			
			if( targetLocation.getStrand() != sr.targetLocation.getStrand()) {
				return null;
			}
			
			// find the distance of source and target pair and compare
			int sourceLocationDifference, targetLocationDifference;
			sourceLocationDifference = sr.sourceLocation.getStart() - 
			sourceLocation.getStart();
			if( targetLocation.getStrand() == 1 ) {
				targetLocationDifference = sr.targetLocation.getStart() -	
				targetLocation.getStart();
			} else {
				targetLocationDifference = targetLocation.getEnd() - 
				sr.targetLocation.getEnd();
			}
			
			int diff = Math.abs( sourceLocationDifference - targetLocationDifference );
			float distScore = 1.0f - Math.abs((float)diff/sourceLocationDifference) - Math.abs((float) diff/targetLocationDifference);
			if( distScore < 0.5f ) { return null; }
			float newScore = distScore * (sr.score + this.score)/2.0f;
			if( newScore > 1.0f ) {
				System.out.println( "Merge failed to produce good score " + newScore );
			}
			// make a new Location for source and target that covers them and the
			// gap between them
			// make a new SyntenyRegion from those two and the newScore
			Location newSourceLocation = sourceLocation.copy();
			if( sr.sourceLocation.getStart() < newSourceLocation.getStart()) {
				newSourceLocation.setStart( sr.sourceLocation.getStart());
			}
			if( sr.sourceLocation.getEnd() > newSourceLocation.getEnd()) {
				newSourceLocation.setEnd( sr.sourceLocation.getEnd());
			}
			
			Location newTargetLocation = targetLocation.copy();
			if( sr.targetLocation.getStart() < newTargetLocation.getStart()) {
				newTargetLocation.setStart( sr.targetLocation.getStart());
			}
			if( sr.targetLocation.getEnd() > newTargetLocation.getEnd()) {
				newTargetLocation.setEnd( sr.targetLocation.getEnd());
			}
			
			result = new SyntenyRegion(newSourceLocation, newTargetLocation, newScore );
			return result;
		}
	}
	
}

