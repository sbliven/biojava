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

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

import org.biojava.bio.program.sax.BlastLikeSAXParser;
import org.biojava.bio.search.SeqSimilaritySearchResult;
import org.biojava.bio.seq.StrandedFeature;

/**
 * <code>SSBindNCBItblastn2_0_11Test</code> tests object bindings for
 * Blast-like SAX events.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class SSBindNCBItblastn2_0_11Test extends SSBindCase
{
    public SSBindNCBItblastn2_0_11Test(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        setTopHitValues(326d, "Y00463",
                        1, 168, null,
                        31, 534, StrandedFeature.POSITIVE);

        setBotHitValues(34.8d, "U03645",
                        2, 77, null,
                        707, 919, StrandedFeature.POSITIVE);

        String blastOutputFileName = "ncbi_tblastn_2.0.11.out.gz";

        URL blastOutputURL = SSBindNCBItblastn2_0_11Test.class
            .getResource(blastOutputFileName);
        File blastOutputFile = new File(blastOutputURL.getFile());

        searchStream = new GZIPInputStream(new
            FileInputStream(blastOutputFile));

        // XMLReader -> (SAX events) -> adapter -> builder -> objects
        XMLReader reader = (XMLReader) new BlastLikeSAXParser();

        reader.setContentHandler(adapter);
        reader.parse(new InputSource(searchStream));
    }

    public void testBlastResultHitCount()
    {
        SeqSimilaritySearchResult result =
            (SeqSimilaritySearchResult) searchResults.get(0);

        assertEquals(175, result.getHits().size());
    }
}
