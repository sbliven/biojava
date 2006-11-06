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
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.InvalidLocationException;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.Translation;
import org.ensembl.datamodel.impl.ExonImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.ExonAdaptor;
import org.ensembl.driver.LocationConverter;
import org.ensembl.util.NotImplementedYetException;
import org.ensembl.util.Warnings;

// TODO - implement store/delete

public class ExonAdaptorImpl
  extends BaseFeatureAdaptorImpl
  implements ExonAdaptor {

  private static final Logger logger =
    Logger.getLogger(ExonAdaptorImpl.class.getName());

  
  public ExonAdaptorImpl(CoreDriverImpl driver) {
    super(driver, TYPE);
  }

  // -----------------------------------------------------------------

  /* 
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#columns()
   */
  protected String[] columns() {

    String[] cols =
      {
        "e.exon_id",
        "e.seq_region_id",
        "e.seq_region_start",
        "e.seq_region_end",
        "e.seq_region_strand",
        "e.phase",
        "e.end_phase",
        "esi.stable_id",
        "esi.version",
        "esi.created_date",
        "esi.modified_date"};

    return cols;

  }

  // -----------------------------------------------------------------

  /* 
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#tables()
   */
  protected String[][] tables() {

    String[][] tables = { { "exon", "e" }, {
        "exon_stable_id", "esi" }
    };

    return tables;

  }

  // -----------------------------------------------------------------

  /*
  	 * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#leftJoin()
  	 */
  public String[][] leftJoin() {

    String[][] lj = { { "exon_stable_id", "esi.exon_id = e.exon_id" }
    };

    return lj;

  }

  // -----------------------------------------------------------------

  /* 
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#createObject(java.sql.ResultSet)
   */
  public Object createObject(ResultSet rs) throws AdaptorException {

    Exon e = null;


    try {
      LocationConverter locationConverter = driver.getLocationConverter();
      if (rs.next()) {
        Location loc =
          locationConverter.idToLocation(
            rs.getLong("seq_region_id"),
            rs.getInt("seq_region_start"),
            rs.getInt("seq_region_end"),
            rs.getInt("seq_region_strand"));

        e = new ExonImpl(driver);
        e.setLocation(loc);
        e.setInternalID(rs.getLong("exon_id"));
        e.setAccessionID(rs.getString("stable_id"));
        e.setCreatedDate( rs.getDate( "created_date"));
        e.setModifiedDate( rs.getDate( "modified_date"));
        e.setVersion(rs.getInt("version"));
        e.setPhase(rs.getInt("phase"));
        e.setEndPhase(rs.getInt("end_phase"));
        e.setDriver(getDriver());

        // transcripts and gene are lazy loaded (see ExonImpl) if required

      }

    } catch (InvalidLocationException ee) {
      throw new AdaptorException("Error when building Location", ee);
    } catch (SQLException se) {
      throw new AdaptorException("SQL error when building object", se);
    }

    return e;

  }

  // -----------------------------------------------------------------

  public void fetchAccessionID(Exon exon) throws AdaptorException {

    Warnings.deprecated(
      "Accession IDs are now fetched by default - fetchAccessionID() is no longer required.");

  }

  // -----------------------------------------------------------------

  public void fetchVersion(Exon exon) throws AdaptorException {

    Warnings.deprecated(
      "Versions are now fetched by default - fetchVersion() is no longer required.");

  }

  // -----------------------------------------------------------------

  /**
   * Includes gene and transcripts ?????????????.
   * @return exon with specified internalID, or null if non found.
   */
  public Exon fetch(long internalID) throws AdaptorException {

    // just use base class method with appropriate return type
    return (Exon) super.fetchByInternalID(internalID);

  }

  // -----------------------------------------------------------------

  /**
   * Includes accession, gene and transcripts ???
   */
  public Exon fetch(String accessionID) throws AdaptorException {

    //	optimisation: do a separate SQL query to get the internal ID,
    // then call fetch(internalID)

    List l =
      super.fetchByNonLocationConstraint("esi.stable_id='" + accessionID + "'");
    if (l.size() == 0) {
      return null;
    } else if (l.size() > 1) {
      throw new AdaptorException(
        "Expeced one exon with accession ID "
          + accessionID
          + " but found "
          + l.size());
    } else {
      return (Exon) l.get(0);
    }

  }

  //-----------------------------------------------------------------


  private Gene fetchGeneByExonID(long exonID) throws AdaptorException {

    if (exonID < 1)
      throw new IllegalArgumentException(
        "Can not lazy load exon invalid internalID, must be >0: " + exonID);
    
    String sql =
      "SELECT gene_id FROM exon_transcript et, transcript t WHERE et.transcript_id=t.transcript_id AND et.exon_id="
        + exonID;
    Connection conn = null;
    conn = getConnection();
    ResultSet rs = executeQuery(conn, sql);
    final long geneID;
    try {
      geneID = rs.next() ? rs.getLong(1) : -1;
    } catch (SQLException e) {
      throw new AdaptorException(
        "Failed to find gene ID corresponding to exon ID: " + exonID,
        e);
    } finally {
      close(conn);
    }
    
    return (geneID==-1) ? null : driver.getGeneAdaptor().fetch(geneID);
  }


  public Exon fetchComplete(Exon exon) throws AdaptorException {

    // Load the gene and transcript parents of this exon and insert
    // references to this exon into them. 

    // exonID -> geneID
    final long exonID = exon.getInternalID();
    
    Gene gene = fetchGeneByExonID(exonID);
    
    if (gene!=null) {

      // set the gene in the exon
      exon.setGene(gene);

      //  Replace relevant exon references in gene
      List geneExons = gene.getExons();
      final int nGeneExons = geneExons.size();
      for (int g = 0; g < nGeneExons; ++g) {
        Exon geneExon = (Exon) geneExons.get(g);
        if (geneExon.getInternalID() == exonID) {
          geneExons.set(g, exon);
          break;
        }
      }

      //  Replace relevant exon references in transcripts and translations
      // and add relevant transcripts to exon
      exon.setTranscripts( new ArrayList());
      List transcripts = gene.getTranscripts();
      final int nTranscripts = transcripts.size();
      for (int t = 0; t < nTranscripts; ++t) {

        Transcript transcript = (Transcript) transcripts.get(t);
        List transcriptExons = transcript.getExons();

        final int nTranscriptExons = transcriptExons.size();
        for (int te = 0; te < nTranscriptExons; ++te) {

          Exon transcriptExon = (Exon) transcriptExons.get(te);
          if (transcriptExon.getInternalID() == exonID) {
            // replace exon reference in transcript's exon list
            transcriptExons.set(te, exon);
            exon.getTranscripts().add(transcript);
            break;
            // only one reference so we skip the rest of the transcriptExons
          }
        }
        
        Translation translation = transcript.getTranslation();
        if ( translation!=null ) {
          if ( translation.getStartExonInternalID()==exonID) translation.setStartExon(exon);
          if ( translation.getEndExonInternalID()==exonID) translation.setEndExon(exon);
        }
      }

    } else {
      // no gene corresponding to exon so just set transcripts to empty array
      // this will prevent the exon from trying to lazy load in future.
      exon.setTranscripts(Collections.EMPTY_LIST);
    }

    return exon;
  }

  //-----------------------------------------------------------------

  /**
   * @return List of >=0 exons matching the query.
   * @deprecated since version 27.0 use other fetch(...) methods.
   */
  public List fetch(org.ensembl.datamodel.Query query) throws AdaptorException {

    List result = new ArrayList();

    if (query.getInternalID() > 0) {

      result.add((Exon) fetch(query.getInternalID()));

    } else if (
      query.getAccessionID() != null && query.getAccessionID().length() > 0) {

      result.add(fetch(query.getAccessionID()));

    } else if (query.getLocation() != null) {

      result = fetch(query.getLocation());

    }

    // TODO - finish fetch by Query as in GeneAdaptorImpl

    return result;

  }

  /**
   * Fetch all the exons related to a particular transcript.
   * @param transcriptID The ID of the transcript.
   */
  public List fetchAllByTranscript(long transcriptID) throws AdaptorException {

    String sql =
      "SELECT exon_id FROM exon_transcript "
        + " WHERE "
        + " transcript_id="
        + transcriptID
        + " ORDER BY rank";

    return fetch(fetchInternalIDsBySQL(sql));

  }
  // -----------------------------------------------------------------

  /**
   * Stores exon in database.
   */
  public long store(Exon exon) throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented in new API");

  }

  // -----------------------------------------------------------------

  public long store(Connection conn, Exon exon) throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented in new API");

  }

  // -----------------------------------------------------------------

  public void delete(Exon exon) throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented in new API");

  }

  // -----------------------------------------------------------------

  public void delete(long internalID) throws AdaptorException {

    throw new NotImplementedYetException("Not yet implemented in new API");

  }

  // -----------------------------------------------------------------


  // -----------------------------------------------------------------

  public List fetchAllByGeneID(long geneID) throws AdaptorException {

    List result = new ArrayList();

    String sql =
      "SELECT et.* FROM exon_transcript et,  transcript t"
        + " WHERE t.gene_id="
        + geneID
        + " AND t.transcript_id=et.transcript_id";

    Connection conn = getConnection();

    try {

      ResultSet rs = executeQuery(conn, sql);
      while (rs.next()) {

        long exonID = rs.getLong("exon_id");
        Exon e = fetch(exonID);
        result.add(e);
        System.out.println(
          "Fetched exon " + e.getAccessionID() + " for gene " + geneID);
      }

    } catch (SQLException se) {
      throw new AdaptorException(
        "Error while fetching transcripts: " + se.getMessage());
    }

    CoreDriverImpl.close(conn);

    return result;

  }

  // -----------------------------------------------------------------

}
