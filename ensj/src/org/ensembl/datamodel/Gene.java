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

package org.ensembl.datamodel;

import java.util.List;

import org.ensembl.driver.AdaptorException;

/**
 * Gene. 
 */
public interface Gene extends Accessioned, Feature {

  /**
   * Returns the status of the gene.
   * 
   * Delegates to getStatus(). 
   * @deprecated since version 34.1. Use getStatus() instead.
   * @see #getStatus()
   */
  String getConfidence();
  
  /**
   * The status of the gene.
   * 
   * Normally one of these Strings: 
   * 'KNOWN','NOVEL','PUTATIVE','PREDICTED'.
   * @return status of the gene..
   */
	String getStatus();
	
	/**
   * Set the status this the gene.
   * @param status status of the gene.
   */
	void setStatus(String status);

        String getSource();
        void setSource(String source);

	boolean isCompleted();
	void setCompleted(boolean completed);

	boolean isSplicable();
	void setSplicable(boolean splicable);

	/**
   * External refs for this gene, it's transcripts and 
   * translations. This is the same as getExternalRefs(true).
   * Use getExternalRefs(false) if you only want
   * external refs for the gene and not the translation.
   * @return list of ExternalRef objects, an empty list if none available.
   */
  public List getExternalRefs();


  /**
   * External refs for this gene and optionally
   * those of it's transcripts and translations.
   * @param includeTranscriptsAndTranslations whether to include external refs
   * for the transcripts and translations.
   * @return list of ExternalRef objects, an empty list if none available.
   */
  public List getExternalRefs(boolean includeTranscriptsAndTranslations);
  
  
  
  /**
   * Interpro IDs associated with this gene.
   * @return array of zero or more interpro IDs.
   * @throws AdaptorException if a problem occurs retrieving the IDs from the database.
   */
  String[] getInterproIDs() throws AdaptorException;

	void setTranscriptsAndExons(List transcripts, List exons);

	List getTranscripts();

	List getExons();

	void setLocation(Location location);

	void setSequence(Sequence sequence);

	/**
	 * Set the biological type of the gene.
	 * @param type biological type.
	 */
	void setBioType(String type);
	
	/**
	 * Return the biological type of the gene.
	 * @return biological type.
	 * @deprecated since ensj34.2 use getBioType() instead.
	 * @see #getBioType()
	 */
	String getType();
	
	/**
	 * Return the biological type of the gene.
	 * @return biological type.
	 */
	String getBioType();

	/**
	 * Whether this is a known Gene. Genes are known any of their 
	 * transcripts are known.
	 * @return value of known.  */
	boolean isKnown();

}
