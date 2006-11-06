/*
 * Copyright (C) 2002 EBI, GRL
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.ensembl.test;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.ensembl.datamodel.Analysis;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.SimpleFeature;
import org.ensembl.datamodel.impl.AnalysisImpl;
import org.ensembl.driver.AdaptorException;
import org.ensembl.driver.impl.AnalysisAdaptorImpl;
import org.ensembl.driver.impl.SimpleFeatureAdaptorImpl;

/**
 * JUnit tests for SimpleFeatureAdaptor.
 */
public class SimpleFeatureAdaptorTest extends CoreBase {

	private static final Logger logger = Logger.getLogger(SimpleFeatureAdaptorTest.class.getName());

	SimpleFeatureAdaptorImpl simpleFeatureAdaptor = null;
	AnalysisAdaptorImpl analysisAdaptor = null;

	Location chr1Loc1;
	Location chr1Loc2;

	public SimpleFeatureAdaptorTest(String arg0) {
		super(arg0);


	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(SimpleFeatureAdaptorTest.class);
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
	  super.setUp();
    simpleFeatureAdaptor = (SimpleFeatureAdaptorImpl)driver.getSimpleFeatureAdaptor();
    analysisAdaptor = (AnalysisAdaptorImpl)driver.getAnalysisAdaptor();
    
    chr1Loc1 = new Location(chromosomeCS, "1", 1, 1000000, 1);
  	chr1Loc2 = new Location(chromosomeCS, "1", 1, 2000000, 1);
	}
  

	public void testGetPrimaryTable() {

		assertEquals(simpleFeatureAdaptor.getPrimaryTableName(), "simple_feature");
		assertEquals(simpleFeatureAdaptor.getPrimaryTableSynonym(), "sf");
    
	}

	public void testFetchAllByLocation() {

		List features = null;

		try {
			features = simpleFeatureAdaptor.fetch(chr1Loc1);
		} catch (AdaptorException e) {
			e.printStackTrace();
		}

		assertNotNull(features);
		assertTrue(features.size() > 0);
		logger.fine("11 simple features in first 100mb of chr 1:");
		for (Iterator it = features.iterator(); it.hasNext();) {
			SimpleFeature sf = (SimpleFeature)it.next();
			logger.fine(sf.toString());
		}

	}

	public void testFetchByInternalID() {

		SimpleFeature sf = null;
		try {
			sf = simpleFeatureAdaptor.fetch(100);
		} catch (AdaptorException e) {
			e.printStackTrace();
		}
		assertNotNull(sf);
		logger.fine(sf.toString());

	}

	//---------------------------------------------------------------------
	// The following 4 methods test functionality in the base feature adaptor
	// class.

	public void testFetchByLogicalName() {

		// note this method may be slow - pick a logic name that has as few
		// matching features as possible
		List features = null;
		try {
			features = simpleFeatureAdaptor.fetch("tRNAscan");
		} catch (AdaptorException e) {
			e.printStackTrace();
		}
		assertNotNull(features);
		assertTrue(features.size() > 0);

	}

	public void testFetchByLocationAndAnalysis() {

		List features = null;

		Analysis anal = new AnalysisImpl();
		anal.setLogicalName("Eponine");

		try {
			features = simpleFeatureAdaptor.fetch(chr1Loc1, anal);
		} catch (AdaptorException e) {
			e.printStackTrace();
		}
		assertNotNull(features);
		assertTrue(features.size() > 0);

	}

	public void testFetchByLocationAndLogicNamesArray() {

		List features = null;

		String[] logicNames = { "Eponine", "tRNAscan" };

		try {
			features = simpleFeatureAdaptor.fetch(chr1Loc1, logicNames);
		} catch (AdaptorException e) {
			e.printStackTrace();
		}
		assertNotNull(features);
		assertTrue(features.size() > 0);

	}

	public void testFetchByLocationAndAnalysisArray() {

		List features = null;

		String[] logicNames = { "Eponine", "tRNAscan" };
		Analysis[] analyses = new Analysis[logicNames.length];

		for (int i = 0; i < logicNames.length; i++) {
			analyses[i] = new AnalysisImpl();
			analyses[i].setLogicalName(logicNames[i]);
		}

		try {
			features = simpleFeatureAdaptor.fetch(chr1Loc1, analyses);
		} catch (AdaptorException e) {
			e.printStackTrace();
		}
		assertNotNull(features);
		assertTrue(features.size() > 0);

	}

	//---------------------------------------------------------------------

}
