/*
 * Copyright (C) 2003 EBI, GRL
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
package org.ensembl.driver.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ensembl.datamodel.ExternalDatabase;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.Translation;
import org.ensembl.datamodel.impl.AttributeImpl;
import org.ensembl.datamodel.impl.TranslationImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.ConfigurationException;
import org.ensembl.driver.ExonAdaptor;
import org.ensembl.driver.ExternalDatabaseAdaptor;
import org.ensembl.driver.TranscriptAdaptor;
import org.ensembl.driver.TranslationAdaptor;
import org.ensembl.util.IDSet;
import org.ensembl.util.NotImplementedYetException;
import org.ensembl.util.StringUtil;

public class TranslationAdaptorImpl
  extends BaseAdaptor
  implements TranslationAdaptor {

  private static final Logger logger =
    Logger.getLogger(TranslationAdaptorImpl.class.getName());

  public TranslationAdaptorImpl(CoreDriverImpl driver) {

    super(driver);

//    try {
//      transcriptAdaptor =
//        (TranscriptAdaptorImpl) driver.getTranscriptAdaptor();
//      // done this way to avoid problems with ordering of adaptors in CoreDriverImpl
//      exonAdaptor = (ExonAdaptorImpl) driver.getExonAdaptor();
//    } catch (AdaptorException e) {
//      System.out.println("Error creating adaptors: ");
//      e.printStackTrace();
//    }

  }

  // -----------------------------------------------------------------

  /**
   * Does nothing. Need to disable the default configure() method in BaseAdaptor.
   */
  void configure() throws ConfigurationException {

  }

  // -----------------------------------------------------------------
  public String getType() throws AdaptorException {

    return TYPE;

  }

  // -----------------------------------------------------------------

  public Translation fetch(long internalID) throws AdaptorException {

    return fetch(internalID, true);

  }

  // -----------------------------------------------------------------

  public Translation fetch(String accessionID) throws AdaptorException {

    // find internal ID of gene related to this translation
    Connection conn = null;
    long geneInternalID;
    try {
      String sql =
        "SELECT gene_id from translation_stable_id tsi, translation tn, transcript tt "
          + " WHERE tsi.stable_id='"
          + accessionID
          + "' "
          + " AND tsi.translation_id = tn.translation_id "
          + " AND tn.transcript_id = tt.transcript_id ";
      conn = getConnection();
      logger.fine(sql);
      ResultSet rs = conn.createStatement().executeQuery(sql);

      if (!rs.next())
        return null;

      geneInternalID = rs.getLong(1);
    } catch (SQLException e) {
      throw new AdaptorException(
        "Failed to find translation " + accessionID,
        e);
    } finally {
      close(conn);
    }

    // load gene
    Gene gene = driver.getGeneAdaptor().fetch(geneInternalID);

    // return the relevant translation from inside the gene; this will return
    // a translation which is part of a complete object graph.
    List transcripts = gene.getTranscripts();
    for (int i = 0; i < transcripts.size(); ++i) {
      Transcript transcript = (Transcript) transcripts.get(i);
      Translation translation = transcript.getTranslation();
      if (accessionID.equalsIgnoreCase(translation.getAccessionID()))
        return translation;
    }

    return null;
  }

  // -----------------------------------------------------------------

  /**
   * This only works with xrefs that maps directly to Translations.
   * 
   * @return Translations matching the synonym, empty list if non found.
   */
  public List fetchBySynonym(String synonym) throws AdaptorException {

    // The synonym will often match multiple translations in the same gene
    // and we only want to load each gene once. We then extract the matching
    // translations from these genes.

    List translations = Collections.EMPTY_LIST;

    Set geneIDs = new HashSet();
    Set translationIDs = new HashSet();
    loadIDsViaSynonym(synonym, geneIDs, translationIDs);

    if (translationIDs.size() > 0) {
      // iterate over the genes and their translations looking for translations
      // that matched the synonym.
      translations = new ArrayList(translationIDs.size());
      Iterator geneIDIter = geneIDs.iterator();
      while (geneIDIter.hasNext()) {
        Long geneID = (Long) geneIDIter.next();
        Gene gene = driver.getGeneAdaptor().fetch(geneID.longValue());
        List transcripts = gene.getTranscripts();
        for (int i = 0; i < transcripts.size(); ++i) {
          Transcript t = (Transcript) transcripts.get(i);
          Translation tn = t.getTranslation();
          if (translationIDs.contains(new Long(tn.getInternalID())))
            translations.add(tn);
        }
      }
    }

    return translations;
  }

  // -----------------------------------------------------------------
  /**
   * Constructs sql statements that search for gene+translation ids corresponding to synonym. Inserts IDs into sets.
   */
  private void loadIDsViaSynonym(
    String synonym,
    Set geneIDs,
    Set translationIDs)
    throws AdaptorException {

    String[] sql = new String[] {
      // Translations with a display_label synonym
      "SELECT "
        + "    ts.gene_id, ox.ensembl_id "
        + " FROM "
        + "    object_xref ox, xref x, translation tl, transcript ts "
        + " WHERE "
        + "    x.xref_id=ox.xref_id and tl.translation_id=ox.ensembl_id "
        + "    AND ts.transcript_id=tl.transcript_id "
        + "    AND ox.ensembl_object_type='Translation'"
        + "    and x.display_label='"
        + synonym
        + "'"

      // Translations with a dbprimary_acc synonym
      ,
"SELECT "
        + "    ts. gene_id, ox.ensembl_id "
        + " FROM "
        + "    object_xref ox, xref x, translation tl, transcript ts "
        + " WHERE "
        + "    x.xref_id=ox.xref_id and tl.translation_id=ox.ensembl_id "
        + "    AND ts.transcript_id=tl.transcript_id "
        + "    AND ox.ensembl_object_type='Translation'"
        + "    AND x.dbprimary_acc='"
        + synonym
        + "'" };

    Connection conn = null;
    try {
      conn = getConnection();
      for (int i = 0; i < sql.length; ++i) {
        logger.fine(sql[i]);
        ResultSet rs = executeQuery(conn, sql[i]);
        while (rs.next()) {
          geneIDs.add(new Long(rs.getLong(1)));
          translationIDs.add(new Long(rs.getLong(2)));
        }
      }
    } catch (Exception e) {
      throw new AdaptorException("Failed to retrieve translation synonyms", e);
    } finally {
      close(conn);
    }

  }

  // -----------------------------------------------------------------
  /**
   * @return Translation matching internalID, or null if non found in persistant store.
   */
  public Translation fetch(long internalID, boolean loadChildren)
    throws AdaptorException {

    // Now just gets the transcript ID and uses fetchByTranscript
    Transcript transcript = ((TranscriptAdaptorImpl)driver.getTranscriptAdaptor()).fetchByTranslation(internalID);

    if (transcript == null)
      return null;

    return fetchByTranscript(transcript.getInternalID(), loadChildren);

  }

  public List fetchByTranscripts(
    long[] transcriptInternalIDs,
    boolean loadChildren)
    throws AdaptorException {

    if (transcriptInternalIDs.length == 0)
      return Collections.EMPTY_LIST;

    List translations = new ArrayList();

    String sql =
      "SELECT tl.translation_id, tl.transcript_id, tl.start_exon_id, tl.end_exon_id "
        + ", tl.seq_start, tl.seq_end "
        + ", tlsi.stable_id, tlsi.version, tlsi.created_date, tlsi.modified_date "
        + ", ta.value, at.code, at.name, at.description"
        + " FROM translation tl "
        + " LEFT JOIN translation_stable_id tlsi ON tlsi.translation_id = tl.translation_id "
        + " LEFT JOIN translation_attrib ta ON ta.translation_id=tl.translation_id"
        + " LEFT JOIN attrib_type at ON  at.attrib_type_id = ta.attrib_type_id"
        + " WHERE tl.transcript_id IN ( "
        + StringUtil.toString(transcriptInternalIDs, true)
        + ")"
        + " ORDER BY tl.translation_id ";

    Connection conn = null;
    Translation translation = null;
    long currentID = -999;
    try {

      conn = getConnection();
      ResultSet rs = conn.createStatement().executeQuery(sql);
      ExonAdaptor exonAdaptor = driver.getExonAdaptor();
      TranscriptAdaptor transcriptAdaptor = driver.getTranscriptAdaptor();
      while (rs.next()) {

        long id = rs.getLong("translation_id");
        if (currentID != id) {

          currentID = id;

          translation = new TranslationImpl(driver);
          translations.add(translation);

          if (logger.isLoggable(Level.FINE))
            logger.fine(org.ensembl.util.JDBCUtil.toString(rs));

          translation.setInternalID(id);
          translation.setAccessionID(rs.getString("stable_id"));
          translation.setCreatedDate( rs.getDate( "created_date" ));
          translation.setModifiedDate( rs.getDate( "modified_date" ));
          translation.setVersion(rs.getInt("version"));
          translation.setStartExonInternalID(rs.getLong("start_exon_id"));
          translation.setPositionInStartExon(rs.getInt("seq_start"));
          translation.setEndExonInternalID(rs.getLong("end_exon_id"));
          translation.setPositionInEndExon(rs.getInt("seq_end"));
          translation.setTranscriptInternalID(rs.getLong("transcript_id"));

          if (loadChildren) {

            if (exonAdaptor == null) {
              exonAdaptor = (ExonAdaptorImpl) getDriver().getAdaptor("exon");
            }

            if (transcriptAdaptor == null) {
              transcriptAdaptor =
                (TranscriptAdaptorImpl) getDriver().getAdaptor("transcript");
            }

            translation.setStartExon(
              exonAdaptor.fetch(rs.getLong("start_exon_id")));
            translation.setEndExon(
              exonAdaptor.fetch(rs.getLong("end_exon_id")));

          }
        }

        String value = rs.getString("value");
        if (value != null) {
          translation.addAttribute(
            new AttributeImpl(
              rs.getString("code"),
              rs.getString("name"),
              rs.getString("description"),
              value));
        }
      }

    } catch (SQLException e) {
      throw new AdaptorException("Rethrow + stacktrace", e);

    } finally {
      close(conn);
    }

    return translations;

  }

  // -----------------------------------------------------------------

  /**
   * Delegates to fetchByTranscript(transcriptIntriptInternalID,false).
   * @see #fetchByTranscript(long,boolean)
   */
  public Translation fetchByTranscript(long transcriptInternalID)
    throws AdaptorException {
    return fetchByTranscript(transcriptInternalID, false);
  }

  public Translation fetchByTranscript(
    long transcriptInternalID,
    boolean loadChildren)
    throws AdaptorException {

    List tmp =
      fetchByTranscripts(new long[] { transcriptInternalID }, loadChildren);
    return tmp.size() == 0 ? null : (Translation) tmp.get(0);

  }

  // -----------------------------------------------------------------
  /**
   * Retrieves the translation's accession and sets it.
   * 
   * @param translation Item to find and set accession for.
   */
  public void fetchAccessionID(Translation translation)
    throws AdaptorException {
	  fetchStableIdInfo( translation );
  }

  
  private void fetchStableIdInfo( Translation tl ) throws AdaptorException {

	    final long internalID = tl.getInternalID();

	    if (internalID < 0)
	      throw new AdaptorException("InternalID not set.");

	    Connection conn = null;
	    try {
	      // Retrieve row from database
	      conn = getConnection();
	      String sql =
	        "SELECT  " +
	        "stable_id, version, created_date, modified_date "
	          + " WHERE "
	          + " translation_id = "
	          + internalID;

	      ResultSet rs = executeQuery(conn, sql);
	      if( rs.next() ) {
	    	  	tl.setAccessionID( rs.getString( "stable_id"));
	    	  	tl.setVersion( rs.getInt( "version"));
	    	  	tl.setCreatedDate( rs.getDate( "created_date"));
	    	  	tl.setModifiedDate( rs.getDate( "modified_date"));
	      }
	    } catch (SQLException e) {
	      throw new AdaptorException(
	        "Failed to retrieve version for translation: " + tl,
	        e);
	    } finally {
	      close(conn);
	    }	  
  }
  // -----------------------------------------------------------------
  /**
   * Sets the translation's version based on the contents of the database; if the translation has a version in then
   * translation_stable_id table then it is used, otherwise the version is set to 0;
   * 
   * @param translation translation.internalID must be >=0
   * @throws AdaptorException if a problem occurs whilst attempting to retrieve the version.
   */
  public void fetchVersion(Translation translation) throws AdaptorException {
	  fetchStableIdInfo( translation );
  }

  // -----------------------------------------------------------------
  /**
   * Retrieves from persistent store whether translation is known and sets translation.known.
   */
  public void fetchKnown(Translation translation) throws AdaptorException {

    fetchKnown(new Translation[] { translation });
  }

  // -----------------------------------------------------------------
  /**
   * Retrieves from persistent store whether translations are known and for each one sets translation.known.
   */
  public void fetchKnown(Translation[] translations) throws AdaptorException {

    // Get the externalDatabase.internalIDs corresponding to external refs
    // hanging off this translation then get the externalDatabase from the
    // relevant adaptor. This is quicker than joining to the external_db
    // table in the sql.

    Connection conn = null;
    try {

      String sql =
        "SELECT "
          + " external_db_id "
          + " FROM "
          + " object_xref ox "
          + " ,xref x "
          + " WHERE "
          + "   ox.ensembl_object_type='Translation' "
          + "   AND ox.ensembl_id = ? "
          + " 	AND x.xref_id = ox.xref_id ";

      conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);

      for (int t = 0; t < translations.length; ++t) {
        ps.setLong(1, translations[t].getInternalID());

        ResultSet rs = executeQuery(ps, sql);
        boolean known = false;
        ExternalDatabaseAdaptor xdbAdaptor =
          driver.getExternalDatabaseAdaptor();

        while (xdbAdaptor != null
          && rs.next()
          && !known // stop as soon as we find a known external database
        ) {
          ExternalDatabase xdb = xdbAdaptor.fetch(rs.getLong(1));
          known = xdb.isKnown();
        }
        translations[t].setKnown(known);
      }
    } catch (Exception e) {
      throw new AdaptorException("Failed to load translation.known", e);
    } finally {
      close(conn);
    }
  }

  // -----------------------------------------------------------------
  long store(Connection conn, Translation translation)
    throws AdaptorException, SQLException {

    /*
     * StringBuffer sql = new StringBuffer(); sql.append(" INSERT INTO translation "); sql.append(" ( translation_id, seq_start,
     * start_exon_id, seq_end, end_exon_id ) "); sql.append(" VALUES ( "); sql.append(" NULL, ");
     * sql.append(translation.getPositionInStartExon()).append(", "); sql.append(translation.getStartExonInternalID()).append(",
     * "); sql.append(translation.getPositionInEndExon()).append(", "); sql.append(translation.getEndExonInternalID());
     * sql.append(" ) "); // correctly hook up transcript -> translation long translationID = executeAutoInsert(conn,
     * sql.toString()); translation.setInternalID(translationID);
     * translation.getTranscript().setTranslationInternalID(translationID);
     * 
     * String stableID = translation.getAccessionID(); if (stableID != null) storeStableID(conn, translationID, stableID);
     * 
     * GeneAdaptorImpl.storeInObjectXrefTable( conn, driver.getExternalRefAdaptor(), translationID, "Translation",
     * translation.getExternalRefs());
     * 
     * return translationID;
     */
    throw new NotImplementedYetException("Not yet implemented in new API");

  }

  // -----------------------------------------------------------------

  private void storeStableID(
    Connection conn,
    long translationID,
    String stableID)
    throws AdaptorException {

    StringBuffer sql = new StringBuffer();
    sql.append(" INSERT INTO translation_stable_id ");
    sql.append(" ( translation_id, stable_id ) VALUES (");
    sql.append(translationID).append(", '");
    sql.append(stableID);
    sql.append("' )");
    executeUpdate(conn, sql.toString());

  }

  // -----------------------------------------------------------------

  void delete(Connection conn, long translationInternalID)
    throws AdaptorException {

    executeUpdate(
      conn,
      "delete from translation where translation_id=" + translationInternalID);
    executeUpdate(
      conn,
      "delete from translation_stable_id where translation_id="
        + translationInternalID);
    executeUpdate(
      conn,
      "delete from object_xref where ensembl_id="
        + translationInternalID
        + " AND ensembl_object_type = 'Translation'");

  }

  /**
   * @see org.ensembl.driver.TranslationAdaptor#fetchByInterproID(java.lang.String)
   */
  public List fetchByInterproID(String interproID) throws AdaptorException {


    IDSet ids = new IDSet();
    Connection conn = null;
    try {
      conn = getConnection();
      String sql =
        "SELECT DISTINCT translation_id FROM interpro, protein_feature "
          + "WHERE interpro.id = protein_feature.hit_id AND interpro.interpro_ac='"
          + interproID
          + "'";
      ResultSet rs = executeQuery(conn, sql);
      while (rs.next())
        ids.add(rs.getLong(1));

    } catch (SQLException e) {
      throw new AdaptorException(
        "Failed to get translations for interproID = " + interproID,
        e);
    } finally {
      close(conn);
    }
    
    List r = new ArrayList();
    long[] idArray = ids.to_longArray();
    for (int i = 0; i < idArray.length; i++) 
      r.add(fetch(idArray[i]));
    return r;
  }

  /**
   * @see org.ensembl.driver.TranslationAdaptor#completeInterproIDs(org.ensembl.datamodel.Translation)
   */
  public void completeInterproIDs(Translation translation)
    throws AdaptorException {

    List r = new ArrayList();

    String sql =
      "SELECT DISTINCT interpro_ac FROM interpro, protein_feature "
        + "WHERE interpro.id = protein_feature.hit_id AND protein_feature.translation_id=?";

    Connection conn = null;
    try {
      conn = getConnection();
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setLong(1, translation.getInternalID());
      ResultSet rs = executeQuery(ps, sql);

      while (rs.next())
        r.add(rs.getString(1));

    } catch (SQLException e) {
      throw new AdaptorException(
        "Failed to get interproIDs for translation: " + translation,
        e);
    } finally {
      close(conn);
    }

    translation.setInterproIDs((String[]) r.toArray(new String[r.size()]));
  }

  public List fetchAll() throws AdaptorException {

    List result = new ArrayList();

    Connection con = getConnection();
    int i = 0;
    try {

      ResultSet rs =
        executeQuery(con, "SELECT translation_id FROM translation");

      while (rs.next()) {
        result.add(fetch(rs.getLong(1)));
      }

    } catch (SQLException e) {
      throw new AdaptorException("Failed to fetch all translations", e);
    } finally {
      close(con);
    }

    return result;

  }

}