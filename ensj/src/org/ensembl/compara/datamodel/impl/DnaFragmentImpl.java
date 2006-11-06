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
package org.ensembl.compara.datamodel.impl;

import org.ensembl.compara.datamodel.DnaFragment;
import org.ensembl.compara.datamodel.GenomeDB;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.impl.PersistentImpl;

/**
 * I am a part of the compara- analysis
**/
public class DnaFragmentImpl extends PersistentImpl implements DnaFragment{

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


  private String name;
  private int length;
  private String coordSystemName;
  private long genomeDbInternalId;
  private GenomeDB genomeDB;
  private Location location;

  public Location getLocation(){
    return location;
  }//end getLocation
  
  public void setLocation(Location newValue){
    location = newValue;
  }//end setLocation

  public String getName(){
    return name;
  }//end getName
  
  public void setName(String newValue){
    name = newValue;
  }//end setName

  public String getCoordSystemName(){
    return coordSystemName;
  }//end getCoordSystemName
  
  public void setCoordSystemName(String newValue){
    coordSystemName = newValue;
  }//end setCoordSystemName
  
  public long getGenomeDbInternalId(){
    return genomeDbInternalId;
  }//end getGenomeDbInternalId
  
  public void setGenomeDbInternalId(long id){
    genomeDbInternalId = id;
  }//end setGenomeDbInternalId
  
  public int getLength(){
    return length;
  }//end getLength
  
  public void setLength(int id){
    length = id;
  }//end setLength
  
  public GenomeDB getGenomeDB() {
    return genomeDB;
  }//end getGenomeDB
  
  public void setGenomeDB(GenomeDB genomeDB) {
    this.genomeDB = genomeDB;
  }//end setGenomeDB

  public String toString(){
    StringBuffer buffer = 
      (new StringBuffer())
        .append("DnaFragment: (")
        .append(getInternalID())
        .append(")[")
        .append(getCoordSystemName())
        .append(",")
        .append(getName())
        .append(",")
        .append(getLength())
        .append("]");
    
    return buffer.toString();
  }//end toString
}//end DnaFragmentImpl
