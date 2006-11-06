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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.ensembl.datamodel.Translation;
import org.ensembl.util.LongSet;
import org.ensembl.util.SerialUtil;

import cern.colt.map.OpenLongObjectHashMap;

/**
 * Perform mapping of internal IDs based on scores and other criteria.
 */
public class InternalIDMapper {


	private boolean debug = true;

	// scores are considered the same if (2.0 * (s1-s2))/(s1 + s2) < this
	private static final float SIMILAR_SCORE_RATIO = 0.01f;

	/**
	 * The cache provides access to gene, transcripts and exons of hte source
	 * and target database
	 */
	private Cache cache;
	private Config conf;
	
	public InternalIDMapper(Config conf, Cache cache) {

		this.conf = conf;
		this.cache = cache;

	}


		// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	/**
	 * Map gene internal IDs based on gene scores.
	 * 
	 * @return A list of Entry objects containing the mappings.
	 */
	public List cachedGeneMapping(ScoredMappingMatrix geneScores, ScoredMappingMatrix transcriptScores, 
				GeneScoreBuilder gsb ) {
	
		List mappings;
	
		// read from file if already done
		String geneMappingFileName = conf.rootDir + File.separator
				+ "geneMappings.ser";
		File f = new File(geneMappingFileName);
		if (!f.exists()) {
			System.out.println( "  -- Basic Gene mapping -- ");
			mappings = basicMapping( geneScores );

			ScoredMappingMatrix newGeneScores = gsb.createShrinkedMatrix( geneScores, mappings );
			
			// build the synteny from non ambiguous mappings
			System.out.println( "\n -- Synteny Framework building -- ");
			SyntenyFramework sf = new SyntenyFramework(conf, cache );
			sf.buildSyntenyFromGeneMappings( mappings );

			System.out.println( " \n -- Synteny assisted mapping -- ");
			// use it to rescore the genes
			sf.rescoreGeneMatrix( newGeneScores );
			// and try to map again
			List geneMappings2 = basicMapping( newGeneScores );
			System.out.println( "Found " + geneMappings2.size() + " additional mappings." );

			// rescore with simple scoring function and try again
			ScoredMappingMatrix simpleGeneScores = gsb.createShrinkedMatrix(  newGeneScores, geneMappings2 );
			gsb.simpleGeneRescore( simpleGeneScores, transcriptScores );

			System.out.println( "\n  -- Retry with simple best transcript score -- " );
			List geneMappings3 = basicMapping( simpleGeneScores );
			System.out.println( "Found " + geneMappings3.size() + " additional mappings." );
            
      // rescore by penalising scores between genes with different biotypes  
      ScoredMappingMatrix biotypeGeneScores = gsb.createShrinkedMatrix(  simpleGeneScores, geneMappings3 );
      gsb.ambiguousGeneRescore( biotypeGeneScores );
      System.out.println( "\n  -- Retry with biotype disambiguation -- " );
      List geneMappings4 = basicMapping( biotypeGeneScores );
      System.out.println( "Found " + geneMappings4.size() + " additional mappings." );
            

      // selectively rescore by penalising scores between genes with different internalIDs  
      ScoredMappingMatrix internalIDGeneScores = gsb.createShrinkedMatrix(  biotypeGeneScores, geneMappings4 );
      gsb.ambiguousInternalIDGeneRescore( internalIDGeneScores );
      System.out.println( "\n  -- Retry with internalID disambiguation -- " );
      List geneMappings5 = basicMapping( internalIDGeneScores );
      System.out.println( "Found " + geneMappings5.size() + " additional mappings." );
      
      
      ScoredMappingMatrix scoresRemain = gsb.createShrinkedMatrix( internalIDGeneScores, geneMappings5 );
      System.out.println( "  " + scoresRemain.getSourceCount() + " source genes are ambiguous with " +
                          scoresRemain.getTargetCount() + " target Genes." );
			
	
      mappings.addAll( geneMappings2 );
      mappings.addAll( geneMappings3 );
      mappings.addAll( geneMappings4 );
      mappings.addAll( geneMappings5 );
      
      SerialUtil.writeObject(mappings, geneMappingFileName);
      System.out.println("Wrote gene internal ID mappings to "
                         + geneMappingFileName);
      
      reportAmbiguous( scoresRemain, "Gene" );
      
		} else {
	
			System.out.println("Using existing gene internal ID mappings in "
					+ geneMappingFileName);
			mappings = (ArrayList) SerialUtil.readObject(geneMappingFileName);
		}
	
		if (debug) {
		    Entry.writeToFile(mappings, conf.rootDir + File.separator + "gene_mappings.txt");
		}
	
		return mappings;
	}	
	
	
	public List cachedTranscriptMapping( ScoredMappingMatrix transcriptScores, List geneMappings, 
			TranscriptScoreBuilder tsb  ) {
		// read from file if already done
		List transcriptMappings;
		
		String transcriptMappingFileName = conf.rootDir + File.separator
				+ "transcript_mappings.ser";
		File f = new File(transcriptMappingFileName);		
 		if (!f.exists()) {
 			
			System.out.println( "  -- Basic Transcript mapping -- ");
 			// normal mapping
 			transcriptMappings = basicMapping( transcriptScores );
 			ScoredMappingMatrix transcriptScores2 = 
 				tsb.createShrinkedMatrix( transcriptScores, transcriptMappings );			

 			// handle the case with exact match but different translation
			System.out.println( "\n  -- Exact Transcript non exact Translation -- ");
 			tsb.punishNonEquivalentTranslations( transcriptScores2 );
 			List transcriptMappings2 = basicMapping( transcriptScores2 );
 			ScoredMappingMatrix transcriptScores3 = 
 				tsb.createShrinkedMatrix( transcriptScores2, transcriptMappings2 );			
 			
			System.out.println( "\n  -- Transcripts in mapped Genes -- ");
 			// reduce scores for mappings of transcripts not between mapped genes
 			tsb.punishNonMappedGenes( transcriptScores3, geneMappings );
 			List transcriptMappings3 = basicMapping( transcriptScores3 );
 			ScoredMappingMatrix transcriptScores4 = 
 				tsb.createShrinkedMatrix( transcriptScores3, transcriptMappings3); 			
 			
			System.out.println( "\n  -- Transcripts in single Genes -- ");
 			// handle ambiguities only between transcripts inside one gene
 			List transcriptMappings4 = sameGeneTranscriptMapping( transcriptScores4 );
 			
 			// report remaining ambiguities
 			ScoredMappingMatrix transcriptScoresRemain = tsb.createShrinkedMatrix( transcriptScores4, transcriptMappings4 );
 			reportAmbiguous( transcriptScoresRemain, "Transcript" );
			System.out.println( "  " + transcriptScoresRemain.getSourceCount() + " source transcripts are ambiguous with " +
					transcriptScoresRemain.getTargetCount() + " target Transcripts." );
 			
 			transcriptMappings.addAll( transcriptMappings2 );
 			transcriptMappings.addAll( transcriptMappings3 );
 			transcriptMappings.addAll( transcriptMappings4 );

 			SerialUtil.writeObject( transcriptMappings, transcriptMappingFileName);

		} else {

			System.out
					.println("Using existing transcript internal ID mappings in "
							+ transcriptMappingFileName);
			transcriptMappings = (ArrayList) SerialUtil
					.readObject(transcriptMappingFileName);
		}

		if (debug) {
		    Entry.writeToFile( transcriptMappings, conf.rootDir + File.separator+"transcript_mappings.txt");
		}

		return transcriptMappings;
	}
	
