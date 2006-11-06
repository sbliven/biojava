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
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.Attribute;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.ExternalRef;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Sequence;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.Translation;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.RuntimeAdaptorException;
import org.ensembl.driver.TranscriptAdaptor;
import org.ensembl.driver.TranslationAdaptor;
import org.ensembl.util.IDMap;
import org.ensembl.util.SequenceUtil;

public class TranslationImpl extends PersistentImpl implements Translation {

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



	private static final Logger logger = Logger.getLogger(TranslationImpl.class
			.getName());

	public TranslationImpl(CoreDriver driver) {
		super(driver);
	}

	public TranslationImpl() {
	}

	private String accessionID;

	private int version;
	
    private Date idModifiedDate;
    private Date idCreatedDate;


	private List externalRefs;

	private Transcript transcript;

	private long transcriptInternalID;

	private Exon startExon;

	private long startExonInternalID;

	private int positionInStartExon;

	private Exon endExon;

	private long endExonInternalID;

	private int positionInEndExon;

	private Sequence sequence;

	private String[] interproIDs;

	private AttributesHelper attributesHelper = new AttributesHelper(
			new String[] { "_selenocysteine" });

	// flags to prevent constantly trying to lazy load data
	private boolean lazyLoadedAccession = false;

	private boolean lazyLoadedExternalRefs = false;

	private boolean known;

	private boolean lazyLoadKnown = true;

	private List codingLocations;

	private List fivePrimeUTR;

	private List threePrimeUTR;

	private String peptide;

	private List proteinFeatures;

	public synchronized List getCodingLocations() {

		if (codingLocations != null)
			return codingLocations;

		// This method also create _fivePrimeUTR_ and _threePrimeUTR_ as
		// by-products in addition to the _codingLocations_.

		if (positionInStartExon < 1)
			throw new IllegalStateException(
					"positionInStartExon is invalid, should be >1: "
							+ positionInStartExon);

		if (positionInEndExon < 1)
			throw new IllegalStateException(
					"positionInEndExon is invalid, should be >1: "
							+ positionInEndExon);

		final List exons;
		Transcript transcript = getTranscript();
		if (codingLocations == null && transcript != null
				&& (exons = transcript.getExons()) != null) {

			final int nExons = exons.size();

			codingLocations = new ArrayList(nExons); // reasonable initial size
			fivePrimeUTR = new ArrayList();
			threePrimeUTR = new ArrayList();

			boolean firstExon = false;
			boolean lastExon = false;
			boolean codingExon = false;
			boolean threePrimeUTRExon = false;

			for (int e = 0; e < nExons; ++e) {

				final Exon exon = (Exon) exons.get(e);
				final long exonID = exon.getInternalID();
				final Location exonLoc = exon.getLocation();

				firstExon = (exonID == startExonInternalID);

				if (firstExon) {

					codingExon = true;
				}
				lastExon = (exonID == endExonInternalID);
				if (lastExon) {
					threePrimeUTRExon = true;
				}

				if (codingExon) {

					Location codingLoc = exonLoc;

					if (firstExon) {
						// positionInStartExon is relative to strand
						// A positive crop means shrink location.
						final int crop = positionInStartExon - 1;
						if (crop != 0) {

							codingLoc = exonLoc.transform(crop, 0);

							Location utr = exonLoc.transform(0, crop
									- exonLoc.getLength());
							fivePrimeUTR.add(utr);
						}

					}
					if (lastExon) {

						// positionInEndExon is relative to strand.
						// A negative crop means shrink location.
						int crop;
						if (lastExon && firstExon) {
					           crop	= positionInEndExon - positionInStartExon +1 
								- codingLoc.getLength();
                                                } else {
					           crop	= positionInEndExon 
								- exonLoc.getLength();
						}    
						if (crop != 0) {

							if (!firstExon)
								codingLoc = exonLoc.transform(0, crop);
							else 
								codingLoc = codingLoc.transform(0, crop);

							Location utr = exonLoc.transform(exonLoc
									.getLength()
									+ crop, 0);
							threePrimeUTR.add(utr);
						}

					}
					codingLocations.add(codingLoc);

					if (lastExon)
						codingExon = false;
				} else {
					if (threePrimeUTRExon)
						threePrimeUTR.add(exonLoc);
					else
						fivePrimeUTR.add(exonLoc);
				}
			}
		}

		return codingLocations;
	}

