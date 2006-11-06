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

package org.ensembl.probemapping;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ensembl.datamodel.ExternalDatabase;
import org.ensembl.datamodel.ExternalRef;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.OligoArray;
import org.ensembl.datamodel.OligoFeature;
import org.ensembl.datamodel.OligoProbe;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.impl.ExternalRefImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.CoreDriverFactory;
import org.ensembl.driver.FeatureIterator;
import org.ensembl.util.JDBCUtil;
import org.ensembl.util.LogFormatter;
import org.ensembl.util.LongSet;
import org.ensembl.util.MessageOnlyFormatter;
import org.ensembl.util.SerialUtil;
import org.ensembl.util.Timer;

import cern.colt.map.OpenLongObjectHashMap;

/**
 * Maps MicroArray probesets to transcripts.
 * 
 * <p>
 * INPUT: The input data is loaded from source database(s) as
 * OligoFeatures,OligoArrays, OligoProbes and Transcripts.
 * </p>
 * 
 * <ul>
 * OUTPUT:
 * 
 * <li>The mappings are saved as ExternalReferences in the output database.
 * </li>
 * 
 * <li>A record of overlapping probeset-transcript pairs and why they were or
 * were not mapped is written to the file probeset2transcript.log.</li>
 * 
 * <li>A log of events during the mapping process is also written to the file
 * probe_mapper.log.</li>
 * </ul>
 * 
 * 
 * @see org.ensembl.datamodel.OligoProbe
 * @see org.ensembl.datamodel.OligoFeature
 * @see org.ensembl.datamodel.OligoArray
 * @see org.ensembl.datamodel.Transcript
 * @see org.ensembl.datamodel.ExternalRef
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 *  
 */
public class ProbeMapper {

  private class Chunk implements Comparable{
    private Location location;
    private ArrayList buf = new ArrayList();
    private MappableTranscript[] mappableTranscripts = null;
    
    public Chunk(Location location) {
      this.location=location;
      if (location==null) throw new NullPointerException("location is null");
    }
    
    public Chunk(Location location, MappableTranscript[] mappableTranscripts) {
      this.location = location;
      this.mappableTranscripts = mappableTranscripts;
      for (int i = 0; i < mappableTranscripts.length; i++) {
        buf.add(mappableTranscripts[i]);
      }
    }
    
    private void add(MappableTranscript t) {
      buf.add(t);
    }
    
    /**
     * 
     * @return all mappable transcripts, sorted by location
     */
    public MappableTranscript[] getMappableTranscripts() {
      if (mappableTranscripts!=null) return mappableTranscripts;
      mappableTranscripts = (MappableTranscript[]) buf.toArray(new MappableTranscript[buf.size()]);
      Arrays.sort(mappableTranscripts); 
      return mappableTranscripts;
    }
    
    public Location getLocation() {
      return location;
    }
    
    public String toString() {
      return "[location="+location+", nMappableTranscripts="+buf.size()+"]";
    }

    // can be used to sort by DESCENDING location length
    public int compareTo(Object o) {
      final int l1 = location.getLength();
      final int l2 = ((Chunk)o).location.getLength();
      return l2-l1;
    }
    
    
  }
  
  // DEFAULT PARAMETERS
  private static final double DEFAULT_THRESHOLD = 0.5;

  private static final int DEFAULT_TRANSCRIPTS_PER_PROBE_SET_THRESHOLD = 100;

  private static final int DEFAULT_DOWN_STREAM_FLANK = 2000;

  private static final int DEFAULT_MAX_TRANSCRIPTS_PER_COMPOSITE = 100;

  private static final String DEFAULT_WORKING_DIRECTORY = ".";

  private static final String DEFAULT_LOG_FILENAME = "probeset2transcript.log";

  // PARAMETERS
  private File workingDirectory = new File(DEFAULT_WORKING_DIRECTORY);

  private int maxTranscriptsPerCompositeThreshold = DEFAULT_TRANSCRIPTS_PER_PROBE_SET_THRESHOLD;

  private final static Logger logger = Logger.getLogger(ProbeMapper.class
      .getName());

  private Location locationFilter;

  private CoreDriver transcriptDriver;

  private CoreDriver probeDriver;

  private CoreDriver outputDriver;

  private int downStreamFlank = DEFAULT_DOWN_STREAM_FLANK;

  private double threshold = DEFAULT_THRESHOLD;

  private String logFilename = DEFAULT_LOG_FILENAME;

  private boolean verbose = false;

  private String defaultDriverFilepath;

  private String outputDriverFilepath;

  private String transcriptDriverFilepath;

  private String probeDriverFilepath;

  private boolean skipXrefCheck = false;
  
