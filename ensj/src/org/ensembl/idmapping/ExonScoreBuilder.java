/*
 * Created on Aug 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.ensembl.idmapping;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ensembl.datamodel.Transcript;
import org.ensembl.util.SerialUtil;

import cern.colt.map.OpenLongObjectHashMap;

/**
 * @author arne
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExonScoreBuilder extends ScoreBuilder {
	
	public Config conf;
	private boolean debug = true;
	
	public ExonScoreBuilder( Config conf, Cache cache ) {
		super( cache );
		this.conf = conf;
	}
	
	/**
	 * Build exon scoring matrix based on overlap and/or sequence matching using
	 * exonerate.
	 */
	public ScoredMappingMatrix scoreExons(Config conf) {

		ScoredMappingMatrix exonScoringMatrix = new ScoredMappingMatrix();

		// read from file if already done
		String exonScoreFileName = conf.rootDir + File.separator + "exon_scores.ser";
		File f = new File(exonScoreFileName);
		if (!f.exists()) {

			System.out
					.println("Did not find existing exon score matrix, will build a new one");
			ExonDirectMapper exonDirectMapper = new ExonDirectMapper(conf, cache);

			try {

				if (exonDirectMapper.mappedComparable()) {

					debug("Building exon overlap scores");
					exonDirectMapper.buildOverlapScoring();
					// Retrieve source exons, store projections to common
					// coordinate system. Retrieve target exons. Store
					// projection. Find overlaps.
					exonScoringMatrix = exonDirectMapper.getScoringMatrix();

				} else {

					// no direct exon mapping applicable
					System.out
							.println("No direct exon mapping - will only use exonerate (if enabled)");

				}

			} catch (Exception e) {

				System.err.println("Exception doing exon overlap scoring: ");
				e.printStackTrace();

			}

			// dump low-scoring exons to a FASTA file and map using exonerate
			if (useExonerate()) {

				System.out
						.println("\n----- Generating exon scores using exonerate -----");
				debug("Dumping exons to FASTA files for exonerate");
				boolean sourceAndTargetExonsInFastFiles = new ExonDumper().dumpFilteredExons(cache, exonScoringMatrix,
						conf.rootDir);
                if (sourceAndTargetExonsInFastFiles) {
                  debug("Running exonerate");
                  ScoredMappingMatrix exonerateScoringMatrix = new ExonerateRunner()
                  .run( conf.rootDir);
                  debug("Exonerate scoring matrix size: "
                      + exonerateScoringMatrix.getEntryCount());
                  System.out
                  .print("Combining direct and exonerate scores; size & average before "
                      + exonScoringMatrix.getEntryCount()
                      + " "
                      + exonScoringMatrix.getAverageScore());
                  exonScoringMatrix.combineWith(exonerateScoringMatrix);
                  exonerateScoringMatrix = null;
                  System.out.println(" after "
                      + exonScoringMatrix.getEntryCount() + " "
                      + exonScoringMatrix.getAverageScore());
                } else {
                  debug("Skipping exonerate as one or both exon fasta files are empty.");
                }
			}

			SerialUtil
			    .writeObject(exonScoringMatrix, exonScoreFileName);
			debug("Wrote exon scoring matrix to " + exonScoreFileName);
		} else {

			System.out.println("Using existing exon score matrix in "
					+ exonScoreFileName);
			exonScoringMatrix = (ScoredMappingMatrix) SerialUtil
					.readObject(exonScoreFileName);

		}

		if (debug) {
			exonScoringMatrix.dumpToFile(conf.debugDir, "exon_scores.txt");
		}

		debug(exonScoringMatrix.toString());

		return exonScoringMatrix;

	}
	
	// -------------------------------------------------------------------------
	/**
	 * @return true if the source and target exons belong to transcripts that
	 *         have mappings.
	 */
	public void punishNonMappedTranscript( ScoredMappingMatrix exonScores, List transcriptMappings ) {
		// make a temporary map of transcript mappings.
		Map tmpTranscriptMappings = new HashMap();

		Iterator it = transcriptMappings.iterator();
		while (it.hasNext()) {
			Entry e = (Entry) it.next();
			tmpTranscriptMappings.put(new Long(e.getSource()), new Long(e
					.getTarget()));
		}

		Iterator i = exonScores.getAllEntries().iterator();
		while( i.hasNext() ) {
			Entry e = (Entry) i.next();
			if( ! exonsTranscriptsMap( e, tmpTranscriptMappings )) {
				e.score *= 0.8f;
			}
		}
	}
	
	
	private boolean exonsTranscriptsMap(Entry e, Map transcriptMappings ) {
		
		OpenLongObjectHashMap targetTranscriptsByExonID = cache.getTargetTranscriptsByExonInternalID();
		OpenLongObjectHashMap sourceTranscriptsByExonID = cache.getSourceTranscriptsByExonInternalID();
		
		List sourceTranscripts = (List) sourceTranscriptsByExonID.get( e.getSource( ));
		List targetTranscripts = (List) targetTranscriptsByExonID.get( e.getTarget( ));

		Iterator stit = sourceTranscripts.iterator();
		while (stit.hasNext()) {

			Transcript sourceTranscript = (Transcript) stit.next();
			Iterator ttit = targetTranscripts.iterator();
			while (ttit.hasNext()) {

				Transcript targetTranscript = (Transcript) ttit.next();
				Long target = (Long) transcriptMappings.get(new Long(
						sourceTranscript.getInternalID()));
				if (target != null) {
					if (target.longValue() == targetTranscript.getInternalID()) {
						return true;
					}
				}
			}
		}

		return false;
	}


	/**
	 * The system should always use exonerate ...
	 * TODO change this
	 * @return
	 */
	private boolean useExonerate() {

		String prop = System.getProperty("idmapping.use_exonerate");
		if (prop != null) {
			prop = prop.toLowerCase();
			if (prop.equals("yes") || prop.equals("true") || prop.equals("1")) {
				return true;
			}
		}

		return false;
	}


	private void debug( String mesg ) {
		if( debug ) 
			System.out.println( mesg );	
	}
}
