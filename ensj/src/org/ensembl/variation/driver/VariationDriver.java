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
package org.ensembl.variation.driver;

import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.CoreDriver;
import org.ensembl.driver.EnsemblDriver;
import org.ensembl.variation.datamodel.AlleleConsequenceAdaptor;

/**
 * The point of this class is ...
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 *  
 */
public interface VariationDriver extends EnsemblDriver {

	/**
	 * Core driver associated with this variation driver.
	 */
	CoreDriver getCoreDriver();

	/**
	 * Core driver associated with this variation driver.
	 * 
	 * @param coreDriver
	 *            driver providing access to the core database 
	 *            associated with the variation database.
	 * @throws AdaptorException
	 */
	void setCoreDriver(CoreDriver coreDriver) throws AdaptorException;

	/**
	 * The variation feature adaptor.
	 * 
	 * @return the variation feature adaptor, or null if not available.
	 * @throws AdaptorException
	 */
	VariationFeatureAdaptor getVariationFeatureAdaptor()
			throws AdaptorException;

	/**
	 * The variation adaptor.
	 * 
	 * @return the variation adaptor, or null if not available.
	 * @throws AdaptorException
	 */
	VariationAdaptor getVariationAdaptor() throws AdaptorException;

	/**
	 * The population adaptor.
	 * 
	 * @return the population adaptor, or null if unavailable.
	 * @throws AdaptorException
	 */
	PopulationAdaptor getPopulationAdaptor() throws AdaptorException;

	/**
	 * The variation group adaptor.
	 * 
	 * @return the variation group adaptor, or null if unavailable.
	 * @throws AdaptorException
	 */
	VariationGroupAdaptor getVariationGroupAdaptor() throws AdaptorException;

	/**
	 * The variation group feature adaptor.
	 * 
	 * @return the variation group adaptor, or null if unavailable.
	 * @throws AdaptorException
	 */
	VariationGroupFeatureAdaptor getVariationGroupFeatureAdaptor()
			throws AdaptorException;

	/**
	 * The allele group adaptor.
	 * 
	 * @return the allele group adaptor, or null if unavailable.
	 * @throws AdaptorException
	 */
	AlleleGroupAdaptor getAlleleGroupAdaptor() throws AdaptorException;

	/**
	 * The individual adaptor.
	 * 
	 * @return the individual adaptor, or null if unavailable.
	 * @throws AdaptorException
	 */
	IndividualAdaptor getIndividualAdaptor() throws AdaptorException;

	/**
	 * The individual genotype adaptor.
	 * 
	 * @return the individual genotype adaptor, or null if unavailable.
	 * @throws AdaptorException
	 */
	IndividualGenotypeAdaptor getIndividualGenotypeAdaptor()
			throws AdaptorException;

	/**
	 * The population genotype adaptor.
	 * 
	 * @return the individual genotype adaptor, or null if unavailable.
	 * @throws AdaptorException
	 */
	PopulationGenotypeAdaptor getPopulationGenotypeAdaptor()
			throws AdaptorException;

	/**
	 * The transcript variation adaptor.
	 * 
	 * @return the transcript variation adaptor, or null if unavailable.
	 * @throws AdaptorException
	 */
	TranscriptVariationAdaptor getTranscriptVariationAdaptor()
			throws AdaptorException;

	/**
	 * The LDFeature adaptor.
	 * 
	 * @return the LDFeature adaptor, or null if unavailable.
	 * @throws AdaptorException
	 */
	LDFeatureAdaptor getLDFeatureAdaptor() throws AdaptorException;

	
	/**
	 * The AlleleFeature adaptor.
	 * 
	 * @return the AlleleFeature adaptor, or null if unavailable.
	 * @throws AdaptorException
	 */
	AlleleFeatureAdaptor getAlleleFeatureAdaptor() throws AdaptorException;	
	
	
	/**
	 * The AlleleConsequence adaptor.
	 * 
	 * @return the AlleleConsequence adaptor, or null if unavailable.
	 * @throws AdaptorException
	 */
	AlleleConsequenceAdaptor getAlleleConsequenceAdaptor() throws AdaptorException;	
	
}
