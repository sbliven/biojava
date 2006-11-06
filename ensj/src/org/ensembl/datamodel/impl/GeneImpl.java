/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.datamodel.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.ExternalRef;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.Translation;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.GeneAdaptor;
import org.ensembl.driver.LocationConverter;
import org.ensembl.driver.RuntimeAdaptorException;

public class GeneImpl extends BaseFeatureImpl implements Gene {

  /**
   * Used by the (de)serialization system to determine if the data in a
   * serialized instance is compatible with this class.
   * 
   * It's presence allows for compatible serialized objects to be loaded when
   * the class is compatible with the serialized instance, even if:
   * 
   * <ul>
   * <li>the compiler used to compile the "serializing" version of the class
   * differs from the one used to compile the "deserialising" version of the
   * class.</li>
   * 
   * <li>the methods of the class changes but the attributes remain the same.
   * </li>
   * </ul>
   * 
   * Maintainers must change this value if and only if the new version of this
   * class is not compatible with old versions. e.g. attributes change. See Sun
   * docs for <a
   * href="http://java.sun.com/j2se/1.4.2/docs/guide/serialization/"> details.
   * </a>
   *  
   */
  private static final long serialVersionUID = 1L;

  private static final Logger logger = Logger.getLogger(GeneImpl.class
      .getName());

  int version;

  private String accessionID;

  private Date idCreatedDate, idModifiedDate;
  
  private boolean splicable;

  private List transcripts;

  private List exons;

  private String status;

  private String source;

  private boolean completed;

  private String type;

  private boolean exonAndTranscriptsLazyLoaded = false;

  // flags to prevent constantly trying to lazy load data
  private boolean lazyLoadedAccession = false;

  /**
   * ExternalRefs that are associated with the gene (not it's transcripts or
   * translations)
   */
  private List xrefs;

  public GeneImpl() {
    super();
  }

  public GeneImpl(long internalID, Location location) {
    super(internalID, location);
  }

