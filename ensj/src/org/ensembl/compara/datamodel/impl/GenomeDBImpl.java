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
import org.ensembl.compara.datamodel.GenomeDB;
import org.ensembl.datamodel.impl.PersistentImpl;

/**
 * I represent a single species which is part of the compara- analyses.
**/
public class GenomeDBImpl extends PersistentImpl implements GenomeDB{

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


  String name;
  String assembly;
  int taxonId;
  boolean defaultAssembly;
  String geneBuild;
  String locator;
  
  public String getAssembly() {
    return assembly;
  }//get getAssembly
  
  public void setAssembly(String newValue) {
    assembly = newValue;
  }//end getAssembly

  public String getName() {
    return name;
  }//end getName
  
  public void setName(String name) {
    this.name = name;
  }//end setName
  
  public int getTaxonId() {
    return taxonId;
  }//end getTaxonId
  
  public void setTaxonId(int taxonId){
    this.taxonId = taxonId;
  }//end setTaxonId
  
  public boolean isDefaultAssembly(){
    return defaultAssembly;
  }
  
  public void setDefaultAssembly(boolean newValue){
    this.defaultAssembly = newValue;
  }
  
  public String getGeneBuild(){
    return geneBuild;
  }
  
  public void setGeneBuild(String newValue){
    geneBuild = newValue;
  }
  
  public String getLocator(){
    return locator;
  }
  
  public void setLocator(String newValue){
    locator = newValue;
  }
  
  public String toString(){
    return 
      (new StringBuffer())
        .append("GenomeDB (")
        .append(getInternalID())
        .append(")[")
        .append("Name: ")
        .append(getName())
        .append(", Assembly: ")
        .append(getAssembly())
        .append(", TaxonId: ")
        .append(getTaxonId())
        .append(", Genebuild: ")
        .append(getGeneBuild())
        .append(", Locator: ")
        .append(getLocator())
        .append(", Default Assembly? ")
        .append(isDefaultAssembly())
        .append("]")
        .toString();
  }//end toString
}//end GenomeDBImpl