  /**
   * Runs the application.
   * 
   * Loads the data, performs the mapping, saves the results to an output
   * database and produces a log file of overlapping transcripts and probesets
   * and whether they where mapped or not.
   * 
   * @param args
   *          command line parameters which are passed to the constructor.
   */
  public static void main(String[] args) {

    Timer timer = new Timer().start();

    try {
      ProbeMapper app = new ProbeMapper(args);
      logger.info(app.configurationDescription());
      app.run();

    } catch (Exception e) {

      logger.log(Level.SEVERE, "ProbeMapper failed: ", e);

    } finally {

      logger.info("ProbeMapper finished in = " + timer.getDurationInSecs()
          + " secs.");

    }
  }

  private CoreDriver createDriver(String name, String preferedConfigFilePath,
      String defaultConfigFilePath) {
    CoreDriver d = null;

    if (preferedConfigFilePath != null)
      d = loadDriver(name, preferedConfigFilePath);
    else if (defaultConfigFilePath != null)
      d = loadDriver("default", defaultConfigFilePath);
    if (d == null)
      error(name + " driver is not set.");
    if (!d.testConnection())
      error("Cannot connect to " + name + " database.");

    return d;
  }

  private String configurationDescription() {
    return "ProbeMapper Configuration:" + "\n=========================="
        + "\nTranscript Database: " + transcriptDriver + "\nProbe Database: "
        + probeDriver + "\nOutput Database: " + outputDriver
        + "\nLocation batches: " + locationFilter + "\nDown stream flank: "
        + downStreamFlank + "\nMapping overlap threshold: "
        + NumberFormat.getPercentInstance().format(threshold)
        + "\nMax transcripts per composite: "
        + maxTranscriptsPerCompositeThreshold + "\nWorking directory: "
        + workingDirectory.getAbsolutePath() + "\n==========================";
  }

  /**
   * Initialises the java.util.logging package which is used by this program for
   * both logging and verbose output management.
   * 
   * It uses the _verbose_ and _workingDirectory_ attributes.
   * 
   * @throws IOException
   * @throws SecurityException
   */
  private void initLogging() throws SecurityException, IOException {

    // Output to log file in working directory
    String logFile = new File(workingDirectory, "probe_mapper.log")
        .getAbsolutePath();
    FileHandler fh = new FileHandler(logFile);
    fh.setLevel(Level.INFO);
    fh.setFormatter(new LogFormatter());

    // Output to console (if verbose then include INFO messages)
    ConsoleHandler ch = new ConsoleHandler();
    ch.setLevel((verbose) ? Level.INFO : Level.WARNING);
    ch.setFormatter(new MessageOnlyFormatter());

    Logger root = Logger.getLogger("");
    root.setLevel(Level.WARNING); // default warning for all classes except this
    // one
    Handler[] hs = root.getHandlers();
    for (int i = 0; i < hs.length; i++)
      root.removeHandler(hs[i]);
    root.addHandler(fh);
    root.addHandler(ch);

    // Log all messages >= INFO from this class.
    // Rely on handlers to filter irrelevant messages.
    logger.setLevel(Level.INFO);

  }

  
  /**
   * Splits _ts_ into chunks where each chunk a set of transcripts on the same 
   * sequence region.
   * 
   * Note: side effect- sorts ts by location.
   * 
   * Note: ALTERNATIVE CHUNKING STRATEGY could be used to REDUCE MEMORY REQUIREMENTS.
   * This might be necessary if there are a large number of probe features.
   * For example chunking by 1 mb regions or chunking by 10 consecutive transcripts
   * would require less memory than chunking by sequence region. 
   * 
   * 
   * @param ts MappableTranscripts, sorted by location.
   * @param locationFilter lcoation filter. Can be null. If not null return a single chunk
   * with all transcripts on the same chunk.
   * @return array of Chunks covering all sequence regions with transcripts and 
   * sorted by location. 
   * @throws AdaptorException
   */
  private Chunk[] chunks(MappableTranscript[] ts, Location locationFilter) throws AdaptorException {

    if (locationFilter != null)
      return new Chunk[] { new Chunk(locationFilter, ts) };
    
    ArrayList chunks = new ArrayList();

    // create a chunk for each sequence region containing all the transcripts
    // on that region
    Chunk chunk = null;
    String csName = null;
    String srName = null;
    Arrays.sort(ts); // must be sorted by location for logic below to work
    for (int i = 0; i < ts.length; i++) {
      MappableTranscript t = ts[i]; 
      Location tLoc = t.getLocation();
      String tCsName = tLoc.getCoordinateSystem().getName();
      String tSrName = tLoc.getSeqRegionName();
      try {
        if (!tCsName.equals(csName) || !tSrName.equals(srName) ) {
          csName = tCsName;
          srName = tSrName;
          Location l = new Location(tCsName+":"+tSrName);
          l = transcriptDriver.getLocationConverter().fetchComplete(l);
          if (l==null) 
            throw new NullPointerException("Can't create location for this string " + tCsName+":"+tSrName );
          chunk = new Chunk(l);
          chunks.add(chunk);
        }
        chunk.add(t);
      } catch (ParseException e) {
        throw new AdaptorException("Failed to load data: ", e);
      }  

    }
      
    Chunk[] r = (Chunk[]) chunks.toArray(new Chunk[chunks.size()]);
    Arrays.sort(r);
    
    return r;
  }
  