	// -------------------------------------------------------------------------
	/**
	 * Map exon internal IDs based on exon scores and transcript mappings.
	 * 
	 * @return A list of Entry objects containing the mappings.
	 */
	public List cachedExonMapping(ScoredMappingMatrix exonScores,
			List transcriptMappings, ExonScoreBuilder esb ) {
	
		List mappings;
		// read from file if already done
		String exonMappingFileName = conf.rootDir + File.separator
				+ "exon_mappings.ser";
		File f = new File(exonMappingFileName);

		if (!f.exists()) {
	
			mappings = basicMapping( exonScores );
			ScoredMappingMatrix scores2 = esb.createShrinkedMatrix( exonScores, mappings );

			// now punish exons that are in non mapped transcripts
			esb.punishNonMappedTranscript( scores2, transcriptMappings );
			List mappings2 = basicMapping( scores2 );

			ScoredMappingMatrix scoresRemain = esb.createShrinkedMatrix( scores2, mappings2 );
			mappings.addAll( mappings2 );
	
			reportAmbiguous( scoresRemain, "Exon" );
			SerialUtil.writeObject(mappings, exonMappingFileName);
	
		} else {
	
			System.out.println("Using existing exon internal ID mappings in "
					+ exonMappingFileName);
			mappings = (List) SerialUtil.readObject(exonMappingFileName);	
		}
	
		if (debug) {
		    Entry.writeToFile(mappings, conf.rootDir + File.separator+"exon_mappings.txt");
		}
	
		return mappings;
	
	}