  public GeneImpl(CoreDriver driver) {
    super(driver);
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public int getVersion() {
    return version;
  }

  public String getAccessionID() {
    if (accessionID == null && driver != null && !lazyLoadedAccession) {
      try {
        driver.getGeneAdaptor().fetchAccessionID(this);
        lazyLoadedAccession = true;
      } catch (AdaptorException e) {
      }
    }
    return accessionID;
  }

  public void setAccessionID(String accessionID) {
    this.accessionID = accessionID;
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
  
  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public boolean isSplicable() {
    return splicable;
  }

  public void setSplicable(boolean splicable) {
    this.splicable = splicable;
  }

  
  /**
   * Whether this is a known transcript. 
   * @return true if status is KNOWN.
   * @see getStatus()
   */
  public boolean isKnown() {
    return "KNOWN".equals(status);
  }

  /**
   * References to external databases. They are a collection of all the
   * transcript.externalRefs.
   * 
   * @return list of ExternalRef objects, list is empty if no external
   *         references available.
   */
  public List getExternalRefs() {
    return getExternalRefs(true);
  }

  public List getExternalRefs(boolean includeTranscriptsAndTranslations) {

    List r = new ArrayList();

    // lazy load xrefs for gene
    if (xrefs == null) {
      if (driver != null) {
        try {
          xrefs = driver.getExternalRefAdaptor().fetch(this.internalID,
              ExternalRef.GENE);
        } catch (AdaptorException e) {
          throw new RuntimeAdaptorException(e);
        }
      } else {
        xrefs = new ArrayList();
      }
    }
    r.addAll(xrefs);

    if (includeTranscriptsAndTranslations) {
      getTranscripts(); // will force transcripts to be lazy loaded if necessary
      final int nTranscripts = transcripts.size();
      for (int t = 0; t < nTranscripts; ++t) {
        Transcript transcript = (Transcript) transcripts.get(t);
        r.addAll(transcript.getExternalRefs(true));
      }
    }

    // Remove any duplicate xrefs that have the same displayID.
    Map displayID2xref = new HashMap();
    for (int i = 0; i < r.size(); ++i) {
      ExternalRef xref = (ExternalRef) r.get(i);
      String displayID = xref.getDisplayID();
      displayID2xref.put(displayID, xref);
    }
    if (displayID2xref.size() != xrefs.size())
      r = new ArrayList(displayID2xref.values());

    return r;
  }

  public void setTranscriptsAndExons(List transcripts, List exons) {
    this.transcripts = transcripts;
    this.exons = exons;
    // set the gene references in the exons and transcripts to point to this
    // gene
    Iterator it = transcripts.iterator();
    while (it.hasNext()) {
      Transcript t = (Transcript) it.next();
      t.setGene(this);
      t.setGeneInternalID(getInternalID());
    }
    it = exons.iterator();
    while (it.hasNext()) {
      Exon e = (Exon) it.next();
      e.setGene(this);
      e.setGeneInternalID(getInternalID());
    }
  }

  /**
   * @return transcripts if available, otherwise empty list.
   */
  public List getTranscripts() {
    if (transcripts == null && driver != null)
      lazyLoadExonsAndTranscripts();
    if (transcripts == null)
      transcripts = new ArrayList();
    return transcripts;
  }

  /**
   * @return exons if available, otherwise empty list.
   */
  public List getExons() {
    if (exons == null && driver != null)
      lazyLoadExonsAndTranscripts();
    if (exons == null)
      exons = new ArrayList();
    return exons;
  }

  public void setBioType(String type) {
    this.type = type;
  }

  public String getBioType() {
    return type;
  }

  
  public String getType() {
    return getBioType();
  }
  
  public Object clone() throws CloneNotSupportedException {
    return null;
  }

  public Location getLocation() {
    if (location == null)
      location = TranscriptImpl.deriveLocationFromExons(getExons());
    return location;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("[");
    buf.append("{").append(super.toString()).append("}, ");
    buf.append("accessionID=").append(accessionID).append(", ");
    buf.append("type=").append(type).append(", ");
    buf.append("status=").append(status).append(", ");
    buf.append("description=").append(getDescription()).append(", ");
    buf.append("nTranscripts=").append(getTranscripts().size()).append(", ");
    buf.append("exons=").append(exons);
    buf.append("]");

    return buf.toString();
  }

  private void lazyLoadExonsAndTranscripts() {

    // Prevent constantly trying to lazy load if not available.
    if (exonAndTranscriptsLazyLoaded)
      return;
    exonAndTranscriptsLazyLoaded = true;

    logger.fine("lazy loading gene : " + internalID);

    try {
      // Load a new gene with same internalID
      GeneAdaptor ga = (GeneAdaptor) driver.getAdaptor("gene");
      Gene gene = (Gene) ga.fetch(new long[] { internalID }, true).get(0);

      // Copy that genes exons + transcripts
      exons = gene.getExons();
      transcripts = gene.getTranscripts();

      // Change those exon and transcript gene references to point to this
      // instance
      final int nExons = exons.size();
      for (int i = 0; i < nExons; ++i) {
        Exon e = (Exon) exons.get(i);
        e.setGene(this);
        e.setGeneInternalID(internalID);
      }
      final int nTranscripts = transcripts.size();
      for (int i = 0; i < nTranscripts; ++i) {
        Transcript t = (Transcript) transcripts.get(i);
        t.setGene(this);
        t.setGeneInternalID(internalID);
      }
    } catch (AdaptorException e) {
      logger.warning(e.getMessage());
      // Tidy up to avoid leaving gene in invalid state
      exons = null;
      transcripts = null;
    }

    logger.fine("gene loc = " + getLocation());

  }


  public String[] getInterproIDs() throws AdaptorException {

    // derive interpro ids from constituent translations

    Set ids = new HashSet();
    List ts = getTranscripts();
    for (int i = 0, n = ts.size(); i < n; i++) {
      Transcript t = (Transcript) ts.get(i);
      Translation tn = t.getTranslation();
      if (tn != null) {
        String[] interproIDs = tn.getInterproIDs();
        for (int j = 0; j < interproIDs.length; j++)
          ids.add(interproIDs[j]);
      }
    }

    return (String[]) ids.toArray(new String[ids.size()]);
  }

  /**
   * Updates the location.coordinate system for this gene and, if set, it's
   * transcripts and exons.
   * 
   * @see org.ensembl.datamodel.Locatable#setCoordinateSystem(org.ensembl.datamodel.CoordinateSystem,
   *      org.ensembl.driver.LocationConverter)
   */
  public void setCoordinateSystem(CoordinateSystem coordinateSystem,
      LocationConverter locationConverter) throws AdaptorException {

    super.setCoordinateSystem(coordinateSystem, locationConverter);

    if (exons != null)
      for (int i = 0; i < exons.size(); i++)
        ((Exon) exons.get(i)).setCoordinateSystem(coordinateSystem,
            locationConverter);

    if (transcripts != null)
      for (int j = 0; j < transcripts.size(); j++)
        ((Transcript) transcripts.get(j)).setCoordinateSystem(coordinateSystem,
            locationConverter);

  }

/**
 * @return Returns the idCreatedDate.
 */
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
}