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
import java.sql.SQLException;

/**
 * @author Thomas Down
 * @author Matthew Pocock
 */
public abstract class DBHelper {
    public static DBHelper getDBHelperForURL(String ourURL) {
	if (ourURL.startsWith("jdbc:")) {
	    ourURL = ourURL.substring(5);
	}
  if(!Character.isLetter(ourURL.charAt(0))) {
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
}