  /**
   * Find, map, store and log all overlapping transcripts and probesets.
   */
  public void run() throws IOException {


    xrefCheck(outputDriver);
    
    // Log file will contains an entry for every overlapping pair of transcript and probeset.
    FileWriter logWriter = new FileWriter(new File(workingDirectory,
        logFilename));

    
    // Load transcripts matching the locationFilter. Note that if locationFilter==null
    // transcripts which appear on HAP and PAR regions will only be returned once.
    // They will not be duplicated; once for the HAP/PAR and once for the underlying region. 
    MappableTranscript[] ts = loadTranscripts(locationFilter);
    
    
    // Perform mapping one chunk (seq region) at a time.
    Chunk[] chunks = chunks(ts, locationFilter);    
    Map xrefCache = new HashMap();
    for (int i = 0; i < chunks.length; i++) {
      
      Chunk chunk = chunks[i];
      Map probeSets = new HashMap();
      MappableOligoFeature[] afs = loadOligoData(chunk.getLocation(), probeSets, chunk.getMappableTranscripts());
      mapTranscripts2OligoFeatures(afs, chunk.getMappableTranscripts(), chunk.getLocation());
      markProbeSetsThatHitTooManyTranscripts(
          maxTranscriptsPerCompositeThreshold, probeSets);
      List mss = findOverlappingProbeSetAndTranscripts(probeSets);
      writeLog(mss, logWriter);
      logWriter.flush(); // write to log even if db store fails next
      store(mss, xrefCache, outputDriver);
    }

    logWriter.close();

  }

  /**
   * Throws a RuntimeException if there are already oligo xrefs in the ouptut database.
   * 
   */
  private void xrefCheck(CoreDriver outputDriver) {
  
    if (skipXrefCheck)
      return;
    
    Connection conn = null;
    try {
      conn = outputDriver.getConnection();
      String sql = "select * from xref where external_db_id>3000 and external_db_id<3200";
      ResultSet rs = conn.createStatement().executeQuery(sql);
      if (rs.next())
        throw new RuntimeException("Output database already contains oligo xrefs. Remove them before running this program.");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } catch (AdaptorException e) {
      throw new RuntimeException(e);
    } finally {
      JDBCUtil.close(conn);
    }
  }

  /**
   * Create MappingStatuses for all overlapping ProbeSet and Transcript pairs.
   * 
   * The relationship stores whether the pair are mapped or unmapped and if
   * unmapped why not.
   */
  private List findOverlappingProbeSetAndTranscripts(Map probeSets) {

    List mss = new ArrayList();

    for (Iterator iter = probeSets.values().iterator(); iter.hasNext();) {

      ProbeSet ps = (ProbeSet) iter.next();
      // probesetSize * threshold
      OligoArray sampleArray = (OligoArray) ps.getOligoArrays().get(0);
      int exonFlankThreshold = (int) Math
          .ceil((sampleArray.getProbeSetSize() * threshold));

      // create a relationship for each probeset2transcript relationship
      for (Iterator iterator = ps.getOverlappingTranscripts().iterator(); iterator
          .hasNext();) {

        Object o = iterator.next();
        MappableTranscript t = (MappableTranscript) o;// iterator.next();

        int reverseStrandHitCount = 0;
        int exonFlankHitCount = 0;
        int intronHitCount = 0;

        for (Iterator probeIter = ps.oligoFeatures.iterator(); probeIter
            .hasNext();) {

          final MappableOligoFeature af = (MappableOligoFeature) probeIter.next();

          final Location afLoc = af.oligoFeature.getLocation();
          
          final int probeLen = af.oligoFeature.getProbe().getLength();

          // probe hit either strand of transcript location (Location.overlaps()
          // ignores strand)?
          // Optimisation: call overlaps() before overlapSize()
          if (afLoc.overlaps(t.getLocation())
              && afLoc.overlapSize(t.getLocation(), false) == probeLen) {

            // probe hit exon or flank?
            if (afLoc.overlapSize(t.getCDNALocation(), true) == probeLen)
              exonFlankHitCount++;

            // probe hit intron?
            else if (afLoc.overlapSize(t.getLocation(), true) == probeLen)
              intronHitCount++;

            else
              reverseStrandHitCount++;
          }

        }

        MappingStatus r = new MappingStatus(ps, exonFlankThreshold, t,
            exonFlankHitCount, intronHitCount, reverseStrandHitCount);
        mss.add(r);
      }

    }

    if (logger.isLoggable(Level.INFO)) {
      logger.info("Found " + mss.size()
          + " overlapping probe sets and transcript pairs.");
      int t = 0;
      for (int i = 0, n = mss.size(); i < n; i++)
        if (((MappingStatus) mss.get(i)).isMapped())
          t += 1;
      logger.info("Mapped " + t + " probe sets to transcripts.");
    }

    return mss;
  }

