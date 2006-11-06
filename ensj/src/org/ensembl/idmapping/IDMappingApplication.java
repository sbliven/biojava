/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.idmapping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.ensembl.datamodel.Accessioned;
import org.ensembl.driver.AdaptorException;
import org.ensembl.util.NamedTimer;
import org.ensembl.util.SerialUtil;
import org.ensembl.util.StringUtil;
import org.ensembl.util.SystemUtil;
import org.ensembl.util.Util;

import cern.colt.map.OpenLongObjectHashMap;

public class IDMappingApplication implements Runnable {

  private static final Logger logger = Logger
      .getLogger(IDMappingApplication.class.getName());

  private Config conf;

  private Cache cache;

  private ScoredMappingMatrix geneScoringMatrix;

  private ScoredMappingMatrix transcriptScoringMatrix;

  private ScoredMappingMatrix exonScoringMatrix;

  private GeneScoreBuilder gsb;

  private TranscriptScoreBuilder tsb;

  private ExonScoreBuilder esb;

  private List geneMappings;

  private List transcriptMappings;

  private List exonMappings;

  private List translationMappings;

  private Set retrofitStableIDEvents = new HashSet();
  
  private NamedTimer timer = new NamedTimer();

  private boolean debug = true;

  // put this many deleted genes in the summary email
  private int SAMPLE_SIZE = 20;

  private StableIDMapper stableIDMapper;

  private String mode;
  
  

  // ---------------------------------------------------------------------

  /**
   * Creates IDMappingLogs from specified source and target data sets.
   */

  public static void main(String[] args) {

    if (args.length > 1 || (args.length == 1 && args[0].equals("-h"))) {
      System.out
          .println("Usage: IDMappingApplication {properties file}\n\nIf no properties file is specified, resources/data/idmapping.properties is used.");
      System.exit(1);
    }

    String configFile;

    if (args.length == 1) {
      configFile = args[0];
    } else {
      configFile = ".." + File.separator + "resources" + File.separator
          + "data" + File.separator + "idmapping.properties";
    }

    // if the config validates do the id mapping
    System.out.println("\n----- Checking configuration -----");

    IDMappingApplication app = new IDMappingApplication(configFile);

    app.run();

    // if you've come that far, exit successfully
    System.exit(0);
  }

  public IDMappingApplication(String configFile) {

    conf = new Config(configFile);
    if (!conf.validateConfig()) {
      System.err.println("Configuration check failed");
      System.exit(1);
    }

  }

  // -------------------------------------------------------------------------
  /**
   * Do the scoring, and internal and stable ID mapping.
   */
  public void run() {

    timer.start("all");

    System.out.println("Using working directory " + conf.rootDir);

    mode = conf.getMode();

    if ("normal".equals(mode))
      runNormal();

    else if ("similarity".equals(mode))
      runSimilarity();

    else if ("archive".equals(mode))
      runArchive();

    else
      throw new RuntimeException("Unkown mode");
    System.out.println("\n----- ID Mapping finished -----");

  }


  private void runSimilarity() {
    buildCaches();
    stableIDMapper = new StableIDMapper(conf, cache);
    stableIDMapper.generateNewMappingSessionID();
    buildScores();
    map();
    assignStableIDsAndMakeCreationAndDeletionAndMappedEvents();
    generateSimilarityEvents();
    generateRetrofittedStableIDEvents();
    
  }

  /**
   * Creates gene_archive and peptide_archive entries for
   * changed and deleted items.
   * 
   * User must set "idmapping.retrofit_mapping_session_id"
   * in config file.
   *
   */
  private void runArchive() {
    buildCaches();
    stableIDMapper = new StableIDMapper(conf, cache);
    archive(false, false, stableIDMapper.getRetrofitMappingSessionID());
  }

