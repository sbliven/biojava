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

package org.ensembl.variation.datamodel.impl;

import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.impl.BaseFeatureImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.RuntimeAdaptorException;
import org.ensembl.variation.datamodel.LDFeature;
import org.ensembl.variation.datamodel.Population;
import org.ensembl.variation.datamodel.VariationFeature;
import org.ensembl.variation.driver.VariationDriver;

/**
 * LD relationship between two variation features.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class LDFeatureImpl extends BaseFeatureImpl implements LDFeature {

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



	private VariationFeature variationFeature1;
	private long variationFeature1ID;
	private VariationFeature variationFeature2;
	private long variationFeature2ID;
	private Population population;
	private long populationID;
	private double rSquare;
	private double dPrime;
	private int sampleCount;
	private VariationDriver vdriver;

	/**
	 * Creates a fully populated LDFeature.
	 * 
	 * @param vDriver parent driver
	 * @param location location of the LDFeature
	 * @param variationFeature1ID internalID of the first VariationFeature associated with the LDFeature
	 * @param variationFeature2ID internalID of the second VariationFeature associated with the LDFeature
	 * @param populationID internalID of the poulation this LDFeature belongs to
	 * @param rSquare rSquare value
	 * @param dPrime dPrime value
	 * @param sampleCount sample count
	 */
	public LDFeatureImpl(VariationDriver vDriver, Location location,
			long variationFeature1ID, long variationFeature2ID,
			long populationID, double rSquare, double dPrime,
			int sampleCount) {
		super(vDriver.getCoreDriver());
    this.vdriver = vDriver;
		this.location = location;
		this.variationFeature1ID = variationFeature1ID;
		this.variationFeature2ID = variationFeature2ID;
		this.populationID = populationID;
		this.rSquare = rSquare;
		this.dPrime = dPrime;
		this.sampleCount = sampleCount;
	}
	/**
	 * @see org.ensembl.variation.datamodel.LDFeature#getVariationFeature1()
	 */
	public VariationFeature getVariationFeature1() {
		if (variationFeature1==null && variationFeature1ID>0 && vdriver!=null)
			try {
				variationFeature1 = vdriver.getVariationFeatureAdaptor().fetch(variationFeature1ID);
			} catch (AdaptorException e) {
				throw new RuntimeAdaptorException("Failed to lazy load variationFeature1: " + variationFeature1ID,e);
			}		
		return variationFeature1;
	}

	/**
	 * @see org.ensembl.variation.datamodel.LDFeature#getVariationFeature2()
	 */
	public VariationFeature getVariationFeature2() {
		if (variationFeature2==null && variationFeature2ID>0 && vdriver!=null)
			try {
				variationFeature2 = vdriver.getVariationFeatureAdaptor().fetch(variationFeature2ID);
			} catch (AdaptorException e) {
				throw new RuntimeAdaptorException("Failed to lazy load variationFeature2: " + variationFeature2ID,e);
			}return variationFeature2;
	}

	/**
	 * @see org.ensembl.variation.datamodel.LDFeature#getPopulation()
	 */
	public Population getPopulation() {
		if (	population==null && populationID>0 && vdriver!=null)
			try {
				population = vdriver.getPopulationAdaptor().fetch(populationID);
			} catch (AdaptorException e) {
				throw new RuntimeAdaptorException("Failed to lazy load population: " + populationID,e);
			}
		return population;
	}

	/**
	 * @see org.ensembl.variation.datamodel.LDFeature#getRSquare()
	 */
	public double getRSquare() {
		return rSquare;
	}

	/**
	 * @see org.ensembl.variation.datamodel.LDFeature#getDPrime()
	 */
	public double getDPrime() {
		return dPrime;
	}

	/* (non-Javadoc)
	 * @see org.ensembl.variation.datamodel.LDFeature#getSampleCount()
	 */
	public int getSampleCount() {
		return sampleCount;
	}

}