  /**
   * Mark each probe set that maps to too many transcripts.
   * 
   * We do this because we do not want to map promiscuous probesets to any
   * transcripts.
   * 
   * @param maxTranscriptsPerCompositeThreshold2
   */
  private void markProbeSetsThatHitTooManyTranscripts(
      int maxTranscriptsPerCompositeThreshold2, Map probeSets) {

    for (Iterator iter = probeSets.values().iterator(); iter.hasNext();) {
      ProbeSet ps = (ProbeSet) iter.next();
      if (ps.getOverlappingTranscripts().size() > maxTranscriptsPerCompositeThreshold)
        ps.tooManyTranscripts = true;
    }

  }

  /**
   * Writes all MappingStatuses to log.
   * 
   * @param mappingStatuses
   *          zero or MappingStatuses to write to log file.
   * @param log
   *          output log.
   * @throws IOException
   */
  private void writeLog(List mappingStatuses, Writer log) throws IOException {

    for (Iterator iter = mappingStatuses.iterator(); iter.hasNext();) {

      log.write(iter.next().toString());
      log.write("\n");

    }

  }

  /**
   * Stores mapped probeset-transcripts xrefs and object_xrefs.
   * 
   * One xref is created for each probeset-mappableTranscripts permutation. They are only
   * stored if they do not already exist in the database. Each of these xrefs is
   * linked to the transcipt via an object_xref entry.
   * 
   * @param mappingStatuses
   *          zero or MappingStatuses to store in database.
   */
  private void store(final List mappingStatuses, final Map xrefCache,
      final CoreDriver outputDriver) throws AdaptorException {

    for (int k = 0, n = mappingStatuses.size(); k < n; k++) {

      MappingStatus status = (MappingStatus) mappingStatuses.get(k);

      if (!status.isMapped())
        continue;

      String probeSetName = status.probeSet.probeSetName;

      List arrays = status.probeSet.getOligoArrays();
      for (int i = 0, m = arrays.size(); i < m; i++) {

        OligoArray array = (OligoArray) arrays.get(i);
        ExternalDatabase xdb = array.getExternalDatabase();

        String cacheKey = xdb.getName() + "__" + probeSetName;
        ExternalRef xref = (ExternalRef) xrefCache.get(cacheKey);

        if (xref == null) {

          // xref might already be in the database
          // but NOT in the cache
          List xrefs = outputDriver.getExternalRefAdaptor().fetch(probeSetName);
          for (int j = 0; xref == null && j < xrefs.size(); ++j) {
            ExternalRef tmp = (ExternalRef) xrefs.get(j);
            if (tmp.getExternalDbId() == xdb.getInternalID())
              xref = tmp;
          }

          if (xref == null) {
            // store this probeset as an xref
            xref = new ExternalRefImpl(outputDriver);
            xref.setExternalDbId(xdb.getInternalID());
            xref.setPrimaryID(probeSetName);
            xref.setDisplayID(probeSetName);
            xref.setVersion("1");
            xref.setDescription(null);
            outputDriver.getExternalRefAdaptor().store(xref);
          }

          xrefCache.put(cacheKey, xref);

        }

        status.xrefs.add(xref);

        // store one object_xref for each mapped
        // transcript-probeset-microarray permutation
        outputDriver.getExternalRefAdaptor().storeObjectExternalRefLink(
            status.transcript.getInternalID(), ExternalRef.TRANSCRIPT,
            xref.getInternalID());
      }
    }
  }

