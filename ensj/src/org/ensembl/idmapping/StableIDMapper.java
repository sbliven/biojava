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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ensembl.datamodel.Accessioned;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Sequence;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.Translation;
import org.ensembl.driver.impl.BaseAdaptor;
import org.ensembl.util.AscendingInternalIDComparator;
import org.ensembl.util.StringUtil;
import org.ensembl.util.Util;

import cern.colt.Sorting;
import cern.colt.list.ObjectArrayList;
import cern.colt.map.OpenLongObjectHashMap;

/**
 * Map stable IDs based on internal ID mappings, and generate stable ID events.
 */

public class StableIDMapper {

	private String rootDir;

	// stable id events created during this session
	private Map createdStableIDEvents = new HashMap();

	private Map similarityStableIDEvents = new HashMap();
  
  private List retrofitGenes = new ArrayList();
  private List retrofitTranscripts = new ArrayList();
  private List retrofitTranslations = new ArrayList();

	// Map of Lists of old new stableID mappings for debugging
	// Keyed on type (exon, transcript, translation, gene)
	private Map debugMappings = new HashMap();

	// mapping session ID used for this session
	private long currentMappingSessionID = 0;

  // mapping session for retrofitted similarities
  private long retrofitMappingSessionID = Long.MIN_VALUE;

	private Date mappingSessionDate;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

	private Config conf;

	private Cache cache;

	public StableIDMapper(Config conf, Cache cache) {

		this.rootDir = conf.rootDir;
		this.conf = conf;
		this.cache = cache;
	}

	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------

