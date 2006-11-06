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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.Analysis;
import org.ensembl.datamodel.Attribute;
import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.ExternalRef;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Sequence;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.Translation;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.LocationConverter;
import org.ensembl.driver.RuntimeAdaptorException;
import org.ensembl.util.StringUtil;

/**
 * Transcript implementation with lazy loading capabilities.
 */
public class TranscriptImpl extends BaseFeatureImpl implements Transcript {

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



	private static Logger logger =
		Logger.getLogger(TranscriptImpl.class.getName());
	private final String[] EMPTY_STING_ARRAY = new String[0];

	private Translation translation;
	private List exons;
	private long geneInternalID;
	private long translationInternalID;
	private String displayName;
	private Gene gene;
	private Sequence sequence;
	//private Location location;
	private int length = -1;
	private List xrefs = null;
	private boolean sequenceEditsEnabled = true;
	private AttributesHelper attributesHelper =
		new AttributesHelper(new String[] { "_rna_edit" });

	// flags to prevent constantly trying to lazy load data 
	private boolean lazyLoadedAccession = false;

	private boolean lazyLoadedExternalRefs = false;
	private boolean translationLazyLoaded = false;

	private List supportingFeatures;
	
	private String accessionID;
	/** -1 indicates unset status. */
	private int version = -1;
    private Date idModifiedDate;
    private Date idCreatedDate;


    private String status;



    private String biotype;

	public TranscriptImpl(CoreDriver driver) {
		super(driver);
	}

	public TranscriptImpl() {
	}

	public List getExternalRefs() {
		return getExternalRefs(true);
	}

	public List getExternalRefs(boolean includeTranslation) {

		List r = new ArrayList();

		if (xrefs == null) {

			if (driver != null) {
				try {
					xrefs =
						driver.getExternalRefAdaptor().fetch(
							this.internalID,
							ExternalRef.TRANSCRIPT);
				} catch (AdaptorException e) {
          throw new RuntimeAdaptorException(e);
				}
			} else {
				xrefs = new ArrayList();
			}
		}

		r.addAll(xrefs);
    getTranslation();//lazy load if needed
		if (includeTranslation && translation != null)
			r.addAll(translation.getExternalRefs());

		return r;
	}

	/**
	 * @return exons.
	 */
	public List getExons() {

		if (exons == null && driver != null)
			getGene();

		return exons;
	}

	public void setExons(List exons) {
		this.exons = exons;

		// associate each of the exons with this transcript
		Iterator it = exons.iterator();
		while (it.hasNext()) {
			Exon e = (Exon) it.next();
			e.addTranscript(this);
		}

		//location = null;
	}

	/**
	 * @return accession if available, otherwise null.
	 */
	public String getAccessionID() {
		if (accessionID == null && driver != null && !lazyLoadedAccession) {
			try {
				// loads accession into this instance.
				driver.getTranscriptAdaptor().fetchAccessionID(this);
				lazyLoadedAccession = true;
			} catch (AdaptorException e) {
			}
		}
		return accessionID;
	}

