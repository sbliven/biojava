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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.biojava.bio.BioRuntimeException;


/**
 * This is a <code>DBHelper</code> that provides support for Oracle
 * databases.
 *
 * @author Len Trigg
 * @author Eric Haugen
 */
public class OracleDBHelper extends DBHelper {

    private final JoinStyle mJoinStyle;

    public OracleDBHelper(Connection connection) {
        JoinStyle joinStyle = JOIN_GENERIC;
        try {
            DatabaseMetaData metadata = connection.getMetaData();
            String version = metadata.getDatabaseProductVersion();
            if ((version != null) && version.startsWith("Oracle8")) {
                joinStyle = JOIN_ORACLE8;
            }
        } catch (SQLException e) {
            System.err.println("Exception getting DatabaseMetaData:" +  e.getMessage());
            // Stick with generic style
        }
        mJoinStyle = joinStyle;
    }


    // Inherit docs
    public JoinStyle getJoinStyle() {
        return mJoinStyle;
    }


    // Inherit docs
    public int getInsertID(Connection conn, String table, String columnName) throws SQLException {
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            // We assume that the Oracle BioSQL schema uses sequences for the autoincrement fields,
            // one sequence per table.
            rs = st.executeQuery("select " + table + "_pk_seq.CURRVAL from dual");
            int id = -1;
            if (rs.next()) {
                id = rs.getInt(1);
            }
            
            if (id < 1) {
                throw new SQLException("Couldn't get last insert id");
            }
            return id;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException se) { }
            if (st != null) try { st.close(); } catch (SQLException se) { }
        }
    }

    
    // Inherit docs
    public boolean containsTable(DataSource ds, String tablename) {
        if (ds == null) {
            throw new NullPointerException("Require a datasource.");
        }
        if ((tablename == null) || (tablename.length() == 0)) {
            throw new IllegalArgumentException("Invalid table name given");
        } 
        //System.err.println("Checking for table existence: " + tablename);
        Connection conn = null;
        try {
            boolean present;
            conn = ds.getConnection();
            PreparedStatement ps = conn.prepareStatement("select rownum from " + tablename + " where rownum < 1");
            try {
              ps.executeQuery();
              present = true;
            } catch (SQLException ex) {
                //System.err.println("Table " + tablename + " does not exist.");
                present = false;
            } finally {
                ps.close();
                if (conn != null) {
                    conn.close();
                }
            }
            return present;
        } catch (SQLException ex) {
            throw new BioRuntimeException(ex);
        }
    }

}
