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
import org.ensembl.datamodel.impl.OligoArrayImpl;
import org.ensembl.driver.Adaptor;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.OligoArrayAdaptor;

/**
 * Implemementation of the OligoArrayAdaptor that works with standard ensembl
 * myssql databases.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class OligoArrayAdaptorImpl extends BaseFeatureAdaptorImpl implements
		Adaptor, OligoArrayAdaptor {

	/**
	 * @param driver
	 */
	public OligoArrayAdaptorImpl(CoreDriverImpl driver) {
		super(driver, TYPE, 100);
	}

	/**
	 * @see org.ensembl.driver.OligoArrayAdaptor#fetch(long)
	 */
	public OligoArray fetch(long internalID) throws AdaptorException {
	  OligoArray aa = (OligoArray) fetchFromCache(internalID);
	  if (aa==null) {
	    aa = (OligoArray) fetchByInternalID(internalID);
	    if (aa!=null)
	      addToCache(aa, aa.getName());
	  }
		return aa;
	}

	/**
	 * @see org.ensembl.driver.OligoArrayAdaptor#fetch()
	 */
	public List fetch() throws AdaptorException {
	  List r = fetchByNonLocationConstraint("");
	  addToCache(r);
	  return r;
	}

	/**
	 * @see org.ensembl.driver.OligoArrayAdaptor#fetch(java.lang.String)
	 */
	public OligoArray fetch(String name) throws AdaptorException {
	  OligoArray aa = (OligoArray) fetchFromCache(name);
	  if (aa==null) {
			List tmp = fetchByNonLocationConstraint(" name = '" + name + "'");
			addToCache(tmp);
			aa = (OligoArray) (tmp.size() == 0 ? null : tmp.get(0));	    
	  }
		return aa;
	}


	private void addToCache(List arrays) {
	  for (int i = 0; i < arrays.size(); i++) {
      OligoArray aa = (OligoArray) arrays.get(i);
      addToCache(aa, aa.getName());
    }
    
  }
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#tables()
	 */
	protected String[][] tables() {
		final String[][] tables = { { "oligo_array", "aa" } };
		return tables;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#columns()
	 */
	protected String[] columns() {
		final String[] columns = { "aa.oligo_array_id", "aa.parent_array_id",
				"aa.probe_setsize", "aa.name", "aa.type" };
		return columns;
	}

	/**
	 * @see org.ensembl.driver.impl.BaseFeatureAdaptorImpl#createObject(java.sql.ResultSet)
	 */
	public Object createObject(ResultSet rs) throws AdaptorException {
		try {
			if (!rs.next())
				return null;

			return new OligoArrayImpl(driver, rs.getLong(1), rs.getString(4), rs
					.getInt(3), rs.getString(5));
		} catch (SQLException e) {
			throw new AdaptorException(
					"Failed to create OligoArray from database", e);
		}
	}

}
