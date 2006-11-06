/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.driver.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.InvalidLocationException;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.impl.GeneImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.GeneAdaptor;
import org.ensembl.util.IDList;
import org.ensembl.util.IDMap;
import org.ensembl.util.NotImplementedYetException;
import org.ensembl.util.StringUtil;
import org.ensembl.util.Warnings;

public class GeneAdaptorImpl
  extends BaseFeatureAdaptorImpl
  implements GeneAdaptor {

  private String[] cols;

  
  
  
  
  public GeneAdaptorImpl(CoreDriverImpl driver) {
    super(driver, TYPE);
  }

  // -----------------------------------------------------------------
  // Implementation of required methods from BaseFeatureAdaptorImpl

  /*
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#tables()
   */
  protected String[][] tables() {

    String[][] tables = { { "gene", "g" }, {
        "gene_stable_id", "gsi" }, {
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

    // lazy loading prevents need for db connection at object construction.
    if (cols==null) {

      // Simple backward compatibility implementation sets old columns to NULL
      // if equivalent old columns don't exist in the table.
      // If data really needed and was available from other tables then 
      // reimplement based on code in previous ensj version(s).  

      cols = new String[]
      {
        "g.gene_id",
        "g.seq_region_id",
        "g.seq_region_start",
        "g.seq_region_end",
        "g.seq_region_strand",
        "g.analysis_id",
        schemaSpecificColumn("g.type as biotype", 31, "g.biotype" ),
        schemaSpecificColumn("NULL as source", 31, "g.source"), 
        "g.display_xref_id",
        schemaSpecificColumn("NULL as DESCRIPTION", 31, "g.description"),
        "gsi.stable_id",
        "gsi.version",
        schemaSpecificColumn("NULL as created_date", 25, "gsi.created_date"),
        schemaSpecificColumn("NULL as modified_date", 25, "gsi.modified_date"),
        "x.display_label",
        "exdb.db_name",
        "exdb.status",
        schemaSpecificColumn("NULL AS gene_status", 31, "g.confidence AS gene_status", 34, "g.status AS gene_status")
        };
    }

    return cols;

  }

  
  // -----------------------------------------------------------------

  /*
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#leftJoin()
   */
  public String[][] leftJoin() {

    String[][] lj = { { "gene_description", "gd.gene_id=g.gene_id" }, {
        "gene_stable_id", "gsi.gene_id=g.gene_id" }, {
        "xref", "x.xref_id=g.display_xref_id" }, {
        "external_db", "exdb.external_db_id=x.external_db_id" }
    };

    return lj;

  }

  // -----------------------------------------------------------------

  /*
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#createObject(java.sql.ResultSet)
   */
  public Object createObject(ResultSet rs) throws AdaptorException {

    Gene g = null;

    try {
      if (rs.next()) {

      	// we will add seq region name and coord sys later...
        Location loc = new Location(
            rs.getLong("seq_region_id"),
            rs.getInt("seq_region_start"),
            rs.getInt("seq_region_end"),
            rs.getInt("seq_region_strand"));

      	
        g = new GeneImpl(rs.getLong("seq_region_id"), loc);
        g.setInternalID(rs.getLong("gene_id"));
        g.setDriver(driver);
        g.setAnalysisID(rs.getLong("analysis_id"));
        g.setBioType(rs.getString("biotype"));
        g.setSource(rs.getString("source"));
        g.setDescription(rs.getString("description"));
        g.setAccessionID(rs.getString("stable_id"));
        g.setVersion(rs.getInt("version"));
        g.setCreatedDate( rs.getDate( "created_date"));
        g.setModifiedDate( rs.getDate( "modified_date"));
        // this can be null
        g.setDisplayName(rs.getString("display_label"));
        String status = rs.getString("gene_status");
        
        if(status != null)
          g.setStatus(status);
          //g.setStatus(status.intern());
        // display_xref_id, exdb.name, exdb.status - read from database,
        // used in Perl API - not used here?

      }

    } catch (InvalidLocationException e) {
      throw new AdaptorException("Error when building Location", e);
    } catch (SQLException e) {
      throw new AdaptorException("SQL error when building object", e);
    }

    return g;

  }

  // -----------------------------------------------------------------
  // Implementation of GeneAdaptor interface - see interface for Javadoc

  public Gene fetch(long internalID) throws AdaptorException {

    // just use base class method with appropriate return type
    return (Gene) super.fetchByInternalID(internalID);

  }

  /**
   * Fetches genes and optionally preloads their children.
   * 
   * The gene's children are transcripts, translations and exons.
   * Preloading them may be faster than loading lazy loading them.
   * 
   * @param internalIDs internalIDs of genes to fetch. 
   * @param loadChildren whether to prefetch translation and exons.
   * @return zero or more genes with the specified internalIDs.
   * @throws AdaptorException
   */
  public List fetch(long[] internalIDs, boolean loadChildren)
    throws AdaptorException {

    List gs = fetch(internalIDs);
    if (loadChildren)
      loadChildren(gs, null);
    return gs;

  }

  //-----------------------------------------------------------------

  //-----------------------------------------------------------------

  /**
   * Fetch genes overlapping location and optionally preload
   * their children.
   *
   * The gene's children are transcripts, translations and exons.
   * Preloading them may be faster than loading lazy loading them.
   * 
   * @param location location filter
   * @param loadChildren whether to preload children.
   * @return List of >=0 transcripts found at the specified location.
   */
  public List fetch(Location location, boolean loadChildren)
    throws AdaptorException {

    List gs = fetch(location);
    if (loadChildren) 
      loadChildren(gs, location.getCoordinateSystem());
      
    return gs;

  }

  //-----------------------------------------------------------------

  /**
   * @param genes genes to load exons and transcripts for
   * @param targetCoordinateSystem target coordinate system that exon and transcript locations should
   * be in. Can be null if location conversion is not required.
   */
  private void loadChildren(List genes, CoordinateSystem targetCoordinateSystem) throws AdaptorException {

  	if (genes.size()==0) return;
  	
    long[] geneIDs = new IDList(genes).toArray();
    List transcripts =
      driver.getTranscriptAdaptor().fetchByGeneIDs(geneIDs, true);

    // Collect transcripts and exons, bind gene to transcripts and
    // exons
    IDMap id2Gene = new IDMap(genes);
    IDMap geneID2Transcripts = new IDMap();
    IDMap geneID2Exons = new IDMap();
    for (int i = 0, n = transcripts.size(); i < n; i++) {

      Transcript t = (Transcript) transcripts.get(i);

      Gene g = (Gene) id2Gene.get(t.getGeneInternalID());
      t.setGene(g);

      long geneID = g.getInternalID();

      // collect transcripts for gene
      List ts = (List) geneID2Transcripts.get(geneID);
      if (ts == null) {
        ts = new ArrayList();
        geneID2Transcripts.put(geneID, ts);
      }
      ts.add(t);

      // collect exons from the transcript 
      IDMap es = (IDMap) geneID2Exons.get(g.getInternalID());
      if (es == null) {
        es = new IDMap();
        geneID2Exons.put(geneID, es);
      }
      List tes = t.getExons();
      for (int j = 0, m = tes.size(); j < m; j++) {
        Exon e = (Exon) tes.get(j);
        es.put(e);
      }
    }

    // bind transcripts and exons to gene
    for (int i = 0, n = genes.size(); i < n; i++) {

      Gene g = (Gene) genes.get(i);
      long geneID = g.getInternalID();

      List ts = (List) geneID2Transcripts.get(geneID);
      Collections.sort(ts);

      IDMap es = (IDMap) geneID2Exons.get(geneID);
      List exons = new ArrayList(es.values());
      Collections.sort(exons);

      g.setTranscriptsAndExons(ts, exons);
      if (targetCoordinateSystem!=null)
        g.setCoordinateSystem(targetCoordinateSystem, driver.getLocationConverter());
    }
  }

  //-----------------------------------------------------------------

  public Gene fetch(String accessionID) throws AdaptorException {

    List l =
      super.fetchByNonLocationConstraint("gsi.stable_id='" + accessionID + "'");
    if (l.size() == 0) {
      return null;
    } else if (l.size() > 1) {
      throw new AdaptorException(
        "Expeced one gene with accession ID "
          + accessionID
          + " but found "
          + l.size());
    } else {
      return (Gene) l.get(0);
    }
  }

  // -----------------------------------------------------------------

  public List fetchBySynonym(String synonym) throws AdaptorException {

    String translation =
      "SELECT gene_id FROM object_xref ox, xref x, transcript ts, translation tl WHERE"
        + " x.xref_id=ox.xref_id AND tl.transcript_id=ts.transcript_id AND tl.translation_id=ox.ensembl_id "
        + " AND ox.ensembl_object_type='Translation'";
    String transcript =
      "SELECT gene_id FROM object_xref ox, xref x, transcript ts WHERE"
        + " x.xref_id=ox.xref_id AND transcript_id=ox.ensembl_id "
        + " AND ox.ensembl_object_type='Transcript'";
    String gene = "SELECT ox.ensembl_id FROM object_xref ox, xref x WHERE "
    		+ " x.xref_id=ox.xref_id AND ox.ensembl_object_type='Gene'";
    
    String display = " AND x.display_label='" + synonym + "'";
    String dbprimary = " AND x.dbprimary_acc='" + synonym + "'";
    
    // order of these queries is important because more items likely to be
    // searched for are in display_label.
    String[] sql =
      {
    		translation + display,
    		transcript + display,
    		gene + display,
        translation + dbprimary,
				transcript + dbprimary,
				gene + dbprimary,
        };

    long[] geneIDs = fetchIDsBySQL(sql);

    return fetch(geneIDs);

  }

  // -----------------------------------------------------------------

  public void fetchAccessionID(Gene gene) throws AdaptorException {

    Warnings.deprecated(
      "Accession IDs are now fetched by default - fetchAccessionID() is no longer required.");

  }

  // -----------------------------------------------------------------

  /**
   * @deprecated since version 27.0. Use other fetch methods.
   */
  public List fetch(org.ensembl.datamodel.Query query) throws AdaptorException {

    List result = new ArrayList();

    String type = query.getType();

    // if type is set, it should be "ensembl"
    if (type == null
      || (type.length() == 0)
      || (type.length() > 0 && type.equalsIgnoreCase("ensembl"))) {
      if (query.getInternalID() > 0) {

        result.add((Gene) fetch(query.getInternalID()));

      } else if (
        query.getAccessionID() != null
          && query.getAccessionID().length() > 0) {

        result.add(fetch(query.getAccessionID()));

      } else if (query.getLocation() != null) {

        result = fetch(query.getLocation());

      }
    }

    // load exons and transcripts if required
    if (query.getIncludeChildren()) {

      TranscriptAdaptorImpl transcriptAdaptor = (TranscriptAdaptorImpl)driver.getTranscriptAdaptor();
      ExonAdaptorImpl exonAdaptor = (ExonAdaptorImpl) driver.getExonAdaptor();
      

      Iterator it = result.iterator();
      while (it.hasNext()) {

        Gene gene = (Gene) it.next();

        List transcripts =
          transcriptAdaptor.fetchByGeneID(gene.getInternalID());

        Map exons = new HashMap();
        // fetch all the exons for each transcript; avoid adding duplicate
        // exons
        Iterator transcriptIter = transcripts.iterator();
        while (transcriptIter.hasNext()) {
          Transcript t = (Transcript) transcriptIter.next();
          List transcriptExons =
            exonAdaptor.fetchAllByTranscript(t.getInternalID());
          ListIterator transcriptExonsIterator = transcriptExons.listIterator();
          // allows replacement as well
          while (transcriptExonsIterator.hasNext()) {
            Exon e = (Exon) transcriptExonsIterator.next();

            Long id = new Long(e.getInternalID());
            if (!exons.containsKey(id)) {
              exons.put(id, e);
            } else {
              transcriptExonsIterator.set(exons.get(id));
            }
          }
          // replace existing list of exons in transcript with new one
          t.setExons(transcriptExons);
        }
        gene.setTranscriptsAndExons(transcripts, new ArrayList(exons.values()));

      }

    }

    // Note query.includeSequence is currently ignored

    return result;

  }

  //---------------------------------------------------------------------
  /**
   * Fetch a list of genes by executing one or more SQL statements.
   * 
   * @param sql
   *          The array of SQL statements (as Strings) to execute.
   * @return An array of unique gene IDs retrieved by the SQL statement(s).
   *         Note that if the same gene ID is returned by more than one SQL
   *         statement it only appears once in the returned array.
   */
  private long[] fetchIDsBySQL(String[] sql) throws AdaptorException {

    List ids = new ArrayList();

    Connection conn = getConnection();

    try {
      for (int i = 0; i < sql.length; i++) {
        ResultSet rs = executeQuery(conn, sql[i]);
        while (rs.next()) {
          Long id = new Long(rs.getLong(1));
          if (!ids.contains(id))
            ids.add(id);
        }
      }
    } catch (SQLException se) {
      throw new AdaptorException(
        "Error while fetching synonyms: " + se.getMessage());
    }

    // need to return an array of long; can't use toArray because of dumb
    // Long vs long
    long[] idArray = new long[ids.size()];
    int i = 0;
    for (Iterator iter = ids.iterator(); iter.hasNext();) {
      idArray[i++] = ((Long) iter.next()).longValue();
    }

    CoreDriverImpl.close(conn);

    return idArray;

  }

  // -----------------------------------------------------------------

  public long store(Gene gene) throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented in new API");

  }

  // -----------------------------------------------------------------

  public void delete(Gene gene) throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented in new API");

  }

  // -----------------------------------------------------------------

  public void delete(long geneInternalID) throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented in new API");

  }

  /**
   * @see org.ensembl.driver.GeneAdaptor#fetchByInterproID(java.lang.String)
   */
  public List fetchByInterproID(String interproID) throws AdaptorException {
    List r = new ArrayList();

    String sql =
      "SELECT DISTINCT gene_id FROM interpro, protein_feature, translation, transcript "
        + "WHERE "
        + " interpro.id = protein_feature.hit_id "
        + " AND protein_feature.translation_id = translation.translation_id"
        + " AND translation.transcript_id = transcript.transcript_id "
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
        "Failed to get genes for interproID = " + interproID,
        e);
    } finally {
      close(conn);
    }

    return r;
  }

  // -----------------------------------------------------------------

}
