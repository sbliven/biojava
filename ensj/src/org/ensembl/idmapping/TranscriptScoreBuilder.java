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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.Translation;
import org.ensembl.util.ProgressPrinter;
import org.ensembl.util.SerialUtil;
import org.ensembl.util.Util;

import cern.colt.list.ObjectArrayList;
import cern.colt.map.OpenLongObjectHashMap;

/**
 * Build transcript scores based on exon scores.
 */

public class TranscriptScoreBuilder extends ScoreBuilder {

	private Config conf;
	private boolean debug = true;
	
	
	public TranscriptScoreBuilder( Config conf, Cache cache) {

		super(cache);
		this.conf = conf;
	}


	// -------------------------------------------------------------------------
	
	public ScoredMappingMatrix scoreTranscripts(
			ScoredMappingMatrix exonScoringMatrix) {
	
		ScoredMappingMatrix transcriptScoringMatrix = null;
	
		// read from file if already done
		String transcriptScoreFileName = conf.rootDir + File.separator
				+ "transcript_scores.ser";
		File f = new File(transcriptScoreFileName);
		if (!f.exists()) {
	
			System.out
					.println("Did not find existing transcript score matrix, will build a new one");
			// build a transcript score matrix from exon score matrix
			transcriptScoringMatrix = transcriptScoresFromExonScores(exonScoringMatrix);
			debug("Transcript scoring matrix has = "
					+ transcriptScoringMatrix.getEntryCount() + " entries");
			SerialUtil.writeObject(transcriptScoringMatrix,
					transcriptScoreFileName);
			System.out.println("Wrote transcript scoring matrix to "
					+ transcriptScoreFileName);
	
		} else {
	
			System.out.println("Using existing transcript score matrix in "
					+ transcriptScoreFileName);
			transcriptScoringMatrix = (ScoredMappingMatrix) SerialUtil
					.readObject(transcriptScoreFileName);
	
		}
	
		debug( "Total source transcripts: " + cache.getSourceTranscriptsByInternalID().size() +
					" Scored: " + transcriptScoringMatrix.getSourceCount() );
		debug( "Total target transcripts: " + cache.getTargetTranscriptsByInternalID().size() +
				" Scored: " + transcriptScoringMatrix.getTargetCount() );
		debug(transcriptScoringMatrix.toString());
	
		if ( debug ) {
			transcriptScoringMatrix.dumpToFile(conf.debugDir,
					"transcript_scores.txt");
		}
	
		return transcriptScoringMatrix;
	
	}

	// -------------------------------------------------------------------------
	/**
	 * Build a matrix of source/target transcript scores based on exon scores.
	 */
	private ScoredMappingMatrix transcriptScoresFromExonScores(ScoredMappingMatrix exonScores) {

		ScoredMappingMatrix transcriptScores = new ScoredMappingMatrix();

		ObjectArrayList allSourceTranscripts = cache
				.getSourceTranscriptsByInternalID().values();
		ObjectArrayList allTargetTranscripts = cache
				.getTargetTranscriptsByInternalID().values();

		// build source and target hashmaps of transcripts keyed on internal ID
		Map sourceTranscriptsMap = new HashMap();
		Map targetTranscriptsMap = new HashMap();
		final int n = allSourceTranscripts.size();
		for(int i=0; i<n; ++i) {
			Transcript t = (Transcript) allSourceTranscripts.getQuick(i);
			sourceTranscriptsMap.put(new Long(t.getInternalID()), t);
		}
		final int m = allTargetTranscripts.size();
		for(int i=0;i<m;++i) {
		  Transcript t = (Transcript) allTargetTranscripts.getQuick(i);
			targetTranscriptsMap.put(new Long(t.getInternalID()), t);
		}

		// find which transcript pairs actually score, store result in a
		// "flag" matrix
		ScoredMappingMatrix transcriptFlagMatrix = flagMatrixFromExonScores(
				allSourceTranscripts, allTargetTranscripts, exonScores);

		transcriptScores = scoreMatrixFromFlagMatrix(sourceTranscriptsMap,
				targetTranscriptsMap, transcriptFlagMatrix, exonScores);

		return transcriptScores;

	}