	// -------------------------------------------------------------------------
	/**
	 * Map translation internal IDs based on transcript mappings.
	 * 
	 * @return A list of Entry objects containing the mappings.
	 */
	public List cachedTranslationMapping(List transcriptMappings) {
	
		List translationMappings = new ArrayList();
	
		// read from file if already done
		String translationMappingFileName = conf.rootDir + File.separator
				+ "translation_mappings.ser";
		File f = new File(translationMappingFileName);
		if (!f.exists()) {
	
			int transcriptsWithoutTranslations = 0;
	
			Iterator it = transcriptMappings.iterator();
			while (it.hasNext()) {
	
				Entry transcriptEntry = (Entry) it.next();
				Long sourceTranscriptID = new Long(transcriptEntry.getSource());
				Translation sourceTranslation = (Translation) cache
						.getSourceTranslationsByTranscriptInternalID().get(
								sourceTranscriptID);
				Long targetTranscriptID = new Long(transcriptEntry.getTarget());
				Translation targetTranslation = (Translation) cache
						.getTargetTranslationsByTranscriptInternalID().get(
								targetTranscriptID);
	
				// avoid storing translation mappings for transcripts that have
				// no translation (e.g. pseudogenes)
				if (sourceTranslation != null && targetTranslation != null) {
	
					// note the score in the translation mapping Entry is the
					// corresponding TRANSCRIPT score
					Entry translationEntry = new Entry(sourceTranslation
							.getInternalID(),
							targetTranslation.getInternalID(), transcriptEntry
									.getScore());
					translationMappings.add(translationEntry);
	
				} else {
	
					transcriptsWithoutTranslations++;
				}
	
			}
	
			System.out.println("Skipped " + transcriptsWithoutTranslations
					+ " transcripts without translations");
	
			SerialUtil.writeObject(translationMappings,
					translationMappingFileName);
	
		} else {
	
			System.out
					.println("Using existing translation internal ID mappings in "
							+ translationMappingFileName);
			translationMappings = (ArrayList) SerialUtil
					.readObject(translationMappingFileName);
	
		}
	
		return translationMappings;
	
	}


	private List basicMapping( ScoredMappingMatrix scores ) {

		ArrayList mappings = new ArrayList();

    // track which source and target items have been mapped
		LongSet sourcesDone = new LongSet();
		LongSet targetsDone = new LongSet();

    // buffers containing alternative (ambiguous) mappings related to current entry
		ArrayList otherSources = new ArrayList();
		ArrayList otherTargets = new ArrayList();
		
		Iterator i = scores.getSortedEntries().iterator();
		while( i.hasNext() ) {

			Entry e = (Entry) i.next();
			
			if( sourcesDone.contains( new Long( e.source )) ||
					targetsDone.contains( new Long( e.target ))) {
				continue;
			}			
			
			if( higherScoresExist( e, scores, sourcesDone, targetsDone )) {
				// higher scores for any of e exist, cant map this one
				continue;
			}

      otherTargets.clear();
      otherSources.clear();
			if( ambiguousMappingsExist( e, scores, otherSources, otherTargets )) {
				// dealing with real ambiguous ones is deferred

				filterSources( otherSources, sourcesDone );
				filterTargets( otherTargets, targetsDone );

        // still ambiguous
				if( otherSources.size() != 0 || otherTargets.size() != 0 ) 
					continue;

			}

			mappings.add( e );

			sourcesDone.add( new Long( e.source ));
			targetsDone.add( new Long( e.target ));
			
		}
		
		return mappings;
	}

	private void filterSources( List sourceEntries, LongSet sourcesToFilter ) {
    
    if (sourceEntries.size()==0 || sourcesToFilter.size()==0)
      return;
    
    Iterator i = sourceEntries.iterator();
		while( i.hasNext()) {
			Entry e = (Entry) i.next();
			if( sourcesToFilter.contains(e.source))
				i.remove();
			
		}
	}
	




  private void filterTargets( List targetEntries, LongSet targetsToFilter ) {
    
    if (targetEntries.size()==0 || targetsToFilter.size()==0)
      return;
    
    Iterator i = targetEntries.iterator();
		while( i.hasNext()) {
			Entry e = (Entry) i.next();
			if( targetsToFilter.contains(e.target)) 
				i.remove();
			
		}
	}
	
 
  
