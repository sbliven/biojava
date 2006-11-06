/*
    Copyright (C) 2001 EBI, GRL

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
package org.ensembl.variation.datamodel.impl;

import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.RuntimeAdaptorException;
import org.ensembl.variation.datamodel.Allele;
import org.ensembl.variation.datamodel.Population;
import org.ensembl.variation.driver.VariationDriver;

/**
 * Allele type implementation.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class AlleleImpl extends PersistentImpl implements Allele {

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



  private Population population;

  private double frequency;

  private String alleleString;

  private VariationDriver vdriver;



  private long populationID;

  public AlleleImpl(VariationDriver vdriver) {
    this.vdriver = vdriver;
  }



  /**
   * @see org.ensembl.variation.datamodel.Allele#getAlleleString()
   */
  public String getAlleleString() {
    return alleleString;
  }

  /**
   * @see org.ensembl.variation.datamodel.Allele#setAlleleString(java.lang.String)
   */
  public void setAlleleString(String alleleString) {
    this.alleleString = alleleString;
  }

  /**
   * @see org.ensembl.variation.datamodel.Allele#getFrequency()
   */
  public double getFrequency() {
    return frequency;
  }

  /**
   * @see org.ensembl.variation.datamodel.Allele#setFrequency(double)
   */
  public void setFrequency(double frequency) {
    this.frequency = frequency;
    
  }

  /**
   * @see org.ensembl.variation.datamodel.Allele#getPopulation()
   */
  public Population getPopulation() {
    if (population==null && populationID>0 && vdriver!=null)
      try {
        population = vdriver.getPopulationAdaptor().fetch(populationID);
      } catch (AdaptorException e) {
        throw new RuntimeAdaptorException(e);
      }
    return population;
  }

  /**
   * @see org.ensembl.variation.datamodel.Allele#setPopulation(org.ensembl.variation.datamodel.Population)
   */
  public void setPopulation(Population population) {
    this.population = population;
    this.populationID = population.getInternalID();
  }



  /**
   * @see org.ensembl.variation.datamodel.Allele#setPopulationID(long)
   */
  public void setPopulationID(long populationID) {
    this.populationID = populationID;
  }



  /**
   * @see org.ensembl.variation.datamodel.Allele#getPopulationID()
   */
  public long getPopulationID() {
    return populationID;
  }

  

}
