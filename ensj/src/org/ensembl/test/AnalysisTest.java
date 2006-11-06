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
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.Analysis;
import org.ensembl.driver.AnalysisAdaptor;

public class AnalysisTest extends CoreBase {

	private static Logger logger = Logger.getLogger(AnalysisTest.class.getName());
	private static boolean useDefaultInitialisation = true;
	private AnalysisAdaptor analysisAdaptor;

	public AnalysisTest(String name) {
		super(name);
	}

	public static void main(String[] args) throws Exception {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		return new TestSuite(AnalysisTest.class);
	}

	protected void setUp() throws Exception {
	  super.setUp();
		analysisAdaptor = driver.getAnalysisAdaptor();
	}

	public void testFetchAnalysisByID() throws Exception {

		Analysis a = analysisAdaptor.fetch(4);
		assertNotNull(a);
		System.out.println(a.toString());
	}

	public void testFetchAnalysisByLogicalName() throws Exception {

		Analysis a = analysisAdaptor.fetchByLogicalName("RepeatMask");
		assertNotNull(a);

	}

	public void testFetchAnalysisByGffFeature() throws Exception {

		List list = analysisAdaptor.fetchByGffFeature("similarity");
		assertNotNull(list);
		assertTrue(list.size() > 0);
		for (int i = 0; i < list.size(); i++) {
			Analysis a = (Analysis)list.get(i);
			assertNotNull(a);
		}
    
    list = analysisAdaptor.fetchByGffFeature("similarity");
        assertNotNull(list);
        assertTrue(list.size() > 0);
        for (int i = 0; i < list.size(); i++) {
          Analysis a = (Analysis)list.get(i);
          assertNotNull(a);
        }
	}

	public void testFetchAllAnalysis() throws Exception {

		List list = analysisAdaptor.fetch();
		assertNotNull(list);
		for (int i = 0; i < list.size(); i++) {
			Analysis a = (Analysis)list.get(i);
			assertNotNull(a);
		}

	}

}
