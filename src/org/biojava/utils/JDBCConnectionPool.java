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

import java.util.*;
import java.sql.*;
import java.lang.reflect.*;

import java.util.Date; // TIE-BREAK

/**
 * Really simple connection pool for JDBC databases
 *
 * @author Thomas Down
 */

public class JDBCConnectionPool {
    private final String dbURL;
    private final String dbUser;
    private final String dbPass;

    private List connectionPool;

    {
	connectionPool = new ArrayList();
    }

    public JDBCConnectionPool(String url, String user, String pass) 
    {
	dbURL = url;
	dbUser = user;
	dbPass = pass;
    }

    public JDBCConnectionPool(String url)
    {
        this(url, null, null);
    }

    //
    // Manage a pool of transactions with the database.
    //

    public Connection takeConnection()
        throws SQLException
    {
	Connection conn = null;

	synchronized (connectionPool) {
	    if (connectionPool.size() > 0) {
		conn = (Connection) connectionPool.remove(0);
	    }
	}

	// We don't perform the isClosed in the synchronized block in case the
	// network is being slow.

	if (conn != null) {
	    if (!conn.isClosed()) {
		return conn;
	    } else {
		// We simply drop conn on the floor.  It should be safely collected.
		return takeConnection();
	    }
	}

	// Statement-pool was empty -- let's create a new connection.

        if(dbUser != null) {
 	  conn = DriverManager.getConnection(dbURL, dbUser, dbPass);
        } else {
          conn = DriverManager.getConnection(dbURL);
        }
	Statement st = conn.createStatement();
	// st.execute("SET OPTION SQL_BIG_SELECTS=1");
	st.close();
	return conn;
    }

    public void putConnection(Connection c) 
        throws SQLException
    {
	synchronized (connectionPool) {
	    connectionPool.add(c);
	}
    }

    public Statement takeStatement() 
        throws SQLException
    {
	return takeConnection().createStatement();
    }

    public void putStatement(Statement st)
        throws SQLException
    {
	putConnection(st.getConnection());
	st.close();
    }
}
