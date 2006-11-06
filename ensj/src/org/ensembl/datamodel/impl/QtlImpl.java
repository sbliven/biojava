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

import java.util.List;

import org.ensembl.datamodel.Marker;
import org.ensembl.datamodel.Qtl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;

/**
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class QtlImpl extends PersistentImpl implements Qtl,Comparable {

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




  private List synoyms;
  private String trait;
  private float lodScore;
  private Marker peakMarker;
  private Marker flankMarker1;
  private Marker flankMarker2;
  private List qtlFeatures;
  
  public QtlImpl(CoreDriver driver) {
    super(driver);
  }

  public QtlImpl() {
    super();
  }


  




  public Marker getFlankMarker1() {
    return flankMarker1;
  }




  public Marker getFlankMarker2() {
    return flankMarker2;
  }




  public float getLodScore() {
    return lodScore;
  }




  public Marker getPeakMarker() {
    return peakMarker;
  }




  public List getSynoyms() {
    return synoyms;
  }




  public String getTrait() {
    return trait;
  }




  public void setFlankMarker1(Marker i) {
    flankMarker1 = i;
  }




  public void setFlankMarker2(Marker i) {
    flankMarker2 = i;
  }




  public void setLodScore(float f) {
    lodScore = f;
  }




  public void setPeakMarker(Marker i) {
    peakMarker = i;
  }




  public void setSynoyms(List list) {
    synoyms = list;
  }




  public void setTrait(String string) {
    trait = string;
  }

  /**
   * Effectively does nothing but we need it because instances of this class are
   * sorted using Collections.sort() and that method requires this one to be implemented. 
   * @return 0
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object o) {
    return 0;
  }

  /**
   * Returns Qtl Features, attempts to lazy load them if necessary.
   * @return zero or more QtlFeatures, or null if none available and driver not set.
   */
  public List getQtlFeatures() {
    if ( qtlFeatures==null && driver!=null) 
      try {
        qtlFeatures = driver.getQtlFeatureAdaptor().fetch(this);
      } catch (AdaptorException e) {
        throw new RuntimeException(e);
      } 
    return qtlFeatures;
  }




  public void setQtlFeatures(List list) {
    qtlFeatures = list;
  }

}
