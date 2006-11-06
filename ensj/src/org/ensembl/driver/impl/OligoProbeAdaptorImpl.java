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

import org.ensembl.datamodel.OligoArray;
import org.ensembl.datamodel.OligoProbe;
import org.ensembl.datamodel.impl.OligoProbeImpl;
import org.ensembl.driver.Adaptor;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.OligoProbeAdaptor;

/**
 * The point of this class is....
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class OligoProbeAdaptorImpl extends BaseFeatureAdaptorImpl implements
    Adaptor, OligoProbeAdaptor {

  /**
   * @param driver
   */
  public OligoProbeAdaptorImpl(CoreDriverImpl driver) {
    super(driver, TYPE);
  }

  /**
   * @see org.ensembl.driver.OligoProbeAdaptor#fetch(long)
   */
  public OligoProbe fetch(long internalID) throws AdaptorException {
    return (OligoProbe) fetchByInternalID(internalID);
  }

  /**
   * @see org.ensembl.driver.OligoProbeAdaptor#fetch(org.ensembl.datamodel.OligoArray)
   */
  public List fetch(OligoArray array) throws AdaptorException {
    return fetchByNonLocationConstraint(" ap.oligo_array_id = "
        + array.getInternalID());
  }

  /**
   * @see org.ensembl.driver.OligoProbeAdaptor#fetch(java.lang.String)
   */
  public List fetch(String probeSetName) throws AdaptorException {
    return fetchByNonLocationConstraint(" ap.probeset = '" + probeSetName + "'");
  }

  /**
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#tables()
   */
  protected String[][] tables() {
    final String[][] tables = { { "oligo_probe", "ap" } };
    return tables;
  }

  /**
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#columns()
   */
  protected String[] columns() {
    final String[] columns = { "ap.oligo_probe_id", "ap.oligo_array_id",
        "ap.probeset", "ap.name", "ap.length", "ap.description" };
    return columns;
  }

  public String finalClause() {
    return " order by ap.oligo_probe_id, ap.oligo_array_id";
  }

  /**
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#createObject(java.sql.ResultSet)
   */
  public Object createObject(ResultSet rs) throws AdaptorException {

    // Probes are represented by 1 or more rows. 
    
    try {

      if (rs.isAfterLast())
        return null;

      if (rs.isBeforeFirst())
        rs.next();
        
      final long internalID;
      try {
        internalID = rs.getLong(1);
      } catch (SQLException e) {
        // SQLException is thrown if rs is empty. 
        // This could hide a 'genuine' SQLExceptions but alternative 
        // implementations of this method requiring rs.previous()
        // breaks the jDTS JDBC driver / MS SQL combination.
        return null;
      }

      OligoProbe p = new OligoProbeImpl(driver, internalID, rs.getString(3), rs.getInt(5), rs.getString(6));
      
      boolean relevantRow = false;
      do {
        relevantRow = internalID == rs.getLong(1);
        if (relevantRow) 
          p.addArrayWithProbeName(driver.getOligoArrayAdaptor().fetch(
              rs.getLong(2)), rs.getString(4));

      } while (relevantRow && rs.next());

      return p;
      
    } catch (SQLException e) {
      throw new AdaptorException("Problem loading OligoProbe from database.", e);
    }
  }
}