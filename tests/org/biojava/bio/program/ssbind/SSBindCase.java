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

package org.biojava.bio.program.ssbind;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.InputSource;

import junit.framework.TestCase;

import org.biojava.bio.search.SeqSimilaritySearchResult;
import org.biojava.bio.search.SeqSimilaritySearchHit;
import org.biojava.bio.search.SequenceDBSearchResult;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.bio.seq.db.DummySequenceDB;
import org.biojava.bio.seq.db.DummySequenceDBInstallation;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.seq.db.SequenceDBInstallation;

public class SSBindCase extends TestCase
{
    protected SequenceDB             queryDB;
    protected SequenceDBInstallation dbInstallation;

    protected SeqSimilarityAdapter adapter;
    protected InputStream          searchStream;
    protected List                 searchResults;

    protected double   topHitScore;
    protected String   topHitSeqID;
    protected int     topHitQStart;
    protected int       topHitQEnd;
    protected Strand topHitQStrand;
    protected int     topHitSStart;
    protected int       topHitSEnd;
    protected Strand topHitSStrand;

    public SSBindCase(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        queryDB        = new DummySequenceDB("query");
        dbInstallation = new DummySequenceDBInstallation();
        searchResults  = new ArrayList();

        // Set builder to build into a List
        BlastLikeSearchBuilder builder =
            new BlastLikeSearchBuilder(searchResults);

        // Set the holder for query sequences and databases
        builder.setQuerySeqHolder(queryDB);
        builder.setSubjectDBInstallation(dbInstallation);

        // Adapter from SAX -> search result construction interface
        adapter = new SeqSimilarityAdapter();

        // Set the handler which will instantiate result objects
        adapter.setSearchContentHandler(builder);
    }

    protected void tearDown() throws Exception
    {
        searchStream.close();
    }

    public void testResultCount()
    {
        assertEquals(1, searchResults.size());
    }

    public void testResultGetQuerySequence() throws Exception
    {
        SeqSimilaritySearchResult result =
            (SeqSimilaritySearchResult) searchResults.get(0);

        assertEquals(queryDB.getSequence(""), result.getQuerySequence());
    }

    public void testResultGetSequenceDB()
    {
        SeqSimilaritySearchResult result =
            (SeqSimilaritySearchResult) searchResults.get(0);

        assertEquals(dbInstallation.getSequenceDB(""), result.getSequenceDB());
    }

    public void testResultGetAnnotation()
    {
        SequenceDBSearchResult result =
            (SequenceDBSearchResult) searchResults.get(0);

        assertEquals(2, result.getAnnotation().keys().size());
    }

    public void testTopHit()
    {
        SeqSimilaritySearchResult result =
            (SeqSimilaritySearchResult) searchResults.get(0);

        SeqSimilaritySearchHit hit =
            (SeqSimilaritySearchHit) result.getHits().get(0);

        assertEquals(topHitScore, hit.getScore(), 0.0);
        assertEquals(topHitSeqID, hit.getSequenceID());

        assertEquals(topHitQStart, hit.getQueryStart());
        assertEquals(topHitQEnd,   hit.getQueryEnd());
        assertSame(topHitQStrand,  hit.getQueryStrand());

        assertEquals(topHitSStart, hit.getSubjectStart());
        assertEquals(topHitSEnd,   hit.getSubjectEnd());
        assertSame(topHitSStrand,  hit.getSubjectStrand());
    }

    protected void setTopHitValues(double score, String id,
                                   int qStart, int qEnd, Strand qStrand,
                                   int sStart, int sEnd, Strand sStrand)
    {
        topHitScore   = score;
        topHitSeqID   = id;
        topHitQStart  = qStart;
        topHitQEnd    = qEnd;
        topHitQStrand = qStrand;
        topHitSStart  = sStart;
        topHitSEnd    = sEnd;
        topHitSStrand = sStrand;
    }
}
