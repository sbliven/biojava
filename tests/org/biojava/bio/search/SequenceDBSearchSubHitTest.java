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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.io.TokenParser;
import org.biojava.bio.symbol.Alignment;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleAlignment;

/**
 * <code>SequenceDBSearchSubHitTest</code> tests the behaviour of
 * <code>SequenceDBSearchSubHit</code>.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 */
public class SequenceDBSearchSubHitTest extends TestCase
{
    private SeqSimilaritySearchSubHit sh1;
    private SeqSimilaritySearchSubHit sh2;

    private Alignment                 al1;
    private Alignment                 al2;

    private double            score = 100.0d;
    private double           eValue = 1e-10d;
    private double           pValue = 1e-10d;
    private int          queryStart = 1;
    private int            queryEnd = 10;
    private Strand   querySeqStrand = StrandedFeature.POSITIVE;
    private int        subjectStart = 2;
    private int          subjectEnd = 8;
    private Strand subjectSeqStrand = StrandedFeature.POSITIVE;

    private String     subjectSeqID = "subjectID";
    private String   querySeqTokens = "TRYPASNDEF";
    private String subjectSeqTokens = "-RYPASND--";

    public SequenceDBSearchSubHitTest(String name)
    {
        super(name);
    }

    protected void setUp()
    {
        TokenParser tp = new TokenParser(ProteinTools.getAlphabet());

        try
        {
            Map labelMap1 = new HashMap();
            labelMap1.put(SeqSimilaritySearchSubHit.QUERY_LABEL,
                          tp.parse(querySeqTokens));
            labelMap1.put(subjectSeqID,
                          tp.parse(subjectSeqTokens));

            al1 = new SimpleAlignment(labelMap1);

            Map labelMap2 = new HashMap();
            labelMap2.put(SeqSimilaritySearchSubHit.QUERY_LABEL,
                          tp.parse(querySeqTokens));
            labelMap2.put(subjectSeqID,
                          tp.parse(subjectSeqTokens));

            al2 = new SimpleAlignment(labelMap2);
        }
        catch (IllegalSymbolException ise)
        {
            ise.printStackTrace();
        }

        sh1 = new SequenceDBSearchSubHit(score,
                                         eValue,
                                         pValue,
                                         queryStart,
                                         queryEnd,
                                         querySeqStrand,
                                         subjectStart,
                                         subjectEnd,
                                         subjectSeqStrand,
                                         al1);

        sh2 = new SequenceDBSearchSubHit(score,
                                         eValue,
                                         pValue,
                                         queryStart,
                                         queryEnd,
                                         querySeqStrand,
                                         subjectStart,
                                         subjectEnd,
                                         subjectSeqStrand,
                                         al2);
    }

    public void testEquals()
    {
        assertEquals(sh1, sh1);
        assertEquals(sh2, sh2);
        assertEquals(sh1, sh2);
        assertEquals(sh2, sh1);
    }

    public void testScores()
    {
        assertEquals(sh1.getScore(),  100.0d, 0.0d);
        assertEquals(sh1.getEValue(), 1e-10d, 0.0d);
        assertEquals(sh1.getPValue(), 1e-10d, 0.0d);
    }

    public void testQuery()
    {
        assertEquals(sh1.getQueryStart(), 1);
        assertEquals(sh1.getQueryEnd(),  10);
        assertEquals(sh1.getQueryStrand(), StrandedFeature.POSITIVE);
    }

    public void testSubject()
    {
        assertEquals(sh1.getSubjectStart(), 2);
        assertEquals(sh1.getSubjectEnd(),   8);
        assertEquals(sh1.getSubjectStrand(), StrandedFeature.POSITIVE);
    }
}
