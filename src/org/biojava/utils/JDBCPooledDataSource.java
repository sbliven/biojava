/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */
package org.biojava.utils;

import javax.sql.DataSource;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.DataSourceConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.BasicDataSource;

/**
* Returns a DataSource that implements connection pooling
*
* Uses Jakarta Commons DBCP and Pool packages.
* See the description of the dbcp package at 
*		http://jakarta.apache.org/commons/dbcp/api/overview-summary.html#overview_description
*
* @author Simon Foote
*/

public class JDBCPooledDataSource {

	public static DataSource getDataSource(	String driver, 
																					String url,
																					String user,
																					String pass)
			throws Exception {

    BasicDataSource ds = new BasicDataSource();
    ds.setUrl(url);
    ds.setDriverClassName(driver);
		ds.setUsername(user);
		ds.setPassword(pass);
    // Set BasicDataSource properties such as maxActive and maxIdle, as described in
    // http://jakarta.apache.org/commons/dbcp/api/org/apache/commons/dbcp/BasicDataSource.html
    ds.setMaxActive(10);
		ds.setMaxIdle(5);
		ds.setMaxWait(10000);
  
    // Create a PoolableDataSource as described in http://jakarta.apache.org/commons/dbcp/api/overview-summary.html#overview_description
    ObjectPool connectionPool = new GenericObjectPool(null);
    ConnectionFactory connectionFactory = new DataSourceConnectionFactory(ds);
    PoolableObjectFactory poolableConnectionFactory = new 
				PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
    PoolingDataSource dataSource = new PoolingDataSource(connectionPool);

    return dataSource;
  }
}