  /**
	 * Although we do mapping from top score to bottom score
	 * there will be cases where there still exists higher scores
	 * for an entry making a mapping not correct. check this here.
	 * @param e
	 * @param scores
	 * @param sourcesDone
	 * @param targetsDone
	 * @return
	 */
	private boolean higherScoresExist( Entry e, ScoredMappingMatrix scores, HashSet sourcesDone, HashSet targetsDone ) {

		long[] targets = scores.getTargetsForSource(e.getSource());
		Entry otherEntry = null;
		
		for (int i = 0; i < targets.length; i++) {
			otherEntry = scores.getEntry(e.getSource(), targets[i]);
			if (targets[i] != e.getTarget() &&
					!targetsDone.contains( new Long( otherEntry.target )) &&
					e.score < otherEntry.score ) {
				return true;
			}
		}

		long[] sources = scores.getSourcesForTarget(e.getTarget());
		for (int i = 0; i < sources.length; i++) {
			otherEntry = scores.getEntry(sources[i], e.getTarget());
			if (sources[i] != e.getSource() &&
					!sourcesDone.contains( new Long( otherEntry.source )) &&
					e.score < otherEntry.score ) {
				return true;
			}
		}

		return false;
	}
	
	// slightly modified basic Mapper that maps traanscript that are ambiguous 
	// but only within one gene..
	
	private List sameGeneTranscriptMapping( ScoredMappingMatrix transcriptScores ) {
		ArrayList transcriptMappings = new ArrayList();
		
		LongSet sourcesDone = new LongSet();
		LongSet targetsDone = new LongSet();
	
		// find the gene for the transcript
		OpenLongObjectHashMap sTr2Ge, tTr2Ge;
		sTr2Ge = cache.getSourceGeneByTranscriptInternalID();
		tTr2Ge = cache.getTargetGeneByTranscriptInternalID();

		Iterator i;
		ArrayList otherTargets, otherSources;
		otherSources = new ArrayList();
		otherTargets = new ArrayList();
		HashSet sourceGenes = new HashSet();
		HashSet targetGenes = new HashSet();
		
		i = transcriptScores.getSortedEntries().iterator();
		while( i.hasNext() ) {
			Entry e = (Entry) i.next();
			
			if( sourcesDone.contains( new Long( e.source )) ||
					targetsDone.contains( new Long( e.target ))) {
				continue;
			}			
			
			if( ambiguousMappingsExist( e, transcriptScores, otherSources, otherTargets )) {
				// dealing with real ambiguous ones is deferred

				filterSources( otherSources, sourcesDone );
				filterTargets( otherTargets, targetsDone );

				sourceGenes.add( sTr2Ge.get( e.source ));
				targetGenes.add( tTr2Ge.get( e.target ));
				
				for( int j = otherSources.size(); j-->0; ) {
					Entry entry = (Entry) otherSources.get(j);
					sourceGenes.add( sTr2Ge.get( entry.source ));
				}
				for( int j = otherTargets.size(); j-->0; ) {
					Entry entry = (Entry) otherTargets.get(j);
					targetGenes.add( tTr2Ge.get( entry.target ));
				}
				
				// act if only one source and target gene involved
				if( sourceGenes.size() == 1 && targetGenes.size() == 1) {
					transcriptMappings.add( e );
				}
			} else {
				transcriptMappings.add( e );				
			}
			
			sourceGenes.clear();
			targetGenes.clear();
			otherSources.clear();
			otherTargets.clear();
			sourcesDone.add( new Long( e.source ));
			targetsDone.add( new Long( e.target ));
		}
		return transcriptMappings;
	}
	
	
	private boolean ambiguousMappingsExist(Entry e, ScoredMappingMatrix scores, List otherSources,
			List otherTargets ) {

		// iterate over related sources and targets
		// if any has a score similar (within MAPPING_THRESHOLD of e's score)
		// it is ambiguous and not considered.
		long[] targets = scores.getTargetsForSource(e.getSource());
		Entry otherEntry = null;
		boolean result = false;
		
		for (int i = 0; i < targets.length; i++) {
			otherEntry = scores.getEntry(e.getSource(), targets[i]);
			if (targets[i] != e.getTarget() &&
				( scoresSimilar(e.getScore(), otherEntry.getScore()) ||
				 e.score < otherEntry.score )) {
				result = true;
				if( otherTargets != null ) 
					otherTargets.add( otherEntry );
			}
		}

		long[] sources = scores.getSourcesForTarget(e.getTarget());
		for (int i = 0; i < sources.length; i++) {
			otherEntry = scores.getEntry(sources[i], e.getTarget());
			if (sources[i] != e.getSource() &&
				( scoresSimilar(e.getScore(), otherEntry.getScore())) ||
				e.score < otherEntry.score ) {
				result = true;
				if( otherSources != null )
					otherSources.add( otherEntry );
			}
		}

		return result;
	}