	//---------------------------------------------------------------------
		/**
		 * Find which source and transcripts actually have scoring exons.
		 * 
		 * @return A ScoredMappingMatrix with entries only for scoring source/target
		 *         transcript pairs.
		 */
		private ScoredMappingMatrix flagMatrixFromExonScores(
				ObjectArrayList allSourceTranscripts, ObjectArrayList allTargetTranscripts,
				ScoredMappingMatrix exonScores) {
	
			// build target hashmap of transcripts keyed on exon internal ID (may be
			// >1 transcript per exon, so this is a map of lists)
			Map targetTranscriptsByExon = new HashMap();
	//		Iterator attIt = allTargetTranscripts.iterator();
	//		while (attIt.hasNext()) {
			final int m = allTargetTranscripts.size();
			for(int i=0;i<m;++i) {
				Transcript t = (Transcript) allTargetTranscripts.getQuick(i);
				Iterator eIt = t.getExons().iterator();
				while (eIt.hasNext()) {
					Exon e = (Exon) eIt.next();
					Util.addToMapList(targetTranscriptsByExon, new Long(e
							.getInternalID()), t);
	
				}
			}
	
			Collection values = targetTranscriptsByExon.values();
			Iterator vit = values.iterator();
			int n = 0;
			int nn = 0;
			while (vit.hasNext()) {
				List l = (List) vit.next();
				if (l == null) {
					n++;
				} else {
					nn++;
				}
			}
	
			// build "flag" score matrix
			ScoredMappingMatrix result = new ScoredMappingMatrix();
	
			final int t = allSourceTranscripts.size();
			for(int i=0; i<t; ++i) {
			
			  Transcript sourceTranscript = (Transcript) allSourceTranscripts.getQuick(i);
	
				Iterator eIt = sourceTranscript.getExons().iterator();
				while (eIt.hasNext()) {
	
					Exon e = (Exon) eIt.next();
					// find all scoring target exons
					List scoringEntries = exonScores.sourceEntries(e
							.getInternalID());
					Iterator scoringEntriesIterator = scoringEntries.iterator();
					while (scoringEntriesIterator.hasNext()) {
	
						Entry scoringEntry = (Entry) scoringEntriesIterator.next();
						// use target hashmap to get transcripts from target exons
						List scoringTargetTranscripts = (List) targetTranscriptsByExon
								.get(new Long(scoringEntry.getTarget()));
	
						if (scoringTargetTranscripts != null) {
	
							// flag scores in matrix
							Iterator scoringTargetTranscriptIterator = scoringTargetTranscripts
									.iterator();
							while (scoringTargetTranscriptIterator.hasNext()) {
	
								Transcript targetTranscript = (Transcript) scoringTargetTranscriptIterator
										.next();
								result.addScore(sourceTranscript.getInternalID(),
										targetTranscript.getInternalID(), 1.0f);
	
							}
						}
	
					}
	
				}
			}
	
			return result;
	
		}

