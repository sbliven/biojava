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

public class SSBindWUblastx2_0a19Test extends SSBindCase
{
    public SSBindWUblastx2_0a19Test(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        super.topHitScore   = 311d;
        super.topHitSeqID   = "sp|P09429|HMG1_HUMAN";
        super.topHitQStart  = 4;
        super.topHitQEnd    = 2322;
        super.topHitQStrand = StrandedFeature.POSITIVE;
        super.topHitSStart  = 1;
        super.topHitSEnd    = 214;
        super.topHitSStrand = StrandedFeature.POSITIVE;

        String blastOutputFileName = "wu_blastx_2.0a19.out.gz";

        URL blastOutputURL = SSBindWUblastx2_0a19Test.class
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

        assertEquals(4, result.getHits().size());
    }
}
