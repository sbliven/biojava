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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ensembl.datamodel.MiscSet;
import org.ensembl.datamodel.impl.MiscSetImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.MiscSetAdaptor;

/**
 * MiscSetAdaptor implementation which lazy loads all
 * the MiscSets into memory. 
 * 
 * This class is thread safe.
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class MiscSetAdaptorImpl
  extends BaseAdaptor
  implements MiscSetAdaptor {

  private Map cache;

  /**
   * @param driver
   */
  public MiscSetAdaptorImpl(CoreDriverImpl driver) {
    super(driver);
  }

  public List fetch() throws AdaptorException {
    lazyLoadFullCache();
    return new ArrayList(cache.values());
  }

  /**
   * Loads all MiscSets from the db into _cache_.
   * This method is synchronised to prevent multiple
   * reads of the cache whilst it being loaded.
   */
  private synchronized void lazyLoadFullCache() throws AdaptorException {

    if (cache != null) return;

    cache = new HashMap();

    String sql =
      "SELECT misc_set_id, code, name, description, max_length FROM misc_set";

    Connection conn = null;
    conn = getConnection();
    ResultSet rs = executeQuery(conn, sql);
    try {
      while (rs.next()) {

        MiscSet ms =
          new MiscSetImpl(
            rs.getLong("misc_set_id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getInt("max_length"));

        cache.put(new Long(ms.getInternalID()), ms);
      }
    } catch (SQLException e) {
      throw new AdaptorException("Failed to build MiscSet cache", e);
    } finally {
      close(conn);
    }
  }

  public MiscSet fetch(long internalID) throws AdaptorException {
    lazyLoadFullCache();
    return (MiscSet) cache.get(new Long(internalID));
  }

  public MiscSet fetch(String code) throws AdaptorException {
    lazyLoadFullCache();
    MiscSet r = null;
    for (Iterator i = cache.values().iterator(); i.hasNext();) {
      MiscSet ms = (MiscSet) i.next();
      if (ms.getCode().equals(code))
        r = ms;
    }

    return r;
  }

  public String getType() throws AdaptorException {
    return TYPE;
  }

  public List fetchByCodePattern(String regexp) throws AdaptorException {
    
    lazyLoadFullCache();
    
    List r = new ArrayList();

    for (Iterator i = cache.values().iterator(); i.hasNext();) {
      MiscSet ms = (MiscSet) i.next();
      if (ms.getCode().matches(regexp))
        r.add(ms);
    }

    return r;
  }


  public void clearCache() {
    cache = null;
  }

}