	// -------------------------------------------------------------------------
	/**
	 * Score source transcripts with target transcripts. Only transcripts with
	 * an entry in the flag matrix are compared. For each pair of source and
	 * target transcripts caclulate the score as : For each source Exon Get all
	 * scores to target exons, take the highest one Multiply with the source
	 * exon length and add to source score. Multiply it with the target exon
	 * length and sum it into target score. }
	 * 
	 * Transcript pair's score is (source score + target score) / (source length +
	 * target length)
	 *  
	 */
	private ScoredMappingMatrix scoreMatrixFromFlagMatrix(
			Map sourceTranscripts, Map targetTranscripts,
			ScoredMappingMatrix transcriptFlagMatrix,
			ScoredMappingMatrix exonScores) {

		float transcriptScoreThreshold = 0.0f;
		if (System.getProperty("idmapping.transcript_score_threshold") != null) {
			transcriptScoreThreshold = Float.parseFloat(System
					.getProperty("idmapping.transcript_score_threshold"));
		} else {
			transcriptScoreThreshold = 0.0f;
		}

		//System.out.println("Building scoring matrix from transcripts and
		// 'flag' matrix - using transcript scoring threshold of " +
		// transcriptScoreThreshold);

		ProgressPrinter pp = new ProgressPrinter(0, sourceTranscripts.size(),
				"% of transcript scores calculated");
		ScoredMappingMatrix transcriptScores = new ScoredMappingMatrix();

		// iterate over ALL source transcripts, but only score against target
		// transcripts that are in the flag matrix
		Iterator sit = sourceTranscripts.keySet().iterator();
		int i = 0;
		while (sit.hasNext()) {
			//System.out.print(".");
			//System.out.flush();
			long sourceTranscriptID = ((Long) sit.next()).longValue();
			Transcript sourceTranscript = (Transcript) sourceTranscripts
					.get(new Long(sourceTranscriptID));

			List sourceExons = sourceTranscript.getExons();

			// find which entries in the flag matrix have this source
			// transcript as the source
			List scoringTargetTranscriptEntries = transcriptFlagMatrix
					.sourceEntries(sourceTranscriptID);
			Iterator stit = scoringTargetTranscriptEntries.iterator();

			// compare this source transcript with each scoring target
			// transcript
			while (stit.hasNext()) {

				Entry e = (Entry) stit.next();
				Transcript targetTranscript = (Transcript) targetTranscripts
						.get(new Long(e.getTarget()));

				long targetTranscriptID = targetTranscript.getInternalID();
				float sourceTranscriptScore = 0.0f;
				float targetTranscriptScore = 0.0f;

				// We are only interested in scoring with exons that are in the
				// target transcript.
				// The sourceExons scored mapping matrix may contain scores for
				// exons that aren't
				// in this transcript so store a map of the target transcript's
				// exons

				Map targetTranscriptExonMap = new HashMap();
				Iterator tteit = targetTranscript.getExons().iterator();
				while (tteit.hasNext()) {
					Exon ex = (Exon) tteit.next();
					targetTranscriptExonMap.put(new Long(ex.getInternalID()),
							ex);
				}

				// now find the highest scoring entry that has a target exon
				// belonging to the target transcript (hence second
				// argument to findHighestScoreEntry()
				Iterator seit = sourceExons.iterator();
				while (seit.hasNext()) {

					Exon sourceExon = (Exon) seit.next();
					List targetEntries = exonScores.sourceEntries(sourceExon
							.getInternalID());
					// not all source exons will have corresponding targets
					if (targetEntries.size() > 0) {
						Entry highestScoreEntry = findHighestScoreTargetEntry(
								targetEntries, targetTranscriptExonMap, true);
						if (highestScoreEntry != null) { // may be none
							sourceTranscriptScore += highestScoreEntry
									.getScore()
									* sourceExon.getLocation().getLength();
						}
					}

				}

				// calculate target transcript score similarly for target exons
				Map sourceTranscriptExonMap = new HashMap();
				Iterator steit = sourceTranscript.getExons().iterator();
				while (steit.hasNext()) {
					Exon ex = (Exon) steit.next();
					sourceTranscriptExonMap.put(new Long(ex.getInternalID()),
							ex);
				}
				Iterator teit = targetTranscript.getExons().iterator();
				while (teit.hasNext()) {

					Exon targetExon = (Exon) teit.next();
					List sourceEntries = exonScores.targetEntries(targetExon
							.getInternalID());
					// not all target exons will have corresponding targets
					if (sourceEntries.size() > 0) {
						Entry highestScoreEntry = findHighestScoreSourceEntry(
								sourceEntries, sourceTranscriptExonMap, true);
						if (highestScoreEntry != null) { // may be none
							targetTranscriptScore += highestScoreEntry
									.getScore()
									* targetExon.getLocation().getLength();
						}
					}

				}

				// calculate transcript score and store in transcriptScores
				float transcriptScore = 0.0f;
				int sourceTranscriptLength = sourceTranscript.getLength();
				int targetTranscriptLength = targetTranscript.getLength();
				if (sourceTranscriptLength + targetTranscriptLength > 0) {

					if (sourceTranscriptScore > sourceTranscriptLength
							|| targetTranscriptScore > targetTranscriptLength) {
						System.out.println("source score: "
								+ sourceTranscriptScore + " source length: "
								+ sourceTranscriptLength + " target score: "
								+ targetTranscriptScore + " target length: "
								+ targetTranscriptLength);
					}

					transcriptScore = (sourceTranscriptScore + targetTranscriptScore)
							/ (sourceTranscriptLength + targetTranscriptLength);

				} else {

					System.err
							.println("Error: Combined lengths of source transcript "
									+ sourceTranscriptID
									+ " and target transcript "
									+ targetTranscriptID + " are zero!");
				}
				if (transcriptScore > transcriptScoreThreshold) {
					transcriptScores.addScore(sourceTranscriptID,
							targetTranscriptID, transcriptScore);
				}

			} // while target transcript

			i++;
			pp.printUpdate(i);

		} // while source transcript

		pp.printUpdate(sourceTranscripts.size());

		System.out.println();

		return transcriptScores;

	}

