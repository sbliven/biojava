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

package org.ensembl.variation.datamodel.impl;

import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.RuntimeAdaptorException;
import org.ensembl.variation.datamodel.TranscriptVariation;
import org.ensembl.variation.datamodel.VariationFeature;
import org.ensembl.variation.driver.VariationDriver;

/**
 * The point of this class is....
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class TranscriptVariationImpl extends PersistentImpl implements
		TranscriptVariation {

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



	private VariationDriver vdriver;

	private Transcript transcript;

	private long transcriptID;

	private VariationFeature variationFeature;

	private long variationFeatureID;

	private String peptideAlleleString;

	private int cDNAstart;

	private int cDNAend;

	private int translationStart;

	private int translationEnd;

	private String consequenceType;

	/**
	 * Creates a TranscriptVariation instance that will lazy load transcript and
	 * variation feature on demand.
	 * 
	 * @param vdriver
	 * @param transcriptID
	 * @param translationStart
	 * @param translationEnd
	 * @param variationFeatureID
	 * @param cDNAstart
	 * @param cDNAend
	 * @param peptideAlleleString
	 * @param consequenceType
	 */
	public TranscriptVariationImpl(VariationDriver vdriver, long transcriptID,
			int translationStart, int translationEnd, long variationFeatureID,
			int cDNAstart, int cDNAend, String peptideAlleleString,
			String consequenceType) {
		super(vdriver.getCoreDriver());
		this.vdriver = vdriver;
		this.transcriptID = transcriptID;
		this.translationStart = translationStart;
		this.translationEnd = translationEnd;
		this.variationFeatureID = variationFeatureID;
		this.cDNAstart = cDNAstart;
		this.cDNAend = cDNAend;
		this.peptideAlleleString = peptideAlleleString;
		this.consequenceType = consequenceType;
	}

	/**
	 * Returns transcript which is lazy loaded on demand.
	 * 
	 * @see org.ensembl.variation.datamodel.TranscriptVariation#getTranscript()
	 * @throws RuntimeAdaptorException if lazy loading is attempted and fails due
	 * to an AdaptorException.
	 */
	public Transcript getTranscript() {
		CoreDriver driver = null;
		if (transcript == null && transcriptID > 0 && vdriver != null
				&& (driver = vdriver.getCoreDriver()) != null)
			try {
				transcript = driver.getTranscriptAdaptor().fetch(transcriptID);
			} catch (AdaptorException e) {
				throw new RuntimeAdaptorException(
						"Failed to lazy load transcript: " + transcriptID, e);
			}
		return transcript;
	}

	/**
	 * Returns variation feature which is lazy loaded on demand.
	 * @see org.ensembl.variation.datamodel.TranscriptVariation#getVariationFeature()
	 * @throws RuntimeAdaptorException if lazy loading is attempted and fails due
	 * to an AdaptorException.
	 */
	public VariationFeature getVariationFeature() {
		if (variationFeature==null && variationFeatureID>0 && vdriver!=null)
			try {
				variationFeature = vdriver.getVariationFeatureAdaptor().fetch(variationFeatureID);
			} catch (AdaptorException e) {
				throw new RuntimeAdaptorException("Failed to lazy load variationFeature: " + variationFeatureID,e);
			}
		return variationFeature;
	}

	/**
	 * @see org.ensembl.variation.datamodel.TranscriptVariation#getPeptideAlleleString()
	 */
	public String getPeptideAlleleString() {
		return peptideAlleleString;
	}

	/**
	 * @see org.ensembl.variation.datamodel.TranscriptVariation#getCDNAstart()
	 */
	public int getCDNAstart() {
		return cDNAstart;
	}

	/**
	 * @see org.ensembl.variation.datamodel.TranscriptVariation#getCDNAend()
	 */
	public int getCDNAend() {
		return cDNAend;
	}

	/**
	 * @see org.ensembl.variation.datamodel.TranscriptVariation#getTranslationStart()
	 */
	public int getTranslationStart() {
		return translationStart;
	}

	/**
	 * @see org.ensembl.variation.datamodel.TranscriptVariation#getTranslationEnd()
	 */
	public int getTranslationEnd() {
		return translationEnd;
	}

	/**
	 * @see org.ensembl.variation.datamodel.TranscriptVariation#getConsequenceType()
	 */
	public String getConsequenceType() {
		return consequenceType;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();

		buf.append("[");
		buf.append(super.toString());
		buf.append(", transcript=").append(getTranscript());
		buf.append(", translationStart=").append(translationStart);
		buf.append(", translationeEnd=").append(translationEnd);
		buf.append(", variationFeature=").append(variationFeature);
		buf.append(", cDNAstart=").append(cDNAstart);
		buf.append(", cDNAend=").append(cDNAend);
		buf.append(", peptideAlleleString=").append(peptideAlleleString);
		buf.append(", consequenceType=").append(consequenceType);
		buf.append("]");

		return buf.toString();
	}
}
