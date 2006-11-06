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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.ensembl.driver.LoggingManager;
import org.ensembl.util.ConnectionPoolDataSource;

/**
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp</a>
 *
 */
public class ConnectionPoolTest extends TestCase {

  
  /**
   * Constructor for ConnectionPoolTest.
   * @param arg0
   */
  public ConnectionPoolTest(String arg0) {
    super(arg0);
    LoggingManager.configure();
  }

  
  
  public void testSimpleUse() throws Exception {

    ConnectionPoolDataSource ds =
      new ConnectionPoolDataSource(
          "com.mysql.jdbc.Driver",
          "jdbc:mysql://ensembldb.ensembl.org/",
          "anonymous",
          null,
          1);
    
    Connection conn = ds.getConnection();
    testConnection(conn);

    boolean exception = false;
    try {
      Connection conn2 = ds.getConnection();
      // shouldn't get this far because we only
      fail();
    } catch (SQLException e) {
      exception = true;
    } finally {
      assertTrue(exception);
    }

    // check that releasing it back to pool works
    conn.close();
    Connection conn3 = ds.getConnection();
    testConnection(conn3);

    // check we got the same connection from the pool
    assertSame(conn, conn3);

    // check again that we can't create another connection
    exception = false;
    try {
      Connection conn2 = ds.getConnection();
      // shouldn't get this far because we only
      fail();
    } catch (SQLException e) {
      exception = true;
    } finally {
      assertTrue(exception);
    }
  }

  public void testPoolingAcrossDatabases() throws Exception {
    ConnectionPoolDataSource ds =
      new ConnectionPoolDataSource(
          "com.mysql.jdbc.Driver",
          "jdbc:mysql://ensembldb.ensembl.org/",
          "anonymous",
          null,
          1);
  
    // open and close a few times
    
    Connection conn = ds.getConnection();
    assertEquals("", conn.getCatalog());
    conn.close();
    
    String db = "homo_sapiens_variation_32_35e"; 
    conn = ds.getConnection(db);
    assertEquals(db, conn.getCatalog());
    conn.close();
    
    conn = ds.getConnection(db);
    assertEquals(db, conn.getCatalog());
    conn.close();

    db = "ensembl_compara_32";
    conn = ds.getConnection(db);
    assertEquals(db, conn.getCatalog());
    conn.close();

    // test proxies
    
    conn = ds.createDataSourceProxy(db).getConnection();
    assertEquals(db, conn.getCatalog());
    conn.close();
    
    conn = ds.createDataSourceProxy(db).getConnection();
    assertEquals(db, conn.getCatalog());
    conn.close();
    
    ds.createDataSourceProxy(db).getConnection();
    try {
      ds.createDataSourceProxy(db).getConnection();
      fail("Pool should have thrown an exception");
    } catch (Exception e) {}
    
     
  }
   
  
  /**
   * @param conn
   */
  private void testConnection(Connection conn) throws Exception {
    ResultSet rs = conn.createStatement().executeQuery("show databases");
    while (rs.next()) {
    }
  }

}
