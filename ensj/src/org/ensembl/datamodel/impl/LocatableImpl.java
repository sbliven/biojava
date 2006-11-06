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

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Locatable;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Sequence;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.LocationConverter;

public class LocatableImpl
  extends PersistentImpl
  implements Cloneable, Locatable, Comparable {

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



  private static final Logger logger =
    Logger.getLogger(Locatable.class.getName());
  protected Location location;
  protected Sequence sequence;

  public LocatableImpl(long internalID) {
    this.internalID = internalID;
  }

  public LocatableImpl(long internalID, Location location) {
    this.internalID = internalID;
    this.location = location;
  }

  public LocatableImpl() {
  }

  public LocatableImpl(CoreDriver driver) {
    super(driver);
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public Object clone() throws CloneNotSupportedException {
    Locatable other = (Locatable) super.clone();
    return other;
  }

  /**
   * Returns sequence if set, or attempts to lazy load if driver and location
   * are set and sequence is not.
   * @return sequence if set or it can be lazy loaded, otherwise null.
   */
  public Sequence getSequence() {

    if (sequence == null && driver != null) {
      try {

        if (location == null)
          location = getLocation();

        if (location != null)
          setSequence(driver.getSequenceAdaptor().fetch(location));

      } catch (AdaptorException e) {
        logger.warning(e.getMessage());
      }
    }

    if (sequence != null && sequence.getString().length() == 0) {
      new Exception("Returning empty sequence string for : " + toString())
        .printStackTrace();
    }

    return sequence;
  }

  public void setSequence(Sequence sequence) {
    this.sequence = sequence;

    if (sequence != null && sequence.getString().length() == 0) {
      new Exception("Setting empty sequence string for : " + toString())
        .printStackTrace();
    }

  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("[");
    buf.append("internalID=").append(internalID).append(", ");
    buf.append("location=").append(location);
    buf.append("]");

    return buf.toString();
  }

  /**
   * Implementation of Comparable interface.
   * Compares this feature with another based upon the start of their Locations.
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object other) {

    final Location otherLoc = ((Locatable) other).getLocation();
    if (location == otherLoc)
      return 0;
    else if (location == null)
      return -1;
    else
      return location.compareTo(otherLoc);

  }

  /**
   * @see org.ensembl.datamodel.Locatable#setCoordinateSystem(org.ensembl.datamodel.CoordinateSystem, org.ensembl.driver.LocationConverter)
   */
  public void setCoordinateSystem(CoordinateSystem coordinateSystem, LocationConverter locationConverter) throws AdaptorException {
    if (location!=null && location.getCoordinateSystem()!=coordinateSystem) 
      location = locationConverter.convert(location, coordinateSystem);
  }

}