  /**
   * Maps transcripts to oligo features.
   * 
   * Uses a variety of optimisations for example _seqRegionNameFilter_ and
   * _knownTranscripts2Probesets_ are applied to avoid computationally expensive
   * comparisons.
   * 
   * @param afs
   *          oligo features to be mapped. They should all be on the
   *          _locationFilter_.
   * @param ts
   *          transcripts to be mapped.
   * @param knownTranscript2ProbesetsFilter
   *          all the currently mapped transcript-probeset pairs.
   * @param locFilter
   *          only attempt to map transcripts on this location.
   * 
   * 
   * @throws IOException
   */
  private void mapTranscripts2OligoFeatures(MappableOligoFeature[] afs,
      MappableTranscript[] ts, Location locFilter) throws IOException {

    final int nts = ts.length;
    final int nafs = afs.length;

    int mappedCount = 0;
    int comparisonCount = 0;

    // We sort the transcripts and oligo features by genomic location
    // before we start the search for overlapping pairs as this allows
    // us to avoid many uncessary location comparisons.
    Arrays.sort(ts);
    Arrays.sort(afs);

    // Optimisation: All oligo features in _afs_ are on the same sequence region
    // so we can
    // ignore all the transcripts on other ones.
    final String seqRegionNameFilter = locFilter.getSeqRegionName();

    int afiStart = 0;
    for (int ti = 0; ti < nts; ti++) {

      final MappableTranscript t = ts[ti];
      final Location tLoc = t.getLocation();
      final int tStrand = tLoc.getStrand();
      final int tStart = tLoc.getStart();

      // different sequence regions to the oligo features.
      final int srDiff = seqRegionNameFilter.compareTo(tLoc.getSeqRegionName());
      if (srDiff > 0)
        continue;
      else if (srDiff < 0)
        break;

      for (int afi = afiStart; afi < nafs; afi++) {

        final MappableOligoFeature af = afs[afi];
        final Location afLoc = af.location;

        if (tStart > afLoc.getStart()) {
          afiStart = afi + 1;
          continue;
        }

        if (tStrand != afLoc.getStrand())
          continue;

        // We ignore strand in this overlap check otherwise
        // we might break out of loop below prematurely.
        final boolean overlap = tLoc.overlaps(afLoc, false);

        comparisonCount++;

        if (overlap) {

          final int overlapSize = t.getCDNALocation().overlapSize(afLoc, true);
          if (overlapSize == afLoc.getLength()) {

            mappedCount += 1;
            af.addTranscript(t);
          }
        } else if (afLoc.compareTo(tLoc) == 1) {

          // the current transcript can't overlap with the
          // the remaining oligo features
          break;
        }
      }
    }

    logger.info("Found " + mappedCount
        + " raw mappings between oligo_features and transcripts: " + locFilter
        + ". (" + comparisonCount + " comparisons).");

  }

  private void error(String msg) {
    error(msg, null);
  }

  private void error(String msg, Throwable t) {
    logger.log(Level.SEVERE, msg, t);
    logger.info(usage());
    System.exit(0);
  }

  private File cacheFile(String filename) {
    return new File(workingDirectory + File.separator + filename);
  }
  
  /**
   * Loads oligo features and probesets for each location in locations.
   * 
   * Optimisation: Use cache files to speed loading.
   * 
   * @param locations
   * @throws AdaptorException
   */
  private MappableOligoFeature[] loadOligoData(final Location location,
      final Map probeSets, final MappableTranscript[] ts) throws AdaptorException {

    Timer timer = new Timer().start();

    File featureCacheFile = cacheFile("mappable_oligo_features_"+location+".ser");
    
    MappableOligoFeature[] mafs = loadOligoDataFromFile(featureCacheFile, location, probeSets);
    if (mafs==null) {
      mafs = loadOligoDataFromDB(location, probeSets, ts);
      writeOligoDataToFile(mafs, probeSets, featureCacheFile);
    }
    
    logger.info("Loaded " + mafs.length + " Oligo Features and "
        + probeSets.size() + " Probesets for location " + location + " in "
        + timer.stop().getDurationInSecs() + "secs.");
    
    return mafs;
  }