	/**
	 * Reduce all those transcript Scores where they dont belong to Genes which 
	 * were mapped to each other..
	 * @param transcriptScores
	 * @param geneMappings
	 */
	public void punishNonMappedGenes( ScoredMappingMatrix transcriptScores, List geneMappings ) {
		OpenLongObjectHashMap sTr2Ge, tTr2Ge;
		sTr2Ge = cache.getSourceGeneByTranscriptInternalID();
		tTr2Ge = cache.getTargetGeneByTranscriptInternalID();
		
		Iterator i = geneMappings.iterator();
		HashMap mappedGene = new HashMap();
		while( i.hasNext()) {
			Entry e = (Entry) i.next();
			mappedGene.put( new Long( e.source ), new Long( e.target ));
		}
		
		i = transcriptScores.getAllEntries().iterator();
		while( i.hasNext() ) {
			Entry e = (Entry) i.next();
			Gene sg = (Gene) sTr2Ge.get( e.source );
			Gene tg = (Gene) tTr2Ge.get( e.target );
			Long mappedTarget = (Long) mappedGene.get( new Long( sg.getInternalID()));
			if( mappedTarget == null || mappedTarget.longValue() != tg.getInternalID()) {
				// inconsistent score
				e.score *= 0.8f;
			}
		}
	}
	
	/**
	 * Reduce score on all perfect transcript matches that dont have
	 * perfect peptide matches.
	 * 
	 * @param scores
	 */
	public void punishNonEquivalentTranslations( ScoredMappingMatrix scores ) {
		Iterator i = scores.getSortedEntries().iterator();
		Entry e;
		Map sTr2Tl, tTr2Tl;
		sTr2Tl = cache.getSourceTranslationsByTranscriptInternalID();
		tTr2Tl = cache.getTargetTranslationsByTranscriptInternalID();
		int count = 0;
		
		while( i.hasNext() ) {
			e = (Entry) i.next();
			if( e.score < 1.0f ) { break;}

			Translation stl, ttl;
			stl = (Translation) sTr2Tl.get( new Long( e.source ));
			ttl = (Translation) tTr2Tl.get( new Long( e.target ));
			if( stl == null && ttl == null ) {
				continue;
			}
			if( stl == null || ttl == null ||
					!stl.getPeptide().equals( ttl.getPeptide( ))) {
				// choose arbitrary a non 1.0f value as score
				e.score = 0.98f;
				count++;
			}			
		}
		System.out.println( "Non perfect peptides on perfect transcripts " + count );
	}
	
	private void debug( String mesg ) {
		if( debug ) 
			System.out.println( mesg );
	}
	//---------------------------------------------------------------------

}