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
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Map;
import java.util.HashMap;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.DummySymbolList;
import org.biojava.utils.cache.Cache;
import org.biojava.utils.cache.FixedSizeCache;
import org.biojava.utils.cache.CacheMap;
import org.biojava.utils.cache.AutoClearCacheMap;
import org.biojava.utils.AbstractChangeable;

import javax.sql.ConnectionPoolDataSource;

public class GadflyDB
{
    ConnectionPoolDataSource poolDB;

    // links name of top-level sequences to the
    // assemblies themselves that underlie them.
    Map topLevelSeq;

    // cache for sequences
    CacheMap seqCache;

    // cache for FeatureHolders
    CacheMap fhCache;

    // cache for Features
    CacheMap featureCache;

    public GadflyDB(ConnectionPoolDataSource poolDB)
    {
        this.poolDB = poolDB;

        topLevelSeq = new HashMap();
        seqCache = new AutoClearCacheMap(100, 1000); // clear every 100 secs, doing up to 1000 clearances
        fhCache = new AutoClearCacheMap(100, 1000); // clear every 100 secs, doing up to 1000 clearances
        featureCache = new AutoClearCacheMap(100, 1000); // clear every 100 secs, doing up to 1000 clearances
        System.out.println("created caches");
    }

/*
    private void initialise()
        throws SQLException
    {
        // get a connection
        Connection conn = getConnection();

        // identify and recover top-level sequences
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT seq.id, seq.name FROM seq, seq_feature WHERE seq.name=seq_feature.name AND src_seq_id is null"
            );

        while (rs.next()) {
            // process each top-level sequence
            int seq_id = rs.getInt(1);
            String name = rs.getString(2);
            Sequence seq = new GFTopLevelSequence(this, seq_id, name);
            topLevelSeq.put(seq.getName(), seq);
        }

        conn.close();
    }
*/
    Connection getConnection()
        throws SQLException
    {
        return poolDB.getPooledConnection().getConnection();
    }

    void cacheFeatureHolder(int sf_id, GFFeatureHolder fh)
    {
        fhCache.put(new Integer(sf_id), fh);
    }

    GFFeatureHolder getCachedFeatureHolder(int sf_id)
    {
        // get the appropriate FH
        // if unavailable recreate it

        return (GFFeatureHolder) fhCache.get(new Integer(sf_id));
    }

    GFFeatureHolder getFeatureHolder(int seq_id)
    {
        return null;
    }

    void cacheFeature(int sf_id, GFFeature f)
    {
        featureCache.put(new Integer(sf_id), f);
    }

    GFFeature getCachedFeature(int sf_id)
    {
        return (GFFeature) featureCache.get(new Integer(sf_id));
    }

    GFFeature createFeature(int sf_id)
    {
        try {
        GFFeature f = new GFResultSetFeature(this, sf_id);

        cacheFeature(sf_id, f);
        return f;
        }
        catch (IllegalArgumentException iae) { return null; }
    }

    // retrieves the specified symbolList
    // if there is an invalid specification
    // for that sequence id,
    // it throws InvalidSequenceIDException.
    // if that id exists but has no sequence
    // it returns an EmptySymbolList of appropriate type and length
    public Sequence getSequence(int seq_id)
        throws InvalidSequenceIDException, GadflyException
    {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        Sequence seq;

        try {
            // is it available from the cache?
            seq = (Sequence) seqCache.get(new Integer(seq_id));

            if (seq == null) {

                // go get the symbols for this symbolList
                conn = getConnection();

                stmt = conn.createStatement();
                rs = stmt.executeQuery("SELECT name, molecule_type, length, residues FROM seq WHERE id=" + seq_id + ";");            

                if (rs.next()) {
                    String name = rs.getString(1);

                    String molecule_type = rs.getString(2);
                    if (rs.wasNull()) throw new GadflyException("molecule_type undefined");

                    String lengthStr = rs.getString(3);
                    if (rs.wasNull()) throw new GadflyException("no length of sequence!");
                    int length = Integer.parseInt(lengthStr);

                    String residues = rs.getString(4);
                    if (rs.wasNull()) residues = null;

                    // turn it into a SymbolList
                    if (molecule_type.equals("dna")) {
                        // return dna
                        if (residues != null)
                            seq = DNATools.createDNASequence(residues, name);
                        else
                            seq = new SimpleSequence(new DummySymbolList(DNATools.getDNA(), length), name, "", Annotation.EMPTY_ANNOTATION);
                    }
                    else if (molecule_type.equals("protein")) {
                        if (residues != null)
                            seq = ProteinTools.createProteinSequence(residues, name);
                        else
                            seq = new SimpleSequence(new DummySymbolList(ProteinTools.getAlphabet(), length), name, "", Annotation.EMPTY_ANNOTATION);
                    }
                    else if (molecule_type.equals("rna")) {
                        if (residues != null)
                            seq = RNATools.createRNASequence(residues, name);
                        else
                            seq = new SimpleSequence(new DummySymbolList(RNATools.getRNA(), length), name, "", Annotation.EMPTY_ANNOTATION);
                    }
                    else
                        throw new GadflyException("unknown molecule_type: " + molecule_type);;
                }
                else {
                    throw new InvalidSequenceIDException(null, seq_id, null);
                }
            }
        }
        catch (SQLException se) { throw new GadflyException(se); }
        catch (IllegalSymbolException ise) { throw new GadflyException(ise); }
        finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            }
            catch (SQLException se) { /* this is ridiculous! */ }
        }
        
        // return result
        seqCache.put(new Integer(seq_id), seq);
        return seq;
    }
}
