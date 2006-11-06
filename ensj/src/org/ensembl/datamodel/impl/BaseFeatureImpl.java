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
package org.ensembl.datamodel.impl;

import org.ensembl.datamodel.Analysis;
import org.ensembl.datamodel.Feature;
import org.ensembl.datamodel.Location;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.RuntimeAdaptorException;

/**
 * All locatable classes have locations and have sequences.
 **/

public class BaseFeatureImpl extends LocatableImpl implements Feature {

  /**
   * Used by the (de)serialization system to determine if the data 
   * in a serialized instance is compatible with this class.
   *
   * It's presence allows for compatible serialized objects to be loaded when
   * the class is compatible with the serialized instance, even if:
   *
   * <ul>
   * <li> the compiler used to compile the "serializing" version of the class
   * differs from the one used to compile the "deserialising" version of the
   * class.</li>
   *
   * <li> the methods of the class changes but the attributes remain the same.</li>
   * </ul>
   *
   * Maintainers must change this value if and only if the new version of
   * this class is not compatible with old versions. e.g. attributes
   * change. See Sun docs for <a
   * href="http://java.sun.com/j2se/1.4.2/docs/guide/serialization/">
   * details. </a>
   *
   */
  private static final long serialVersionUID = 1L;



	private String displayName;
	private String description;
	protected Analysis analysis;
  protected long analysisID;

	
	public BaseFeatureImpl(long internalID) {
		super(internalID);
	}

	public BaseFeatureImpl(long internalID, Location location) {
		super(internalID, location);
	}

	public BaseFeatureImpl() {
		super();
	}

	public BaseFeatureImpl(CoreDriver driver) {
		super(driver);
	}

	/**
	 * @param driver
	 * @param internalID
	 * @param location
	 */
	public BaseFeatureImpl(CoreDriver driver, long internalID, Location location) {
		super(driver);
		this.internalID = internalID;
		this.location = location;
	}

	public String getDisplayName() {
		// Write your code here
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		// Write your code here
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		buf.append("{").append(super.toString()).append("}, ");
		buf.append("displayName=").append(getDisplayName()).append(", ");
		buf.append("description=").append(getDescription()).append(", ");
		buf.append("]");

		return buf.toString();
	}


	public Analysis getAnalysis() {
	  if (analysis==null && analysisID>0 && driver!=null)
      try {
        analysis = driver.getAnalysisAdaptor().fetch(analysisID);
      } catch (AdaptorException e) {
        throw new RuntimeAdaptorException(e);
      }
		return analysis;
	}

	public long getAnalysisID() {
		return analysisID;
	}


	public void setAnalysis(Analysis analysis) {
		this.analysis = analysis;
		analysisID = analysis.getInternalID();
	}


	public void setAnalysisID(long analysisID) {
	  if (analysis!=null && analysisID!=analysis.getInternalID())
	    throw new RuntimeException("You can't set analysisID if it is different to the internalID of" +
	    		"the analysis currently set. Call setAnalysis(null) first.");
	  this.analysisID = analysisID;
	}
	
}