  /**
   * Algorithm in short: - cache all involved ensembl objects, otherwise it will
   * be very slow - build exon exon scores either with overlap or with align
   * (exonerate) - build transcript transcript scores - build gene gene scores -
   * map uniquely mappable genes - Build a SyntenyFramework from those gene
   * mappings - rescore the rest of the genes - take old score and multiply it
   * by lets say 0.5 - Take every scoreing pair (entry) through the
   * SyntenyFramework and get scores from every SyntenyRegion (implemented in
   * SyntenyFramework - map uniquely mappable transcripts - map transcripts with
   * all ambiguous choices in same gene randomly - build SyntenyFramework from
   * uniquely mapped genes (locations) - adjust all transcript transcript scores
   * with SyntenyFramework --- 0.5*old_score+0.5*synteny_derived_score - do more
   * mapping in transcripts - use the mapped transcript and the exon scores to
   * map the exons.
   */
  private void runNormal() {
    buildCaches();
    stableIDMapper = new StableIDMapper(conf, cache);
    stableIDMapper.generateNewMappingSessionID();
    buildScores();
    map();
    assignStableIDsAndMakeCreationAndDeletionAndMappedEvents();

    generateSimilarityEvents();
    generateRetrofittedStableIDEvents();
    dumpEvents();

    archive(true, true, stableIDMapper.getCurrentMappingSessionID());

    uploadMappingSessionAndEvents();
    uploadStableIDs();
    uploadArchive();

    timer.stop("all");
    printTimings(timer);
    try {
      new ResultsAnalysis(conf, cache.getSourceGenesByInternalID(),
          cache.getTargetGenesByInternalID(), 
          geneMappings,
          stableIDMapper.getSimilarityStableIDEvents().values()).dump();
    } catch (AdaptorException e) {
      throw new RuntimeException(e);
    }
    createSummaryEmail(timer);
  }

  private void dumpEvents() {

    // default behaviour is to copy relevant stable_id_events from old db
    if (!Config.booleanFromProperty("idmapping.copy_stable_id_events", "yes")) {
      System.out.println("Skipping copying of existing stable_id_events.");
      return;
    }

    // TODO filter obsolete all-latest entries
    Util.dumpTableToFile(conf.getSourceConnection(), "stable_id_event",
        conf.rootDir + File.separator + "stable_id_event_existing.txt");
  }

  private void uploadMappingSessionAndEvents() {
    if (doUpload("events")) {

      stableIDMapper.uploadMappingSession();
      stableIDMapper.uploadExistingStableIDEvents();
      stableIDMapper.uploadNewStableIDEvents();
      stableIDMapper.uploadSimilarityStableIDEvents();

    } else {

      System.out
          .println("Upload property not set, new mapping session and stable ID events not uploaded.");

    }
  }

  private void generateSimilarityEvents() {

    debug("Generating similarity events");

    stableIDMapper.generateSimilarityEvents(geneMappings, geneScoringMatrix,
        "gene");
    ScoredMappingMatrix filteredTranscriptMatrix = stableIDMapper.filterSameGeneTransciptSimilarities(transcriptScoringMatrix);
    stableIDMapper.generateSimilarityEvents(transcriptMappings,
        filteredTranscriptMatrix, "transcript");
    stableIDMapper.generateTranslationSimilarityEvents(translationMappings,
        filteredTranscriptMatrix);

    stableIDMapper.writeSimilarityIDEvents();
    
    stableIDMapper.writeRetrofitStableIDEvents();

  }

  
  /**
   * Dump similarity events (stable_id_events) for two previously mapped databases.
   * 
   * This method is skipped unless 
   * "idmapping.retrofit_mapping_session_id=X" where X is an integer >0.
   * 
   * Input: XXXMappings and XXXretrofit datasets plus source and
   * target genes (which must have stable ids set).
   * 
   * Output: similarities between data in the source and target databases.
   * These are represented as stable_id_event "rows" and are written
   * to the file StableIDMapper.getRetrofitStableIDEventFileName().
   * 
   * @see StableIDMapper#getRetrofitStableIDEventFileName()
   */
  private void generateRetrofittedStableIDEvents() {
    
    // skip unless user specified idmapping.retrofit_mapping_session_id
    if (stableIDMapper.getRetrofitMappingSessionID()<1) {
      System.out.println("Skipping similarity retrofit stage for stable_id_event table.");
      return;
    }
    
    System.out.println("Retrofitting similarities for stable_id_event table.");
    
    generateRetrofittedStableIDEvents("gene", cache.getSourceGenesByInternalID(), 
        cache.getTargetGenesByInternalID(), geneMappings, stableIDMapper.getRetrofitGenes());
    generateRetrofittedStableIDEvents("transcript", cache.getSourceTranscriptsByInternalID(), 
        cache.getTargetTranscriptsByInternalID(), transcriptMappings, stableIDMapper.getRetrofitTranscripts());
    generateRetrofittedStableIDEvents("translation", cache.getSourceTranslationsByInternalID(), 
        cache.getTargetTranslationsByInternalID(), translationMappings, stableIDMapper.getRetrofitTranslations());
    
    StableIDMapper.writeStableIDEvents(retrofitStableIDEvents, stableIDMapper.getRetrofitStableIDEventFileName());
    writeRetrofitSQLPatch();    
  }

