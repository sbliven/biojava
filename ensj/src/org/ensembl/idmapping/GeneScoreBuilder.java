/*
 * Copyright (C) 2004 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.idmapping;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Transcript;
import org.ensembl.util.SerialUtil;
import org.ensembl.util.StringUtil;

import cern.colt.map.OpenLongObjectHashMap;

/**
 * Build gene scores based on transcript scores.
 */
public class GeneScoreBuilder extends ScoreBuilder {
	
	private Config conf;
	private boolean debug = true;
	
    public GeneScoreBuilder(Config conf, Cache cache) {

        super(cache);
        this.conf = conf;
    }


	/**
	 * Score genes based on transcript scores. This one just checks for the 
	 * serialized version and loads it if its there.
	 * 
	 * If not it delegates to buildScoringMatrix
	 */
	public ScoredMappingMatrix scoreGenes(ScoredMappingMatrix transcriptScores ) {
	
		ScoredMappingMatrix geneScoringMatrix = null;
	
		// read from file if already done
		String geneScoreFileName = conf.rootDir + File.separator + "gene_scores.ser";
		File f = new File(geneScoreFileName);
		if (!f.exists()) {
	
			System.out
					.println("Did not find existing gene score matrix, will build a new one");
	
			// build a gene score matrix from transcript score matrix
			geneScoringMatrix = geneScoresFromTranscriptScores(transcriptScores);
			debug("Gene scoring matrix has = "
					+ geneScoringMatrix.getEntryCount() + " entries");
			SerialUtil.writeObject(geneScoringMatrix, geneScoreFileName);
			System.out.println("Wrote gene scoring matrix to "
					+ geneScoreFileName);
	
		} else {
	
			System.out.println("Using existing gene score matrix in "
					+ geneScoreFileName);
			geneScoringMatrix = (ScoredMappingMatrix) SerialUtil
					.readObject(geneScoreFileName);
	
		}
		
		debug( "Total source genes: " + cache.getSourceGenesByInternalID().size() +
				" scored: " + geneScoringMatrix.getSourceCount());
		debug( "Total target genes: " + cache.getTargetGenesByInternalID().size() +
				" scored: " + geneScoringMatrix.getTargetCount());
		debug(geneScoringMatrix.toString());
		if ( debug ) {
			geneScoringMatrix.dumpToFile(conf.debugDir, "gene_scores.txt");
		}
	
		return geneScoringMatrix;
	
	}

	public ScoredMappingMatrix simpleGeneRescore( ScoredMappingMatrix oldGeneScores, ScoredMappingMatrix transcriptScores ) {
		return scoringMatrixFromFlagMatrix( oldGeneScores, transcriptScores, true );
	}
	
	// -------------------------------------------------------------------------
	
	//  -------------------------------------------------------------------------
	/**
	 * Build a matrix of source/target gene scores based on transcript scores.
	 */
	private ScoredMappingMatrix geneScoresFromTranscriptScores(ScoredMappingMatrix transcriptScores) {
	
	    // find which gene pairs actually score, store result in a "flag" matrix
	    // this avoids having to do all-vs-all comparison
	    ScoredMappingMatrix geneFlagMatrix = flagMatrixFromTranscriptScores( transcriptScores );
	
	    ScoredMappingMatrix geneScores = scoringMatrixFromFlagMatrix( geneFlagMatrix, transcriptScores, false );
	
	    return geneScores;	
	}
	
	//---------------------------------------------------------------------
	/**
	 * Find which source and target genes actually have scoring transcripts.
	 * 
	 * @return A ScoredMappingMatrix with entries only for scoring source/target gene pairs.
	 */
	private ScoredMappingMatrix flagMatrixFromTranscriptScores( ScoredMappingMatrix transcriptScores ) {
	
	    OpenLongObjectHashMap targetGeneByTranscript = cache.getTargetGeneByTranscriptInternalID();
	    OpenLongObjectHashMap sourceGeneByTranscript = cache.getSourceGeneByTranscriptInternalID();
	    
	    // build "flag" score matrix
	    ScoredMappingMatrix result = new ScoredMappingMatrix();
	
	    /* Algorithm:
	     * For every transcript scoring matrix entry, make an entry in the 
	     * Gene flag matrix.
	     */
	    Iterator i = transcriptScores.getAllEntries().iterator();
	    
	    while( i.hasNext() ) {
	    		Entry e = (Entry) i.next();
	    		Gene sourceGene = (Gene) sourceGeneByTranscript.get( e.source );
	    		Gene targetGene = (Gene) targetGeneByTranscript.get( e.target );
	    		
	    		result.addScore( sourceGene.getInternalID(), targetGene.getInternalID(), 1.0f );
	    }
	
	    return result;
	}

