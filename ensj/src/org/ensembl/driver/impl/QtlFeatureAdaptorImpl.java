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

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Qtl;
import org.ensembl.datamodel.QtlFeature;
import org.ensembl.datamodel.impl.QtlFeatureImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.QtlFeatureAdaptor;


/**
 * This class is responsible of retrieving QtlFeatures (and their associated
 * Qtls) from the database.
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class QtlFeatureAdaptorImpl
  extends BaseFeatureAdaptorImpl
  implements QtlFeatureAdaptor {

  /**
   * @param driver driver this adaptor is associated with.
   */
  public QtlFeatureAdaptorImpl(CoreDriverImpl driver) {
    super(driver, TYPE);
  }

  /**
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#tables()
   */
  protected String[][] tables() {
    String[][] tables = {
      {"qtl_feature", "qf"}
    };
    return tables;
  }

  /**
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#columns()
   */
  protected String[] columns() {
    String[] columns = {"qf.seq_region_id", 
                        "qf.seq_region_start", 
                        "qf.seq_region_end",
                        "qf.analysis_id",
                         "qf.qtl_id"
    };
    return columns;
  }



  /**
   * Creates a QtlFeature bound to it's QTL and analysis.
   * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#createObject(java.sql.ResultSet)
   */
  public Object createObject(ResultSet rs) throws AdaptorException {

    QtlFeature f = null;
    
    try {
      
    if (rs.next()) {

        f = new QtlFeatureImpl(driver);
        Location loc = new Location(rs.getLong("seq_region_id"),
                                    rs.getInt("seq_region_start"),
                                    rs.getInt("seq_region_end"),
                                    0);
        
        f.setLocation(loc);
        f.setQtlID( rs.getLong("qtl_id"));
        f.setAnalysisID(rs.getLong("analysis_id") );
        
    }
    
    } catch (SQLException e) {
      throw new AdaptorException("Failed to retrieve QtlFeatures: ", e);
    }
    
    return f;
  }

  /**
   * Retrieves qtl features corresponding to the qtl and binds them to the
   * qtl and vice versa.
   * @see org.ensembl.driver.QtlFeatureAdaptor#fetch(org.ensembl.datamodel.Qtl)
   */
  public List fetch(Qtl qtl) throws AdaptorException {
    List fs = genericFetch("qf.qtl_id = " + qtl.getInternalID(),null);
    
    // bind qtl <-> features
    for(int i=0;i<fs.size();++i) {
      QtlFeature f = (QtlFeature)fs.get(i);
      f.setQtl(qtl);
    }
    qtl.setQtlFeatures(fs);
    
    return fs;
  }

}
