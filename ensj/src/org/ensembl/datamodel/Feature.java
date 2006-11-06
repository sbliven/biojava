/*
    Copyright (C) 2002 EBI, GRL

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




package org.ensembl.datamodel;


/**
 * Simple genomic feature. 
 */
public interface Feature extends Locatable {

    String getDisplayName();
    void setDisplayName(String displayName);

    String getDescription();

    void setDescription(String description);

    /**
  	 * Get the analysis that created this feature.
  	 */
  	Analysis getAnalysis();

    /**
  	 * Get analysisID of the analysis that created this feature. 
  	 * 
  	 * @return internal ID of the analysis that created this feature.
  	 * @see #getAnalysis()
  	 */
  	long getAnalysisID();

	/**
	 * Set the analysisID of the analysis that created this feature. 
	 *
	 * @param analysisID internalID of the analysis that created this feature.
	 * @see #setAnalysis(Analysis)
	 */
  void setAnalysisID(long analysisID);
  
  /**
	 * Set the analysis that created this feature.
	 * 
	 * @param analysis analysis that created this feature.
	 */
  void setAnalysis(Analysis analysis);
    
    
}