  /**
   * Writes SQL patch file which  will delete existing stable_id_event similarities
   * and upload the retroffited ones.
   *
   */
  private void writeRetrofitSQLPatch() {
    
    StringBuffer buf = new StringBuffer();
    
    buf.append("DELETE FROM stable_id_event WHERE mapping_session_id=")
    .append(stableIDMapper.getRetrofitMappingSessionID())
    .append(" AND old_stable_id IS NOT NULL AND new_stable_id IS NOT NULL AND old_stable_id!=new_stable_id;\n\n");
    
    buf.append("LOAD DATA LOCAL INFILE '").append(stableIDMapper.getRetrofitStableIDEventFileName()).append("' INTO TABLE stable_id_event;\n");
    
    StableIDMapper.writeStringToFile(buf.toString(), stableIDMapper.getRetrofitSQLPatchFileName());
  }

  /**
   * Add retrofitted similarity events for the specified type to _retrofitEvents_. 
   *  
   * @param type type of data {gene | transcript | translation }
   * @param srcByInternalID source internalID to Accessioned map
   * @param tgtByInternalID target internalID to Accessioned map
   * @param mappings mappings found during this mapping session.
   * @param similarities similarities found during this mapping session.
   */
  private void generateRetrofittedStableIDEvents(String type, OpenLongObjectHashMap srcByInternalID, OpenLongObjectHashMap tgtByInternalID, 
      List mappings, List similarities) {
    
    // Treat all mappings and similarites as if they are similarities
    // because they both mean "looks like" in this context.
    List allSimilarities = new ArrayList(mappings);
    allSimilarities.addAll(similarities);
    
    for (int i = 0; i < allSimilarities.size(); i++) {
    
      Entry e = (Entry) allSimilarities.get(i);
      Accessioned src = (Accessioned)srcByInternalID.get(e.source);
      Accessioned tgt = (Accessioned)tgtByInternalID.get(e.target);
      
      if (tgt.getAccessionID()==null) {
        System.err.println("Cannot retrofit "+type+" similarity data because target "+type+" lacks stable ID: " + e.target);
        break;
      } 
      
      else if (src.getAccessionID().equals(tgt.getAccessionID())) {
        // skip mappings that were made in the original mapping session
        continue;
      }
      
      retrofitStableIDEvents.add( new StableIDEventRow(src.getAccessionID(), src.getVersion(), 
          tgt.getAccessionID(), tgt.getVersion(), type, e.score, stableIDMapper.getRetrofitMappingSessionID()));
    }
    
  }

  private void archive(boolean checks, boolean dumpExistingData, long mappingSessionID) {

    // default behaviour is to dump source archive and create new
    // entries
    if (checks && !Config.booleanFromProperty("idmapping.archive", "yes")) 
      return;


    System.out.println("\n----- Updating gene and peptide archives -----");

    try {

      timer.start("archiving");

      Archiver archiver = new Archiver(conf, cache);
      archiver.createGenePeptideArchive(mappingSessionID);
      
      if (dumpExistingData) {
        Util.dumpTableToFile(conf.getSourceConnection(), "peptide_archive",
            conf.rootDir + File.separator + "peptide_archive_existing.txt");
        Util.dumpTableToFile(conf.getSourceConnection(), "gene_archive",
            conf.rootDir + File.separator + "gene_archive_existing.txt");
      }
      

      timer.stop("archiving");
    } catch (Exception e) {
      System.out.println("Couldnt do archiving: \n" + e.getMessage());
    }
  }

