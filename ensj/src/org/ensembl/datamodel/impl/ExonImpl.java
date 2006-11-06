/*
 * Copyright (C) 2003 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ensembl.datamodel.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.ExonAdaptor;
import org.ensembl.driver.SupportingFeatureAdaptor;

public class ExonImpl extends BaseFeatureImpl implements Exon {

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



    public static Exon[] toArray(List exons) {

        Exon[] array = new Exon[exons.size()];
        exons.toArray(array);
        return array;
    }

    private static final Logger logger = Logger.getLogger(ExonImpl.class.getName());

    public ExonImpl(long internalID, Location location) {

        super(internalID, location);
    }

    public ExonImpl() {

        super();
    }

    public ExonImpl(CoreDriver driver) {

        super(driver);
    }

    public String getAccessionID() {

        if (accessionID == null && driver != null) {
            try {
                ((ExonAdaptor) driver.getAdaptor("exon")).fetchAccessionID(this);
            } catch (AdaptorException e) {
                logger.warning(e.getMessage());
            }
        }
        return accessionID;
    }

    public List getSupportingFeatures() {

        if (supportingFeatures == null && driver != null) {
            try {
                supportingFeatures = ((SupportingFeatureAdaptor) driver.getAdaptor("supporting_feature")).fetch(this);
            } catch (AdaptorException e) {
                logger.warning(e.getMessage());
            }
        }
        return supportingFeatures;
    }

    public void setAccessionID(String accessionID) {

        this.accessionID = accessionID;
    }

    public double getConfidence() {

        return confidence;
    }

    public void setConfidence(double confidence) {

        this.confidence = confidence;
    }

    public Gene getGene() {

        // lazy load if necessary.
        if (gene == null) getTranscripts();
        return gene;
    }

    public void setGene(Gene gene) {

        this.gene = gene;
        this.geneInternalID = gene.getInternalID();
    }

    public List getTranscripts() {

        if (transcripts == null && driver != null) {
            try {
                driver.getExonAdaptor().fetchComplete(this);
            } catch (AdaptorException e) {
                throw new RuntimeException("Failed to lazy load contents of exon: " + this, e);
            }
        }

        return transcripts;
    }

    public void setTranscripts(List transcripts) {

        this.transcripts = transcripts;
        int nTranscripts = 0;

        if (transcriptInternalIDs == null || transcriptInternalIDs.length != nTranscripts) {
            transcriptInternalIDs = new long[transcripts.size()];
            nTranscripts = transcriptInternalIDs.length;
        } //end if

        for (int i = 0; i < nTranscripts; ++i) {
            transcriptInternalIDs[i] = ((Transcript) transcripts.get(i)).getInternalID();
        }
    }

    public void addTranscript(Transcript t) {

        if (transcripts == null) transcripts = new ArrayList();
        transcripts.add(t);
        setTranscripts(transcripts);
    }

    public int getExonID() {

        return exonID;
    }

    public Object clone() {

        return null;
    }

    public long getGeneInternalID() {

        if (gene != null) gene.setInternalID(geneInternalID);
        return geneInternalID;
    }

    public void setGeneInternalID(long geneInternalID) {

        if (gene != null) gene.setInternalID(geneInternalID);
        this.geneInternalID = geneInternalID;
    }

    public long[] getTranscriptInternalIDs() {

        // If available dynamically load these values, must do this because the
        // internalIDs could be dynamically changed.
        // XXX IS this right???
        if (transcripts == null) {
            transcripts = getTranscripts();
        }
        final int nTranscripts = transcripts.size();
        transcriptInternalIDs = new long[nTranscripts];
        for (int t = 0; t < nTranscripts; ++t) {
            transcriptInternalIDs[t] = ((Transcript) transcripts.get(t)).getInternalID();
        }

        return transcriptInternalIDs;
    }

    /**
     * @throws IllegalArgumentException if phase not -1,0,1 or2.
     */
    public void setPhase(int phase) {

        if (phase < -1 || phase > 2)
                throw new IllegalArgumentException("Phase must be -1,0, 1 or 2. " + "Attempt to set it to " + phase);
        this.phase = phase;
    }

    public int getPhase() {

        return phase;
    }

    /**
     * @throws IllegalArgumentException if endPhase not -1,0,1 or2.
     */
    public void setEndPhase(int endPhase) {

        if (endPhase < -1 || endPhase > 2)
                throw new IllegalArgumentException("EndPhase must be -1,0, 1 or 2. " + "Attempt to set it to " + phase);
        this.endPhase = endPhase;
    }

    public int getEndPhase() {

        return endPhase;
    }

    public String toString() {

        StringBuffer buf = new StringBuffer();
        buf.append("[");
        buf.append("{").append(super.toString()).append("}, ");
        buf.append("accessionID=").append(accessionID).append(", ");
        // Avoid circual toString() calls by just printing gene.internalID rather
        // than the gene itself.
        String geneStr = "UNSET";
        if (gene != null) geneStr = Long.toString(gene.getInternalID());
        buf.append("gene=").append(geneStr).append(", ");
        buf.append("geneInternalID=").append(geneInternalID).append(", ");
        // Avoid circual toString() calls by just printing number of transcripts
        // rather than the transcripts themselves.
        String transcriptsStr = null;
        if (transcripts != null) transcriptsStr = Integer.toString(transcripts.size());
        buf.append("num transcripts=").append(transcriptsStr).append(", ");
        buf.append("phase=").append(phase).append(", ");
        buf.append("endPhase=").append(endPhase).append(", ");
        buf.append("]");

        return buf.toString();
    }

    public void setVersion(int version) {

        this.version = version;
    }

    /**
     * Lazy loads value if necessary.
     * 
     * @return version; 0 normally indicates that the version is not set in datasource. <0 means that the version was not set and
     *         could not be lazy loaded.s
     */
    public int getVersion() {

        if (version < 0 && driver != null) {
            try {
                driver.getExonAdaptor().fetchVersion(this);
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


    private double confidence;

    private String accessionID;
    private Date idModifiedDate;
    private Date idCreatedDate;
    
    private Gene gene;

    private List transcripts = null;

    private int exonID;

    /** default value of -1 means that the version is unset. */
    private int version = -1;

    private long geneInternalID;

    private long[] transcriptInternalIDs;

    private int phase = Integer.MIN_VALUE; // deafult 'unset' value

    private int endPhase = Integer.MIN_VALUE; // deafult 'unset' value

    private List supportingFeatures;
}