	public Location getCodingLocation() {

		Location r = null;

		List locations = getCodingLocations();
		for (int i = 0, n = locations.size(); i < n; i++) {
			Location l = ((Location) locations.get(i)).copy();
			if (r == null)
				r = l;
			else
				r.append(l);
		}

		return r;
	}

	/**
	 * @return list of Locations specifing the UTR at the end of the
	 *         translation, or null if not available.
	 */
	public List getThreePrimeUTR() {
		if (threePrimeUTR == null)
			getCodingLocations();

		return threePrimeUTR;

	}

	/**
	 * @return list of Locations specifing the UTR at the beggining of the
	 *         translation, or null if not available
	 */
	public List getFivePrimeUTR() {

		if (fivePrimeUTR == null)
			getCodingLocations();

		return fivePrimeUTR;
	}

	/**
	 * Converts the position of an amino acid in the translation into the
	 * genomic location of the FIRST nucleic acid which codes for that amino
	 * acid.
	 * 
	 * @param aminoAcidPosition
	 *            indexInPeptide >=1 Position in peptide.
	 * @return The genomic location of first nucleic base used to constuct the
	 *         amino acid at the specified position in the peptide, or null if
	 *         position bigger than translation.
	 *  
	 */
	public Location getAminoAcidStart(int aminoAcidPosition) {

	    if (aminoAcidPosition<1)
	        throw new IllegalArgumentException("aminoAcidPosition must be >=1: "+aminoAcidPosition);
	    
		int transcriptPosition = (aminoAcidPosition - 1) * 3;
		return getCodingLocation().relative(transcriptPosition);
	}

	public Exon getEndExon() {
		if (endExon == null && endExonInternalID > 0 && transcript != null)
			endExon = (Exon) new IDMap(transcript.getExons())
					.get(endExonInternalID);
		return endExon;
	}

	public void setEndExon(Exon endExon) {
		this.endExon = endExon;
		endExonInternalID = endExon.getInternalID();
	}

	public Exon getStartExon() {
		if (startExon == null && startExonInternalID > 0 && transcript != null)
			startExon = (Exon) new IDMap(transcript.getExons())
					.get(startExonInternalID);
		return startExon;
	}

	public void setStartExon(Exon startExon) {
		this.startExon = startExon;
		startExonInternalID = startExon.getInternalID();
	}

	public int getPositionInEndExon() {
		return positionInEndExon;
	}

	public void setPositionInEndExon(int positionInEndExon) {
		this.positionInEndExon = positionInEndExon;
	}

	public int getPositionInStartExon() {
		return positionInStartExon;
	}

	public void setPositionInStartExon(int positionInStartExon) {
		this.positionInStartExon = positionInStartExon;
	}

	/**
	 * References to external databases
	 * 
	 * @return list of ExternalRef objects
	 */
	public List getExternalRefs() {
		if (externalRefs == null && driver != null)
			lazyLoadExternalRefs();
		return externalRefs;
	}

	public void setExternalRefs(List externalRefs) {
		this.externalRefs = externalRefs;
	}

	/**
	 * Sets transcript and transcriptInternalID.
	 */
	public void setTranscript(Transcript transcript) {
		this.transcript = transcript;
		this.transcriptInternalID = transcript.getInternalID();
	}

	public Transcript getTranscript() {
		if (transcript == null && driver != null)
			lazyLoadTranscript();
		return transcript;
	}

	public List getSimilarityFeatures() {
		return null;
	}

	public long getTranscriptInternalID() {
		if (transcript != null)
			transcriptInternalID = transcript.getInternalID();
		return transcriptInternalID;
	}

	public void setTranscriptInternalID(long transcriptInternalID) {
		this.transcriptInternalID = transcriptInternalID;
		if (transcript != null)
			transcript.setInternalID(transcriptInternalID);
	}

	public long getEndExonInternalID() {
		if (endExon != null)
			endExonInternalID = endExon.getInternalID();
		return endExonInternalID;
	}

	public void setEndExonInternalID(long endExonInternalID) {
		this.endExonInternalID = endExonInternalID;
		if (endExon != null)
			endExon.setInternalID(endExonInternalID);

	}

