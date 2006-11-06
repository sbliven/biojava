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
 

import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ensembl.datamodel.AssemblyMapper;
import org.ensembl.datamodel.CoordinateSystem;
import org.ensembl.datamodel.Location;
import org.ensembl.driver.AssemblyMapperAdaptor;
import org.ensembl.driver.CoordinateSystemAdaptor;
import org.ensembl.util.mapper.Coordinate;

/**
 * Test class for Locations. 
 */
public class AssemblyMapperTest extends CoreBase {

  private static Logger logger = Logger.getLogger(AssemblyMapperTest.class.getName());


  public static final void main(String[] args) throws Exception {
    junit.textui.TestRunner.run(suite());
  }

  public AssemblyMapperTest (String name){
    super(name);
  }

  public static Test suite() { 
    TestSuite suite = new TestSuite();
    suite.addTestSuite( AssemblyMapperTest.class );
    //suite.addTest( new LocationTest("testResize") );
    return suite;
  }



  public void testmaps() throws Exception {
		CoordinateSystemAdaptor csa = driver.getCoordinateSystemAdaptor();
		CoordinateSystem cs1 = csa.fetch( "chromosome", Base.LATEST_HUMAN_CHROMOSOME_VERSION );
		CoordinateSystem cs2 = csa.fetch( "contig", "" );
		CoordinateSystem cs3 = csa.fetch( "clone", "" );
		Location l1 = new Location( cs1, "6", 1, 10000000, 1 );
		AssemblyMapperAdaptor ama = driver.getAssemblyMapperAdaptor();
		AssemblyMapper am = ama.fetchByCoordSystems( cs1, cs2 );
		Location l2 = new Location( cs2, "AL031906.1.1.99035", 1000, 2000, 1 );
//		AL031906.1.1.99035:1-98935,-1
		Coordinate coords[] = am.map( l1 );
		System.out.println( "Chromosome --> Contig SimpleMapper");
		for( int i=0; i<coords.length; i++ ) {
			System.out.println( coords[i] );
		}
		
		coords = am.map(l2);
		System.out.println( "Contig --> Chromosome SimpleMapper");
		for( int i=0; i<coords.length; i++ ) {
			System.out.println( coords[i] );
		}

		Location l3 = new Location( cs3, "AL031906.1", 1, 2000, 1 );
		am = ama.fetchByCoordSystems( cs1, cs3 );
		coords=  am.map( l3 );
		System.out.println( "Clone --> Chromosome ChainedMapper");
		for( int i=0; i<coords.length; i++ ) {
			System.out.println( coords[i] );
		}
		
  }





}
