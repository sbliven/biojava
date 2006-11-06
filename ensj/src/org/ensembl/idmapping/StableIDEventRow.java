/*
 * Copyright (C) 2004 EBI, GRL
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

package org.ensembl.idmapping;

/**
 * Represents a row in the stable_id_event_table.
 */
public class StableIDEventRow {

  private String oldStableID, newStableID;

  private int oldVersion, newVersion;

  private String type;

  private String mapping_type;

  private long mappingSessionID;
  
  private float score;

  /**
   * Create a new StableIDEventRow. Note either oldStableID or newStableID
   * may be null (but not both at the same time!)
   */
  public StableIDEventRow(String oldStableID, int oldVersion, String newStableID, int newVersion, String type,
      float score, String mapping_type, long mappingSessionID) {

    this.oldStableID = oldStableID;
    this.oldVersion = oldVersion;
    this.newStableID = newStableID;
    this.newVersion = newVersion;
    this.type = type;
    this.mappingSessionID = mappingSessionID;
    this.score = score;

    // mapping type can only be same or similarity
    if (!mapping_type.equals("same") && !mapping_type.equals("similarity")) {
      throw new RuntimeException("Illegal mapping type, needs to be \"same\" or \"similarity\" not " + mapping_type);
    }
    this.mapping_type = mapping_type;
  }

  /**
   * Original version without mapping type. Mapping type is same.
   * 
   * @param oldStableID
   * @param oldVersion
   * @param newStableID
   * @param newVersion
   * @param type
   * @param mappingSessionID
   */
  public StableIDEventRow(String oldStableID, int oldVersion, String newStableID, int newVersion, String type, float score, 
      long mappingSessionID) {
    this(oldStableID, oldVersion, newStableID, newVersion, type, score, "same", mappingSessionID);
  }

  /**
   * Copy constructor.
   */
  public StableIDEventRow(StableIDEventRow sidec) {

    this.oldStableID = sidec.getOldStableID();
    this.oldVersion = sidec.getOldVersion();
    this.newStableID = sidec.getNewStableID();
    this.newVersion = sidec.getNewVersion();
    this.type = sidec.getType();
    this.mappingSessionID = sidec.getMappingSessionID();
    this.mapping_type = sidec.mapping_type;
  }

  /**
   * @return Returns the newStableID.
   */
  public String getNewStableID() {

    return newStableID;
  }

  /**
   * @param newStableID
   *          The newStableID to set.
   */
  public void setNewStableID(String newStableID) {

    this.newStableID = newStableID;
  }

  /**
   * @return Returns the newVersion.
   */
  public int getNewVersion() {

    return newVersion;
  }

  /**
   * @param newVersion
   *          The newVersion to set.
   */
  public void setNewVersion(int newVersion) {

    this.newVersion = newVersion;
  }

  /**
   * @return Returns the oldStableID.
   */
  public String getOldStableID() {

    return oldStableID;
  }

  /**
   * @param oldStableID
   *          The oldStableID to set.
   */
  public void setOldStableID(String oldStableID) {

    this.oldStableID = oldStableID;
  }

  /**
   * @return Returns the oldVersion.
   */
  public int getOldVersion() {

    return oldVersion;
  }

  /**
   * @param oldVersion
   *          The oldVersion to set.
   */
  public void setOldVersion(int oldVersion) {

    this.oldVersion = oldVersion;
  }

  /**
   * @return Returns the type.
   */
  public String getType() {

    return type;
  }

  /**
   * @param type
   *          The type to set.
   */
  public void setType(String type) {

    this.type = type;
  }

  /**
   * @return Returns the currentMappingSessionID.
   */
  public long getMappingSessionID() {

    return mappingSessionID;
  }

  /**
   * @param currentMappingSessionID
   *          The currentMappingSessionID to set.
   */
  public void setMappingSessionID(long mappingSessionID) {

    this.mappingSessionID = mappingSessionID;
  }

  
  /**
   * Return the score between the source and target items.
   * @return score between the source and target items.
   */
  public float getScore() {
    return score;
  }

  /**
   * Set the score between the source and target items.
   * @param score score between the source and target items.
   */
  public void setScore(float score) {
    this.score = score;
  }

  public String toString() {

    return type + " " + oldStableID + ":" + oldVersion + " " + newStableID + ":" + newVersion + " " + mappingSessionID + " " + score;

  }


  public String getKey() {
    // ignore score 
    return oldStableID + "." + oldVersion + ":" + newStableID + "." + newVersion + ":" + type + ":" + mappingSessionID;

  }

  public int hashCode() {
    // ignore score 
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + (int) (mappingSessionID ^ (mappingSessionID >>> 32));
    result = PRIME * result + ((mapping_type == null) ? 0 : mapping_type.hashCode());
    result = PRIME * result + ((newStableID == null) ? 0 : newStableID.hashCode());
    result = PRIME * result + newVersion;
    result = PRIME * result + ((oldStableID == null) ? 0 : oldStableID.hashCode());
    result = PRIME * result + oldVersion;
    result = PRIME * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  public boolean equals(Object obj) {
    // ignore score 
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final StableIDEventRow other = (StableIDEventRow) obj;
    if (mappingSessionID != other.mappingSessionID)
      return false;
    if (mapping_type == null) {
      if (other.mapping_type != null)
        return false;
    } else if (!mapping_type.equals(other.mapping_type))
      return false;
    if (newStableID == null) {
      if (other.newStableID != null)
        return false;
    } else if (!newStableID.equals(other.newStableID))
      return false;
    if (newVersion != other.newVersion)
      return false;
    if (oldStableID == null) {
      if (other.oldStableID != null)
        return false;
    } else if (!oldStableID.equals(other.oldStableID))
      return false;
    if (oldVersion != other.oldVersion)
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }
  
  

}