  /**
   * Loads oligo features and necessary probesets from database.
   * 
   * @param featureCacheFile
   * @param location location to load data for.
   * @param probeSets cache for probesets, contains probesets already encountered and new ones will be loaded by this method.
   * @param mappableTranscripts transcripts to be mapped.
   * @return
   * @throws AdaptorException
   */
  private MappableOligoFeature[] loadOligoDataFromDB(Location location, Map probeSets, MappableTranscript[] mappableTranscripts) throws AdaptorException {
    
    logger.info("Loading oligo data for location: " + location + " from database..." );
    
    for(Iterator iter=probeDriver.getOligoArrayAdaptor().fetch().iterator();iter.hasNext();) {
      OligoArray a = (OligoArray)iter.next();
      // Ensure mappableTranscripts.externalDB is loaded so it 
      // can later be written to cache.
      a.getExternalDatabase();
    }

    // Speed Optimisation: collect probeIDs while loading OligoFeatures
    // so we can load probes in one step later. 
    LongSet pIDs = new LongSet();    
    List featureBuffer = loadFeatures(location, mappableTranscripts, pIDs);
    OpenLongObjectHashMap id2probe = loadProbesets(probeSets, pIDs);
    
    // Collect unique MappabeOligoFeatures, ignoring duplicates.
    List features = new ArrayList();
    for (Iterator fi = featureBuffer.iterator(); fi.hasNext();) {

      OligoFeature af = (OligoFeature) fi.next();
      fi.remove(); 

      // af -> probe -> probeSetName
      OligoProbe p = (OligoProbe)id2probe.get(af.getProbeInternalID());
      
      // we only map affy arrays, ignore others which should be mapped 
      // by another (xref) system
      boolean ignore = false;
      List arrays = p.getArraysContainingThisProbe();
      for (int i = 0; !ignore && i < arrays.size(); i++) 
        if (!"AFFY".equals(((OligoArray) arrays.get(i)).getType()))
            ignore = true;
      if (ignore)
        continue;
      
      ProbeSet ps = (ProbeSet) probeSets.get(p.getProbeSetName());
      MappableOligoFeature maf = new MappableOligoFeature(af, ps);
      if (ps.addMappableOligoFeatureIfUnique(maf))
        features.add(maf);
    }
    
    return (MappableOligoFeature[]) features.toArray(new MappableOligoFeature[features.size()]);  
  }

  private LinkedList loadFeatures(Location location, MappableTranscript[] mappableTranscripts, LongSet pIDs) throws AdaptorException {
    LinkedList featureBuffer = new LinkedList();
    LocationOverlapComparator overlapComparator = new LocationOverlapComparator();
    //  Memory Optimisation: We use Use fetchIterators to minimize memory usage. 
    for(Iterator fi = probeDriver.getOligoFeatureAdaptor().fetchIterator(location, false,1000000); 
        fi.hasNext();) {
      OligoFeature af = (OligoFeature) fi.next();
      
      // Memory + speed Optimisation: only remember features which overlap a transcript
      int insertionPoint = Arrays.binarySearch(mappableTranscripts, af, overlapComparator);
      if (insertionPoint>-1) {
        featureBuffer.add(af);
        pIDs.add(af.getProbeInternalID());
      }
    }
    return featureBuffer;
  }

  /**
   * Load probes into probesets and store in _probeSets_.
   * 
   * @param probeSets probe sets.
   * @param pIDs ids of probes that need loading.
   * @return probe.internalID to probeset map.
   * @throws AdaptorException
   */
  private OpenLongObjectHashMap loadProbesets(Map probeSets, LongSet pIDs) throws AdaptorException {
    // Create probesets
    // Speed optimisation: Load probes via probeIDs in one step,
    // faster than lazy loading probes one at a time.
    OpenLongObjectHashMap id2probe = new OpenLongObjectHashMap();
    for (Iterator pi = probeDriver.getOligoProbeAdaptor().fetchIterator(
        pIDs.to_longArray()); pi.hasNext();) {

      OligoProbe p = (OligoProbe) pi.next();
      id2probe.put(p.getInternalID(), p); 
      
      String psName = p.getProbeSetName();
      ProbeSet ps = (ProbeSet) probeSets.get(psName);
      if (ps == null)
        probeSets.put(psName, ps = new ProbeSet(psName));
      ps.addOligoArrays(p.getArraysContainingThisProbe());
    }
    return id2probe;
  }

  private void writeOligoDataToFile(MappableOligoFeature[] mafs, Map probeSets, File featureCacheFile) {
    SerialUtil.writeObject(new Object[]{mafs,probeSets}, featureCacheFile);
  }
  
  private MappableOligoFeature[] loadOligoDataFromFile(File featureCacheFile, Location location, Map probeSets) {
    
    if (featureCacheFile.exists()) {
      logger.info("Loading oligo data for location: " + location + " from cache files..." );
      Object[] tmp = (Object[]) SerialUtil.readObject(featureCacheFile);
      MappableOligoFeature[] mafs = (MappableOligoFeature[]) tmp[0];
      Map tmpProbeSets = (Map)tmp[1];
      if (mafs!=null && tmpProbeSets!=null) {
        // we need to COPY probesets from cache file into live object
        probeSets.putAll(tmpProbeSets);
        return mafs;
      }
    }
    return null;
    
  }

