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

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

import javax.sql.ConnectionPoolDataSource;

import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.RangeLocation;
import org.biojava.bio.symbol.PointLocation;
import org.biojava.bio.symbol.Location;

public class GadflyTools
{
/*
    public GadflyDB instantiateGadflyDB(URL url)
        throws SQLException
    {
        // create a DataSource
        // that corresponds to the specified url
        ConnectionPoolDataSource poolDS = new MysqlConnectionPoolDataSource();
        poolDS.setURL(url);

        return new GadflyDB(poolDS);
    }
*/
    public static Location locFromZeroBased(int start, int end)
    {
        // to convert, find the lower of the two
        // and increment by one.
        int min = Math.min(start, end) + 1;
        int max = Math.max(start, end);

        if (min == max)
            return new PointLocation(min);
        else
            return new RangeLocation(min, max);
    }

    public static StrandedFeature.Strand strandFromZeroBased(int start, int end)
    {
        if (start > end)
            return StrandedFeature.POSITIVE;
        else if (end < start)
            return StrandedFeature.NEGATIVE;
        else
            return StrandedFeature.UNKNOWN;
    }
}

