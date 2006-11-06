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

import org.ensembl.datamodel.FeaturePair;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Sequence;
import org.ensembl.driver.CoreDriver;

/**
 * All locatable classes have locations and have sequences.
 **/

public class BaseFeaturePairImpl extends BaseFeatureImpl implements FeaturePair {

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



  private String   hitDisplayName;
  private String   hitDescription;
  private String   hitAccession;
  private Location hitLocation;
  private String   cigarString;
  private double percentageIdentity;
  private double evalue;
  private double score;
  private Sequence hitSequence;

  public BaseFeaturePairImpl(long internalID) {
      super(internalID);
  }

  public BaseFeaturePairImpl(long internalID,Location location,Location hitLocation) {
      super(internalID, location);
      setHitLocation(hitLocation);
  }

  public BaseFeaturePairImpl() {
      super();
  }

  public BaseFeaturePairImpl( CoreDriver driver) {
    super( driver );
  }

  public String getHitDisplayName(){

      return hitDisplayName;
  }

  public void setHitDisplayName(String hitDisplayName){ 
      this.hitDisplayName = hitDisplayName; 
  }


  public String getHitDescription(){
      return hitDescription;
  }

  public void setHitLocation(Location loc) {
      this.hitLocation = loc;
  }

  public Location getHitLocation() {
    return hitLocation;
  }

  public void setHitDescription(String hitDescription){ 
    this.hitDescription = hitDescription; 
  }

  public double  getPercentageIdentity() {
    return percentageIdentity;
  }

  public void setPercentageIdentity( double percentageIdentity ) {
    this.percentageIdentity = percentageIdentity;
  }

  public double getEvalue() { return evalue; }

  public void setEvalue( double evalue ) { this.evalue = evalue; }

  public double getScore() { return score; }

  public void setScore( double score ) { this.score = score; }

  public String getHitAccession(){
      return hitAccession;
  }

  public void setHitAccession(String hitAccession){ 
      this.hitAccession = hitAccession; 
  }

  public Sequence  getHitSequence() {
    return hitSequence;
  }

  public void setHitSequence(Sequence hitSequence) { this.hitSequence = hitSequence; }

  public String getCigarString() { return cigarString; }
  public void setCigarString(String cigarString) { this.cigarString = cigarString; }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    
    buf.append("[");
    buf.append("{").append(super.toString()).append("}, ");
    buf.append(", hitDisplayName=").append( getHitDisplayName() );
    buf.append(", hitDescription=").append( getHitDescription() );
    buf.append(", hitAccesion=").append( getHitAccession() );
    buf.append(", hitLocation=").append( getHitLocation() );
    buf.append(", cigarString=").append( getCigarString() );
    buf.append(", percentageIdentity=").append( getPercentageIdentity());
    buf.append(", evalue=").append( getEvalue() );
    buf.append(", score=").append( getScore() );
    buf.append(", hitSequence=").append( hitSequence );
    buf.append("]");

    return buf.toString();
  }

}
