package org.biojavax.bio.seq.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;

/**
 * @author Bubba Puryear
 */
public class GenbankFormatTest extends TestCase {
    private GenbankFormat gbFormat;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() {
        this.gbFormat = new GenbankFormat();
    }

    public void testGenbankWithNoAccession() {
        InputStream inStream = this.getClass().getResourceAsStream("/files/NoAccession.gb");
        BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
        SymbolTokenization tokenization = RichSequence.IOTools.getDNAParser();
        Namespace namespace = RichObjectFactory.getDefaultNamespace();
        SimpleRichSequenceBuilder builder = new SimpleRichSequenceBuilder();
        try {
            assertFalse(this.gbFormat.readRichSequence(br, tokenization, builder, namespace));
            RichSequence sequence = builder.makeRichSequence();
            assertNotNull(sequence);
            assertEquals("NoAccess", sequence.getAccession());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected exception: "+e);
        }
    }
}