	public long getStartExonInternalID() {
		if (startExon != null)
			startExonInternalID = startExon.getInternalID();
		return startExonInternalID;
	}

	public void setStartExonInternalID(long startExonInternalID) {
		this.startExonInternalID = startExonInternalID;
		if (startExon != null)
			startExon.setInternalID(startExonInternalID);
	}

	/**
	 * @see org.ensembl.datamodel.Translation#getSequence()
	 */
	public Sequence getSequence() {

		if (sequence == null) {
			Transcript transcript = getTranscript();
			//System.out.println("Transcript is " + transcript + " in
			// translation");

			if (transcript != null) {
				//System.out.println("Transcript is not null in translation");
				StringBuffer buf = new StringBuffer(500);
				List exons = transcript.getExons();
				final int nExons = exons.size();

				boolean firstExon = false;
				boolean lastExon = false;
				boolean relevantExon = false;

				boolean containsMonkeyExon = false;
				int prevEndPhase = -1;

				for (int i = 0; i < nExons && !lastExon; ++i) {

					final Exon exon = (Exon) exons.get(i);
					final long exonInternalID = exon.getInternalID();
					final int phase = exon.getPhase();

					// Flags to mark status of current exon
					if (exonInternalID == startExonInternalID)
						firstExon = true;
					else
						firstExon = false;
					if (exonInternalID == endExonInternalID)
						lastExon = true;
					if (firstExon)
						relevantExon = true;

					if (relevantExon) {

						String padding = null;
            
            if (firstExon && phase>0 ) {
             
              if (phase==1) 
                padding = "N";
              else if (phase==2)
                padding = "NN";
              else
                throw new RuntimeAdaptorException("Invalid phase: " + phase + " on exon " + exon);
              
            } else if (!firstExon && prevEndPhase != -1
								&& prevEndPhase != phase) {
						
						  // Handle monkey peptides where (consecutive exons are
              // out of phase) by inserting Ns to pad DNA to ensure 
              // prev exon ends on complete codon and new one begins 
              // with complete codon.
              
              switch (prevEndPhase) {

							case 0:
								if (phase == 1)
									padding = "N";
								else if (phase == 2)
									padding = "NN";
								break;

							case 1:
								if (phase == 0)
									padding = "NN";
								else if (phase == 2)
									padding = "NNNN";
								break;

							case 2:
								if (phase == 0)
									padding = "N";
								else if (phase == 1)
									padding = "NN";
								break;
							}
              
              
						}
						if (padding != null) {
							buf.append(padding);
							if ( !firstExon ) 
							  containsMonkeyExon = true;
						}

						//exon sequence is already in 5' to 3' direction
						String exonSeq = exon.getSequence().getString();

						try {

							if (firstExon && lastExon) // crop beginning and end
								exonSeq = exonSeq.substring(
										positionInStartExon - 1,
										positionInEndExon);
							else if (firstExon) // crop beginning
								exonSeq = exonSeq.substring(
										positionInStartExon - 1, exonSeq
												.length());

							else if (lastExon) // crop end
								exonSeq = exonSeq.substring(0,
										positionInEndExon);
						} catch (StringIndexOutOfBoundsException e) {
							logger
									.warning("Failed to get peptide for translation : "
											+ getAccessionID()
											+ "("
											+ internalID
											+ ") ["
											+ "exon = "
											+ exon
											+ ", exonseq = "
											+ exonSeq
											+ ", positionInStartExon = "
											+ positionInStartExon
											+ ", positionInEndExon = "
											+ positionInEndExon
											+ " : "
											+ e.getMessage());
							throw e;
						}
						buf.append(exonSeq);
					}

					prevEndPhase = exon.getEndPhase();
				}

				sequence = new SequenceImpl();
				sequence.setString(buf.toString());
				setSequence(sequence);

				// Included for development purposes
				if (containsMonkeyExon)
					logger.warning("Translation contains monkey exons : "
							+ getAccessionID() + "(" + getInternalID() + ")");

			}
		}

		return sequence;
	}

	public void setSequence(Sequence sequence) {
		this.sequence = sequence;
	}

