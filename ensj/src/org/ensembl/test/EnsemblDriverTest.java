/*
	Copyright (C) 2005 EBI, GRL

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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.ensembl.datamodel.Gene;
import org.ensembl.util.JDBCUtil;

/**
 * Tests the EnsemblDriver table manipulation functionality such
 * as backup, clear and restore.
 *
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 */
public class EnsemblDriverTest extends CoreBase {

  
	public EnsemblDriverTest(String name) {
		super(name);
	}

	public void  testBasicDatabaseManipulationSupport() throws Exception {
	  
	  assertNotNull(UNINITIALISED_TEST_DB_CORE_DRIVER_ERROR,testCoreDriver);
		
		Connection conn = testCoreDriver.getConnection();
		Statement s = conn.createStatement();
		
		// Populate table with dummy values
		s.execute("create table if not exists test_table (number int not null)");
		s.execute("delete from test_table");
		s.execute("insert into test_table values (100)");
		s.execute("insert into test_table values (200)");
		ResultSet rs = s.executeQuery("select number from test_table order by number");
		assertTrue(rs.next());
		assertEquals(100, rs.getInt(1));
		assertTrue(rs.next());
		assertEquals(200, rs.getInt(1));
		assertTrue(!rs.next());
		rs.close();
		
		testCoreDriver.backupTable("test_table");
		testCoreDriver.clearTable("test_table");
		
		
		// check table is now empty
		rs = s.executeQuery("select number from test_table order by number");
		assertTrue(!rs.next());
		
		testCoreDriver.restoreTable("test_table");
		// Check table has original contents again
		rs = s.executeQuery("select number from test_table order by number");
		assertTrue(rs.next());
		assertEquals(100, rs.getInt(1));
		assertTrue(rs.next());
		assertEquals(200, rs.getInt(1));
		assertTrue(!rs.next());
		rs.close();
		
		rs.close();
		s.close();
		conn.close();
		
	}
	

	public void testHighLevelDatabaseManipulationSupport() throws Exception {
	  
	  assertNotNull(UNINITIALISED_TEST_DB_CORE_DRIVER_ERROR,testCoreDriver);
	  
	  // Note: we call clearCache() below to force adaptor to look in database.
	  
	  long[] geneIDs = testCoreDriver.getGeneAdaptor().fetchInternalIDs();
	  final int nGenes = geneIDs.length;
	  
	  // can't run test without any genes!
		assertTrue(nGenes>0);
		
		// get a gene for future use...
		final long geneID = geneIDs[0];
		Gene gene = testCoreDriver.getGeneAdaptor().fetch(geneID);
		assertEquals(geneID, gene.getInternalID());
		
		// Test manipulating single tables
		testCoreDriver.backupAndClearTable("gene");
		
		testCoreDriver.getGeneAdaptor().clearCache(); 
		assertEquals(null, testCoreDriver.getGeneAdaptor().fetch(geneID));
		assertEquals(0, testCoreDriver.getGeneAdaptor().fetchInternalIDs().length);
		
		testCoreDriver.restoreTable("gene");
		assertEquals(geneID, testCoreDriver.getGeneAdaptor().fetch(geneID).getInternalID());
		assertEquals(nGenes, testCoreDriver.getGeneAdaptor().fetchInternalIDs().length);
		
		Connection conn = null;
		Statement s = null;
		try {
		  conn = testCoreDriver.getConnection();
		  s = conn.createStatement();
		  s.execute("delete from gene where gene_id = " + geneID);
		  testCoreDriver.getGeneAdaptor().clearCache(); 
		  assertEquals(null, testCoreDriver.getGeneAdaptor().fetch(geneID));
		  testCoreDriver.restoreTable("gene"); // use the backup we made earlier
		  assertEquals(geneID, testCoreDriver.getGeneAdaptor().fetch(geneID).getInternalID());
		} finally {
		  JDBCUtil.close(s);
		  JDBCUtil.close(conn);
		}
		
		// Test manipulating ALL tables
		testCoreDriver.backupAndClearTables();
		
		testCoreDriver.getGeneAdaptor().clearCache(); 
		assertEquals(null, testCoreDriver.getGeneAdaptor().fetch(geneID));
		assertEquals(0, testCoreDriver.getGeneAdaptor().fetchInternalIDs().length);
		
		testCoreDriver.restoreTables();
		assertEquals(geneID, testCoreDriver.getGeneAdaptor().fetch(geneID).getInternalID());
		assertEquals(nGenes, testCoreDriver.getGeneAdaptor().fetchInternalIDs().length);
		
	}
	
	
	
  
  
}
