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

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.DnaDnaAlignment;
import org.ensembl.datamodel.Location;
import org.ensembl.driver.DnaDnaAlignmentAdaptor;

public class DnaDnaAlignmentTest extends CoreBase {

  private static Logger logger = Logger.getLogger(DnaDnaAlignmentTest.class
      .getName());

  private DnaDnaAlignmentAdaptor adaptor;

  public DnaDnaAlignmentTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(DnaDnaAlignmentTest.class);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  public static void main(String[] args) {
    (new DnaDnaAlignmentTest("testRetrieveByLocation")).run();
  }

  public void testRetrieveByLocation() throws Exception {
    CoordinateSystem chromosomeCS = new CoordinateSystem("chromosome");
    Location aloc = new Location(chromosomeCS, "20", 1000000, 1100000, 1);
    List dsfs = driver.getDnaDnaAlignmentAdaptor().fetch(aloc);
    System.out.println("\n\nfetch with location: " + aloc + "returns "
        + dsfs.size() + " hits");
    for (int i = 0; i < dsfs.size(); i++) {
      logger.fine("\n\nfetched dsf: \n" + (DnaDnaAlignment) dsfs.get(i));
    }
  }
  
  public void testRetrieveByNonExistentAnalysis() throws Exception {
    List r = driver.getDnaDnaAlignmentAdaptor().fetch(new Location("chromosome:1:1-100"), "fake_analysis_called_bob");
    assertEquals(0, r.size());
  }
}