	public String getAccessionID() {
		if (accessionID == null && driver != null && !lazyLoadedAccession) {
			try {
				driver.getTranslationAdaptor().fetchAccessionID(this);
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
				driver.getTranslationAdaptor().fetchVersion(this);
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

	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		buf.append("internalID=").append(internalID).append(", ");
		buf.append("accessionID=").append(accessionID).append(", ");

		buf.append("startExonInternalID=").append(
				Long.toString(startExonInternalID)).append(", ");
		buf.append("positionInStartExon=").append(
				Long.toString(positionInStartExon)).append(", ");
		buf.append("startExon=").append(startExon).append(", ");

		buf.append("endExonInternalID=").append(
				Long.toString(endExonInternalID)).append(", ");
		buf.append("positionInEndExon=").append(
				Long.toString(positionInEndExon)).append(", ");
		buf.append("endExon=").append(endExon).append(", ");

		buf.append("externalRefs=").append(externalRefs).append(", ");
		buf.append("transcript=").append(transcript).append(", ");
		buf.append("transcriptInternalID=").append(
				Long.toString(transcriptInternalID)).append(", ");

		List three = getThreePrimeUTR();
		int threeLen = (three == null) ? 0 : three.size();
		List five = getFivePrimeUTR();
		int fiveLen = (five == null) ? 0 : five.size();
		List codingLocs = getCodingLocations();
		int codingLocsLen = (codingLocs == null) ? 0 : codingLocs.size();
		buf.append("#threePrimeUTR=").append(threeLen).append(", ");
		buf.append("#fivePrimeUTR=").append(fiveLen).append(", ");
		buf.append("#codingLocations=").append(codingLocsLen).append(", ");

		buf.append("]");

		return buf.toString();
	}

	/**
	 * Get the translated peptide sequence as single letter amino acid code
	 * string. Note: even if the translation sequence contains a stop codon the
	 * peptide does not end in a "*".
	 */
	public synchronized String getPeptide() {

		if (peptide != null)
			return peptide;

		Sequence seq = getSequence();

		if (seq != null) {
			String dnaSeq = seq.getString();
			peptide = SequenceUtil.dna2protein(dnaSeq, true);

			if (peptide.length() > 0) {
				if (transcript != null && transcript.isSequenceEditsEnabled())
					peptide = attributesHelper.applyEdits(peptide);
			}

		}

		return peptide;

	}

	public void setPeptide(String peptide) {
		this.peptide = peptide;
	}

	/**
	 * Whether this is a known translation.
	 * 
	 * @return value of known.
	 */
	public boolean isKnown() {
		if (lazyLoadKnown)
			lazyLoadKnown();
		return known;
	}

	/**
	 * Sets whether this is a known translation.
	 * 
	 * @param v
	 *            Value to assign to known.
	 */
	public void setKnown(boolean v) {
		lazyLoadKnown = false;
		this.known = v;
	}

	private synchronized void lazyLoadExternalRefs() {
		if (lazyLoadedExternalRefs)
			return;

		try {
			externalRefs = driver.getExternalRefAdaptor().fetch(
					this.internalID, ExternalRef.TRANSLATION);
		} catch (AdaptorException e) {
			throw new RuntimeAdaptorException(e);
		} finally {
			lazyLoadedExternalRefs = true;
		}
	}

	private synchronized void lazyLoadTranscript() {

		try {
			// Load a new transcript corresponding to the transcriptInternalID
			TranscriptAdaptor ta = (TranscriptAdaptor) driver
					.getTranscriptAdaptor();
			Transcript transcript = ta.fetch(getTranscriptInternalID());

			// set the transcript
			setTranscript(transcript);
			transcript.setTranslation(this);

		} catch (AdaptorException e) {
			logger.warning(e.getMessage());
			// Tidy up to avoid leaving tranlation in invalid state
			transcript = null;
		}
	}

	private synchronized void lazyLoadKnown() {

		lazyLoadKnown = false;

		if (driver != null) {
			try {

				TranslationAdaptor ta = driver.getTranslationAdaptor();
				if (ta != null)
					ta.fetchKnown(this);

			} catch (AdaptorException e) {
				System.err.println("Failed to lazy load translation.known. " );
				e.printStackTrace( System.err );
			}
		}
	}

	public boolean hasMonkeyExons() {

		boolean containsMonkeyExon = false;

		if (sequence == null) {
			Transcript transcript = getTranscript();
			//System.out.println("Transcript is " + transcript + " in
			// translation");

			if (transcript != null) {
				//System.out.println("Transcript is not null in translation");

				List exons = transcript.getExons();
				final int nExons = exons.size();

				boolean firstExon = false;
				boolean lastExon = false;
				boolean relevantExon = false;

				containsMonkeyExon = false;
				int prevEndPhase = -1;

				for (int i = 0; i < nExons && !lastExon; ++i) {

					final Exon exon = (Exon) exons.get(i);
					final long exonInternalID = exon.getInternalID();
					final int phase = exon.getPhase();

					// Flags to mark status of current exon
					if (exonInternalID == startExonInternalID)
						firstExon = true;
					else
						firstExon = false;
					if (exonInternalID == endExonInternalID)
						lastExon = true;
					if (firstExon)
						relevantExon = true;

					if (relevantExon) {

						// Handle monkey peptides where (consecutive exons are
						// out of
						// phase) by inserting Ns to pad DNA to ensure prev exon
						// ends on
						// complete codon and new one begins with complete
						// codon.
						String padding = null;
						if (!firstExon && prevEndPhase != -1
								&& prevEndPhase != phase) {
							System.out.println(getAccessionID() + " phase: "
									+ phase + " prev end phase: "
									+ prevEndPhase);
							switch (prevEndPhase) {

							case 0:
								if (phase == 1)
									padding = "N";
								else if (phase == 2)
									padding = "NN";
								break;

							case 1:
								if (phase == 0)
									padding = "NN";
								else if (phase == 2)
									padding = "NNNN";
								break;

							case 2:
								if (phase == 0)
									padding = "N";
								else if (phase == 1)
									padding = "NN";
								break;
							}
						}
						if (padding != null) {

							containsMonkeyExon = true;
							logger.fine(padding + ": " + prevEndPhase + ","
									+ phase);
						}

						//exon sequence is already in 5' to 3' direction
						String exonSeq = exon.getSequence().getString();

						if (firstExon && lastExon) // crop beginning and end
							exonSeq = exonSeq.substring(
									positionInStartExon - 1, positionInEndExon);
						else if (firstExon) // crop beginning
							exonSeq = exonSeq.substring(
									positionInStartExon - 1, exonSeq.length());

						else if (lastExon) // crop end
							exonSeq = exonSeq.substring(0, positionInEndExon);

					}

					prevEndPhase = exon.getEndPhase();
				}

			}
		}

		return containsMonkeyExon;

	}

	public String[] getInterproIDs() throws AdaptorException {
		if (interproIDs != null)
			return interproIDs;
		else {
			if (driver != null) {
				driver.getTranslationAdaptor().completeInterproIDs(this);
			} else {
				interproIDs = new String[0];
			}
		}
		return interproIDs;
	}

	public void setInterproIDs(String[] strings) {
		interproIDs = strings;
	}

	/**
	 * Sequence edits are sorted by start values.
	 * 
	 * @see org.ensembl.datamodel.Translation#getSequenceEdits()
	 */
	public synchronized List getSequenceEdits() {
		return attributesHelper.getSequenceEdits();
	}

	/**
	 * @see org.ensembl.datamodel.Translation#getAttributes()
	 */
	public List getAttributes() {
		return attributesHelper.getAttributes();
	}

	public void addAttribute(Attribute attribute) {
		peptide = null;
		attributesHelper.addAttribute(attribute);
	}

	public boolean removeAttribute(Attribute attribute) {
		peptide = null;
		return attributesHelper.removeAttribute(attribute);
	}

	/**
	 * @see org.ensembl.datamodel.Translation#getAttributes(java.lang.String)
	 */
	public List getAttributes(String code) {
		return attributesHelper.getAttributes(code);
	}

	public List getProteinFeatures() {
		if (proteinFeatures == null)
			try {
				proteinFeatures = driver.getProteinFeatureAdaptor().fetch(this);
			} catch (AdaptorException e) {
				throw new RuntimeAdaptorException("Problem fetching protein features for translation "+ internalID, e);
			}

		return proteinFeatures;
	}
}
