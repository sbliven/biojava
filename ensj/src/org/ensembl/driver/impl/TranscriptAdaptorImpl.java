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
package org.ensembl.driver.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.InvalidLocationException;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.Translation;
import org.ensembl.datamodel.impl.AttributeImpl;
import org.ensembl.datamodel.impl.TranscriptImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.GeneAdaptor;
import org.ensembl.driver.TranscriptAdaptor;
import org.ensembl.util.IDList;
import org.ensembl.util.IDMap;
import org.ensembl.util.LongSet;
import org.ensembl.util.NotImplementedYetException;
import org.ensembl.util.StringUtil;
import org.ensembl.util.Warnings;

/**
 * Implementation of an adaptor to fetch transcripts from the database.
 *
 * Most of the work is done in BaseFeatureAdaptorImpl.
 */

public class TranscriptAdaptorImpl
  extends BaseFeatureAdaptorImpl
  implements TranscriptAdaptor {

  /**
   * Simple data structure class holding transcript->exon mapping.
   */
  private class ExonTranscriptLink {
    final long transcriptID;
    final long exonID;
    final int rank;

    ExonTranscriptLink(long exonID, long transcriptID, int rank) {
      this.exonID = exonID;
      this.transcriptID = transcriptID;
      this.rank = rank;
    }
  }

  /**
   * Convenience collection for handling TranscriptExonLinks.
   */
  private class ExonTranscriptList extends ArrayList {

	private static final long serialVersionUID = 1L;

	long[] uniqueExonIDArray() {
      LongSet set = new LongSet();
      for (int i = 0, n = size(); i < n; i++) {
        ExonTranscriptLink link = (ExonTranscriptLink) get(i);
        set.add(link.exonID);
      }
      return set.to_longArray();
    }

  }

  private int[] numExonsPerTranscript;
  
  private String analysisColumn;
  
  private String[] cols;
  
  public TranscriptAdaptorImpl(CoreDriverImpl driver) {

    super(driver, TYPE);
  }

  //	-----------------------------------------------------------------
  // Implementation of required methods from BaseFeatureAdaptorImpl

  /*
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#tables()
   */
  protected String[][] tables() {

    String[][] tables = { { "transcript", "t" }, {
        "transcript_stable_id", "tsi" }, {
        "transcript_attrib", "ta" }, {
        "attrib_type", "at" }, {
        "xref", "x" }, {
        "external_db", "exdb" }
    };

    return tables;

  }

  // -----------------------------------------------------------------
  /*
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#columns()
   */
  protected String[] columns() {

    //  lazy loading prevents need for db connection at object construction.
    if (cols==null) {
      
      // Simple backward compatibility implementation sets old columns to NULL/0
      // if equivalent old columns don't exist in the table.
      // If data really needed and was available from other tables then 
      // reimplement based on code in previous ensj version(s).  
      
      cols = new String[]
      {
        "t.transcript_id",
        "t.seq_region_id",
        "t.seq_region_start",
        "t.seq_region_end",
        "t.seq_region_strand",
        "t.gene_id",
        "t.display_xref_id",
        "tsi.stable_id",
        "tsi.version",
        "tsi.created_date",
        "tsi.modified_date",
        "x.display_label",
        "exdb.db_name",
        "exdb.status",
        "ta.value",
        "at.code",
        "at.name",
        "at.description",
        schemaSpecificColumn("NULL AS transcript_status", 31, "t.confidence AS transcript_status", 34, "t.status AS transcript_status"),
        schemaSpecificColumn("NULL as biotype", 31, "t.biotype"),
        schemaSpecificColumn("0 as analysis_id", 38, "t.analysis_id")};
    }
    return cols;

  }

  
 
  // -----------------------------------------------------------------

  /*
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#leftJoin()
   */
  public String[][] leftJoin() {

    String[][] lj =
      { { "transcript_stable_id", "tsi.transcript_id = t.transcript_id" }, {
        "transcript_attrib", "ta.transcript_id=t.transcript_id" }, {
        "attrib_type", "at.attrib_type_id = ta.attrib_type_id" }, {
        "xref", "x.xref_id = t.display_xref_id" }, {
        "external_db", "exdb.external_db_id = x.external_db_id" }
    };

    return lj;

  }

  // -----------------------------------------------------------------

  /*
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#createObject(java.sql.ResultSet)
   */
  public Object createObject(ResultSet rs) throws AdaptorException {

    // TODO comment - can come in with rs before or after first row

    Transcript t = null;

    try {
      boolean more = !rs.isAfterLast();
      if (rs.getRow() == 0)
        more = rs.next();
      final long fakeID = -999;
      long currentID = fakeID;
      
      while (more) {

        long internalID = rs.getLong("transcript_id");
        if (currentID != fakeID && currentID != internalID)
          break;

        if (currentID != internalID) {
          
          currentID = internalID;

          t = new TranscriptImpl(driver);
          t.setInternalID(internalID);

          // genericFetch(...) will update this location
          // with seqRegionName and CS after all Transcripts
          // have been created.
          Location loc = new Location(
              rs.getLong("seq_region_id"),
              rs.getInt("seq_region_start"),
              rs.getInt("seq_region_end"),
              rs.getInt("seq_region_strand"));
          
          t.setLocation(loc);
          if (t.getLocation() == null) {
            System.out.println(
              "Warning location is null for transcript " + t.getInternalID());
          }

          t.setAccessionID(rs.getString("stable_id"));
          t.setVersion(rs.getInt("version"));
          t.setCreatedDate( rs.getDate( "created_date"));
          t.setModifiedDate( rs.getDate( "modified_date" ));
          t.setGeneInternalID(rs.getLong("gene_id"));
          t.setDisplayName(rs.getString("display_label"));
          t.setBioType(rs.getString("biotype"));
          t.setAnalysisID(rs.getLong("analysis_id"));
          String status = rs.getString("transcript_status");
          if(status != null)
          	t.setStatus(status.intern());
          t.setDriver(getDriver());
        }
        
        // load attributes
        String value = rs.getString("value");
        if (value != null) {
          t.addAttribute(
            new AttributeImpl(
              rs.getString("code"),
              rs.getString("name"),
              rs.getString("description"),
              value));
        }

        more = rs.next();

      }

    } catch (InvalidLocationException e) {
      throw new AdaptorException("Error when building Location", e);
    } catch (SQLException e) {
      throw new AdaptorException("SQL error when building object", e);
    }

    return t;

  }

  // -----------------------------------------------------------------
  // Implementation of TranscriptAdaptor interface - see interface for Javadoc

  public Transcript fetch(long internalID) throws AdaptorException {

    Transcript result = null;

    // just use base class method with appropriate return type
    result = (Transcript) fetchByInternalID(internalID);

    return result;

  }

  //-----------------------------------------------------------------

  /**
   * Fetches transcripts and their translations and exons 
   * if requested.
   * @param internalIDs internalIDs of transcripts to fetch. 
   * @param loadChildren whether to prefetch translation and exons.
   * @return zero or more Features with the specified internalIDs.
   * @throws AdaptorException
   */
  public List fetch(long[] internalIDs, boolean loadChildren)
    throws AdaptorException {

    List transcripts = fetch(internalIDs);
    if (loadChildren)
      loadChildren(transcripts);
    return transcripts;

  }

  /**
   * Fetches all children and loads their child data if
   * loadChildren is true.
   * 
   * Preloading the child data is often faster than lazy
   * loading it on demand, depending on how much data has to
   * be loaded.
   * 
   * @param loadChildren whether or not to preload children.
   * @return zero or more Features with the specified internalIDs.
   * @throws AdaptorException
   */
  public List fetchAll(boolean loadChildren) throws AdaptorException {

    List transcripts = fetchAll();
    if (loadChildren)
      loadChildren(transcripts);
    return transcripts;
  }

  private void loadChildren(List transcripts) throws AdaptorException {

    loadExons(transcripts);
    loadTranslations(transcripts);
  }

  private void loadTranslations(List transcripts) throws AdaptorException {

    if (transcripts.size() == 0)
      return;

    long[] transcriptIDs = new IDList(transcripts).toArray();

    List translations =
      driver.getTranslationAdaptor().fetchByTranscripts(transcriptIDs, false);

    // bind transcripts to translations and translations to exons
    IDMap id2transcript = new IDMap(transcripts);
    for (int i = 0, n = translations.size(); i < n; i++) {
      Translation tn = (Translation) translations.get(i);
      Transcript tt =
        (Transcript) id2transcript.get(tn.getTranscriptInternalID());
      tn.setTranscript(tt);
      tt.setTranslation(tn);
      IDMap id2exon = new IDMap(tt.getExons());
      tn.setStartExon((Exon) id2exon.get(tn.getStartExonInternalID()));
      tn.setEndExon((Exon) id2exon.get(tn.getEndExonInternalID()));
    }

  }

  private void loadExons(List transcripts) throws AdaptorException {

    if (transcripts.size() == 0)
      return;

    IDList transcriptIDs = new IDList(transcripts);

    // fetch sorted transcript->exon links
    ExonTranscriptList exonTranscripts = new ExonTranscriptList();
    StringBuffer sql = new StringBuffer();
    sql.append(
      "SELECT exon_id, transcript_id, rank FROM exon_transcript WHERE transcript_id IN (");
    sql.append(transcriptIDs.toCommaSeparatedString()).append(")");
    sql.append(" ORDER BY transcript_id, rank");

    // last exon first for a trick later
    Connection conn = null;
    try {
      conn = getConnection();
      ResultSet rs = executeQuery(conn, sql.toString());
      while (rs.next())
        exonTranscripts.add(
          new ExonTranscriptLink(rs.getLong(1), rs.getLong(2), rs.getInt(3)));
    } catch (SQLException e) {
      throw new AdaptorException(
        "Failed to retrieve transcript->exon links from db.",
        e);
    } finally {
      close(conn);
    }

    // fetch exons
    List exons =
      driver.getExonAdaptor().fetch(exonTranscripts.uniqueExonIDArray());

    // bind transcripts to exons
    IDMap exonCache = new IDMap(exons);
    IDMap transcriptCache = new IDMap(transcripts);

    //    for(Iterator iter = transcriptCache.keySet().iterator();iter.hasNext();){
    //      Object key = iter.next();
    //      Transcript tt = (Transcript) transcriptCache.get(key);
    //      System.out.println("Cache entry: <" + key + ">\t" + tt.getAccessionID());
    //    }

    Transcript t = null;
    List tExons = null;
    for (int i = 0, n = exonTranscripts.size(); i < n; i++) {
      ExonTranscriptLink etl = (ExonTranscriptLink) exonTranscripts.get(i);
      if (t == null || etl.transcriptID != t.getInternalID()) {
        t = (Transcript) transcriptCache.get(etl.transcriptID);
        //        System.out.println(etl.transcriptID);
        //        System.out.println(t);
        //        System.out.println(transcriptCache.size());

        // because exons are sorted in reverse rank order the first rank is actually the 
        // transcripts last so it tells us how many exons there are.
        tExons = new ArrayList();
        t.setExons(tExons);
      }
      Exon e = (Exon) exonCache.get(etl.exonID);
      tExons.add(e);
    }

  }

  //-----------------------------------------------------------------

  /**
   * @return transcript with specified accession, or null if non found.
   */
  public Transcript fetch(String accessionID) throws AdaptorException {

    //		optimisation: do a separate SQL query to get the internal ID,
    // then call fetch(internalID)

    List l =
      super.fetchByNonLocationConstraint("tsi.stable_id='" + accessionID + "'");
    if (l.size() == 0) {
      return null;
    } else if (l.size() > 1) {
      throw new AdaptorException(
        "Expeced one transcript with accession ID "
          + accessionID
          + " but found "
          + l.size());
    } else {
      return (Transcript) l.get(0);
    }

  }

  //----------------------------------------------------------------- 

  private Gene fetchGeneByTranscriptID(long transcriptID)
    throws AdaptorException {

    if (transcriptID < 1)
      throw new IllegalArgumentException(
        "Can not lazy load transcript because invalid internalID, must be >0: "
          + transcriptID);

    String sql =
      "SELECT gene_id FROM transcript WHERE transcript_id=" + transcriptID;
    Connection conn = null;
    conn = getConnection();
    ResultSet rs = executeQuery(conn, sql);
    final long geneID;
    try {
      geneID = rs.next() ? rs.getLong(1) : -1;
    } catch (SQLException e) {
      throw new AdaptorException(
        "Failed to find gene ID corresponding to transcript ID: "
          + transcriptID,
        e);
    } finally {
      close(conn);
    }

    return (geneID == -1) ? null : driver.getGeneAdaptor().fetch(geneID);
  }

  //-----------------------------------------------------------------

  public Transcript fetchComplete(final Transcript transcript)
    throws AdaptorException {

    //  Load the gene and transcript parents of this exon and insert
    // references to this exon into them. 

    // exonID -> geneID
    final long transcriptID = transcript.getInternalID();

    // try to load gene for transcript
    Gene gene = fetchGeneByTranscriptID(transcriptID);
    gene.getTranscripts(); // force lazy load
    gene.getExons(); // force lazy load

    if (gene != null) {

      // set the gene in the transcript
      transcript.setGene(gene);

      //  Replace relevant transcript references in gene.transcripts and exons
      List transcripts = gene.getTranscripts();
      final int nTranscripts = transcripts.size();
      for (int t = 0; t < nTranscripts; ++t) {

        Transcript geneTranscript = (Transcript) transcripts.get(t);
        if (geneTranscript.getInternalID() == transcriptID) {

          // update gene.transcripts
          transcripts.set(t, transcript);
          transcript.setExons(geneTranscript.getExons());

          // update transcript.exons.transcripts 
          final List transcriptExons = transcript.getExons();
          for (int i = 0, n = transcriptExons.size(); i < n; i++) {
            Exon transcriptExon = (Exon) transcriptExons.get(i);
            final List transcriptExonTranscripts =
              transcriptExon.getTranscripts();
            for (int j = 0, n2 = transcriptExonTranscripts.size();
              j < n2;
              j++) {
              Transcript transcriptExonTranscript =
                (Transcript) transcriptExonTranscripts.get(j);
              if (transcriptExonTranscript.getInternalID() == transcriptID)
                transcriptExonTranscripts.set(j, transcript);
            }
          }

          // update transcript <-> translation
          Translation translation = geneTranscript.getTranslation();
          if (translation != null) {
            transcript.setTranslation(translation);
            translation.setTranscript(transcript);
          }

        } 

      }

    } else {
      // no gene corresponding to transcript so just set exons
      List exons =
        driver.getExonAdaptor().fetchAllByTranscript(transcriptID);
      transcript.setExons(exons);
      for (int i = 0, n = exons.size(); i < n; i++) {
        Exon exon = (Exon) exons.get(i);

        // independant copy of this list incase user decides to join some exons
        // with a new transcript
        List transcripts = new ArrayList();
        transcripts.add(transcript);
        exon.setTranscripts(transcripts);
      }

    }

    return transcript;
  }

  //-----------------------------------------------------------------

  /**
   * Fetch transcripts overlapping location 
   * with/out child exons and translations loaded.
   * 
   * Setting "loadChildren=true" will cause the exons
   * and translations to be preloaded in an effiecient way. 
   * This will make
   * accessing them much faster than if "loadChildren=false".
   * In the later case the exons and translations are lazy loaded
   * as needed. If you don't want to access the exons or translations
   * or only a few then not preloading them might be more efficient.
   * 
   * @param location location filter
   * @param loadChildren whether to preload children.
   * @return List of >=0 transcripts found at the specified location.
   */
  public List fetch(Location location, boolean loadChildren)
    throws AdaptorException {

    List transcripts = fetch(location);
    if (loadChildren)
      loadChildren(transcripts);
    return transcripts;

  }

  // -----------------------------------------------------------------

  /**
   * @return List of >=0 transcripts matching the query.
   * @deprecated use one of the other fetch methods.
   */
  public List fetch(org.ensembl.datamodel.Query query) throws AdaptorException {

    List result = new ArrayList();

    if (query.getInternalID() > 0) {

      result.add((Transcript) fetch(query.getInternalID()));

    } else if (
      query.getAccessionID() != null && query.getAccessionID().length() > 0) {

      result.add(fetch(query.getAccessionID()));

    } else if (query.getLocation() != null) {

      result = fetch(query.getLocation());

    }

    if (query.getIncludeChildren())
      loadChildren(result);

    return result;

  }

  // -----------------------------------------------------------------
  public List fetchBySynonym(String synonym) throws AdaptorException {

    String translation =
      "SELECT gene_id, ts.transcript_id FROM object_xref ox, xref x, transcript ts, translation tl WHERE"
        + " x.xref_id=ox.xref_id AND tl.transcript_id=ts.transcript_id AND tl.translation_id=ox.ensembl_id "
        + " AND ox.ensembl_object_type='Translation'";
    String transcript =
      "SELECT gene_id, ts.transcript_id FROM object_xref ox, xref x, transcript ts WHERE"
        + " x.xref_id=ox.xref_id AND transcript_id=ox.ensembl_id "
        + " AND ox.ensembl_object_type='Transcript'";
    
    String display = " AND x.display_label='" + synonym + "'";
    String dbprimary = " AND x.dbprimary_acc='" + synonym + "'";
    
    // order of these queries is important because more items likely to be
    // searched for are in display_label.
    String[] sql =
      {
    		translation + display,
    		transcript + display,
        translation + dbprimary,
				transcript + dbprimary,
        };

 	
    List transcripts = new ArrayList();
    List genes = new ArrayList();

    // load list of transcripts and genes
    sqlToTranscriptAndGeneIDs(sql, genes, transcripts);

    // load all genes
    GeneAdaptor geneAdaptor = driver.getGeneAdaptor();
    for (int i = 0; i < genes.size(); i++) {
      Long id = (Long) genes.get(i);
      Gene g = geneAdaptor.fetch(id.longValue());
      genes.set(i, g);
    }

    // replace each transcript id with the relevant transcript in one of the
    // genes. For two or more transcripts from the same gene this is faster
    // than loading each one individually which would couse the gene to be
    // loaded multiple times.
    for (int t = 0; t < transcripts.size(); ++t) {

      long tID = ((Long) transcripts.get(t)).longValue();

      boolean found = false;

      for (int g = 0; !found && g < genes.size(); ++g) {
        List geneTranscripts = ((Gene) genes.get(g)).getTranscripts();
        for (int gt = 0; !found && gt < geneTranscripts.size(); ++gt) {
          Transcript tt = ((Transcript) geneTranscripts.get(gt));
          if (tID == tt.getInternalID()) {
            transcripts.set(t, tt);
            found = true;
          }
        }
      }

      if (!found)
        throw new AdaptorException("Failed to find transcript " + tID);

    }

    return transcripts;

  }

  private void sqlToTranscriptAndGeneIDs(
    String[] sql,
    List genes,
    List transcripts)
    throws AdaptorException {

    Connection conn = null;

    try {

      conn = getConnection();

      for (int i = 0; i < sql.length && genes.size() == 0; ++i) {

        ResultSet rs = executeQuery(conn, sql[i]);

        // compile a list of unique gene and transcript ids
        while (rs.next()) {

          Long geneID = new Long(rs.getLong(1));
          if (!genes.contains(geneID))
            genes.add(geneID);

          Long transcriptID = new Long(rs.getLong(2));
          if (!transcripts.contains(transcriptID))
            transcripts.add(transcriptID);
        }
      }
    } catch (SQLException e) {
      throw new AdaptorException("Failed to find item by synonym:", e);
    } finally {
      close(conn);
    }

  }

  // -----------------------------------------------------------------
  /**
    * Retrieves transcript's accession from persistent store and sets transcript.accession.
    */
  public void fetchAccessionID(Transcript transcript) throws AdaptorException {

    Warnings.deprecated(
      "Accession IDs are now fetched by default - fetchAccessionID() is no longer required.");

  }

  // -----------------------------------------------------------------
  public void fetchVersion(Transcript transcript) throws AdaptorException {

    Warnings.deprecated(
      "Versions are now fetched by default - fetchAccessionID() is no longer required.");

  }

  // -----------------------------------------------------------------
  /**
   * Array index = transcript.internalID, value = num of exons.
   */
  int[] getNumExonsPerTranscript() throws AdaptorException {
    if (numExonsPerTranscript == null) {
      Connection conn = null;
      try {

        conn = getConnection();

        String sql = "select max( transcript_id ) from exon_transcript";
        ResultSet rs = conn.createStatement().executeQuery(sql);
        rs.next();
        int arrayLen = rs.getInt(1) + 1;
        numExonsPerTranscript = new int[arrayLen];

        sql =
          "select transcript_id, max(rank) from exon_transcript group by transcript_id;";
        rs = conn.createStatement().executeQuery(sql);
        while (rs.next()) {
          numExonsPerTranscript[rs.getInt(1)] = rs.getInt(2);
        }

      } catch (SQLException e) {
        throw new AdaptorException(
          "Failed to build numExonsPerTranscriptCache",
          e);
      } finally {
        close(conn);
      }

    }

    return numExonsPerTranscript;
  }

  void setNumExonsPerTranscript(int[] numExonsPerTranscript) {
    this.numExonsPerTranscript = numExonsPerTranscript;
  }

  boolean hasAllExons(Transcript transcript) throws AdaptorException {

    // Force cache to be loaded.
    if (numExonsPerTranscript == null)
      getNumExonsPerTranscript();

    int internalID = (int) transcript.getInternalID();

    if (internalID < 1)
      throw new AdaptorException(
        "Can't check this transcript because internalID invalid : "
          + internalID);

    if (internalID > numExonsPerTranscript.length)
      throw new AdaptorException(
        "Can't check this transcript because internalID not in cache : "
          + internalID);

    int expectedNumExons = numExonsPerTranscript[internalID];

    if (expectedNumExons == 0)
      throw new AdaptorException(
        "Expected number of exons is 0 for this transcript : " + internalID);

    return expectedNumExons == transcript.getExons().size();
  }

  // -----------------------------------------------------------------
  /**
   * Retrieve a transcript given the accession ID of its translation.
   * @param translationAccession The stable/accession ID of the translation.
   * @return The Transcript associated with translationAccession, or null if none found.
   */
  public Transcript fetchByTranslation(String translationAccession)
    throws AdaptorException {

    Transcript t = null;

    Connection conn = getConnection();

    String sql =
      "SELECT t.transcript_id "
        + "FROM   translation_stable_id tsi, translation t "
        + "WHERE  tsi.stable_id = '"
        + translationAccession
        + "' "
        + "AND    t.translation_id = tsi.translation_id";

    try {

      ResultSet rs = executeQuery(conn, sql);
      if (rs.next()) {
        long id = rs.getLong(1);
        t = fetch(id);
      }
    } catch (Exception se) {
      throw new AdaptorException(
        "Error while fetching transcript for translation "
          + translationAccession
          + "; SQL="
          + sql
          + "\n "
          + se.getMessage());
    }

    CoreDriverImpl.close(conn);

    return t;

  }

  //	-----------------------------------------------------------------
  /**
   * Retrieve a transcript given the ID of its translation.
   * @param translationID The internal ID of the translation.
   * @return The Transcript associated with translationID, or null if none found.
   */
  public Transcript fetchByTranslation(long translationID)
    throws AdaptorException {

    long id = -1;

    Connection conn = null;
    try {
    	conn = getConnection();
    	String sql =
        "SELECT t.transcript_id FROM translation t WHERE t.translation_id = "
          + translationID;
    	ResultSet rs = executeQuery(conn, sql);
      if (rs.next()) 
        id = rs.getLong("transcript_id");
        
    } catch (Exception e) {
      throw new AdaptorException(
        "Error while fetching transcript for translation "
          + translationID
          , e);

    } finally {
    	close(conn);
    }

    return (id==-1) ? null : fetch(id) ;

  }

  // -----------------------------------------------------------------

  /**
   * Get all the transcripts associated with the gene.
   * @param geneID The gene ID to search for.
   * @return Zero more transcripts belonging to the gene.
   * @throws AdaptorException
   * @deprecated use fetchByGeneID(long) instead.
   */
  public List fetchAllByGeneID(long geneID) throws AdaptorException {
    return fetchByGeneID(geneID);
  }

  // -----------------------------------------------------------------

  /**
   * Get all the transcripts associated with a particular gene ID.
   * @param geneID The gene ID to search for.
   * @return A list of all the transcripts for that gene.
   */
  public List fetchByGeneID(long geneID) throws AdaptorException {

    return fetchByNonLocationConstraint("t.gene_id = " + geneID);

  }

  // -----------------------------------------------------------------

  /**
   * Get all the transcripts associated with the specified genes.
   * @param geneIDs internal IDs of genes.
   * @return A list of all the transcripts for that genes in no partticular order.
   */
  public List fetchByGeneIDs(long[] geneIDs, boolean loadChildren)
    throws AdaptorException {

    List r =
      fetchByNonLocationConstraint(
        "t.gene_id IN ( " + StringUtil.toString(geneIDs) + ")");

    if (loadChildren)
      loadChildren(r);

    return r;

  }

  // -----------------------------------------------------------------
  /**
   * Get all the translations associated with a particular exon.
   * @param exonAccession The accession/stable ID of the exon.
   * @return The transcript(s) associated with this exon.
   */
  public List fetchAllByExonAccession(String exonAccession)
    throws AdaptorException {

    List l = new ArrayList();

    Connection conn = getConnection();

    String sql =
      "SELECT et.transcript_id FROM exon_transcript AS et, exon_stable_id AS esi "
        + "WHERE esi.exon_id = et.exon_id AND esi.stable_id = '"
        + exonAccession
        + "'";

    try {

      ResultSet rs = executeQuery(conn, sql);
      while (rs.next()) {
        long id = rs.getLong(1);
        l.add((Transcript) fetch(id));
      }
    } catch (Exception se) {
      throw new AdaptorException(
        "Error while fetching transcript for exon "
          + exonAccession
          + "; SQL="
          + sql
          + "\n "
          + se.getMessage());
    }

    CoreDriverImpl.close(conn);

    return l;

  }

  // -----------------------------------------------------------------

  public void store(Transcript transcript) throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented in new API");

  }

  // -----------------------------------------------------------------

  /**
   * Stores transcript and 'children' optionally exons and translation.
   */
  void store(Connection conn, Transcript transcript, boolean loadChildren)
    throws AdaptorException, SQLException {

    throw new NotImplementedYetException("Not yet implemented in new API");

  }

  void storeStableID(
    Connection conn,
    StringBuffer sql,
    long transcriptID,
    String stableID)
    throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented in new API");

  }


  // -----------------------------------------------------------------

  public void delete(Transcript transcript) throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented in new API");

  }

  // -----------------------------------------------------------------

  public void delete(long transcriptInternalID) throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented in new API");

  }

  // -----------------------------------------------------------------

  /**
   * @see org.ensembl.driver.TranscriptAdaptor#fetchByInterproID(java.lang.String)
   */
  public List fetchByInterproID(String interproID) throws AdaptorException {
    List r = new ArrayList();

    String sql =
      "SELECT DISTINCT transcript_id FROM interpro, protein_feature, translation "
        + "WHERE "
        + " interpro.id = protein_feature.hit_id "
        + " AND protein_feature.translation_id = translation.translation_id"
        + " AND interpro.interpro_ac='"
        + interproID
        + "'";

    Connection conn = null;
    try {
      conn = getConnection();
      ResultSet rs = executeQuery(conn, sql);

      while (rs.next())
        r.add(fetch(rs.getLong(1)));

    } catch (SQLException e) {
      throw new AdaptorException(
        "Failed to get transcripts for interproID = " + interproID,
        e);
    } finally {
      close(conn);
    }

    return r;
  }

  public List fetchByGeneIDs(long[] geneIDs) throws AdaptorException {
    return fetchByGeneIDs(geneIDs, false);
  }

  /**
   * @throws AdaptorException
   * @see org.ensembl.driver.TranscriptAdaptor#fetchSupportingFeatures(long)
   */
  public List fetchSupportingFeatures(long transcriptID) throws AdaptorException {
    List l = new ArrayList();
    
    // get all links
    Connection conn = null;
    try {
      conn = getConnection();

      ResultSet rs =
        executeQuery(
          conn,
          "SELECT sf.feature_type, sf.feature_id "
            + "FROM   transcript_supporting_feature sf "
            + "WHERE  transcript_id = "
            + transcriptID);
      
      while( rs.next() ) {

        // follow link to get supporting features
        String type = rs.getString(1);
        if ( "dna_align_feature".equals(type))
          l.add(driver.getDnaDnaAlignmentAdaptor().fetch(rs.getLong(2)));
        else if  ("protein_align_feature".equals(type))      
          l.add(driver.getDnaProteinAlignmentAdaptor().fetch(rs.getLong(2)));

      }
      rs.close();
      
    } catch (SQLException e) {
      throw new AdaptorException(
        "Failed to fetch supporting evidence for transcript " + transcriptID,
        e);
    } finally {
      close(conn);
    }

  return l;
  }
  // -----------------------------------------------------------------
}
