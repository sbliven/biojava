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

public interface DBHelper {
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

    public int getInsertID(Connection conn,
			   String table,
			   String columnName)
	throws SQLException;

    public DeleteStyle getDeleteStyle();
}
