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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ensembl.driver.AdaptorException;
import org.ensembl.util.LongSet;
import org.ensembl.variation.datamodel.Variation;
import org.ensembl.variation.datamodel.VariationGroup;
import org.ensembl.variation.datamodel.impl.VariationGroupImpl;
import org.ensembl.variation.driver.VariationDriver;
import org.ensembl.variation.driver.VariationGroupAdaptor;

/**
 * Implementation of VariationGroupAdaptor that
 * fetches VariationGroups from an ensembl database.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class VariationGroupAdaptorImpl extends BasePersistentAdaptor implements VariationGroupAdaptor {

  private final static String BASE_QUERY =
    "SELECT vg.variation_group_id, vg.name, s.name as s_name, vg.type, vgv.variation_id"
      + " FROM variation_group vg, source s";

  /**
   * 
   */
  public VariationGroupAdaptorImpl(VariationDriver vdriver) {
    super(vdriver);
  }

  /**
   * @see org.ensembl.variation.driver.VariationGroupAdaptor#fetch(long)
   */
  public VariationGroup fetch(long internalID) throws AdaptorException {
    // left join allows variation groups without any variations to be fetched
    String sql =
      BASE_QUERY
        + " LEFT JOIN variation_group_variation vgv ON"
        + " vgv.variation_group_id = vg.variation_group_id"
        + " WHERE  vg.source_id = s.source_id"
        + " AND    vg.variation_group_id = "
        + internalID;
    return (VariationGroup) fetchByQuery(sql);
  }

  /**
   * @see org.ensembl.variation.driver.VariationGroupAdaptor#fetch(java.lang.String)
   */
  public VariationGroup fetch(String name) throws AdaptorException {
    // left join allows variation groups without any variations to be fetched
    String sql =
      BASE_QUERY
        + " LEFT JOIN variation_group_variation vgv"
        + " ON    vgv.variation_group_id = vg.variation_group_id"
        + " WHERE  vg.source_id = s.source_id"
        + " AND    vg.name = '"
        + name
        + "'";
    return (VariationGroup) fetchByQuery(sql);
  }

  /**
   * @see org.ensembl.variation.driver.VariationGroupAdaptor#fetch(org.ensembl.variation.datamodel.Variation)
   */
  public List fetch(Variation variation) throws AdaptorException {
    String sql =
      BASE_QUERY
        + " , variation_group_variation vgv"
        + " WHERE  vg.source_id = s.source_id"
        + " AND    vgv.variation_group_id = vg.variation_group_id"
        + " AND    vgv.variation_id = "
        + variation.getInternalID()
        + " ORDER BY vg.variation_group_id";
    return fetchListByQuery(sql);
  }

  /**
   * @see org.ensembl.variation.driver.VariationGroupAdaptor#TYPE
   * @see org.ensembl.driver.Adaptor#getType()
   */
  public String getType() throws AdaptorException {
    return TYPE;
  }

  protected Object createObject(ResultSet rs) throws SQLException, AdaptorException {
    if (rs.isAfterLast())
      return null;

    final long internalID = rs.getLong("variation_group_id");
    VariationGroup vg = null;
    LongSet variationIDs = new LongSet();
    
    do {
    
      if (vg == null) {
        vg = new VariationGroupImpl(vdriver);

        vg.setInternalID(internalID);
        vg.setName(rs.getString("name"));
        vg.setSource(rs.getString("s_name"));
        vg.setType(rs.getString("type"));
      }
      
      long variationID = rs.getLong("variation_id");
      if (variationID>0)
        variationIDs.add(variationID);
      
    } while (rs.next() && rs.getLong("variation_group_id") == internalID);

    long[] ids = variationIDs.to_longArray();
    List vs = vdriver.getVariationAdaptor().fetch(ids);
    for (int i = 0; i < vs.size(); i++) 
      vg.addVariation((Variation) vs.get(i)); 

    //cache.put(vg, new Long(vg.getInternalID()), vg.getName());

    return vg;

  }
}
