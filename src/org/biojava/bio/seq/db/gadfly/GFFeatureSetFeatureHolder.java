/**
 * BioJava development code
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

package org.biojava.bio.seq.db.gadfly;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * This FeatureHolder class is intended for
 * use as a base class for FeatureHolder
 * implementations where SQL is used
 * to populate the FeatureHolder.
 * <p>
 * It uses the Set-based mechanism provided
 * by GFFeatureHolder.
 *
 * @author David Huen
 */
public class GFFeatureSetFeatureHolder
    extends GFFeatureHolder
{
    private String sql;

    public GFFeatureSetFeatureHolder(GadflyDB parentDB, String sql)
    {
        super(parentDB);

        this.sql = sql;

        populateFeatureHolder();
    }

    /**
     * The method executes a supplied SQL string
     * that populates the FeatureHolder on execution.
     * The SQL statement must return the seq_feature
     * id.
     */
    private void populateFeatureHolder()
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = parentDB.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            // now we copy everything into our FeatureHolder
            while (rs.next()) {
                int sf_id = rs.getInt(1);
                addFeatureID(sf_id);
            }

            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        }
        catch (SQLException se) {
            // its gone wrong, bail out
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            }
            catch (SQLException s) {}
        }
    }

}
