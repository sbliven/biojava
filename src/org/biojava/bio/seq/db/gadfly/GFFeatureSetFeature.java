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

import org.biojava.bio.ontology.Term;
import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.Sequence;
import org.biojava.utils.cache.Cache;
import org.biojava.utils.cache.CacheReference;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeVetoException;

/**
 * Implementation of a Gadfly feature that
 * has a FeatureHolder backed by a ResultSet.
 * <p>
 * the class is able to initialise itself
 * from the database from just the seq_id alone.
 */
public class GFFeatureSetFeature
    extends GFFeatureSetFeatureHolder
    implements StrandedFeature, GFFeature
{
    GadflyDB parentDB;

    /**
     * columns from seq_feature
     */
    protected int sf_id; // seq_feature id for this feature
    protected String name;
    protected int start; // zero-based coordinates for feature
    protected int end;
    protected int annotation_id;
    protected int seq_id;
    protected int src_seq_id;
    protected String type;

    protected int produced_by_sf_id;

    /**
     * Biojava Feature attributes
     */
    protected String source;
    protected Location loc;
    protected StrandedFeature.Strand strand;

    GFFeatureSetFeature(GadflyDB parentDB, int sf_id)
        throws IllegalArgumentException
    {
        // set up the superclass to use the specified SQL statement.
        super(parentDB,
            "SELECT id FROM sf_produces_sf WHERE produced_by_sf_id=" + sf_id + ";");

        this.parentDB = parentDB;
        this.sf_id = sf_id;

        initialiseSelf();
    }

    private void initialiseSelf()
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            // get connection
            conn = parentDB.getConnection();

            // retrieve own details
            stmt = conn.createStatement();
            rs = stmt.executeQuery(
                "SELECT name,start,end,type,annotation_id,seq_id,src_seq_id from seq_feature where id=" + sf_id + ";");

            name = rs.getString(1);
            start = rs.getInt(2);
            end = rs.getInt(3);
            type = rs.getString(4);
            seq_id = rs.getInt(5);
            src_seq_id = rs.getInt(6);

            loc = GadflyTools.locFromZeroBased(start, end);
            strand = GadflyTools.strandFromZeroBased(start, end);
        }
        catch (SQLException se) {}
        finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            }
            catch (SQLException se) {}
        }
    }

    /**
     * Feature methods
     */
    public Feature.Template makeTemplate()
    {
        // do nothing for now
        return new Feature.Template();
    }

    public Location getLocation()
    {
        return loc;
    }

    public void setLocation(Location loc)
        throws ChangeVetoException
    {
        throw new ChangeVetoException("not permitted in this implementation");
    }

    public StrandedFeature.Strand getStrand()
    {
        return strand;
    }


    public void setStrand(StrandedFeature.Strand strand)
        throws ChangeVetoException
    {
        throw new ChangeVetoException("not permitted in this implementation");
    }

    public String getSource()
    {
        return "";
    }

    public Term getSourceTerm()
    {
        // implement later
        return null;
    }

    public void setSourceTerm(Term t)
        throws ChangeVetoException
    {
        throw new ChangeVetoException("not permitted in this implementation");
    }

    public void setSource(String source)
        throws ChangeVetoException
    {
        throw new ChangeVetoException("not permitted in this implementation");
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
        throws ChangeVetoException
    {
        throw new ChangeVetoException("not permitted in this implementation");
    }

    public Term getTypeTerm()
    {
        // implement later
        return null;
    }

    public void setTypeTerm(Term t)
       throws ChangeVetoException
    {
        throw new ChangeVetoException("not permitted in this implementation");
    }

    public FeatureHolder getParent()
    {
        // implement later
        return null;
    }

    public Sequence getSequence()
    {
        // sort this out later
        return null;
    }

    public SymbolList getSymbols()
    {
        return null;
    }

    /**
     * Annotatable support
     */
    public Annotation getAnnotation()
    {
        return Annotation.EMPTY_ANNOTATION;
    }

    /**************************
     * class-specific methods *
     **************************/

    public int getID() { return sf_id; }

    protected GFFeatureHolder createFeatureHolder()
    {
        return new GFFeatureHolder(parentDB);
    }
}
