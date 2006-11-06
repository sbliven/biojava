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

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Feature;
import org.ensembl.datamodel.Location;
import org.ensembl.driver.SimpleFeatureAdaptor;

public class SimpleFeatureTest extends CoreBase {
	public SimpleFeatureTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(SimpleFeatureTest.class);
	}

	protected void setUp() throws Exception {
	  super.setUp();
		adaptor = (SimpleFeatureAdaptor)driver.getAdaptor("simple_feature");

	}

	public void testRetreiveByID() {
		try {
			long id = 30;
			Feature f = adaptor.fetch(id);
			System.out.println("\nFetched Feature (id=" + id + "): " + f);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	public void testRetrieveByAssemblyLocation() {
		try {
			Location aloc = new Location(new CoordinateSystem("chromosome"), "1", 10, 100000, 1);
			List features = adaptor.fetch(aloc);
			System.out.println("\nFetch by Location: " + aloc);
			for (int i = 0; i < features.size(); i++) {
				System.out.println((Feature)features.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private SimpleFeatureAdaptor adaptor;
}