  /**
   * Load the transcripts.
   * 
   * @param locFilter
   *          location filter, null means load all transcripts by default
   * @return zerp or more mappable transcripts corresponding to the locFilter,
   *         all transcripts if locFilter==null.
   * @throws AdaptorException
   */
  private MappableTranscript[] loadTranscripts(final Location locFilter)
      throws AdaptorException {

    Timer timer = new Timer().start();

    MappableTranscript[] mts = null;

    // 1 - try to load transcripts from file
    File transcriptCacheFile = cacheFile("mappable_transcripts_"+locFilter+".ser");
    if (transcriptCacheFile.exists()) {
      logger.info("Loading transcripts from cache file...");
      mts = (MappableTranscript[]) SerialUtil.readObject(transcriptCacheFile);
    }
    
    // 2 - load from db if no cache file and save to cache file
    if (mts==null) {
      mts = loadTranscriptsFromDB(locFilter);
      SerialUtil.writeObject(mts, transcriptCacheFile);
    }

    logger.info("Loaded " + mts.length + " Transcripts in "
        + timer.stop().getDurationInSecs() + "secs.");

    return mts;
  }
    
    
  private MappableTranscript[] loadTranscriptsFromDB(final Location locFilter ) throws AdaptorException {
    List buf = new ArrayList();

    Iterator iter = null;
    if (locFilter != null) {
      logger.info("Loading transcripts for location: " + locFilter + " from database ...");
      iter = transcriptDriver.getGeneAdaptor().fetchIterator(locFilter, true);
    } else {
      logger.info("Loading all transcripts from database...");
      iter = transcriptDriver.getGeneAdaptor().fetchIterator(true);

    }

    while (iter.hasNext()) {
      Gene g = (Gene) iter.next();
      List ts = g.getTranscripts();
      for (int j = 0; j < ts.size(); j++) {
        Transcript t = (Transcript) ts.get(j);
        buf.add(new MappableTranscript(t, downStreamFlank));
      }
    }

    
    return (MappableTranscript[]) buf
        .toArray(new MappableTranscript[buf.size()]);

  }

  /**
   * Initialise the instance from command line parameters. Parses the command
   * line parameters and validates them before initiailising the instance. Run
   * without parameters or with -h or --help to see usage.
   * 
   * @see org.ensembl.driver.impl.CoreDriverImpl for db config file
   *      specification
   * @param commandLineArgs
   *          command line arguments, see description for more details.
   * @throws IOException
   * @throws SecurityException
   */
  public ProbeMapper(String[] commandLineArgs) throws ParseException,
      SecurityException, IOException {

    boolean showHelp = commandLineArgs.length == 0;

    LongOpt[] longopts = new LongOpt[] {
        new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h'),
        new LongOpt("skip-xref-check", LongOpt.NO_ARGUMENT, null, 's'),
        new LongOpt("verbose", LongOpt.NO_ARGUMENT, null, 'v'),
        new LongOpt("oligo-db", LongOpt.REQUIRED_ARGUMENT, null, 'a'),
        new LongOpt("transcript-db", LongOpt.REQUIRED_ARGUMENT, null, 't'),
        new LongOpt("output-db", LongOpt.REQUIRED_ARGUMENT, null, 'o'),
        new LongOpt("location", LongOpt.REQUIRED_ARGUMENT, null, 'l'),
        new LongOpt("log-file", LongOpt.NO_ARGUMENT, null, 'L'),
        new LongOpt("down-stream-flank", LongOpt.NO_ARGUMENT, null, 'f'),
        new LongOpt("threshold", LongOpt.REQUIRED_ARGUMENT, null, 'T'),
        new LongOpt("max-transcripts-per-composite", LongOpt.REQUIRED_ARGUMENT,
            null, 'n'),
        new LongOpt("dir", LongOpt.REQUIRED_ARGUMENT, null, 'd') };

    Getopt g = new Getopt("ProbeToTranscriptMappingApplication",
        commandLineArgs, "hva:t:o:l:L:f:T:n:d:s", longopts, false);
    int c;
    while ((c = g.getopt()) != -1) {
      switch (c) {

      case 'h':
        showHelp = true;
        break;

      case 'v':
        verbose = true;
        break;

      case 'o':
        outputDriverFilepath = g.getOptarg();
        break;

      case 'a':
        probeDriverFilepath = g.getOptarg();
        break;

      case 't':
        transcriptDriverFilepath = g.getOptarg();
        break;

      case 'l':
        locationFilter = new Location(g.getOptarg());
        break;

      case 'f':
        downStreamFlank = Integer.parseInt(g.getOptarg());
        break;

      case 'T':
        int percentage = Integer.parseInt(g.getOptarg());
        threshold = percentage / 100.0;
        break;

      case 'n':
        maxTranscriptsPerCompositeThreshold = Integer.parseInt(g.getOptarg());
        break;

      case 'd':
        workingDirectory = new File(g.getOptarg());
        break;

      case 'L':
        logFilename = g.getOptarg();
        break;
      
      case 's':
        skipXrefCheck= true;
        break;
      }
    }

    if (showHelp) {
      System.out.println(usage());
      System.exit(0);
    }

    if (g.getOptind() < commandLineArgs.length)
      defaultDriverFilepath = commandLineArgs[g.getOptind()];

    initLogging();

    if (downStreamFlank < 0)
      error("Down stream flank must be >= 0");

    // connect to separate input and output databases if specified
    // by user, otherwise use default
    outputDriver = createDriver("output", outputDriverFilepath,
        defaultDriverFilepath);
    probeDriver = createDriver("probe", probeDriverFilepath,
        defaultDriverFilepath);
    transcriptDriver = createDriver("transcript", transcriptDriverFilepath,
        defaultDriverFilepath);

  }