  private void uploadArchive() {
    System.out.println("\n----- Uploading gene and peptide archives -----");
    try {
      if (doUpload("archive")) {

        Config.uploadFromFile(conf.rootDir + File.separator
            + "peptide_archive_existing.txt", "peptide_archive", conf
            .getTargetConnection(), true);
        Config.uploadFromFile(conf.rootDir + File.separator
            + "gene_archive_existing.txt", "gene_archive", conf
            .getTargetConnection(), true);
        Config.uploadFromFile(conf.rootDir + File.separator
            + "peptide_archive_new.txt", "peptide_archive", conf
            .getTargetConnection(), true);
        Config.uploadFromFile(conf.rootDir + File.separator
            + "gene_archive_new.txt", "gene_archive", conf
            .getTargetConnection(), true);

      }
      timer.stop("upload-archive");
    } catch (Exception e) {
      System.out.println("Couldnt do archiving: \n" + e.getMessage());
    }
  }

  private void uploadStableIDs() {
    if (doUpload("stableids")) {

      String[] tables = { "exon", "transcript", "translation", "gene" };
      for (int i = 0; i < tables.length; i++) {
        String table = tables[i] + "_stable_id";
        Config.uploadFromFile(conf.rootDir + File.separator + table + ".txt",
            table, conf.getTargetConnection(), false);
      }

    }
    timer.stop("stableMapping");
  }

  private void assignStableIDsAndMakeCreationAndDeletionAndMappedEvents() {

    System.out.println("\n----- Stable ID event generation -----");

    timer.start("stableMapping");

    timer.start("exonStableMapping");
    System.out.println("\n----- Mapping exon stable IDs -----");
    stableIDMapper.mapStableIDs(cache.getSourceExonsByInternalID(), cache
        .getTargetExonsByInternalID(), exonMappings, "exon");
    timer.stop("exonStableMapping");

    timer.start("transcriptStableMapping");
    System.out.println("\n----- Mapping transcript Stable IDs -----");
    stableIDMapper.mapStableIDs(cache.getSourceTranscriptsByInternalID(), cache
        .getTargetTranscriptsByInternalID(), transcriptMappings, "transcript");
    timer.stop("transcriptStableMapping");

    timer.start("translationStableMapping");
    System.out.println("\n----- Mapping translation stable IDs -----");
    stableIDMapper.mapStableIDs(cache.getSourceTranslationsByInternalID(),
        cache.getTargetTranslationsByInternalID(), translationMappings,
        "translation");
    timer.stop("translationStableMapping");

    timer.start("geneStableMapping");
    System.out.println("\n----- Mapping gene stable IDs -----");
    stableIDMapper.mapStableIDs(cache.getSourceGenesByInternalID(), cache
        .getTargetGenesByInternalID(), geneMappings, "gene");
    timer.stop("geneStableMapping");

    stableIDMapper.dumpDebugMappingsToFile();
    stableIDMapper.dumpLostGeneAndTranscripts();
    stableIDMapper.writeNewStableIDEvents();

  }

