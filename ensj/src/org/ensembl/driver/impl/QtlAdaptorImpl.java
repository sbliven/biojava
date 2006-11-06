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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ensembl.datamodel.Qtl;
import org.ensembl.datamodel.impl.QtlImpl;
import org.ensembl.datamodel.impl.QtlSynonymImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.MarkerAdaptor;
import org.ensembl.driver.QtlAdaptor;
import org.ensembl.driver.QtlFeatureAdaptor;

/**
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class QtlAdaptorImpl
  extends BaseFeatureAdaptorImpl
  implements QtlAdaptor {

  public QtlAdaptorImpl(CoreDriverImpl driver) {
    super(driver, TYPE);
  }

  public Qtl fetch(long internalID) throws AdaptorException {
    List qtls =  fetchByNonLocationConstraint("q.qtl_id = " + internalID);
    return (Qtl) ((qtls.size()==0) ? null : qtls.get(0));
  }

  public List fetchByTrait(String trait) throws AdaptorException {
    return fetchByNonLocationConstraint("q.trait = '" + trait + "'");
  }

  public List fetchBySourceDatabase(String databaseName)
    throws AdaptorException {
    return fetchByNonLocationConstraint(
      "qs.source_database='" + databaseName + "'");
  }

  public List fetchBySourceDatabase(
    String databaseName,
    String databasePrimaryID)
    throws AdaptorException {
    return fetchByNonLocationConstraint(
      "qs.source_database='"
        + databaseName
        + "' AND "
        + "qs.source_primary_id='"
        + databasePrimaryID
        + "'");
  }

  protected String[][] tables() {
    String[][] tables = { { "qtl", "q" }, {
        "qtl_synonym", "qs" }
    };
    return tables;
  }

  public String[][] leftJoin() {
    String[][] leftJoin = { { "qtl_synonym", "q.qtl_id = qs.qtl_id" }
    };
    return leftJoin;
  }

  protected String[] columns() {
    String[] columns =
      {
        "q.qtl_id",
        "qs.source_database",
        "qs.source_primary_id",
        "q.trait",
        "q.lod_score",
        "q.flank_marker_id_1",
        "q.flank_marker_id_2",
        "q.peak_marker_id" };
    return columns;
  }

  public String finalClause() {
    return "ORDER BY q.qtl_id";
  }

  /**
   * Creates a Qtl from the next 1 or more rows. The Qtl does not have it's Qtl Features set. These will be 
   * lazy loaded on demand.
   * @return a Qtl or null if there are no more rows.
   */
  public Object createObject(ResultSet rs) throws AdaptorException {
    Qtl qtl = null;

    MarkerAdaptor ma = driver.getMarkerAdaptor();
    QtlFeatureAdaptor qfa = driver.getQtlFeatureAdaptor();

    try {

      while (rs.next()) {

        long id = rs.getLong("qtl_id");
        if (qtl == null) {

          qtl = new QtlImpl(getDriver());
          qtl.setInternalID(id);
          qtl.setTrait(rs.getString("trait"));
          qtl.setLodScore(rs.getFloat("lod_score"));

          qtl.setFlankMarker1(ma.fetch(rs.getLong("flank_marker_id_1")));
          qtl.setFlankMarker2(ma.fetch(rs.getLong("flank_marker_id_2")));
          qtl.setPeakMarker(ma.fetch(rs.getLong("peak_marker_id")));

          // don't load the qtl features otherwise we will go into an infinite
          // loop where qtl and qtlfeatures keep trying to load each other.
          //qtl.setQtlFeatures(qfa.fetch(qtl));

          qtl.setSynoyms(new ArrayList());

        } else if (id != qtl.getInternalID()) {
          // stop processing rows when we reach a different QTL
          break;
        }

        // handle synonyms as "database.id"
        qtl.getSynoyms().add(
          new QtlSynonymImpl(
            rs.getString("source_database"),
            rs.getString("source_primary_id")));

      }

      // reset the cursor for the next QTL
      if (qtl != null)
        rs.previous();

    } catch (SQLException se) {
      throw new AdaptorException("SQL error when building object", se);
    }
    return qtl;
  }

}