	// -------------------------------------------------------------------------
    /**
     * Score source genes with target genes.
     *  
     */
    private ScoredMappingMatrix scoringMatrixFromFlagMatrix( ScoredMappingMatrix geneFlagMatrix,
            ScoredMappingMatrix transcriptScores, boolean simpleScoring ) {

        ScoredMappingMatrix geneScores = new ScoredMappingMatrix();

        OpenLongObjectHashMap sourceGenes = cache.getSourceGenesByInternalID();
        OpenLongObjectHashMap targetGenes = cache.getTargetGenesByInternalID();
        
        Iterator flagEntries = geneFlagMatrix.getAllEntries().iterator();
        while( flagEntries.hasNext() ) {
       	
        		Entry flagEntry = (Entry) flagEntries.next();
        		Gene sourceGene = (Gene) sourceGenes.get( flagEntry.source );
        		Gene targetGene = (Gene) targetGenes.get( flagEntry.target );
        		float geneScore;
        		if( simpleScoring ) {
        			geneScore = simpleGeneGeneScore( sourceGene, targetGene, transcriptScores );
        		} else {
        			geneScore = complexGeneGeneScore( sourceGene, targetGene, transcriptScores );
        		}
        		geneScores.addScore(sourceGene.getInternalID(), targetGene.getInternalID(), geneScore);

        } // while flagEntries
        
        return geneScores;
    }

    private float complexGeneGeneScore( Gene sourceGene, Gene targetGene, ScoredMappingMatrix transcriptScores ) {
		List sourceTranscripts = sourceGene.getTranscripts();

		// We are only interested in scoring with transcripts that are in the target gene.
		// The sourceTranscripts scored mapping matrix may contain scores for transcripts
		// that aren't in this gene so store a map of the target gene's transcripts

		Map targetGeneTranscriptMap = new HashMap();
		Iterator tgtit = targetGene.getTranscripts().iterator();
		while (tgtit.hasNext()) {
			Transcript t = (Transcript) tgtit.next();
			targetGeneTranscriptMap.put(new Long(t.getInternalID()), t);
		}
		

		float sourceGeneScore = 0.0f;
		float targetGeneScore = 0.0f;
		long sourceGeneLength = 0; // will be cumulative length of all transcripts
		long targetGeneLength = 0; // will be cumulative length of all transcripts

		// now find the highest scoring entry that has a target transcript belonging to the
		// target gene (hence second argument to findHighestScoreEntry())
		Iterator seit = sourceTranscripts.iterator();
		while (seit.hasNext()) {

			Transcript sourceTranscript = (Transcript) seit.next();
			List targetEntries = transcriptScores.sourceEntries(sourceTranscript.getInternalID());

			// not all source transcripts will have corresponding targets
			if (targetEntries.size() > 0) {
				Entry highestScoreEntry = findHighestScoreTargetEntry(targetEntries, targetGeneTranscriptMap, true );
				if (highestScoreEntry != null) { // may be none
					// note transcript length here is combined length of all transcript's exons
					sourceGeneScore += highestScoreEntry.getScore() * sourceTranscript.getLength();
				}	
			}
			sourceGeneLength += sourceTranscript.getLength();
		}

		// calculate target gene score similarly for target transcripts
		Map sourceGeneTranscriptMap = new HashMap();
		Iterator steit = sourceGene.getTranscripts().iterator();
		while (steit.hasNext()) {
			Transcript t = (Transcript) steit.next();
			sourceGeneTranscriptMap.put(new Long(t.getInternalID()), t);
		}

		Iterator teit = targetGene.getTranscripts().iterator();
		while (teit.hasNext()) {

			Transcript targetTranscript = (Transcript) teit.next();
			List sourceEntries = transcriptScores.targetEntries(targetTranscript.getInternalID());
			// not all target transcripts will have corresponding targets
			if (sourceEntries.size() > 0) {
				Entry highestScoreEntry = findHighestScoreSourceEntry(sourceEntries, sourceGeneTranscriptMap, true);
				if (highestScoreEntry != null) { // may be none
					// note transcript length here is combined length of all transcript's exons
					targetGeneScore += highestScoreEntry.getScore() * targetTranscript.getLength();
				}	
			}	
			targetGeneLength += targetTranscript.getLength();
		}

		// calculate gene score and store in geneScores
		float geneScore = 0.0f;
		if (sourceGeneLength + targetGeneLength > 0) {
			geneScore = (sourceGeneScore + targetGeneScore) / (sourceGeneLength + targetGeneLength);
		} else {
			System.err.println("Error: Combined lengths of source gene " + sourceGene.getInternalID() + " and target gene "
                    + targetGene.getInternalID() + " are zero!");
		}
		
		return geneScore;
    }
    