  /**
   * First round of scoring finished Now need to map what is uniquely mappable.
   * Internally there is rescoring happening, but we hang on to the original
   * scoring matrices just a little longer
   */
  private void map() {

    timer.start("internalMapping");

    // do gene internal ID mapping based on gene scores
    System.out.println("\n----- Mapping gene internal IDs unambiguous -----");
    // TODO use score matrices to populate "is related to" archive
    InternalIDMapper internalIDMapper = new InternalIDMapper(conf, cache);
    geneMappings = internalIDMapper.cachedGeneMapping(geneScoringMatrix,
        transcriptScoringMatrix, gsb);
    // XXX comments
    // do transcript internal ID mapping based on transcript scores
    System.out.println("\n----- Mapping transcript internal IDs -----");

    transcriptMappings = internalIDMapper.cachedTranscriptMapping(
        transcriptScoringMatrix, geneMappings, tsb);

    // do exon internal ID mapping based on exon scores and transcript
    // internal ID mappings
    System.out.println("\n----- Mapping exon internal IDs -----");
    exonMappings = internalIDMapper.cachedExonMapping(exonScoringMatrix,
        transcriptMappings, esb);

    // do translation internal ID mapping based on transcript internal
    // IDmapping
    System.out.println("\n----- Mapping translation internal IDs -----");
    translationMappings = internalIDMapper
        .cachedTranslationMapping(transcriptMappings);
    // build lookup tables of generated internal ID mappings
    debug("Caching internal ID mappings");
    cache.cacheMappings(exonMappings, transcriptMappings, translationMappings,
        geneMappings);

    timer.stop("internalMapping");

  }

  private void buildScores() {

    gsb = new GeneScoreBuilder(conf, cache);
    tsb = new TranscriptScoreBuilder(conf, cache);
    esb = new ExonScoreBuilder(conf, cache);

    // ----------------------------
    // SCORE BUILDING

    timer.start("scoring");

    // calculate scores for exons based on overlap and/or sequence
    // similarity
    timer.start("exonScoring");
    System.out.println("\n----- Generating exon scores -----");
    exonScoringMatrix = esb.scoreExons(conf);
    timer.stop("exonScoring");

    // calculate scores for transcripts based on exon scores
    timer.start("transcriptScoring");
    System.out.println("\n----- Generating transcript scores -----");
    transcriptScoringMatrix = tsb.scoreTranscripts(exonScoringMatrix);
    timer.stop("transcriptScoring");

    // calculate scores for genes based on transcript scores
    System.out.println("\n----- Generating gene scores -----");
    geneScoringMatrix = gsb.scoreGenes(transcriptScoringMatrix);
    timer.stop("scoring");

  }

  // -------------------------------------------------------------------------

  // ---------------------------------------------------------------------

  private void printTimings(NamedTimer nt) {

    System.out.println("\nCaching:       " + gf(nt, "caching"));

    System.out.println("\nScoring");
    System.out.println("    Exon:        " + gf(nt, "exonScoring"));
    System.out.println("    Transcript:  " + gf(nt, "transcriptScoring"));
    System.out.println("    Overall:     " + gf(nt, "scoring"));

    System.out.println("\nInternal ID Mapping " + gf(nt, "internalMapping"));

    System.out.println("\nStable ID Mapping");
    System.out.println("    Exon:        " + gf(nt, "exonStableMapping"));
    System.out.println("    Transcript:  " + gf(nt, "transcriptStableMapping"));
    System.out
        .println("    Translation: " + gf(nt, "translationStableMapping"));
    System.out.println("    Gene:        " + gf(nt, "geneStableMapping"));
    System.out.println("    Overall:     " + gf(nt, "stableMapping"));

    System.out.println("\nArchiving:");
    System.out.println("    Overall:      " + gf(nt, "archiving"));

    System.out.println("\nTotal duration: " + gf(nt, "all"));

  }

  private String gf(NamedTimer nt, String s) {
    return nt.format(nt.getDuration(s));
  }

  // -------------------------------------------------------------------------

  private boolean doUpload(String prop) {

    String fullProp = System.getProperty("idmapping.upload." + prop);
    return (fullProp != null && fullProp.equals("yes"));

  }

  // -------------------------------------------------------------------------

