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

import java.io.BufferedInputStream;
import java.util.zip.GZIPInputStream;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

import org.biojava.bio.program.sax.BlastLikeSAXParser;
import org.biojava.bio.search.SeqSimilaritySearchResult;
import org.biojava.bio.seq.StrandedFeature;

/**
 * <code>SSBindNCBIblastn2_2_3Test</code> tests object bindings for
 * Blast-like SAX events.
 *
 * @author Keith James
 * @since 1.2
 */
public class SSBindNCBIblastn2_2_3Test extends SSBindCase
{
    public SSBindNCBIblastn2_2_3Test(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        queryID = "ECGLTA01_3";
        databaseID = "embl_other-1; embl_other-2; embl_other-3; embl_other-4;";

        setTopHitValues(2545D, "ECGLTA01",
                        1, 1284, StrandedFeature.POSITIVE,
                        1091, 2374, StrandedFeature.NEGATIVE);

        setBotHitValues(44.1D, "AC068733",
                        167, 188, StrandedFeature.POSITIVE,
                        57355, 57376, StrandedFeature.POSITIVE);

        searchStream =
             new GZIPInputStream(new BufferedInputStream(getClass().getResourceAsStream("ncbi_blastn_2.2.3.out.gz")));

        // XMLReader -> (SAX events) -> adapter -> builder -> objects
        XMLReader reader = (XMLReader) new BlastLikeSAXParser();

        reader.setContentHandler(adapter);
        reader.parse(new InputSource(searchStream));
    }

    public void testBlastResultHitCount()
    {
        SeqSimilaritySearchResult result =
            (SeqSimilaritySearchResult) searchResults.get(0);

        assertEquals(126, result.getHits().size());
    }
}
