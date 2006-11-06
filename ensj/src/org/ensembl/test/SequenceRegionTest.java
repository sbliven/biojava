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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.ensembl.datamodel.Attribute;
import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.SequenceRegion;
import org.ensembl.datamodel.impl.AttributeImpl;
import org.ensembl.datamodel.impl.SequenceRegionImpl;

/**
 * Tests for SequenceRegion and SequenceRegionAttribute.
 */
public class SequenceRegionTest extends TestCase {

	private SequenceRegion sr;
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(SequenceRegionTest.class);
	}

	public SequenceRegionTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(SequenceRegionTest.class);
		return suite;
	}
	
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		sr = new SequenceRegionImpl(null);
	}

	public void testGetterSetters() {
		
		sr.setName("name");
		assertEquals(sr.getName(), "name");
		
		sr.setLength(999);
		assertEquals(sr.getLength(), 999);
		
		sr.setInternalID(123456);
		assertEquals(sr.getInternalID(), 123456);
		
		CoordinateSystem cs = new CoordinateSystem("chromosome", "ncbi34");
		sr.setCoordinateSystem(cs);
		assertEquals(sr.getCoordinateSystem(), cs);
		
	}

	public void testAttributes() {
		
		Attribute sra1 = new AttributeImpl("code1", "name1", "desc1", "value1");
		Attribute sra2 = new AttributeImpl("code2", "name2", "desc2", "value2");
		Attribute sra3 = new AttributeImpl("code3", "name3", "desc3", "value3");
		
		sr.addAttribute(sra1);
		sr.addAttribute(sra2);
		sr.addAttribute(sra3);
		
		assertTrue(sr.hasAttributes());
		
		Attribute[] attribs = sr.getAttributes();
		assertEquals(attribs.length, 3);
		
		String at = sr.getAttributeValue("code2");
		assertEquals(at, "value2");
		
	}

}
