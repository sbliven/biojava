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
package org.ensembl.variation.datamodel;

import org.ensembl.datamodel.Persistent;

/**
 * A single allele of a nucleotide variation.
 *
 * In addition to the nucleotide(s) (or absence of) that representthe allele
 * it's frequency and population information may also be present.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public interface Allele extends Persistent {

  /**
   * The allele string is a string of nucleotide sequence, or a '-' representing the
   * absence of sequence (deletion).
   * @return the allele string or '-'.
   */
  String getAlleleString();

  /**
   * Sets the allele string.
   * @param alleleString allele string or '-'.
   * @see #getAlleleString()
   */

  void setAlleleString(String alleleString);
  
  /**
   * The frequency of the occurance of the allele. If the population
   * attribute is set then this is the frequency of the allele within that
   * population.
   * @return frequency, 0 if unknown.
   */
  double getFrequency();

  /**
   * Set the frequency.
   * @param frequency allele's frequency.
   * @see #getFrequency()
   */
  void setFrequency(double frequency);
  
  /**
   * The population where this allele appears. Can be null if
   * not associated with a population.
   * @return the allele's population or null.
   */
  Population getPopulation();

  /**
   * Set the population where the allele appears.
   * @param population population where the allele appears. Can be null if
   * not associated with a population.
   * @see #getPopulation()
   */
  void setPopulation(Population population);

  /**
   * Set the internalID of the population this allele belongs to.
   * @param populationID internalID of the population this allele belongs to.
   */
  void setPopulationID(long populationID);
  
  /**
   * Get the internalID of the population this allele belongs to.
   * @return internalID of the population this allele belongs to.
   */
  long getPopulationID();
}