	public void setAccessionID(String accessionID) {
		this.accessionID = accessionID;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getVersion() {
		if (version < 0 && driver != null) {
			try {
				driver.getTranscriptAdaptor().fetchVersion(this);
			} catch (AdaptorException e) {
				logger.warning(e.getMessage());
			}
		}
		return version;
	}
	
	public Date getCreatedDate() {
		return idCreatedDate;
	}

	/**
	 * @param idCreatedDate The idCreatedDate to set.
	 */
	public void setCreatedDate(Date idCreatedDate) {
		this.idCreatedDate = idCreatedDate;
	}

	/**
	 * @return Returns the idModifiedDate.
	 */
	public Date getModifiedDate() {
		return idModifiedDate;
	}

	/**
	 * @param idModifiedDate The idModifiedDate to set.
	 */
	public void setModifiedDate(Date idModifiedDate) {
		this.idModifiedDate = idModifiedDate;
	}


	/**
	 * @return combined length of all exons.
	 */
	public int getLength() {

		if (length == -1) {
			length = 0;
			if (exons != null) {
				final int nExons = exons.size();
				for (int e = 0; e < nExons; ++e) {
					length += ((Exon) exons.get(e)).getLocation().getLength();
				}
			}
		}

		return length;
	}

	/**
	 * @return dispaly name if available, otherwise null.
	 */
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return list of Locations specifing the UTR at the end of the
	 * translation, or null if not available.
	 */
	public List getThreePrimeUTR() {
		if (translation == null) {
			getTranslation(); // lazy load if possible 
			if (translation == null)
				return null; // failed to lazy load
		}

		return translation.getThreePrimeUTR();
	}

	/**
	 * @return list of Locations specifing the UTR at the beggining of the
	 * translation, or null if not available  */
	public List getFivePrimeUTR() {

		if (translation == null) {
			getTranslation(); // lazy load if possible 
			if (translation == null)
				return null; // failed to lazy load
		}

		return translation.getFivePrimeUTR();
	}

	public Gene getGene() {

		if (gene == null && exons == null && driver != null)
			try {
				driver.getTranscriptAdaptor().fetchComplete(this);
			} catch (AdaptorException e) {
				throw new RuntimeException(
					"Failed to lazy load transcript:" + this,
					e);
			}

		return gene;
	}

	public void setGene(Gene gene) {
		this.gene = gene;
		this.geneInternalID = gene.getInternalID();
	}

	public long getGeneInternalID() {
		if (gene != null)
			geneInternalID = gene.getInternalID();
		return geneInternalID;
	}

	public void setGeneInternalID(long geneInternalID) {
		this.geneInternalID = geneInternalID;
		if (gene != null)
			gene.setInternalID(geneInternalID);
	}

	public long getTranslationInternalID() {
		if (translation != null)
			translationInternalID = translation.getInternalID();
		return translationInternalID;
	}

	public void setTranslationInternalID(long translationInternalID) {
		this.translationInternalID = translationInternalID;
		if (translation != null)
			translation.setInternalID(translationInternalID);
	}

	public void setTranslation(Translation translation) {
		this.translation = translation;
		this.translationInternalID = translation.getInternalID();
	}

	public Translation getTranslation() {
		if (translation == null && !translationLazyLoaded && driver != null) {

			try {

				translation =
					driver.getTranslationAdaptor().fetchByTranscript(
						internalID);
				if (translation != null)
					translation.setTranscript(this);
				translationLazyLoaded = true;

			} catch (AdaptorException e) {
				translation = null;
				throw new RuntimeException(
					"Failed to lazy load translation for transcript"
						+ internalID,
					e);
			}
		}
		return translation;
	}


	/**
	 * Creates a location from the locations contained in the exons.
	 */
	static Location deriveLocationFromExons(List exons) {

		final int nExons = exons.size();

		Location loc = ((Exon) exons.get(0)).getLocation();

		// Simple case, no extra processing needed.
		if (nExons == 1)
			return loc;

		// Find lowest 'start' and highest 'end'
		int start = Integer.MAX_VALUE;
		int end = 0;
		for (int i = 0; i < nExons; ++i) {
			Exon exon = (Exon) exons.get(i);
			for (Location l = exon.getLocation(); l != null; l = l.next()) {
				int s = l.getStart();
				int e = l.getEnd();
				if (s < start)
					start = s;
				if (e > end)
					end = e;
			}
		}

		loc =
			new Location(
				loc.getCoordinateSystem(),
				loc.getSeqRegionName(),
				start,
				end,
				loc.getStrand());

		return loc;
	}

	/**
	 * @see org.ensembl.datamodel.Transcript#getSequence()
	 */
	public synchronized Sequence getSequence() {
		if (sequence == null && getExons() != null) {
			StringBuffer buf = new StringBuffer();
			final int nExons = exons.size();
			for (int i = 0; i < nExons; ++i) {
				Exon exon = (Exon) exons.get(i);
				Sequence seq = exon.getSequence();
				buf.append(seq.getString());
			}
			String tmp = buf.toString();

			if (sequenceEditsEnabled)
				tmp = attributesHelper.applyEdits(tmp);

			sequence = new SequenceImpl(tmp);

		}
		return sequence;
	}

	/**
	 * Sets sequence on parent _translation_.
	 */
	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}

	/**
	 * Whether this is a known transcript. 
	 * @return true if status is KNOWN.
   * @see getStatus()
	 */
	public boolean isKnown() {
		return "KNOWN".equals(status);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		buf.append("{").append(super.toString()).append("}, ");
    buf.append("accessionID=").append(accessionID).append(", ");
		buf.append("description=").append(displayName).append(", ");
		buf.append("status=").append(status).append(", ");
    buf.append("length=").append(getLength()).append(", ");

		List three = getThreePrimeUTR();
		int threeLen = (three == null) ? 0 : three.size();
		List five = getFivePrimeUTR();
		int fiveLen = (five == null) ? 0 : five.size();
		buf.append("#threePrimeUTRs=").append(threeLen).append(", ");
		buf.append("#fivePrimeUTRs=").append(fiveLen).append(", ");

		buf.append("geneInternalID=").append(getGeneInternalID()).append(", ");
		buf.append("gene=").append(StringUtil.setOrUnset(gene)).append(", ");
		buf.append("translationInternalID=").append(
			translationInternalID).append(
			", ");
		buf
			.append("#exons=")
			.append(
				((getExons() == null)
					? "null"
					: Integer.toString(getExons().size())))
			.append(", ");
		;
		buf.append("]");

		return buf.toString();
	}