	/**
	 * Generate the mapping session ID being used (stored internally); initialise
	 * it if necessary. The mapping_session table, with a row for the new session,
	 * is written to mapping_session.txt
	 */
	public void generateNewMappingSessionID() {

		// just do this once
		if (mappingSessionDate != null)
			return;

		mappingSessionDate = new Date();

		try {

			// get the current mapping session ID - max existing + 1
			Connection sourceCon = conf.getSourceConnection();

			String sql = "SELECT MAX(mapping_session_id) FROM mapping_session";

			ResultSet rs = BaseAdaptor.executeQuery(sourceCon, sql);
			if (rs.next()) {

				currentMappingSessionID = rs.getInt(1) + 1;

			} else {

				System.out.println("Cannot get maximum mapping_session_id - mapping_session table empty?");
				currentMappingSessionID = 1;

			}

			rs.close();

			// allow manual specification of mapping session ID
			String manual = System.getProperty("idmapping.mapping_session_id");
			if (manual != null) {
				System.out.println("Using manually-specified mapping_session_id " + manual);
				currentMappingSessionID = Integer.parseInt(manual);
			}

			System.out.println("Mapping session ID for this session is " + currentMappingSessionID);
      
      OutputStreamWriter writer;
      // write the existing mapping session data and the new one to
			try {

        writer = new OutputStreamWriter(new FileOutputStream(rootDir + File.separator + "mapping_session.txt"));

				// existing
				rs = sourceCon.createStatement().executeQuery("SELECT * FROM mapping_session");
				int n = 0;
				while (rs.next()) {
					if (n == 0)
						n = rs.getMetaData().getColumnCount();
					for (int i = 0; i < n; i++) {
						if (i > 0)
							writer.write("\t");
						writer.write(rs.getString(i + 1));
					}
					writer.write("\n");

				}
				rs.close();

				// new
				writer.write(currentMappingSessionID + "\t" + System.getProperty("idmapping.source.database") + "\t"
						+ System.getProperty("idmapping.target.database") + "\t" + conf.getSourceDriver().fetchDatabaseSchemaVersion() + "\t"
						+ conf.getTargetDriver().fetchDatabaseSchemaVersion() + "\t"
						+ conf.getSourceDriver().fetchMetaValues("assembly.default").get(0) + "\t"
						+ conf.getTargetDriver().fetchMetaValues("assembly.default").get(0) + "\t" + dateFormat.format(mappingSessionDate)
						+ "\n");

				writer.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {

			System.err.println("Error getting new mapping session");
			e.printStackTrace();
		}

		System.out.println("Updated mapping_session table written to mapping_session.txt");
	}

	// -------------------------------------------------------------------------
	/**
	 * Generate stable IDs for targets. Where a source has been mapped to a
	 * target, the stable ID of the target is the same as the stable ID of the
	 * source. For new targets, a new stable ID is assigned. Stable ID events for
	 * mappings, and for stable ID creation and deletion are produced.
	 * 
	 * @param sourcesByInternalID
	 *          All the source objects.
	 * @param targetsByInternalID
	 *          All the target objects.
	 * @param mappings
	 *          A List of targets with their accession IDs and versions filled in.
	 * @param objectType
	 *          The tpye of object (gene, transcript, translation or exon) being
	 *          mapped.
	 */
public ObjectArrayList mapStableIDs(OpenLongObjectHashMap sourcesByInternalID, OpenLongObjectHashMap targetsByInternalID, List mappings, String objectType) {

    		generateNewMappingSessionID();
    		
        ObjectArrayList allSources = sourcesByInternalID.values();
	if (allSources.size() == 0) { // e.g. ncRNA-only databases have 0 translations
	    System.out.println("No stable IDs, returning empty list");
	    return targetsByInternalID.values();
	}
        String type = getTypeFromObject(allSources.get(0));

        int[] mappedCount = {0, 0}; // known, novel
        int newCount = 0;
        int[] lostCount = {0, 0}; // known, novel

        // cache mappings
        // OPTIMISE - could use OpenLongObjectHashMap.
        Map sourcesMapped = new HashMap();
        Map targetsMapped = new HashMap();
        OpenLongObjectHashMap target2Entry = new OpenLongObjectHashMap();
        Iterator mIt = mappings.iterator();
        while (mIt.hasNext()) {
            Entry entry = (Entry) mIt.next();
            sourcesMapped.put(new Long(entry.getSource()), new Long(entry.getTarget()));
            targetsMapped.put(new Long(entry.getTarget()), new Long(entry.getSource()));
            target2Entry.put(entry.target, entry);
        }

        // assign existing or new stable IDs to TARGET exons

        // transfer stable ID from source->target for mapped objects
        final ObjectArrayList allTargets = targetsByInternalID.values();
        final int allTargetsLen = allTargets.size();
        for(int i=0; i<allTargetsLen; ++i) {

            Accessioned target = (Accessioned) allTargets.getQuick(i);
            Long targetID = new Long(target.getInternalID());
            if (targetsMapped.containsKey(targetID)) {

                Long sourceID = (Long) targetsMapped.get(targetID);
                Accessioned source = (Accessioned) sourcesByInternalID.get(sourceID.longValue());
                
                // set target's stable ID to be the same as the source's stable
                // ID
                target.setAccessionID(source.getAccessionID());
                //assert source.getCreatedDate()!=null: "source has a null creation date "+ source;
                target.setCreatedDate( source.getCreatedDate());
                
                // version calculation is different for exons, transcripts,
                // genes
                int version = calculateNewVersion(source, target);
                if( version != source.getVersion()) {
                		target.setModifiedDate( mappingSessionDate );
                } else {
                		target.setModifiedDate( source.getModifiedDate());
                }
                target.setVersion(version);
		// System.out.println("Set version of " + target.getAccessionID() + " to " +
		// target.getVersion());

                // generate a "mapped" stable ID event
                if (!type.equals("exon")) {
                  float score = ((Entry)target2Entry.get(target.getInternalID())).score;
                    addStableIDEvent(new StableIDEventRow(source.getAccessionID(), source.getVersion(), target
                            .getAccessionID(), target.getVersion(), getTypeFromObject(target), score, currentMappingSessionID));
                }

                // add this mapping to the list for debugging purposes
                addDebugMapping(type, source.getInternalID(), target.getInternalID(), target.getAccessionID());

                // update the statistics
                mappedCount = updateCount(source, mappedCount);

            }

        }

        // assign new stable IDs to targets that weren't mapped
        // find the highest mapped stable ID, and use it as the base for
        // assigning new ones
        String newID = findHighestStableID(objectType);

        for(int i=0; i<allTargetsLen; i++) {

            Accessioned target = (Accessioned) allTargets.getQuick(i);
            Long targetID = new Long(target.getInternalID());
            if (!targetsMapped.containsKey(targetID)) {
                // not mapped - generate new stable ID
                newID = incrementStableID(newID);
                target.setAccessionID(newID);
                target.setCreatedDate( mappingSessionDate );
                target.setModifiedDate( mappingSessionDate );
                
                // new stable ID, so version is 1
                target.setVersion(1);

                // generate a "new" stable ID event
                if (!type.equals("exon")) {
                    addStableIDEvent(new StableIDEventRow(null, 0, target.getAccessionID(), target.getVersion(),
                            getTypeFromObject(target), 0, currentMappingSessionID));
                }

                // update the statistics
                newCount++;
            }
        }

        // find sources that weren't mapped, mark as lost
        final int allSourcesLen = allSources.size();
        for (int i = 0; i < allSourcesLen; i++) {
            Accessioned source = (Accessioned) allSources.getQuick(i);

            if (!sourcesMapped.containsKey(new Long(source.getInternalID()))) {

                // generate a "lost" stable ID event
                if (!type.equals("exon")) {
                    addStableIDEvent(new StableIDEventRow(source.getAccessionID(), source.getVersion(), null, 0,
                            getTypeFromObject(source), 0, currentMappingSessionID));
                }

                // update the statistics
                lostCount = updateCount(source, lostCount);

            }
        }

        Sorting.quickSort(allTargets.elements(), new AscendingInternalIDComparator());
        
        String fileName = rootDir + File.separator + type + "_stable_id.txt";
        dumpStableIDsToFile(allTargets, fileName);
        System.out.println("Wrote " + allTargets.size() + " " + type + "s to " + fileName);

        String stats = generateMappingStatistics(type, mappedCount, lostCount, newCount);
        System.out.println("\n" + stats);
        writeStringToFile(stats, rootDir + File.separator + type + "_mapping_statistics.txt");

        return allTargets;

    }
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------

	/**
	 * Generate similarity events for split / merge / lost cases. Go through all
	 * the mappings and find if there are similar scores for source and target. Go
	 * through all unmapped sources and targets and create similarity events for
	 * top scorers - over 50% - within 2 percent of the top score
	 * 
	 * @param cache
	 * @param mappings
	 * @param scores
	 */
	public void generateSimilarityEvents(List mappings, ScoredMappingMatrix scores, String type) {

		Iterator i = mappings.iterator();
		ArrayList similarities = new ArrayList();
		// first similarities for mapped entries

		while (i.hasNext()) {
			Entry e = (Entry) i.next();
			Iterator j;
			boolean first = true;
			while (true) {
				if (first)
					j = scores.targetEntries(e.target).iterator();
				else
					j = scores.sourceEntries(e.source).iterator();
				while (j.hasNext()) {
					Entry e2 = (Entry) j.next();
					if (e2.source == e.source && e2.target == e.target)
						continue;
					if (e2.score > e.score * 0.98) {
						similarities.add(e2);
					}
				}

				if (!first) {
					break;
				} else {
					first = false;
				}
			}
		}

		// now similarities for others
		HashSet sourceSet = new HashSet();
		HashSet targetSet = new HashSet();
		i = mappings.iterator();
		while (i.hasNext()) {
			Entry e = (Entry) i.next();
			sourceSet.add(new Long(e.source));
			targetSet.add(new Long(e.target));
		}

		boolean sources = true;
		long[] ids;
		// 2 rounds, source and target
		while (true) {
			if (sources) {
				ids = scores.getAllSources();
			} else {
				ids = scores.getAllTargets();
			}

			for (int j = ids.length; j-- > 0;) {
				List entries;
				if (sources) {
					if (sourceSet.contains(new Long(ids[j])))
						continue;
					entries = scores.sourceEntries(ids[j]);
				} else {
					if (targetSet.contains(new Long(ids[j])))
						continue;
					entries = scores.targetEntries(ids[j]);
				}
				Collections.sort(entries, new EntryScoreReverseComparator());
				float topScore = ((Entry) entries.get(0)).score;
				if (topScore < 0.7)
					continue;
				Iterator k = entries.iterator();
				while (k.hasNext()) {
					Entry e = (Entry) k.next();
					if (e.score > 0.95 * topScore)
						similarities.add(e);
				}
			}

			if (sources) {
				sources = false;
			} else {
				break;
			}
		}

		// now create stableIdEventContainer objects
		long lastSource = -1;
		long lastTarget = -1;
		i = similarities.iterator();
		while (i.hasNext()) {
			Entry e = (Entry) i.next();
			if (e.source == lastSource && e.target == lastTarget)
				continue;
      
      
			lastSource = e.source;
			lastTarget = e.target;
			Accessioned oldAcc = null;
			Accessioned newAcc = null;
			
      if (type.equals("gene")) {
				oldAcc = (Gene) cache.getSourceGenesByInternalID().get(e.source);
				newAcc = (Gene) cache.getTargetGenesByInternalID().get(e.target);
        retrofitGenes.add(e);
			} else if (type.equals("transcript")) {
				oldAcc = (Transcript) cache.getSourceTranscriptsByInternalID().get(e.source);
				newAcc = (Transcript) cache.getTargetTranscriptsByInternalID().get(e.target);
        retrofitTranscripts.add(e);
			} else if (type.equals("translation")) {
				oldAcc = (Translation) cache.getSourceTranslationsByInternalID().get(e.source);
				newAcc = (Translation) cache.getTargetTranslationsByInternalID().get(e.target);
        retrofitTranslations.add(e);
			}

			StableIDEventRow sidec = new StableIDEventRow(oldAcc.getAccessionID(), oldAcc.getVersion(), newAcc
					.getAccessionID(), newAcc.getVersion(), type, e.score, currentMappingSessionID);
      similarityStableIDEvents.put(sidec.getKey(), sidec);

		}
	}
  

  
  public ScoredMappingMatrix filterSameGeneTransciptSimilarities(ScoredMappingMatrix transcriptScoringMatrix) {
    
    // Remove similarities between transcripts in the same gene. This makes the website history
    // simpler.
    
    ScoredMappingMatrix matrix = new ScoredMappingMatrix();
    
    Set mappedAndNew = new HashSet();
    ObjectArrayList ts = cache.getTargetTranscriptsByInternalID().values();
    for (int i = 0; i < ts.size(); i++) {
      Transcript t = (Transcript) ts.get(i);
      mappedAndNew.add(t.getAccessionID());
    }

    List entries = transcriptScoringMatrix.getAllEntries();
    for (int i = 0; i < entries.size(); i++) {
      Entry e = (Entry) entries.get(i);
      
      Transcript srcTranscript = (Transcript) cache.getSourceTranscriptsByInternalID().get(e.source);
      Transcript tgtTranscript = (Transcript) cache.getTargetTranscriptsByInternalID().get(e.target);
      
      // By this stage transcript.gene.accessoinID should be set so we can simply compare the
      // respective gene accessions.
      // We keep links from deleted transcripts.
      if (srcTranscript.getGene().getAccessionID().equals(tgtTranscript.getGene().getAccessionID())
          && mappedAndNew.contains(srcTranscript.getAccessionID()))
        continue;
      
      // create copy to avoid potential dependency issues with original matrix  
      matrix.addEntry(e.copy()); 
    }

    return matrix;
  }
  

	public void generateTranslationSimilarityEvents(List translationMappings, ScoredMappingMatrix transcriptScoringMatrix) {
		ScoredMappingMatrix translationScores = new ScoredMappingMatrix();
		// fake a score matrix for translations
		Map str2tl = cache.getSourceTranslationsByTranscriptInternalID();
		Map ttr2tl = cache.getTargetTranslationsByTranscriptInternalID();
		Iterator i = transcriptScoringMatrix.getAllEntries().iterator();
		while (i.hasNext()) {
			Entry e = (Entry) i.next();
			long stlid, ttlid;
			Translation sourceTranslation = (Translation) str2tl.get(new Long(e.source));
			Translation targetTranslation = (Translation) ttr2tl.get(new Long(e.target));
			if (sourceTranslation == null || targetTranslation == null)
				continue;
			stlid = sourceTranslation.getInternalID();
			ttlid = targetTranslation.getInternalID();
			translationScores.addScore(stlid, ttlid, e.score);
		}
		generateSimilarityEvents(translationMappings, translationScores, "translation");
	}

	public void dumpStableIDsToFile(ObjectArrayList objects, String outputFileName) {

		try {

			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFileName));

			final int n = objects.size();
			for (int i = 0; i < n; i++) {
				Accessioned obj = (Accessioned) objects.getQuick(i);
				Date cdate = obj.getCreatedDate();
				Date mdate = obj.getModifiedDate();
				if (cdate == null) {
					System.err.println("WARNING tgt.createdDate is null: " + obj);
					cdate = new Date(1);
				}
				if (mdate == null) {
					System.err.println("WARNING tgt.modifiedDate is null: " + obj);
					mdate = new Date(1);
				}
				writer.write(obj.getInternalID() + "\t" + obj.getAccessionID() + "\t" + obj.getVersion() + "\t" + dateFormat.format(cdate)
						+ "\t" + dateFormat.format(mdate) + "\n");

			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void dumpLostGeneAndTranscripts() {

		Map deletedGeneIDs = new HashMap();
		Map deletedTranscriptIDs = new HashMap();

		// iterate over all stable ID events
		// find any where target = null (deleted) - store in deletedStableIDs

		Iterator sideit = createdStableIDEvents.values().iterator();
		while (sideit.hasNext()) {

			StableIDEventRow sidec = (StableIDEventRow) sideit.next();
			if (sidec.getNewStableID() == null) {
				if (sidec.getType().equals("gene")) {
					deletedGeneIDs.put(sidec.getOldStableID(), "");
				} else if (sidec.getType().equals("transcript")) {
					deletedTranscriptIDs.put(sidec.getOldStableID(), "");
				}
			}
		}

		// iterate again over stable ID events
		// mark as merged those that have other non-"deleted" events

		sideit = createdStableIDEvents.values().iterator();
		while (sideit.hasNext()) {

			StableIDEventRow sidec = (StableIDEventRow) sideit.next();
			if (sidec.getNewStableID() != null) {
				if (sidec.getType().equals("gene")) {
					if (deletedGeneIDs.containsKey(sidec.getOldStableID())) {
						deletedGeneIDs.put(sidec.getOldStableID(), sidec.getNewStableID());
					}
				} else if (sidec.getType().equals("transcript")) {
					if (deletedTranscriptIDs.containsKey(sidec.getOldStableID())) {
						deletedTranscriptIDs.put(sidec.getOldStableID(), sidec.getNewStableID());
					}
				}
			}
		}

		// now dump them to appropriately-named files
		try {

			OutputStreamWriter deletedGenesWriter = new OutputStreamWriter(new FileOutputStream(rootDir + File.separator
					+ "genes_lost_deleted.txt"));
			OutputStreamWriter mergedGenesWriter = new OutputStreamWriter(new FileOutputStream(rootDir + File.separator
					+ "genes_lost_merged.txt"));
			OutputStreamWriter deletedTranscriptsWriter = new OutputStreamWriter(new FileOutputStream(rootDir + File.separator
					+ "transcripts_lost_deleted.txt"));
			OutputStreamWriter mergedTranscriptsWriter = new OutputStreamWriter(new FileOutputStream(rootDir + File.separator
					+ "transcripts_lost_merged.txt"));

			Iterator deletedGenesIterator = deletedGeneIDs.keySet().iterator();
			while (deletedGenesIterator.hasNext()) {
				String oldGeneID = (String) deletedGenesIterator.next();
				String newGeneID = (String) deletedGeneIDs.get(oldGeneID);
				Gene oldGene = (Gene) cache.getSourceGenesByStableID().get(oldGeneID);

				String knownNovel = oldGene.isKnown() ? "KNOWN" : "NOVEL";
				if (newGeneID.equals("")) { // deleted
					deletedGenesWriter.write(oldGeneID + "\t" + knownNovel + "\n");
				} else { // lost
					mergedGenesWriter.write(oldGeneID + "\t" + newGeneID + "\t" + knownNovel + "\n");
				}
			}

			Iterator deletedTranscriptsIterator = deletedTranscriptIDs.keySet().iterator();
			while (deletedTranscriptsIterator.hasNext()) {
				String oldTranscriptID = (String) deletedTranscriptsIterator.next();
				String newTranscriptID = (String) deletedTranscriptIDs.get(oldTranscriptID);
				Transcript oldTranscript = (Transcript) cache.getSourceTranscriptsByStableID().get(oldTranscriptID);
				String knownNovel = oldTranscript.isKnown() ? "KNOWN" : "NOVEL";
				if (newTranscriptID.equals("")) { // deleted
					deletedTranscriptsWriter.write(oldTranscriptID + "\t" + knownNovel + "\n");
				} else { // lost
					mergedTranscriptsWriter.write(oldTranscriptID + "\t" + newTranscriptID + "\t" + knownNovel + "\n");
				}
			}

			deletedGenesWriter.close();
			mergedGenesWriter.close();
			deletedTranscriptsWriter.close();
			mergedTranscriptsWriter.close();

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	// ---------------------------------------------------------------------
	/**
	 * Find the highest currently-assigned stable ID. Alternatively use the
	 * idmapping.starting.stable_id property if it is set
   *
	 * @param type
	 *          The type of objects being mapped.
	 * 
	 * @return The highest currently-assigned stable ID.
	 */
private String findHighestStableID(String type) {

		// check if property is set
		String maxStableID = System.getProperty("idmapping.starting." + type + ".stable_id");
		if (maxStableID != null) {
			System.out.println("Using " + maxStableID + " as base for new " + type + " stable IDs");
			return maxStableID;
		}
		
		// otherwise search for maximum; done by a query in case only a subset of
		// biotypes are in the
		// cache - still need to search the whole source database to avoid clashes
		maxStableID = "";

    try {

        Statement stmt = conf.getSourceConnection().createStatement();
        ResultSet rs = stmt.executeQuery("SELECT MAX(stable_id) FROM " + type + "_stable_id");
        if (rs.next()) {
            maxStableID = rs.getString(1);
            System.out.println("Highest existing " + type + " stable ID in whole source database is " + maxStableID);
        } else {
            System.out.println("Can't find highest " + type + " stable ID in source database");
        }
        rs.close();
        stmt.close();

    } catch (Exception e) {
        e.printStackTrace();
    }
    
		return maxStableID;

	}
	// ---------------------------------------------------------------------
	/**
	 * Increment by 1 a stable ID, regardless of its prefix. Numeric part of
	 * accession ID will have exactly 11 digits.
	 */
	private String incrementStableID(String stableID) {

		int prefixLength = org.ensembl.util.StringUtil.indexOfFirstDigit(stableID);
		String prefix = stableID.substring(0, prefixLength);
		long number = Long.parseLong(stableID.substring(prefixLength, stableID.length()));
		number++;

		// Pad with 0 if necessary
		DecimalFormat min11Digits = new DecimalFormat();
		min11Digits.setMinimumIntegerDigits(11);
		// Stop ',' being inserted between digits!
		min11Digits.setGroupingSize(min11Digits.getMinimumIntegerDigits() + 1);

		return prefix + min11Digits.format(number);

	}

	// -------------------------------------------------------------------------

	/**
	 * Calculate new version for target object based on source object.
	 */
	private int calculateNewVersion(Accessioned source, Accessioned target) {

		int version = source.getVersion();

		if (source instanceof Exon && target instanceof Exon) {

			// EXONS - increment version if sequence changed, otherwise keep the
			// same
			if (!((Exon) source).getSequence().getString().equals(((Exon) target).getSequence().getString())) {
				version++;
			}

		} else if ((source instanceof Transcript && target instanceof Transcript)) {

			// TRANSCRIPTS - if spliced sequence of exons changed, increment
			// version
			Sequence sourceSequence = ((Transcript) source).getSequence();
			Sequence targetSequence = ((Transcript) target).getSequence();
			if (sourceSequence != null && targetSequence != null) {
				if (!sourceSequence.getString().equals(targetSequence.getString())) {
					version++;
				}
			}

		} else if (source instanceof Translation && target instanceof Translation) {

			// TRANSLATIONS - increment version if transcripts changed
			Transcript sourceTranscript = ((Translation) source).getTranscript();
			Transcript targetTranscript = ((Translation) target).getTranscript();
			Sequence sourceSequence = sourceTranscript.getSequence();
			Sequence targetSequence = targetTranscript.getSequence();
			if (sourceSequence != null && targetSequence != null) {
				if (!sourceSequence.getString().equals(targetSequence.getString())) {
					version++;
				}
			}

		} else if (source instanceof Gene && target instanceof Gene) {

			// GENES - increment version if any transcript changes
			Set sourceTranscriptDescriptions = extractTranscriptAccessionAndVersions((Gene) source);
			Set targetTranscriptDescriptions = extractTranscriptAccessionAndVersions((Gene) target);
			if (!sourceTranscriptDescriptions.equals(targetTranscriptDescriptions)) {
				version++;
			}

		} else {
			System.err.println("Can't calculate version information for objects of type " + source.getClass().getName() + " "
					+ target.getClass().getName());
		}

		return version;

	}

	// -------------------------------------------------------------------------
	/**
	 * Creates a set of strings where each string = transcript.accessionID +
	 * transcript.version.
	 * 
	 * @param gene
	 *          The gene from which to extract the transcripts.
	 * @return set of strings representing each transcript.
	 */
	private Set extractTranscriptAccessionAndVersions(Gene gene) {

		Set descriptions = new HashSet();
		for (Iterator it = gene.getTranscripts().iterator(); it.hasNext();) {
			Transcript transcript = (Transcript) it.next();
			descriptions.add(transcript.getAccessionID() + transcript.getVersion());
		}

		return descriptions;

	}

	// -------------------------------------------------------------------------

	private String getTypeFromObject(Object o) {

		String type = "";

		if (o instanceof Exon) {

			type = "exon";

		} else if (o instanceof Transcript) {

			type = "transcript";

		} else if (o instanceof Translation) {

			type = "translation";

		} else if (o instanceof Gene) {

			type = "gene";

		} else {

			System.err.println("Cannot get type for " + o.toString());

		}

		return type;

	}

	// -------------------------------------------------------------------------

	public static void writeStringToFile(String str, String outputFileName) {

		try {

			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFileName));
			writer.write(str);

			writer.close();

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	// -------------------------------------------------------------------------
	/**
	 * Generate statistics for the mapping.
	 * 
	 * @param Type
	 *          the type of object being considered (gene, transcript,
	 *          translation, exon)
	 * @param mappedCount
	 *          Number of known (1st array entry) and novel (2nd array entry)
	 *          objects.
	 * @param lostCount
	 *          Number of known (1st array entry) and novel (2nd array entry)
	 *          objects.
	 * @param newCount
	 *          Number of new objects.
	 * @return A formatted String of results, or an empty string for exons.
	 */
	private String generateMappingStatistics(String type, int[] mappedCount, int[] lostCount, int newCount) {

		NumberFormat df = new DecimalFormat("##.##%");
		StringBuffer result = new StringBuffer();

		result.append(StringUtil.capitaliseFirstLetter(type) + " Mapping ResultsAnalysis for "
				+ System.getProperty("idmapping.source.database") + " -> " + System.getProperty("idmapping.target.database") + "\n\n");
		result.append("Type\tMapped\tLost\tPercentage\n");
		result.append("-----------------------------------\n");

		int totalMapped;
		int totalLost;

		// Exons aren't categorised into known/novel
		if (type.equalsIgnoreCase("exon")) {

			totalMapped = mappedCount[0];
			totalLost = lostCount[0];

		} else {
			// Genes, transcripts, translations can be known or novel
			for (int i = 0; i < 2; i++) {

				String s = (i == 0 ? "Known" : "Novel");
				float ratio = (float) mappedCount[i] / (float) (mappedCount[i] + lostCount[i]);
				result.append(s + "\t" + mappedCount[i] + "\t" + lostCount[i] + "\t" + df.format(ratio) + "\n");

			}

			totalMapped = mappedCount[0] + mappedCount[1];
			totalLost = lostCount[0] + lostCount[1];
		}

		float ratio = (float) totalMapped / (float) (totalMapped + totalLost);
		result.append("Total\t" + totalMapped + "\t" + totalLost + "\t" + df.format(ratio) + "\n");

		return result.toString();

	}

	// -------------------------------------------------------------------------

	private int[] updateCount(Accessioned object, int[] count) {

		int[] result = { count[0], count[1] };

		if (object instanceof Gene) {
			if (((Gene) object).isKnown()) {
				result[0]++;
			} else {
				result[1]++;
			}
		} else if (object instanceof Transcript) {
			if (((Transcript) object).isKnown()) {
				result[0]++;
			} else {
				result[1]++;
			}
		} else if (object instanceof Translation) {
			if (((Translation) object).isKnown()) {
				result[0]++;
			} else {
				result[1]++;
			}
		} else if (object instanceof Exon) {
			// Exons aren't known/novel, just use first array entry
			result[0]++;
		}

		return result;

	}

	private void addStableIDEvent(StableIDEventRow sidec) {

		createdStableIDEvents.put(sidec.getKey(), sidec);

	}

	// -------------------------------------------------------------------------

	public void writeNewStableIDEvents() {

		// make sure mapping session ID is set correctly
		Iterator it = createdStableIDEvents.values().iterator();
		while (it.hasNext()) {
			StableIDEventRow sidec = (StableIDEventRow) it.next();
			sidec.setMappingSessionID(currentMappingSessionID);
		}

		writeStableIDEvents(new ArrayList(createdStableIDEvents.values()), getCreatedStableIDEventFileName());

	}

	public void writeSimilarityIDEvents() {

		// make sure mapping session ID is set correctly
		Iterator it = similarityStableIDEvents.values().iterator();
		while (it.hasNext()) {
			StableIDEventRow sidec = (StableIDEventRow) it.next();
			sidec.setMappingSessionID(currentMappingSessionID);
		}

		writeStableIDEvents(new ArrayList(similarityStableIDEvents.values()), getSimilarityStableIDEventFileName());

	}

  public void writeRetrofitStableIDEvents() {

    Entry.writeToFile(retrofitGenes, getRetrofitSimilarityFileName("gene"));
    Entry.writeToFile(retrofitTranscripts, getRetrofitSimilarityFileName("transcript"));
    Entry.writeToFile(retrofitTranslations, getRetrofitSimilarityFileName("translation"));    
    
  }
  

	// -------------------------------------------------------------------------

	/**
	 * Write stable ID events to a file.
	 */
	public static void writeStableIDEvents(Collection events, String fileName) {

		try {

      final char sep = '\t';
      
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName));
			Iterator it = events.iterator();
			while (it.hasNext()) {

				StableIDEventRow sidec = (StableIDEventRow) it.next();
				String oldSID = (sidec.getOldStableID() == null) ? "\\N" : sidec.getOldStableID();
				String newSID = (sidec.getNewStableID() == null) ? "\\N" : sidec.getNewStableID();

        writer.write(oldSID);
        writer.write(sep);
        writer.write(Integer.toString(sidec.getOldVersion()));
        writer.write(sep);
        writer.write(newSID);
        writer.write(sep);
        writer.write(Integer.toString(sidec.getNewVersion()));
        writer.write(sep);
        writer.write(Long.toString(sidec.getMappingSessionID()));
        writer.write(sep);
        writer.write(sidec.getType());
        writer.write(sep);
        writer.write(Float.toString(sidec.getScore()));
        writer.write("\n");
			}

			writer.close();

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	private String getCreatedStableIDEventFileName() {

		return rootDir + File.separator + "stable_id_event_new.txt";

	}

	private String getSimilarityStableIDEventFileName() {

		return rootDir + File.separator + "stable_id_event_similarity.txt";

	}


  public String getRetrofitSimilarityFileName(String type) {

    return rootDir + File.separator + type +"_retrofit.txt";

  }
  
  
  public String getRetrofitStableIDEventFileName() {

    return rootDir + File.separator + "stable_id_event_retrofit.txt";

  }
  
  
 
 
  
  public String getRetrofitSQLPatchFileName() {

    return rootDir + File.separator + "stable_id_event_retrofit_patch.sql";

  }
  
  /**
   * Returns value of the configuration parameter idmapping.retrofit_mapping_session_id if set, 
   * otherwise -1.
   * 
   * @return idmapping.retrofit_mapping_session_id if set, otherwise -1.
   */
  public long getRetrofitMappingSessionID() {
    if (retrofitMappingSessionID==Long.MIN_VALUE)
      retrofitMappingSessionID = Long.parseLong(System.getProperty("idmapping.retrofit_mapping_session_id", "-1")); 
    return retrofitMappingSessionID;
  }
  
	// -------------------------------------------------------------------------
	/**
	 * @return A List of StableIDEventContainers for changed objects, i.e. where
	 *         the new version != the old version. Note that this is only for
	 *         existing objects, i.e. new ones are not counted.
	 */
	public List getChanged(String type) {

		List result = new ArrayList();

		Iterator it = createdStableIDEvents.values().iterator();
		while (it.hasNext()) {
			StableIDEventRow sidec = (StableIDEventRow) it.next();
			if (sidec.getType().equalsIgnoreCase(type)) {
				if (sidec.getOldStableID() != null) { // ignore created
					if (sidec.getOldVersion() != sidec.getNewVersion()) {
						result.add(sidec);
					}
				}
			}
		}

		return result;

	}

	// -------------------------------------------------------------------------
	/**
	 * Load the stable ID events into a database.
	 */
	public void uploadNewStableIDEvents() {

		Config.uploadFromFile(getCreatedStableIDEventFileName(), "stable_id_event", conf.getTargetConnection(), false);

	}

	public void uploadSimilarityStableIDEvents() {

		Config.uploadFromFile(getSimilarityStableIDEventFileName(), "stable_id_event", conf.getTargetConnection(), false);

	}

	// -------------------------------------------------------------------------

	public void uploadExistingStableIDEvents() {

		Config.uploadFromFile(rootDir + File.separator + "stable_id_event_existing.txt", "stable_id_event", conf.getTargetConnection(),
				false);

	}

	// -------------------------------------------------------------------------

	public void deleteOldMappingSession() {

		try {

			Statement stmt = conf.getTargetConnection().createStatement();
			stmt.execute("DELETE FROM mapping_session");

		} catch (SQLException e) {

			System.err.println("Can't delete old mapping sessions");
			e.printStackTrace();

		}
	}

	// ------------------------------------------------------------------------

	public void uploadMappingSession() {

		Config.uploadFromFile(rootDir + File.separator + "mapping_session.txt", "mapping_session", conf.getTargetConnection(), false);

	}

	// -------------------------------------------------------------------------

	public long getCurrentMappingSessionID() {
	  return currentMappingSessionID;
	}

	// -------------------------------------------------------------------------

	private long[] getPreviousMappingSessionIDs(long allLatest, long currentMappingSessionID) {

		String sql = "SELECT mapping_session_id FROM mapping_session WHERE mapping_session_id NOT IN (" + allLatest + ", "
				+ currentMappingSessionID + ")";

		Connection con = conf.getTargetConnection();
		List ids = new ArrayList();

		try {

			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				ids.add(new Long(rs.getLong(1)));
			}
			rs.close();
			stmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		// ugh why doesn't toArray work with long[}??
		long[] result = new long[ids.size()];
		Iterator it = ids.iterator();
		int i = 0;
		while (it.hasNext()) {
			Long ll = (Long) it.next();
			result[i++] = ll.longValue();
		}

		return result;

	}

	// -------------------------------------------------------------------------

	private void addDebugMapping(String type, long sourceInternalID, long targetInternalID, String stableID) {

		Util.addToMapList(debugMappings, type, new MappingContainer(sourceInternalID, targetInternalID, stableID));

	}

	// -------------------------------------------------------------------------

	public void dumpDebugMappingsToFile() {

		try {

			Iterator kit = debugMappings.keySet().iterator();

			while (kit.hasNext()) {

				String type = (String) kit.next();
				String fileName = rootDir + File.separator + "debug" + File.separator + type + "_mappings.txt";
				OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName));

				List typeMappings = (List) (debugMappings.get(type));
				Iterator it = typeMappings.iterator();

				while (it.hasNext()) {

					MappingContainer mc = (MappingContainer) it.next();
					writer.write(mc.toString() + "\n");

				}

				writer.close();

				System.out.println("Wrote " + type + " mappings to " + fileName);

			} // while type

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

  public Map getSimilarityStableIDEvents() {
    return similarityStableIDEvents;
  }

  public List getRetrofitGenes() {
    return retrofitGenes;
  }

  public List getRetrofitTranscripts() {
    return retrofitTranscripts;
  }

  public List getRetrofitTranslations() {
    return retrofitTranslations;
  }





	// -------------------------------------------------------------------------

} // StableIDMapper

// -------------------------------------------------------------------------
/**
 * Container to represent a mapping - source & target internal IDs, stableID.
 */

class MappingContainer {

	private long sourceInternalID, targetInternalID;

	private String stableID;

	public MappingContainer(long sourceInternalID, long targetInternalID, String stableID) {

		this.sourceInternalID = sourceInternalID;
		this.targetInternalID = targetInternalID;
		this.stableID = stableID;

	}

	public String toString() {

		return sourceInternalID + "\t" + targetInternalID + "\t" + stableID;

	}
}

// -------------------------------------------------------------------------


