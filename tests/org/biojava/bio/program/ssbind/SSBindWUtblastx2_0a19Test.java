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

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.biojava.bio.search.SeqSimilaritySearchHit;
import org.biojava.bio.search.SeqSimilaritySearchSubHit;
import org.biojava.bio.symbol.Alignment;

import org.biojava.bio.seq.StrandedFeature;

/**
 * <code>SSBindWUtblastx2_0a19Test</code> tests object bindings for
 * Blast-like SAX events.
 *
 * @author Keith James
 * @since 1.2
 */
public class SSBindWUtblastx2_0a19Test extends SSBindCase
{
    public SSBindWUtblastx2_0a19Test(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        setTopHitValues(4354d, "U51677",
                        1, 2575, StrandedFeature.UNKNOWN,
                        1, 2575, StrandedFeature.UNKNOWN);

        setBotHitValues(401d, "M64986",
                        1, 2575, StrandedFeature.UNKNOWN,
                        123, 1019, StrandedFeature.UNKNOWN);

        searchStream =
            new GZIPInputStream(new BufferedInputStream(getClass().getResourceAsStream("wu_tblastx_2.0a19.out.gz")));

        // XMLReader -> (SAX events) -> adapter -> builder -> objects
        XMLReader reader = (XMLReader) new BlastLikeSAXParser();

        reader.setContentHandler(adapter);
        reader.parse(new InputSource(searchStream));
    }

    public void testBlastResultHitCount()
    {
        SeqSimilaritySearchResult result =
            (SeqSimilaritySearchResult) searchResults.get(0);

        assertEquals(4, result.getHits().size());
    }
}