	// -------------------------------------------------------------------------

	private boolean scoresSimilar(float s1, float s2) {

		// always allow for exact match to be mapped in favor of very similar
		// match
		if (s1 == 1.0f && s2 < 1.0f) {
			return false;
		}

		float diff = Math.abs(s1 - s2);

		float pc = (2.0f * diff) / (s1 + s2);

		return pc < SIMILAR_SCORE_RATIO;

	}

	public void reportAmbiguous( ScoredMappingMatrix scores, String type ) {
		
		List entries = scores.getAllEntries();
		Collections.sort( entries, new EntrySourceComparator( new EntryTargetComparator()));
		ArrayList lowScoring = new ArrayList();
		ArrayList highScoring = new ArrayList();
		
		OutputStreamWriter ambiOut;
		try {
			ambiOut = new OutputStreamWriter(
					new FileOutputStream( conf.debugDir + File.separator
							+ "ambiguous_" + type + ".txt" ));

			Iterator i = entries.iterator();
			long lastId = -1;
			while( i.hasNext()) {
				Entry e = (Entry) i.next();
				if( lastId != e.source ) {
					if( lastId != -1 ) {
						// report from lowScoring and highScoring 
						reportAmbiguous( type, ambiOut, lowScoring, highScoring, true );
					}
					lastId = e.source;
				}
				if( e.score < 0.5f ) {
					lowScoring.add( e );
				} else {
					highScoring.add( e );
				}
			}
			if( lastId != -1 ) {
				reportAmbiguous( type, ambiOut, lowScoring, highScoring, true );
			}
			
			Collections.sort( entries, new EntryTargetComparator( new EntrySourceComparator()));
			i = entries.iterator();
			lastId = -1;
			while( i.hasNext()) {
				Entry e = (Entry) i.next();
				if( lastId != e.target ) {
					if( lastId != -1 ) {
						// report from lowScoring and highScoring 
						reportAmbiguous( type, ambiOut, lowScoring, highScoring, false );
					}
					lastId = e.target;
				}
				if( e.score < 0.5f ) {
					lowScoring.add( e );
				} else {
					highScoring.add( e );
				}
			}
			if( lastId != -1 ) {
				reportAmbiguous( type, ambiOut, lowScoring, highScoring, false );
			}
			ambiOut.close();
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	private void reportAmbiguous( String type, OutputStreamWriter out, List lowScoring, List highScoring, 
			boolean source ) throws IOException {

		// If only source or only target is ambiguous, only report from that side
		// Ignore from here	
		if( lowScoring.size() + highScoring.size() <= 1 ) {
			lowScoring.clear();
			highScoring.clear();
			return;
		}
		Entry e;
		
		if( lowScoring.size() > 0 ) {
			e = (Entry) lowScoring.get(0);
		} else {
			e = (Entry) highScoring.get(0);
		}
		long singleId = source?e.source:e.target;
		
		out.write(( source?"Source ":"Target ") + type + " " + singleId + " scores ambiguous \n" );
		if( highScoring.size() > 0 ) {
			out.write( "  High scoring " + (source?"Target":"Source") + "\n");
			Iterator i = highScoring.iterator();
			while( i.hasNext()) {
				e = (Entry) i.next();
				out.write( "     " + type + " " + (source?e.target:e.source) + " Score: " + e.score + "\n" );
			}
		}	

		if( lowScoring.size() > 0 ) {
			int counter = 0;
			out.write( "  Low scoring " + type + " " );
			Iterator i = lowScoring.iterator();
			while( i.hasNext()) {
				e = (Entry) i.next();
				out.write( "" +(source?e.target:e.source) );
				counter++;
				if( i.hasNext()) {
					out.write( ", " );
					if( counter%10 == 0 ) {
						out.write( "\n            ");
					}
				}
			}
		}
		out.write( "\n" );

		lowScoring.clear();
		highScoring.clear();
	}
	
	//  -------------------------------------------------------------------------
	/**
	 * Prints argument string if the programms debug mode is enabled
	 *  
	 */
	private void debug(String s) {

		if (debug) {
			System.out.println(s);
		}

	}

	// -------------------------------------------------------------------------
}
