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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An abstract class that uses a ResultSet
 * implement a FeatureHolder.
 *
 * @atuhor David Huen
 */
public abstract class GFResultSetFeatureHolder
    extends AbstractFeatureHolder
{
    GadflyDB parentDB;
    String sql;

    private class ResultSetIterator implements Iterator
    {
        // this is a first iteration of this class
        // later on it might be more sensible to
        // read the entire seq_feature row and forward
        // it to reduce the network traffic and
        // consequent loss of performance

        ResultSet rs;
        private boolean haveValue = false; // I have read a value that has not been consumed
        private boolean hasNext = true;
        private boolean closed = false; // this iterator is closed
        private int curr_sf_id = -1;

        private ResultSetIterator(ResultSet rs)
        {
            this.rs = rs;
        }

        public boolean hasNext()
        {
            if (!haveValue) {
                haveValue = true;

                hasNext=hasNext();

                if (hasNext) {
                    try {
                        curr_sf_id = rs.getInt(1);
                    }
                    catch (SQLException se) {
                        // some problem has arisen
                        // just indicate end of data
                        haveValue = true;
                        hasNext = false;
                    }
                }
                else {
                    // the Iterator has completed read thru'
                    // close all associated SQL objects
                    try {
                        close();
                    }
                    catch (SQLException se) { return false; }
                }
            }

            return hasNext;
        }

        public Object next()
            throws NoSuchElementException
        {
            try {
                if (!haveValue) hasNext();

                if (hasNext) {
                    // return the object of given id
                    haveValue = false;
                    return parentDB.createFeature(curr_sf_id);
                }
                else {
                    throw new NoSuchElementException();
                }

            }
            catch (NullPointerException npe) {
                return null;
            }
        }

        public void remove()
           throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }

        private synchronized void close()
            throws SQLException
        {
            if (!closed) {
                rs.close();
                rs.getStatement().close();
                rs.getStatement().getConnection().close();
            }
        }

        public void finalize()
        {
            try {
                close();
            }
            catch (SQLException se) {}
        }
    }

    public GFResultSetFeatureHolder(GadflyDB parentDB, String sql)
    {
        super();

        this.parentDB = parentDB;
        this.sql = sql;
    }


    /**
     * this is the key method in this class.
     * We access the sf_produces_sf table here
     * to retrieve the child features.
     */
    public Iterator features()
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        // I hope the connection gets recovered to the pool
        // when it goes out of scope.
        try {
            conn = parentDB.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
        }
        catch (SQLException se) {
            // its gone wrong, bail out
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();

                return null;
            }
            catch (SQLException s) { return null;}
        }

        return new ResultSetIterator(rs);
    }

    protected GFFeatureHolder createFeatureHolder()
    {
        return new GFFeatureHolder(parentDB);
    }

}

