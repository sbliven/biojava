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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This is a <code>DBHelper</code> that provides support for MySQL
 * databases.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */
public class MySQLDBHelper extends DBHelper {

    // Inherit docs
    public int getInsertID(Connection conn,
			   String table,
			   String columnName)
	throws SQLException
    {
        Statement st = conn.createStatement();
	ResultSet rs = st.executeQuery("select last_insert_id()");
	int id = -1;
	if (rs.next()) {
	    id = rs.getInt(1);
	}
        rs.close();
	st.close();
	
	if (id < 1) {
	    throw new SQLException("Couldn't get last_insert_id()");
	}
	return id;
    }

    // Inherit docs
    public DeleteStyle getDeleteStyle() {
	return DELETE_MYSQL4;
    }
}