    /**
     * Simplified scoreing for genes. Score is best scoring transcript pair.
     * This is used when the more elaborate gene representing score does not distinguish
     * very well.
     * 
     * @param sourceGene
     * @param targetGene
     * @param transcriptScores
     * @return
     */
    private float simpleGeneGeneScore( Gene sourceGene, Gene targetGene, ScoredMappingMatrix transcriptScores ) {
    		float geneScore = 0.0f;
    		List sourceTranscripts = sourceGene.getTranscripts();
    		Iterator steit = sourceGene.getTranscripts().iterator();
    		List targetTranscripts = targetGene.getTranscripts();
    		while( steit.hasNext() ) {
    			Iterator tteit = targetTranscripts.iterator();
    			Transcript st = (Transcript) steit.next();
    			while( tteit.hasNext() ) {
    				Transcript tt = (Transcript) tteit.next();    				
    				float score = transcriptScores.getScore( st.getInternalID(), tt.getInternalID());
    				geneScore = (geneScore<score)?score:geneScore;
    			}
    		}
    		return geneScore;
    }
    
    private void debug( String mesg ) {
		if( debug ) 
			System.out.println( mesg );
	}


    /**
     * Penalise scores between genes with different biotypes or displaynames.
     * 
     * This should enable duplicate genes (that overlap on the genome) to be disambiguated. 
     * @param biotypeGeneScores
     */
    public void ambiguousGeneRescore(ScoredMappingMatrix biotypeGeneScores) {
      List entries = biotypeGeneScores.getAllEntries();
      for (int i = 0; i < entries.size(); i++) {
        Entry entry = (Entry) entries.get(i);
        Gene srcGene = cache.getSourceGeneByInternalID(entry.source);
        Gene tgtGene = cache.getTargetGeneByInternalID(entry.target);
        if (!srcGene.getBioType().equals(tgtGene.getBioType()) 
            || 0!=StringUtil.compare(srcGene.getDisplayName(),tgtGene.getDisplayName()))
          entry.score *= 0.8;
      }
    }
    
    /**
     * Penalise scores between ambiguous gene mappings based on internal
     * IDs.
     * 
     * This should be the last gene mapping step as previous steps could
     * provide better disambiguation. 
     * 
     * It attempts to map genes that have been copied (with the same internalID)
     * and ignore duplicates. 
     * 
     * If a src and tgt gene have the same internal ID and there are mappings
     * to other tgt genes then these *other* mappings are penalised.
     * 
     * @param geneScores gene scores to be used and modified in place
     */
    public void ambiguousInternalIDGeneRescore(ScoredMappingMatrix geneScores) {
    long[] srcIDs = geneScores.getAllSources();
    Comparator c = new EntryScoreReverseComparator();
    for (int i = 0; i < srcIDs.length; i++) {
      long srcID = srcIDs[i];
      List entries = geneScores.sourceEntries(srcID);

      // only penalise if src maps to >1 tgt
      if (entries.size() < 2)
        continue;

      // only penalise if mappings are ambiguous
      Collections.sort(entries, c);
      float bestScore = ((Entry) entries.get(0)).score;
      float otherScore = ((Entry) entries.get(1)).score;
      if (bestScore != otherScore)
        continue;

      // only penalise if one srcID==tgtID where score = best score
      boolean ambiguous = false;
      for (Iterator iter = entries.iterator(); !ambiguous && iter.hasNext();) {
        Entry entry = (Entry) iter.next();
        if (entry.target == srcID && entry.score == bestScore)
          ambiguous = true;
      }
      if (!ambiguous)
        continue;

      // penalise where srcID!=tgtID where score==bestScore
      for (Iterator iter = entries.iterator(); iter.hasNext();) {
        Entry entry = (Entry) iter.next();
        if (entry.target != srcID && entry.score == bestScore)
          entry.score *= 0.8;
      }
    }
  }

 }