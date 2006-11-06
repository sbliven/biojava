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

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.OligoArray;
import org.ensembl.datamodel.OligoFeature;
import org.ensembl.datamodel.OligoProbe;
import org.ensembl.datamodel.Location;
import org.ensembl.driver.AdaptorException;
import org.ensembl.util.IDSet;

/**
 * Tests ensj's oligo (microarray) support.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 */
public class OligoTest extends CoreBase {

	private static final long AFFY_ARRAY_ID = 2;
	private static final String AFFY_ARRAY_NAME_FAKE = "BLA BAL BLA";
	private static final String AFFY_ARRAY_NAME = "U133_X3P";
	private static final long AFFY_PROBE_ID = 51;
	private static final long AFFY_FEATURE_ID = 573550;
	private static final String LOCATION = "chromosome:22:20m-20.1m";
	private static final String PROBE_SET_NAME = "75590_at";

  public static Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTestSuite(OligoTest.class);

    //suite.addTest(new OligoTest("testFetchByInternalIDAndAccessingChildData"));
    //suite.addTest(new OligoTest("testFetchAffyFeaturesByProbe"));
    //suite.addTest(new OligoTest("testFetchAffyFeaturesByLocation"));
    //suite.addTest(new OligoTest("testFetchAffyFeaturesByLocationAndArray"));
    //suite.addTest(new OligoTest("testFetchAllAffyArrays"));
    //suite.addTest(new OligoTest("testFetchAffyProbesByProbeSet"));
    
    return suite;
  }	
	
	public OligoTest(String name) {
		super(name); 
	}

	public void testFetchByInternalIDAndAccessingChildData() throws Exception {
	  
		check(driver.getOligoArrayAdaptor().fetch(AFFY_ARRAY_ID));
		assertNull(driver.getOligoArrayAdaptor().fetch(AFFY_ARRAY_NAME_FAKE));
		check(driver.getOligoArrayAdaptor().fetch(AFFY_ARRAY_NAME));
		check(driver.getOligoProbeAdaptor().fetch(AFFY_PROBE_ID));
		check(driver.getOligoFeatureAdaptor().fetch(AFFY_FEATURE_ID),null, null, null);
	}

	public void testFetchAffyFeaturesByProbe() throws Exception {
		OligoProbe p = driver.getOligoProbeAdaptor().fetch(AFFY_PROBE_ID);
		List fs = driver.getOligoFeatureAdaptor().fetch(p);
		assertTrue(fs.size() > 0);
		check((OligoFeature)fs.get(0), null, null, p);
		
	}
	
	public void testFetchAffyFeaturesByLocation() throws Exception {
		Location loc = new Location(LOCATION);
		List fs = driver.getOligoFeatureAdaptor().fetch(loc);
		assertTrue(fs.size() > 0);
		check((OligoFeature)fs.get(0), null, loc, null);
		
	}


	public void testFetchAllAffyArrays() throws AdaptorException {
		List as = driver.getOligoArrayAdaptor().fetch();
		assertTrue(as.size()>0);
		check((OligoArray)as.get(0));
	}

	
	public void testFetchAffyProbesByProbeSet() throws Exception {
				List ps = driver.getOligoProbeAdaptor().fetch(PROBE_SET_NAME);
		OligoProbe p = (OligoProbe) ps .get(0);
		check(p);
		assertEquals(p.getProbeSetName(),PROBE_SET_NAME);
	}
	
	public void fetchAffyProbesByArray() throws Exception {
		OligoArray a = driver.getOligoArrayAdaptor().fetch(AFFY_ARRAY_ID); 
		List ps = driver.getOligoProbeAdaptor().fetch(a);
		OligoProbe p = (OligoProbe) ps.get(0); 
		check(p);
		assertTrue(new IDSet(p.getArraysContainingThisProbe()).contains(a));
	}
	
	public void fetchAffyProbesByAffyFeature() throws Exception {
		OligoFeature f = driver.getOligoFeatureAdaptor().fetch(AFFY_FEATURE_ID);
		check(f.getProbe());
	}
	
	private void check(OligoArray array) {
	  assertNotNull(array);
		assertTrue(array.getInternalID()>0);
		assertTrue(array.getName().length()>0);
		assertTrue(array.getProbeSetSize()>0);
		assertNotNull("Failed to get external database for array:"+array,array.getExternalDatabase());
		assertTrue(array.getExternalDatabase().getInternalID()>0);
    assertNotNull(array.getType());
		// Note: this is too slow to run during regression testing
		//assertTrue(array.getAffyProbes().size()>0);
	}

	/**
	 * 
	 * @param feature feature to be checked.
	 * @param array one of the features parent microarrays. Can be null.
	 * @param loc expected overlapping location. Can be null. 
	 * @param probe expected probe for feature. Can be null.
	 */
	private void check(OligoFeature feature, OligoArray array, Location loc, OligoProbe probe) {
	
	  assertNotNull(feature);
		assertTrue(feature.getInternalID()>0);
		assertNotNull(feature.getLocation());
		
		// check that the expected parent array is available from the feature
		if (array!=null) {
			IDSet s = new IDSet(feature.getProbe().getArraysContainingThisProbe());
			assertTrue(s.contains(array));
		}
		
		if (loc!=null)
			assertTrue(loc.overlaps(feature.getLocation()));
		
		if (probe!=null)
			assertEquals(probe.getInternalID(), feature.getProbe().getInternalID());
		
	}
	
	
	private void check(OligoProbe probe) {
    assertNotNull(probe);
		assertTrue(probe.getInternalID()>0);
		assertTrue(probe.getQualifiedNames()[0].length()>0);
		assertTrue(probe.getProbeSetName().length()>0);
    assertTrue(probe.getLength()>0);
		List fs = probe.getOligoFeatures();
		OligoFeature f = (OligoFeature) fs.get(0);
		assertEquals(f.getProbeSetName(), probe.getProbeSetName());
		check(f,(OligoArray) probe.getArraysContainingThisProbe().get(0), null, probe);
	}
}
