/* -*- c-basic-offset: 4; indent-tabs-mode: nil -*- */
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

package org.biojava.bio.seq.db.biosql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.biojava.bio.BioRuntimeException;
import org.biojava.utils.JDBCConnectionPool;

/**
 * Isolates all code that is specific to a particular RDBMS. To add
 * support for a new RDBMS, write a new <code>DBHelper</code> subclass
 * and ensure that it can be found by editing the
 * <code>getDBHelperForURL</code> method in this class.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @author Len Trigg
 */
public abstract class DBHelper {

    /**
     * Returns a DBHelper implementation suitable for a particular
     * database.
     *
     * @param ourURL the JDBC url
     * @return a <code>DBHelper</code>.
     */
    public static DBHelper getDBHelperForURL(String ourURL) {
	if (ourURL.startsWith("jdbc:")) {
	    ourURL = ourURL.substring(5);
	}
        if (!Character.isLetter(ourURL.charAt(0))) {
            throw new IllegalArgumentException("URL must start with a letter: " + ourURL);
        }
  
	int colon = ourURL.indexOf(':');
	if (colon > 0) {
	    String protocol = ourURL.substring(0, colon);
	    if (protocol.indexOf("mysql") >= 0) {
		// Accept any string containing `mysql', to cope with Caucho driver
	        return new MySQLDBHelper();
	    } else if (protocol.equals("postgresql")) {
		return new PostgreSQLDBHelper();
 	    } else if (protocol.equals("oracle")) {
 		return new OracleDBHelper();
	    }
	}

	return new UnknownDBHelper();
    }

    public static final class DeleteStyle {
	private final String name;

	private DeleteStyle(String name) {
	    this.name = name;
	}

	public String toString() {
	    return "DBHelper.DeleteStyle: " + name;
	}
    }

    public final static DeleteStyle DELETE_POSTGRESQL = new DeleteStyle("Postgresql");;
    public final static DeleteStyle DELETE_MYSQL4 = new DeleteStyle("Mysql 4.02 or later");
    public final static DeleteStyle DELETE_GENERIC = new DeleteStyle("Portable SQL");

    public abstract int getInsertID(Connection conn,
			   String table,
			   String columnName)
	throws SQLException;

    public abstract DeleteStyle getDeleteStyle();

    /**
     * Detects whether a particular table is present in the database.
     *
     * @param pool a <code>JDBCConnectionPool</code> that will provide a connection to the database.
     * @param tablename the name of the table.
     * @return true if the table exists in the database.
     * @throws NullPointerException if pool is null.
     * @throws IllegalArgumentException if tablename is null or empty.
     */
    public boolean containsTable(JDBCConnectionPool pool, String tablename) {
        if (pool == null) {
            throw new NullPointerException("Require a connection pool.");
        }
        if ((tablename == null) || (tablename.length() == 0)) {
            throw new IllegalArgumentException("Invalid table name given");
        } 
        try {
            boolean present;
            Connection conn = pool.takeConnection();
            PreparedStatement ps = conn.prepareStatement("select * from " + tablename + " limit 1");
            try {
                ps.executeQuery();
                present = true;
            } catch (SQLException ex) {
                present = false;
            }
            ps.close();
            pool.putConnection(conn);
            return present;
        } catch (SQLException ex) {
            throw new BioRuntimeException(ex);
        }
    }
}
