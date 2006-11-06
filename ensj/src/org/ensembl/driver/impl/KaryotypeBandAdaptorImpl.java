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
import org.ensembl.datamodel.InvalidLocationException;
import org.ensembl.datamodel.KaryotypeBand;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.impl.KaryotypeBandImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.KaryotypeBandAdaptor;
import org.ensembl.driver.LocationConverter;

/**
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class KaryotypeBandAdaptorImpl
  extends BaseFeatureAdaptorImpl
  implements KaryotypeBandAdaptor {

  public KaryotypeBandAdaptorImpl(CoreDriverImpl driver) {
    super(driver, TYPE);
  }

  protected String[][] tables() {
    String[][] tables = { { "karyotype", "k" }
    };
    return tables;
  }

  protected String[] columns() {
    String[] columns =
      {
        "k.karyotype_id",
        "k.seq_region_id",
        "k.seq_region_start",
        "k.seq_region_end",
        "k.band",
        "k.stain" };

    return columns;
  }

  public Object createObject(ResultSet rs) throws AdaptorException {

    KaryotypeBand kb = null;

    try {
      LocationConverter locationConverter = driver.getLocationConverter();
      
      if (rs.next()) {

        Location loc =
          locationConverter.idToLocation(
            rs.getLong("seq_region_id"),
            rs.getInt("seq_region_start"),
            rs.getInt("seq_region_end"),
            0);

        kb = new KaryotypeBandImpl(getDriver());
        kb.setLocation(loc);
        kb.setBand(rs.getString("band"));
        kb.setStain(rs.getString("stain"));

      }

    } catch (InvalidLocationException e) {
      throw new AdaptorException("Error when building Location", e);
    } catch (SQLException e) {
      throw new AdaptorException("SQL error when building object", e);
    }

    return kb;
  }

  /* (non-Javadoc)
   * @see org.ensembl.driver.KaryotypeAdaptor#fetch(org.ensembl.datamodel.CoordinateSystem, java.lang.String)
   */
  public List fetch(CoordinateSystem coordSys, String chromosome) throws AdaptorException, InvalidLocationException {
    
    // karyotype bands are stored directly on chromosomes and do not use the dereferencing mechanism
    // so we explicitly disable it.
    return fetchAllByConstraint( new Location(coordSys, chromosome),"", false);
  }

  /* (non-Javadoc)
   * @see org.ensembl.driver.KaryotypeAdaptor#fetch(org.ensembl.datamodel.CoordinateSystem, java.lang.String, java.lang.String)
   */
  public List fetch(
    CoordinateSystem coordSys,
    String chromosome,
    String band) throws AdaptorException {
      
    String constraint = "k.band like '"+band+"'";
    // karyotype bands are stored directly on chromosomes and do not use the dereferencing mechanism
    // so we explicitly disable it.
    return fetchAllByConstraint( new Location(coordSys, chromosome),constraint, false);
  }

}