  private void createSummaryEmail(NamedTimer nt) {

    StringBuffer message = new StringBuffer();

    message.append("ID mapping complete for "
        + System.getProperty("idmapping.source.database") + " -> "
        + System.getProperty("idmapping.target.database") + "\n");

    message.append("\nResults:\n\n");
    String[] types = { "exon", "transcript", "translation", "gene_detailed" };
    for (int i = 0; i < types.length; i++) {
      String fileName = conf.rootDir + File.separator + types[i]
          + "_mapping_statistics.txt";
      if ((new File(fileName)).exists()) {
        message.append(StringUtil.readTextFile(fileName));
      } else {
        message
            .append("No " + types[i] + "s found, so no statistics generated");
      }
      message.append("\n");
    }

    String[] uploads = { "stableids", "events", "archive" };
    String[] uploadDescriptions = { "Stable IDs",
        "Stable ID events and mapping session",
        "Gene and peptide archiving information" };

    for (int j = 0; j < uploads.length; j++) {

      String s = doUpload(uploads[j]) ? "" : "not ";
      message.append(uploadDescriptions[j] + " were " + s + "uploaded to "
          + System.getProperty("idmapping.target.database") + "\n");

    }

    message.append("\n");

    message.append("A sample of the first " + SAMPLE_SIZE
        + " deleted known genes is at the end of this email.\n");

    String[] gt = { "genes", "transcripts" };
    for (int k = 0; k < gt.length; k++) {

      message.append("A full list of " + gt[k] + " which were deleted is in "
          + conf.rootDir + File.separator + gt[k] + "_lost_deleted.txt" + "\n");
      message.append("A full list of " + gt[k]
          + " which were lost due to merging, and the " + gt[k]
          + " into which they were merged is in " + conf.rootDir
          + File.separator + gt[k] + "_lost_merged.txt" + "\n");

    }

    message.append("\nTotal run time: " + nt.format(nt.getDuration("all")));

    message
        .append("\n----------------------------------------------------------------------\n");

    String[] deletedGenes = StringUtil.readTextFile(
        conf.rootDir + File.separator + "genes_lost_deleted.txt").split("\\n");

    if (deletedGenes.length > 1) {

      message.append("First " + SAMPLE_SIZE
          + " known genes which were deleted:\n\n");

      int knownCount = 0;
      for (int i = 0; i < deletedGenes.length; i++) {
        String[] bits = deletedGenes[i].split("\\t");
        if (bits[1].equals("KNOWN")) {
          message.append(bits[0]);
          knownCount++;
          String link = Util.makeEnsemblGeneLink(bits[0]);
          if (link != null) {
            message.append("\t" + link);
          }
          message.append("\n");
        }
        if (knownCount > SAMPLE_SIZE) {
          break;
        }
      }

    }

    String fileName = conf.rootDir + File.separator + "summary_email.txt";

    try {

      OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(
          fileName));
      writer.write(message.toString());
      writer.close();

    } catch (IOException e) {

      e.printStackTrace();
    }

    System.out
        .println("\nSummary information suitable for emailing written to "
            + fileName);

  }

  // -------------------------------------------------------------------------

  private void buildCaches() {

    SystemUtil.MemoryStatus memBeforeCaching = SystemUtil.memoryStatus(true);
    logger.fine("*** Memory before building cache: "
        + memBeforeCaching.toStringMb());

    timer.start("caching");
    System.out
        .println("\n----- Reading and caching genes, transcripts, translations and exons -----");

    // read from file if already done
    String fileName = conf.rootDir + File.separator + "cache.ser";
    File f = new File(fileName);

    if (!f.exists()) {
      cache = new Cache(conf);

      debug("Writing cache to " + fileName);
      SerialUtil.writeObject(cache, fileName);
      debug("Finished writing cache");

    } else {

      System.out
          .println("Using cached gene, transcript, translation and exon information in "
              + fileName);
      cache = (Cache) SerialUtil.readObject(fileName);

    }

    timer.stop("caching");

    SystemUtil.MemoryStatus memAfterCaching = SystemUtil.memoryStatus(true);
    System.out.println("Memory after building cache: "
        + memAfterCaching.toStringMb());
    System.out.println("Building the Cache uses : "
        + memAfterCaching.diff(memBeforeCaching).toStringMb());
  }

  // -------------------------------------------------------------------------

  private void debug(String s) {
    if (debug)
      System.out.println(s);
  }

  // -------------------------------------------------------------------------

} // IDMappingApplication

// ---------------------------------------------------------------------

