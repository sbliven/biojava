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
package org.ensembl.datamodel;

import java.io.Serializable;

/**
 * A query defines what to retrieve and how much to
 * retrieve. 
 *
 * What to retrieve means filter conditions and how much means whether
 * children should be included. e.g. genes with or without exons.
 * @deprecated since version 27.0
 */
public class Query implements Cloneable, Serializable {

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



  /**
   * Lazy loading is off by default. 
   */
  public Query() {
  }


  public Query(long internalID) {
    this.internalID = internalID;
  }

  public Query(long[] internalIDs) {
    this.internalIDs = internalIDs;
  }

  public Query(long internalID, boolean loadChildren) {
    this.internalID = internalID;
    this.loadChildren = loadChildren;
  }

  
  public Query(long internalID, 
               boolean loadChildren,
               Location location) {
    this.internalID = internalID;
    this.loadChildren = loadChildren;
    this.location = location;
  }


  public Query(Location location) {
    this.location = location;
  }


  public Query(Location location, 
               boolean loadChildren) {
    this.location = location;
    this.loadChildren = loadChildren;
  }

  public Query(String accessionID) {
    this.accessionID = accessionID;
  }

  public Query(String accessionID, 
               boolean loadChildren) {
    this.accessionID = accessionID;
    this.loadChildren = loadChildren;
  }


  public Query(String accessionID, 
               boolean loadChildren,
               Location location) {
    this.accessionID = accessionID;
    this.loadChildren = loadChildren;
    this.location = location;
  }


  /**
   * Performs a deep copy.
   */
  public Object clone() throws CloneNotSupportedException {
    Query q = (Query)super.clone();
    Location l = q.getLocation();
    if ( l!=null ) q.setLocation( (Location)l.clone() );
    return q;
  }

  
  public Location getLocation(){
    return location;
  }

  public void setLocation(Location location){
    this.location = location;
  }

  public long getInternalID(){
    return internalID;
  }

  public void setInternalID(long internalID){
    this.internalID = internalID;
  }

  public String getAccessionID(){
    return accessionID;
  }

  public void setAccessionID(String accessionID){
    this.accessionID = accessionID;
  }

  public String getType(){
    return type;
  }

  public void setType(String type){
    this.type = type;
  }

  public boolean getIncludeSequence(){
    return includeSequence;
  }

  public void setIncludeSequence(boolean includeSequence){
    this.includeSequence = includeSequence;
  }

  public boolean getIncludeChildren(){
    return loadChildren;
  }

  public void setIncludeChildren(boolean loadChildren){
    this.loadChildren = loadChildren;
  }

  public boolean isInternalIDSet() {
    return internalID>0;
  }


  public boolean isLocationSet() {
    return location!=null;
  }

  
  public boolean isAccessionIDSet() {
    return accessionID!=null;
  }


  /**
   * Resets all values to initial values. 
   * 
   * Values are set to -1, null, false depending on type.  
   */
  public void clear() {
    location = null;
    internalID = -1;
    accessionID = null;
    type = null;
    includeSequence = false;
    loadChildren = false;
  }

  private Location location = null;
  private long internalID = -1;
  private long[] internalIDs = null;
  private String accessionID = null;
  private String type = null;
  private boolean includeSequence = false;
  private boolean loadChildren = false;
  
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("[");
    buf.append("location=").append(location).append(", ");
    buf.append("internalID=").append(internalID).append(", ");
    buf.append("accessionID=").append(accessionID).append(", ");
    buf.append("type=").append(type).append(", ");
    buf.append("includeSequence=").append(includeSequence).append(", ");
    buf.append("loadChildren=").append(loadChildren).append(", ");
    buf.append("]");

    return buf.toString();
  }

 public long[] getInternalIDs() {
    return internalIDs;
  }

  public void setInternalIDs(long[] ls) {
    internalIDs = ls;
  }

  public boolean isInernalIDsSet() {
    return internalIDs!=null;
  }

}

