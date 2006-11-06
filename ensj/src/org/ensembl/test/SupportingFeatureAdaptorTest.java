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
package org.ensembl.test;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Location;
import org.ensembl.driver.ExonAdaptor;
import org.ensembl.driver.SupportingFeatureAdaptor;

/**
 * Test class for SupportingFeatureAdaptors. 
 */
public class SupportingFeatureAdaptorTest extends CoreBase {

	private static Logger logger = Logger.getLogger(SupportingFeatureAdaptorTest.class.getName());

	public static final void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	public SupportingFeatureAdaptorTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(SupportingFeatureAdaptorTest.class);
	}

	protected void setUp() throws Exception {
	  super.setUp();
	  exonAdaptor = (ExonAdaptor)driver.getAdaptor("exon");
		if (exonAdaptor == null)
			throw new Exception("Failed to find exonAdaptor");
		supportingFeatureAdaptor = (SupportingFeatureAdaptor)driver.getAdaptor("supporting_feature");
		if (supportingFeatureAdaptor == null)
			throw new Exception("Failed to find supportingFeatureAdaptor");
	}

	public void testRetrieveByExonDifferentLocations() {

		try {

			//AssemblyMap ucscMap = (AssemblyMap)MapManager.get(ASSEMBLY_MAP_NAME);
			Location assemblyLocation = new Location(new CoordinateSystem("chromosome"), "12", 1, 999660, 0);

			Location chromosome12Location = new Location(chromosomeCS,"12");

			Location wholeGenome = new Location(chromosomeCS);

			Location locations[] = new Location[] {
				// Just do assembly for now cfLocation
				assemblyLocation
				// The next 2 are currently very slow!
				//,chromosome12Location
				//,wholeGenome
			};

			for (int i = 0; i < locations.length; ++i) {
				logger.fine("Retrieving exons for location : " + locations[i]);

				logger.fine("From exon adaptor : " + exonAdaptor);

				List exons = exonAdaptor.fetch(locations[i]);
				logger.fine("Fetch exons at location=" + locations[i]);
				if (exons == null) {
					logger.fine("No exons found at location.");
				} else {
					int counter = 0;
					int nSupport = 0;
					Iterator iter = exons.iterator();
					while (iter.hasNext()) {
						Exon exon = (Exon)iter.next();
						//logger.fine("exon=" + exon.toString());
						counter++;
						List support = exon.getSupportingFeatures();
						nSupport += support.size();
					}
					logger.fine("Number Exons = " + counter);
					logger.fine("Number Supporting Features = " + nSupport);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private ExonAdaptor exonAdaptor;
	private SupportingFeatureAdaptor supportingFeatureAdaptor;

} // ExonTest