	public String[] getInterproIDs() throws AdaptorException {
		Translation t = getTranslation();
		return (t == null) ? EMPTY_STING_ARRAY : t.getInterproIDs();
	}

	public Location getCDNALocation() {
		if (exons == null)
			getExons();
		Location[] exonLocs = new Location[exons.size()];
		for (int i = 0, n = exons.size(); i < n; i++) {
			Exon exon = (Exon) exons.get(i);
			exonLocs[i] = exon.getLocation();
		}

		return new Location(exonLocs);
	}

	/**
	 * @see org.ensembl.datamodel.Transcript#isSequenceEditsEnabled()
	 */
	public boolean isSequenceEditsEnabled() {
		return sequenceEditsEnabled;
	}

	/**
	 * @see org.ensembl.datamodel.Transcript#setSequenceEditsEnabled(boolean)
	 */
	public void setSequenceEditsEnabled(boolean sequenceEditsEnabled) {
		if (this.sequenceEditsEnabled != sequenceEditsEnabled) {
			sequence = null;
		}
		this.sequenceEditsEnabled = sequenceEditsEnabled;
	}

	/**
	 * @see org.ensembl.datamodel.Transcript#getAttributes()
	 */
	public List getAttributes() {
		return attributesHelper.getAttributes();
	}

	/**
	 * @see org.ensembl.datamodel.Transcript#getAttributes(java.lang.String)
	 */
	public List getAttributes(String code) {
		return attributesHelper.getAttributes(code);
	}

	/**
	 * Sequence edits are sorted by start values.
	 * @see org.ensembl.datamodel.Transcript#getSequenceEdits()
	 */
	public synchronized List getSequenceEdits() {
		return attributesHelper.getSequenceEdits();
	}

	/**
	 * @see org.ensembl.datamodel.Transcript#addAttribute(Attribute)
	 */
	public void addAttribute(Attribute attribute) {
		sequence = null;
		attributesHelper.addAttribute(attribute);
	}

	/**
	 * @see org.ensembl.datamodel.Transcript#removeAttribute(Attribute)
	 */
	public boolean removeAttribute(Attribute attribute) {
		boolean r = attributesHelper.removeAttribute(attribute);
		if (r)
			sequence = null;
		return r;
	}

  /**
   * Updates the location.coordinate system for this transcript
   * and it's exons.
   * 
   * @see org.ensembl.datamodel.Locatable#setCoordinateSystem(org.ensembl.datamodel.CoordinateSystem,
   *      org.ensembl.driver.LocationConverter)
   */
  public void setCoordinateSystem(CoordinateSystem coordinateSystem,
      LocationConverter locationConverter) throws AdaptorException {

    super.setCoordinateSystem(coordinateSystem, locationConverter);

    List es = getExons();
    for (int i = 0; i < es.size(); i++) 
      ((Exon) es.get(i)).setCoordinateSystem(coordinateSystem,
          locationConverter);
  }

  public String getStatus() {
    return status;
  }
  
  public void setStatus(String confidence) {
    this.status = confidence;
  }	
  
  public String getConfidence() {
    return getStatus();
  }
  
  
  public List getSupportingFeatures() {

    if (supportingFeatures == null && driver != null) {
        try {
            supportingFeatures = driver.getTranscriptAdaptor().fetchSupportingFeatures(getInternalID());
        } catch (AdaptorException e) {
            throw new RuntimeAdaptorException(e);
        }
    }
    return supportingFeatures;
}

  /**
   * @see org.ensembl.datamodel.Transcript#setBioType(java.lang.String)
   */
  public void setBioType(String biotype) {
    this.biotype = biotype;
  }

  /**
   * @see org.ensembl.datamodel.Transcript#getBioType()
   */
  public String getBioType() {
    return biotype;
  }
  
  
  public Analysis getAnalysis() {
    if (analysis==null && analysisID<1 ) {
      // use gene.analysis if transcript analysis is unset.
      analysis = getGene().getAnalysis();
      analysisID = analysis.getInternalID();
    } else {
      analysis = super.getAnalysis();
    }
    return analysis;
  }

  public long getAnalysisID() {
    // use gene.analysis if transcript analysis is unset.
    if (analysis==null && analysisID<1 ) 
      analysisID = analysis.getInternalID();
    return analysisID;
  }
  
}
