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

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.Analysis;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.PredictionTranscript;
import org.ensembl.datamodel.Sequence;
import org.ensembl.datamodel.Translation;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;

/**
 * PredictionTranscript implementation with lazy loading capabilities.
 */
public class PredictionTranscriptImpl extends BaseFeatureImpl implements PredictionTranscript {

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



  private static Logger logger = Logger.getLogger( PredictionTranscriptImpl.class.getName() );

  private List exons;
  private String displayName;
  private Location location;
  private Sequence sequence;
  private int length = -1;

  private Translation lnkTranslation;


  public PredictionTranscriptImpl( CoreDriver driver) {
    super( driver );
  }


  public PredictionTranscriptImpl() {
  }

  /**
   * @return exons.
   */
  public List getExons(){
    if(exons == null){
      try{
        exons = getDriver().getPredictionExonAdaptor().fetch(this);
      } catch (AdaptorException e) {
        logger.warning(e.getMessage());
        exons = null;
      }
    }
    return exons;
  }


  public void setExons(List exons){
      this.exons = exons;
      location = null;
      setExonCount(exons.size());
  }


  /**
   * @return combined length of all exons.
   */
  public int getLength() {
    return getLocation().getEnd();
  }

  public String translate() {
    return "<translate not implemented>";
  }

  /**
   * @return empty list 
   */
  public List getThreePrimeUTR() {
    return Collections.EMPTY_LIST;
  }

  
  /**
   * @return empty list  
   */
  public List getFivePrimeUTR() {
    return Collections.EMPTY_LIST;
  }
    

  /*
   * @return sequence if available, otherwise null.
   */
  public Sequence getSequence() {
    if ( sequence==null && exons!=null) {
      StringBuffer buf = new StringBuffer();
      final int nExons = exons.size();
      for( int i=0; i<nExons; ++i) {
        Exon exon = (Exon)exons.get(i);
        buf.append( exon.getSequence().getString() );
      }
      sequence = new SequenceImpl();
      sequence.setString( buf.toString() );
    }
    return sequence;
  }



  /**
   * Sets sequence on parent _translation_.
   */
  public void setSequence(Sequence sequence) {
    this.sequence = sequence;
  }


  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("[");
    buf.append( super.toString() ).append(", ");

    buf.append("length=").append(getLength()).append(", ");
    buf.append("nExons=")
      .append( ((exons==null) ? "0" : Integer.toString(exons.size())) )
      .append(", ");
    buf.append("analysis=").append( ((analysis==null) ? "UNSET" : analysis.getLogicalName()) ).append(", ");
    buf.append("exonCount=").append( exonCount );
    buf.append("]");

    return buf.toString();
  }

  private Analysis analysis;

  public void setAnalysis(Analysis analysis){ this.analysis = analysis; }

  private int exonCount;

  /**
   * Analysis which created this prediction.
   */
  public int getExonCount(){ return exonCount; }

  /**
   * Analysis which created this prediction.
   */
  public void setExonCount(int exonCount){ this.exonCount = exonCount; }
}
