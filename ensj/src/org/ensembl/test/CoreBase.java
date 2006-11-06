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

import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.driver.CoordinateSystemAdaptor;
import org.ensembl.driver.CoreDriver;
import org.ensembl.registry.DriverGroup;

/**
 * Base for tests that access the ensembl core databases.
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 * @version $Revision$
 */
public class CoreBase extends Base {

  public CoordinateSystem chromosomeCS = null;

  public CoordinateSystem contigCS = null;

  public CoordinateSystem cloneCS = null;

  public CoordinateSystem superContigCS = null;

  protected CoreDriver driver = null;
  
  protected DriverGroup testGroup = null;
  
  protected CoreDriver testCoreDriver = null;
	
  protected static String UNINITIALISED_TEST_DB_CORE_DRIVER_ERROR = "Skipping test because 'testCoreDriver' is null, is it correcty configured in file 'unit_test.ini'?";

  /**
   * logging.conf parameter in file resources/data/unit_test.conf can be used to
   * specifiy which logging config file to use.
   */
  public CoreBase(String name){
    super(name);
  }

  protected void setUp() throws Exception {
    super.setUp();

    driver = registry.getGroup("human").getCoreDriver();
    CoordinateSystemAdaptor csa = driver.getCoordinateSystemAdaptor(); 
    chromosomeCS = csa.fetch("chromosome", null);
    contigCS = driver.getCoordinateSystemAdaptor().fetch("contig", null);
    cloneCS = driver.getCoordinateSystemAdaptor().fetch("clone", null);
    superContigCS = driver.getCoordinateSystemAdaptor().fetch("supercontig",
        null);

    testGroup = registry.getGroup("test_db");
    if (testGroup!=null)
      testCoreDriver = testGroup.getCoreDriver();
    
  }

} // Base
