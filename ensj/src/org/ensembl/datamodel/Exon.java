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

/**
 * Exon. 
 */
public interface Exon extends Accessioned,  Feature  {
  public double getConfidence();


  public void setConfidence(double confidence);


  public Gene getGene();

  /**
   * Sets gene and geneInternalID.
   */
  public void setGene(Gene gene);


  public List getTranscripts();


  /**
   * Sets transcripts and transcriptInternalIDs.
   */
  public void setTranscripts(List transcripts);

	public void addTranscript(Transcript t);
	
  /**
   * Returns gene internal id. This is the same
   * as gene.internalID if gene is available.
   *
   * @return internalID of the gene.
   */
  public long getGeneInternalID();

  /**
   * Sets gene internal id, also sets the
   * gene.internalID if gene is available.
   */
  public void setGeneInternalID(long geneInternalID);


  public long[] getTranscriptInternalIDs();

  public void setPhase(int phase);

  /**
	 * Supporting features used to build this exon.
	 * 
	 * Supporting features are a mixture of DnaDnaAlignFeatures
	 * and DnaProteinAlignFeatures.
	 * @return zero or more supporting features.
	 * @see DnaDnaAlignFeature
	 * @see DnaProteinAlignment
	 */
	List getSupportingFeatures();

  public int getPhase();

  public void setEndPhase(int phase);

  public int getEndPhase();

  public void setLocation(Location location);

  public void setSequence(Sequence sequence) ;
}
