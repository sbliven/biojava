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

package org.biojava.bio.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.db.HashSequenceDB;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.AlphabetManager;

/**
 * <code>SequenceDBSearchResultTest</code> tests the behaviour of
 * <code>SequenceDBSearchResult</code>.
 *
 * @author Keith James
 */
public class SequenceDBSearchResultTest extends TestCase
{
    private SeqSimilaritySearchResult r1;
    private SeqSimilaritySearchResult r2;

    private SequenceDB database;
    private Map        parameters;
    private SymbolList query;

    private String querySeqTokens = "TRYPASNDEF";

    public SequenceDBSearchResultTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        database   = new HashSequenceDB("test");
        parameters = new HashMap();

        SymbolTokenization tp = ProteinTools.getAlphabet().getTokenization("token");

        query = new SimpleSymbolList(tp, querySeqTokens);

        r1 = new SequenceDBSearchResult(query,
                                        database,
                                        parameters,
                                        new ArrayList(),
                                        Annotation.EMPTY_ANNOTATION);

        r2 = new SequenceDBSearchResult(query,
                                        database,
                                        parameters,
                                        new ArrayList(),
                                        Annotation.EMPTY_ANNOTATION);
    }

    public void testEquals()
    {
        assertEquals(r1, r1);
        assertEquals(r2, r2);
        assertEquals(r1, r2);
        assertEquals(r2, r1);
    }

    public void testSequenceDB()
    {
        assertEquals(r1.getSequenceDB(), database);
    }

    public void testSearchParameters()
    {
        assertEquals(r1.getSearchParameters(), parameters);
    }

    public void testQuerySequence()
    {
        assertEquals(r1.getQuerySequence(), query);
    }

    public void testAnnotation()
    {
        assertEquals(((SequenceDBSearchResult) r1).getAnnotation(),
                     Annotation.EMPTY_ANNOTATION);
    }
}
