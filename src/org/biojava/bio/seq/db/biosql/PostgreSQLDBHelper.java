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

import java.sql.*;
import java.util.*;

public class PostgreSQLDBHelper implements DBHelper {
    public int getInsertID(Connection conn,
			   String table,
			   String columnName)
	throws SQLException
    {
	StringBuffer sequenceName = new StringBuffer();

	/* Old style

	int totalLength = table.length() + columnName.length();

	if (totalLength > 26 && table.length() > 13) {
	    sequenceName.append(table.substring(0, 13));
	} else {
	    sequenceName.append(table);
	}
	sequenceName.append('_');
	if (totalLength > 26 && columnName.length() > 13) {
	    sequenceName.append(columnName.substring(0, 13));
	} else {
	    sequenceName.append(columnName);
	}
	sequenceName.append("_seq");

	*/

	sequenceName.append(table);
	sequenceName.append("_pkey_seq");

	Statement st = conn.createStatement();
	ResultSet rs = st.executeQuery("select currval('" + sequenceName.toString() + "')");
	int id = -1;
	if (rs.next()) {
	    id = rs.getInt(1);
	}
	st.close();
	
	if (id < 1) {
	    throw new SQLException("Couldn't get last_insert_id()");
	}
	return id;
    }
}
