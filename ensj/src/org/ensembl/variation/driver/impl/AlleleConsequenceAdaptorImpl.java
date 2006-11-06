/*
 Copyright (C) 2005 EBI, GRL

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

package org.ensembl.variation.driver.impl;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import org.ensembl.datamodel.TranscriptAlleleHelper;
import org.ensembl.driver.AdaptorException;
import org.ensembl.variation.datamodel.AlleleConsequenceAdaptor;
import org.ensembl.variation.driver.VariationDriver;

/**
 * The point of this class is....
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class AlleleConsequenceAdaptorImpl implements AlleleConsequenceAdaptor {

  private static final Logger logger = Logger
      .getLogger(AlleleConsequenceAdaptorImpl.class.getName());

  private VariationDriver vdriver;

  /**
   * @param vdriver
   *          parent driver
   */

  public AlleleConsequenceAdaptorImpl(VariationDriver vdriver) {
    this.vdriver = vdriver;
  }

  public String getType() {
    return TYPE;
  }

  /**
   * TODO move to Location and make it list capable.
   * 
   * @param src
   *          source location
   * @param tgt
   *          target location
   * @return part of src location that appears after tgt, or null if there is no
   *         such part.
   */
  private Location after(Location src, Location tgt) {
    Location r = null;
    if (src.getEnd() > tgt.getEnd()) {
      r = src.copy();
      r.setStart(tgt.getEnd() + 1);
    }
    return r;
  }

  /**
   * Creates the AlleleTranscriptConsequences that result from applying the
   * AlleleFeature to the transcript.
   * 
   * An AlleleFeature may cause zero, one or more consequences on the transcript
   * depending on whether and where it hits the transcript.
   * 
   * @param transcript
   *          transcript of interest.
   * @param alleleFeatures
   *          zero or more AlleleFeatures to apply to the transcript.
   * @return list of zero or more AlleleTranscriptConsequences representing the
   *         consequences of the alleles on the transcript.
   * @throws AdaptorException
   */
  public List fetch(Transcript transcript, List alleleFeatures) throws AdaptorException {

    List r = createConsequences(new TranscriptAlleleHelper(transcript),
        alleleFeatures);
    Collections.sort(r);
    return r;

  }

  /**
   * Return the AlleleConsequences for the transcript.
   * 
   * A transcript may have zero or more AlleleConsequences.
   * 
   * All AlleleFeatures that overlap the transcript or it's flanking regions
   * (5000 bases up and downstream) are used.
   * 
   * @param transcript
   *          transcript of interest.
   * @param vdriver
   *          variation driver from which to fetch AlleleFeatures.
   * @return list of zero or more AlleleConsequences representing the
   *         consequences of relevant alleles on the transcript.
   * @throws AdaptorException
   */
  public List fetch(Transcript transcript) throws AdaptorException {

    final int flank = 5000;
    Location loc = transcript.getLocation().transform(-flank, flank);
    List alleleFeatures = vdriver.getAlleleFeatureAdaptor().fetch(loc);
    List r = fetch(transcript, alleleFeatures);
    //    System.out.println("IN = " + alleleFeatures.size()
    //        + " alleleFeatures\tOUT = " + r.size() + " alleleConsequences");
    return r;
  }

  /**
   * TODO move to Location and make it list capable.
   * 
   * @param src
   *          source location
   * @param tgt
   *          target location
   * @return part of src location that appears before tgt, or null if there is
   *         no such part.
   */
  private Location before(Location src, Location tgt) {
    Location r = null;
    if (src.getStart() < tgt.getStart()) {
      r = src.copy();
      r.setEnd(tgt.getStart() - 1);
    }
    return r;
  }

  /**
   * 
   * @param transcript
   * @param alleleLocs
   *          list of allele locs. Each loc should be a single node location ,
   *          not a location list.
   * @return
   * @throws AdaptorException
   */
  private List createConsequences(final TranscriptAlleleHelper transcriptWrapper,
      List alleleFeatures) throws AdaptorException {
        return transcriptWrapper.toAlleleConsequences(alleleFeatures);
      }

  /**
   * TODO move to Location and make it list capable.
   * 
   * @param src
   *          source location
   * @param tgt
   *          target location
   * @return part of src location that overlaps tgt, or null if there is no such
   *         part.
   */
  private Location overlaps(Location src, Location tgt) {
    Location r = null;
    if (src.getStart() < tgt.getEnd() && src.getEnd() > tgt.getStart()) {
      r = src.copy();
      r.setStart(Math.max(src.getStart(), tgt.getStart()));
      r.setEnd(Math.min(src.getEnd(), tgt.getEnd()));
    }
    return r;
  }

  /**
   * Does nothing.
   * 
   * @see org.ensembl.driver.Adaptor#closeAllConnections()
   */
  public void closeAllConnections() throws AdaptorException {
  }

  /**
   * Does nothing.
   * 
   * @see org.ensembl.driver.Adaptor#clearCache()
   */
  public void clearCache() throws AdaptorException {
  }

}