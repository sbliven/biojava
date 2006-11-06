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
package org.ensembl.driver;

import org.ensembl.variation.driver.VariationDriver;

/**
 * Provides adaptors for accessing to ensembl core databases.
 * 
 * <p>
 * Drivers are created and, where needed, should be initialised() <b>before </b>
 * any of the getXXXX() methods are called.
 * 
 * @see CoreDriverFactory
 */
public interface CoreDriver extends EnsemblDriver {

	/**
	 * @return default translation adaptor if available otherwise null.
	 */
	TranslationAdaptor getTranslationAdaptor() throws AdaptorException;

	AssemblyMapperAdaptor getAssemblyMapperAdaptor() throws AdaptorException;

	/**
	 * @return default ExternalRefAdaptor if available otherwise null.
	 */
	ExternalRefAdaptor getExternalRefAdaptor() throws AdaptorException;

	/**
	 * @return default ProteinFeatureAdaptor if available otherwise null.
	 */
	ProteinFeatureAdaptor getProteinFeatureAdaptor() throws AdaptorException;

	/**
	 * @return default Sequence adaptor if available otherwise null.
	 */
	SequenceAdaptor getSequenceAdaptor() throws AdaptorException;

	/**
	 * @return default LocationConverter if available otherwise null.
	 */
	LocationConverter getLocationConverter() throws AdaptorException;

	/**
	 * @return default adaptor if available otherwise null.
	 */
	TranscriptAdaptor getTranscriptAdaptor() throws AdaptorException;

	/**
	 * @return default RepeatConsensus adaptor if available otherwise null.
	 */
	RepeatConsensusAdaptor getRepeatConsensusAdaptor() throws AdaptorException;

	/**
	 * @return default ExternalDatabaseAdaptor if available otherwise null.
	 */
	ExternalDatabaseAdaptor getExternalDatabaseAdaptor()
			throws AdaptorException;

	/**
	 * @return default ExonAdaptor if available otherwise null.
	 */
	ExonAdaptor getExonAdaptor() throws AdaptorException;

	/**
	 * @return default GeneAdaptor if available otherwise null.
	 */
	GeneAdaptor getGeneAdaptor() throws AdaptorException;

	/**
	 * @return default SupportingFeatureAdaptor if available otherwise null.
	 */
	SupportingFeatureAdaptor getSupportingFeatureAdaptor()
			throws AdaptorException;

	/**
	 * @return default MarkerAdaptor if available otherwise null.
	 */
	MarkerAdaptor getMarkerAdaptor() throws AdaptorException;

	/**
	 * @return default AnalysisAdaptor if available otherwise null.
	 */
	AnalysisAdaptor getAnalysisAdaptor() throws AdaptorException;

	/**
	 * @return default RepeatFeatureAdaptor if available otherwise null.
	 */
	RepeatFeatureAdaptor getRepeatFeatureAdaptor() throws AdaptorException;

	/**
	 * @return default DnaProteinAlignmentAdaptor if available otherwise null.
	 */
	DnaProteinAlignmentAdaptor getDnaProteinAlignmentAdaptor()
			throws AdaptorException;

	/**
	 * @return default DnaDnaAlignmentAdaptor if available otherwise null.
	 */
	DnaDnaAlignmentAdaptor getDnaDnaAlignmentAdaptor() throws AdaptorException;

	/**
	 * @return default SimpleFeatureAdaptor if available otherwise null.
	 */
	SimpleFeatureAdaptor getSimpleFeatureAdaptor() throws AdaptorException;

	/**
	 * @return default PredictionTranscriptAdaptor if available otherwise null.
	 */
	PredictionTranscriptAdaptor getPredictionTranscriptAdaptor()
			throws AdaptorException;

	/**
	 * @return default PredictionExonAdaptor if available otherwise null.
	 */
	PredictionExonAdaptor getPredictionExonAdaptor() throws AdaptorException;

	/**
	 * @return default StableIDEventAdaptor if available otherwise null.
	 */
	StableIDEventAdaptor getStableIDEventAdaptor() throws AdaptorException;

	/**
	 * @return default CoordinateSystemAdaptor if available otherwise null.
	 */
	CoordinateSystemAdaptor getCoordinateSystemAdaptor()
			throws AdaptorException;

	/**
	 * @return default SequenceRegionAdaptor if available otherwise null.
	 */
	SequenceRegionAdaptor getSequenceRegionAdaptor() throws AdaptorException;

	/**
	 * @return default KaryotypeAdaptor if available otherwise null.
	 */
	KaryotypeBandAdaptor getKaryotypeBandAdaptor() throws AdaptorException;

	/**
	 * @return default MiscFeatureAdaptor if available otherwise null.
	 */
	MiscFeatureAdaptor getMiscFeatureAdaptor() throws AdaptorException;

	/**
	 * @return default MiscSetAdaptor if available otherwise null.
	 */
	MiscSetAdaptor getMiscSetAdaptor() throws AdaptorException;

	/**
	 * @return default MarkerFeatureAdaptor if available, otherwise null.
	 */
	MarkerFeatureAdaptor getMarkerFeatureAdaptor() throws AdaptorException;

	/**
	 * @return default QtlAdaptor if available, otherwise null.
	 */
	QtlAdaptor getQtlAdaptor() throws AdaptorException;

	/**
	 * @return default QtlFeatureAdaptor if available, otherwise null.
	 */
	QtlFeatureAdaptor getQtlFeatureAdaptor() throws AdaptorException;

	/**
	 * @return default OligoProbeAdaptor if available, otherwise null.
	 */
	OligoProbeAdaptor getOligoProbeAdaptor() throws AdaptorException;

	/**
	 * @return default OligoFeatureAdaptor if available, otherwise null.
	 */
	OligoFeatureAdaptor getOligoFeatureAdaptor() throws AdaptorException;

	/**
	 * @return default OligoArrayAdaptor if available, otherwise null.
	 */
	OligoArrayAdaptor getOligoArrayAdaptor() throws AdaptorException;

	/**
	 * @return default AssemblyExceptionAdaptor if available, otherwise null.
	 */
	AssemblyExceptionAdaptor getAssemblyExceptionAdaptor() throws AdaptorException;

  /**
   * Sets the variation driver on the driver.
   * @param vdriver sister variation driver.
   */
  void setVariationDriver(VariationDriver vdriver) throws AdaptorException;
	
  
  /**
   * Get the sister variation driver.
   * @return the sister variation driver if set, otherwise null.
   */
  VariationDriver getVariationDriver();

}
