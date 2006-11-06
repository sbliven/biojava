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

import java.util.List;

import org.ensembl.datamodel.impl.PersistentImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.variation.datamodel.Population;
import org.ensembl.variation.driver.VariationDriver;

/**
 * Implementation of a variation population.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 *  
 */
public class PopulationImpl extends PersistentImpl implements Population {

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



	private VariationDriver vdriver;

	private int size;

	private String name;

	private String description;

	private List subPopulations;

	private List superPopulations;

	public PopulationImpl(VariationDriver vdriver) {
		this.vdriver = vdriver;
	}

	/**
	 * @param internalID
	 *            internal ID.
	 */
	public PopulationImpl(long internalID) {
		this.internalID = internalID;
	}

	/**
	 * Lazy loads sub populations on demand.
	 * 
	 * @see org.ensembl.variation.datamodel.Population#getSubPopulations()
	 */
	public List getSubPopulations() throws AdaptorException {
		if (subPopulations == null)
			subPopulations = vdriver.getPopulationAdaptor()
					.fetchSubPopulations(this);
		return subPopulations;
	}

	/**
	 * Lazy loads super populations on demand.
	 * 
	 * @see org.ensembl.variation.datamodel.Population#getSuperPopulations()
	 */
	public List getSuperPopulations() throws AdaptorException {
		if (superPopulations == null)
			superPopulations = vdriver.getPopulationAdaptor()
					.fetchSuperPopulations(this);
		return superPopulations;
	}

	/**
	 * @see org.ensembl.variation.datamodel.Population#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @see org.ensembl.variation.datamodel.Population#getName()
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.ensembl.variation.datamodel.Population#getSize()
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @see org.ensembl.variation.datamodel.Population#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @see org.ensembl.variation.datamodel.Population#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @see org.ensembl.variation.datamodel.Population#setSize(int)
	 */
	public void setSize(int size) {
		this.size = size;
	}

}