  private CoreDriver loadDriver(String description, String filepath) {
    CoreDriver d = null;
    filepath = new File(workingDirectory, filepath).getAbsolutePath();
    try {
      d = CoreDriverFactory.createCoreDriver(filepath);
    } catch (AdaptorException e) {
      error("Cannot connect to " + description
          + " database specified in file '" + filepath + "'\n", e);
    }
    if (d == null) {
      error("Failed to initialise " + description
          + " database specified in file " + filepath);
    }
    return d;
  }

  /**
   * Usage.
   * 
   * @return string containing command line usage for this program.
   */
  public String usage() {
    String usage = ""
        + "Maps MicroArray probesets and transcripts from ensembl database(s). "
        + "\nThe results are stored in an ensembl database as xrefs and "
        + "\na mapping log file (probeset2transcript.log) is written to the "
        + " working directory."
        + "\nUsage: "
        + "\n  ProbeMapper [OPTIONS] [CONFIG_FILE]"
        + "\n"
        + "\nCONFIG_FILE specifies the database to retrieve the probe and transcript data from "
        + "the database to write results to. Each of these datases can be different and"
        + "specified using the --oligo-probe-db (-a), --transcript-db (-t) and --output_db (-o) options."
        + "\n"
        + "\nWhere options are:"
        + "\n -v                                                        Print verbose information."
        + "\n     --verbose"
        + "\n -T THRESHOLD                                              Minimum percentage of composite to transcript hits for mapping. e.g. 60, 75."
        + "\n     --threshold=THRESHOLD"
        + "\n -f  FLANK                                                 Down stream flank in bases. Default is "
        + DEFAULT_DOWN_STREAM_FLANK
        + "\n     --down-stream-flank FLANK"
        + "\n -n  MAX_TRANSCRIPTS_PER_COMPOSITE_THRESHOLD               Max number of transcripts allowed per transcript (per sequence region). Default is "
        + DEFAULT_MAX_TRANSCRIPTS_PER_COMPOSITE
        + "."
        + "\n     --max-transcripts-per-composite MAX_TRANSCRIPTS_PER_COMPOSITE_THRESHOLD"
        + "\n -l LOCATION                                               Only map probes and transcripts in this location. e.g. chromosome:1:20m-21m"
        + "\n     --location=LOCATION"
        + "\n -s                                                        Skip potentially slow check for xrefs in target database."
        + "\n     --skip-xref-check"
        + "\n -a AFFY_DATABSE_CONFIG_FILE                               File specifying db containing microarray probes."
        + "\n      --oligo-probe-db=AFFY_DATABASE_CONFIG_FILE"
        + "\n -t TRANSCRIPT_DATABASE_CONFIG_FILE                        File specifying db containing transcript."
        + "\n     --transcript-db=TRANSCRIPT_DATABASE_CONFIG_FILE"
        + "\n -o OUTPUT_DATABSEB_CONFIG_FILE                            File specifying db to save results to."
        + "\n     --output-db OUTPUT_DATABSE_CONFIG_FILE"
        + "\n -d WORKING_DIR                                            Working directory. All file paths are relative to this directory. Default is \".\"."
        + "\n     --dir=WORKING_DIR"
        + "\n -L LOG_FILE                                               Log file. Default is "
        + DEFAULT_LOG_FILENAME
        + "."
        + "\n     --log-file LOG_FILE"
        + "\n -h                                                        Print this help message."
        + "\n     --help" + "\n";

    return usage;
  }

  /**
   * @return Returns the threshold.
   */
  public double getThreshold() {
    return threshold;
  }

  /**
   * @param threshold
   *          The threshold to set.
   */
  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }
}
