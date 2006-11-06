/*
    Copyright (C) 2001 EBI, GRL

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
package org.ensembl.variation.driver.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ensembl.datamodel.InvalidLocationException;
import org.ensembl.datamodel.Location;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.impl.BaseFeatureAdaptorImpl;
import org.ensembl.driver.impl.CoreDriverImpl;
import org.ensembl.variation.datamodel.Variation;
import org.ensembl.variation.datamodel.VariationFeature;
import org.ensembl.variation.datamodel.impl.VariationFeatureImpl;
import org.ensembl.variation.driver.VariationFeatureAdaptor;

/**
 * Adaptor for accessing variation features from database.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 * @see org.ensembl.variation.datamodel.VariationFeature
 *
 */
public class VariationFeatureAdaptorImpl
  extends BaseFeatureAdaptorImpl
  implements VariationFeatureAdaptor {

  // TODO use vdriver.getCoreDriver() to get on demand because it can't be passed
  // in via the generic DriverManager.loadVariationAdaptor() mechanism.

  private VariationDriverImpl vdriver;

  public VariationFeatureAdaptorImpl(VariationDriverImpl vdriver) {
    // TODO consider changing baseadaptor to handle CoreDriver instead of CoreDriverImpl
    super((CoreDriverImpl)vdriver.getCoreDriver(), TYPE);
    this.vdriver = vdriver;
  }

  /**
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#tables()
   */
  protected String[][] tables() {
    String[][] tables = {
      {"variation_feature", "vf"}
    };

    return tables;

  }

  /**
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#columns()
   */
  protected String[] columns() {
    String[] cols =
  {
    "vf.variation_feature_id",
    "vf.seq_region_id",
    "vf.seq_region_start",
    "vf.seq_region_end",
    "vf.seq_region_strand",
    "vf.variation_id",
    "vf.allele_string",
    "vf.variation_name",
    "vf.map_weight"
  };
    return cols;
  }
    
  /**
   * Creates a VariationFeature using the next row from the result set.
   * @return a VariationFeature if there is another row, otherwise null.
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#createObject(java.sql.ResultSet)
   */
  public Object createObject(ResultSet rs) throws AdaptorException {
    VariationFeature vf = null;

    try {
      if (rs.next()) {
        Location loc =
          vdriver.getCoreDriver().getLocationConverter().idToLocation(
            rs.getLong("seq_region_id"),
            rs.getInt("seq_region_start"),
            rs.getInt("seq_region_end"),
            rs.getInt("seq_region_strand"));

        vf = new VariationFeatureImpl(vdriver);
        vf.setLocation(loc);
        vf.setInternalID(rs.getLong("variation_feature_id"));
        vf.setAlleleString(rs.getString("allele_string"));
        vf.setVariationName(rs.getString("variation_name"));
        vf.setMapWeight(rs.getInt("map_weight"));
        vf.setVariationInternalID(rs.getLong("variation_id"));
        
      }

    } catch (InvalidLocationException ee) {
      throw new AdaptorException("Error when building Location", ee);
    } catch (SQLException se) {
      throw new AdaptorException("SQL error when building object", se);
    }

    return vf;

  }

  /**
   * @see org.ensembl.variation.driver.VariationFeatureAdaptor#fetch(long)
   */
  public VariationFeature fetch(long internalID) throws AdaptorException {
    return (VariationFeature) super.fetchByInternalID(internalID);
  }

  /**
   * @see org.ensembl.variation.driver.VariationFeatureAdaptor#fetch(org.ensembl.variation.datamodel.Variation)
   */
  public List fetch(Variation variation) throws AdaptorException {
    // TODO assign features to variation?
    return genericFetch("vf.variation_id = "+variation.getInternalID(),null);
  }

  



  /**
   * @see org.ensembl.driver.impl.BaseAdaptor#getConnection()
   */
  public Connection getConnection() throws AdaptorException {
    return vdriver.getConnection();
  }

}
