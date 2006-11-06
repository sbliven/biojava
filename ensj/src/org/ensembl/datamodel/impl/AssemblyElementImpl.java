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

import org.ensembl.datamodel.AssemblyElement;

/**
 * 
 * 
 * 
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 * @version $Revision$
 */

public class AssemblyElementImpl implements AssemblyElement {

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


  public AssemblyElementImpl () {
  }

  public void setAllFields(String chromosome,
			  int chromosomeStart,
			  int chromosomeEnd,
			  long cloneFragmentInternalID,
			  int cloneFragmentStart,
			  int cloneFragmentEnd,
                          int cloneFragmentOri,
                          String type){
    this.chromosome = chromosome;
    this.chromosomeStart = chromosomeStart;
    this.chromosomeEnd = chromosomeEnd;
    this.cloneFragmentInternalID = cloneFragmentInternalID;
    this.cloneFragmentStart = cloneFragmentStart;
    this.cloneFragmentEnd = cloneFragmentEnd;
    this.cloneFragmentOri = cloneFragmentOri;
    this.type = type;
  }

  public void setChromosomeName(String chrName) {
    chromosome = chrName;
  }

  public void setChromosomeStart(int chrStart) {
    chromosomeStart = chrStart;
  }

  public void setChromosomeEnd(int chrEnd) {
    chromosomeEnd = chrEnd;
  }

  public void setCloneFragmentInternalID(long id) {
    cloneFragmentInternalID = id;
  }

  public void setCloneFragmentStart(int cloneFragStart) {
    cloneFragmentStart = cloneFragStart;
  }

  public void setCloneFragmentEnd(int cloneFragEnd) {
    cloneFragmentEnd = cloneFragEnd;
  }

  public void setCloneFragmentOri(int cloneFragOri) {
    cloneFragmentOri = cloneFragOri;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getChromosomeName() {
    return chromosome;
  }

  public int getChromosomeStart() {
    return chromosomeStart;
  }

  public int getChromosomeEnd() {
    return chromosomeEnd;
  }

  public long getCloneFragmentInternalID() {
    return cloneFragmentInternalID;
  }

  public int getCloneFragmentStart() {
    return cloneFragmentStart;
  }

  public int getCloneFragmentEnd() {
    return cloneFragmentEnd;
  }

  public int getCloneFragmentOri() {
    return cloneFragmentOri;
  }

  public String getType() {
    return type;
  }

  private String type;

  private String chromosome;
  private int chromosomeStart;
  private int chromosomeEnd;

  private long cloneFragmentInternalID;
  private int cloneFragmentStart;
  private int cloneFragmentEnd;
  private int cloneFragmentOri;
    
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("[");
    buf.append("chromosome=").append(chromosome).append(", ");
    buf.append("chromosomeStart=").append(chromosomeStart).append(", ");
    buf.append("chromosomeEnd=").append(chromosomeEnd).append(", ");
    buf.append("cloneFragmentInternalID=").append(cloneFragmentInternalID).append(", ");
    buf.append("cloneFragmentStart=").append(cloneFragmentStart).append(", ");
    buf.append("cloneFragmentEnd=").append(cloneFragmentEnd);
    buf.append("cloneFragmentOri=").append(cloneFragmentOri).append(", ");
    buf.append("type=").append(type);
    buf.append("]");

    return buf.toString();
  }

}// AssemblyElementImpl
