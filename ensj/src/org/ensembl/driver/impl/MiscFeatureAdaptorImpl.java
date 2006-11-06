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
import java.util.List;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.MiscFeature;
import org.ensembl.datamodel.MiscSet;
import org.ensembl.datamodel.impl.AttributeImpl;
import org.ensembl.datamodel.impl.MiscFeatureImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.MiscFeatureAdaptor;

/**
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 *  
 */
public class MiscFeatureAdaptorImpl extends BaseFeatureAdaptorImpl implements
    MiscFeatureAdaptor {

  public MiscFeatureAdaptorImpl(CoreDriverImpl driver) {
    super(driver, TYPE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#tables()
   */
  protected String[][] tables() {
    String[][] tables = { { "misc_feature", "mf" },
        { "misc_feature_misc_set", "mfms" }, { "misc_attrib", "ma" },
        { "attrib_type", "at" } };

    return tables;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#columns()
   */
  protected String[] columns() {
    String[] cols = { "mf.misc_feature_id", "mf.seq_region_id",
        "mf.seq_region_start", "mf.seq_region_end", "mf.seq_region_strand",
        "ma.value", "at.code", "mfms.misc_set_id", "at.name", "at.description" };

    return cols;
  }

  public String[][] leftJoin() {
    String[][] leftJoin = {
        { "misc_feature_misc_set", "mf.misc_feature_id = mfms.misc_feature_id" },
        { "misc_attrib", "mf.misc_feature_id = ma.misc_feature_id" },
        { "attrib_type", "ma.attrib_type_id = at.attrib_type_id" } };
    return leftJoin;
  }

  public final String finalClause() {
    return " ORDER BY mf.misc_feature_id";
  }

  /**
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#createObject(java.sql.ResultSet)
   */
  public Object createObject(ResultSet rs) throws AdaptorException {

    // Each MiscFeature will correspond to 1 or more rows. We do a join so
    // features, miscsets, and attributes all come back in the same
    // resultset, for example:

    // Row1: feature1, miscset1, attribute1
    // Row2: feature1, miscset1, attribute2
    // Row3: feature1, miscset2, attribute3
    // Row4: feature1, miscSet2, attribute4
    // Row5: feature2, miscSet3, attribute5 - stop at this row and reset the
    //                                         row cursor tp previous row

    //
    long prevMiscSetID = -1;

    try {

      if (rs.isAfterLast())
        return null;

      if (rs.isBeforeFirst())
        rs.next();

      final long internalID;
      try {
        internalID = rs.getLong("misc_feature_id");
      } catch (SQLException e) {
        // SQLException is thrown if rs is empty.
        // This could hide a 'genuine' SQLExceptions but alternative
        // implementations of this method requiring rs.previous()
        // breaks the jDTS JDBC driver / MS SQL combination.
        return null;
      }

      MiscFeature f = new MiscFeatureImpl(internalID, driver.getLocationConverter()
          .idToLocation(rs.getLong("seq_region_id"), rs
              .getInt("seq_region_start"), rs.getInt("seq_region_end"), rs
              .getInt("seq_region_strand")));

      boolean relevantRow = false;
      do {
        relevantRow = internalID == rs.getLong("misc_feature_id");
        if (relevantRow) {

          long miscSetID = rs.getLong("misc_set_id");
          if (miscSetID != prevMiscSetID) {
            prevMiscSetID = miscSetID;
            f.add(driver.getMiscSetAdaptor().fetch(miscSetID));
          }

          f.add(new AttributeImpl(rs.getString("code"), rs
              .getString("name"), rs.getString("description"), rs
              .getString("value")));
        }
      } while (relevantRow && rs.next());

      return f;

    } catch (SQLException se) {
      throw new AdaptorException("SQL error when building object", se);
    }

  }

  public List fetchByAttributeType(String type) throws AdaptorException {
    return fetchByAttributeTypeAndValue(type, null);
  }

  /**
   * @see org.ensembl.driver.MiscFeatureAdaptor#fetchByAttributeTypeAndValue(java.lang.String,java.lang.String)
   */
  public List fetchByAttributeTypeAndValue(String type, String value)
      throws AdaptorException {

    // SQL to get the ids of all the attributes matching the criteria
    String sql = "SELECT DISTINCT ma.misc_feature_id "
        + "FROM   misc_attrib ma, attrib_type at "
        + "WHERE  ma.attrib_type_id = at.attrib_type_id "
        + "AND    at.code = '" + type + "'";
    if (value != null) {
      sql = sql + " AND ma.value = '" + value + "'";
    }

    // Fetch attributes in batches
    String[] constraints = createConstraintBatches("mf.misc_feature_id", sql);
    return genericFetch(constraints);

  }

  public MiscFeature fetch(long internalID) throws AdaptorException {
    return (MiscFeature) super.fetchByInternalID(internalID);
  }

  public List fetch(MiscSet miscSet) throws AdaptorException {
    long id = miscSet.getInternalID();
    if (id < 1)
      throw new IllegalArgumentException("Misc Set internalID should be >0: "
          + miscSet);
    String constraint = "mfms.misc_set_id = " + id;
    return genericFetch(constraint, null);
  }

  public List fetch(Location location, MiscSet miscSet) throws AdaptorException {

    long id = miscSet.getInternalID();
    if (id < 1)
      throw new IllegalArgumentException("Misc Set internalID should be >0: "
          + miscSet);

    StringBuffer constraint = new StringBuffer();
    constraint.append("mfms.misc_set_id = ").append(id);

    // Optimisation: adds an extra constraint if
    // this misc set has a maximum length
    if (miscSet.getMaxFeatureLength() > 0) {
      int minStart = location.getStart() - miscSet.getMaxFeatureLength();
      constraint.append(" AND ").append(getPrimaryTableSynonym()).append(
          ".seq_region_start >= ").append(minStart);
    }

    return fetchAllByConstraint(location, constraint.toString());
  }

  /**
   * Returns -1.
   * 
   * fetch(Location,MiscSet) inserts it's own start>=X which effectively
   * replaces the "standard" start>= constraint. Returing -1 here 'disables' the
   * standard extra constraint added in fetchAllByConstraint(Location,String).
   * 
   * @param cs
   *          ignored.
   * @param tableName
   *          ignored.
   * @return -1.
   * @see #fetch(Location,MiscSet)
   */
  protected int getMaxFeatureLength(CoordinateSystem cs, String tableName)
      throws AdaptorException {

    return -1;

  }

}