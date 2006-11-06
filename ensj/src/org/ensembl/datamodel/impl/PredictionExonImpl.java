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


import java.util.logging.Logger;

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.PredictionExon;
import org.ensembl.datamodel.PredictionTranscript;
import org.ensembl.driver.CoreDriver;


public class PredictionExonImpl extends BaseFeatureImpl implements PredictionExon {

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



  private static final Logger logger = Logger.getLogger(PredictionExonImpl.class.getName());

  public PredictionExonImpl(long internalID, Location location) {
    super(internalID, location);
  }

  public PredictionExonImpl( CoreDriver driver) {
    super( driver );
  }

  public PredictionExonImpl() {
    super();
  }

  public PredictionTranscript getTranscript() {
    return this.transcript;
  }

  public void setTranscript(PredictionTranscript transcript){
    this.transcript = transcript;
  }

  public void setStartPhase(int startPhase) {
    this.startPhase = startPhase;
  }

  public int getStartPhase() {
    return startPhase;
  }

  public void setPvalue(double pvalue) { this.pvalue = pvalue; }

  private double pvalue;

  public double getPvalue() { return pvalue; }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("[");
    buf.append("{").append(super.toString()).append("}, ");
    String transcriptStr = null;
		if (transcript!=null) transcriptStr = Long.toString(transcript.getInternalID());
    buf.append("phase=").append(startPhase).append(", ");
    buf.append("endPhase=").append(startPhase).append(", ");
    buf.append("]");

    return buf.toString();
  }

  private int rank;

  public int getRank(){ return rank; }

  public void setRank(int rank){ this.rank = rank; }

  private double score;

  public double getScore(){ return score; }

  public void setScore(double score){ this.score = score; }

  private double eValue;

  private PredictionTranscript transcript;

  private int exonID;
  private int startPhase;
}
