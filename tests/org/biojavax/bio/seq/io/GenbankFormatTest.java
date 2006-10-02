package org.biojavax.bio.seq.io;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;

import junit.framework.TestCase;

import org.biojava.bio.seq.SequenceIterator;
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

    public void testCanReadWhatIsWritten() {
    	// Read a genbank file
        InputStream inStream = this.getClass().getResourceAsStream("/files/AY069118.gb");
        BufferedReader input = new BufferedReader(new InputStreamReader(inStream));
        SymbolTokenization dna = RichSequence.IOTools.getDNAParser();
        Namespace defaultNs = RichObjectFactory.getDefaultNamespace();
        RichSequence sequence = null;
        try {
            RichStreamReader reader = new RichStreamReader(input, new GenbankFormat(), dna, RichSequenceBuilderFactory.FACTORY, defaultNs);
            sequence = reader.nextRichSequence();
            assertNotNull(sequence);
        } catch (Exception e) {
        	e.printStackTrace();
        	fail("Unexpected exception: "+e);
        }

        // Write the file to an in-memory buffer
        OutputStream output = new ByteArrayOutputStream();
		RichSequenceFormat genbank = new GenbankFormat();
		RichStreamWriter seqsOut = new RichStreamWriter(output, genbank);
		SequenceIterator seqIterator = new RichSequence.IOTools.SingleRichSeqIterator(sequence);
		try {
			seqsOut.writeStream(seqIterator, null);
		} catch (IOException e) {
        	fail("Unexpected exception: "+e);
		}

		// Re-read the generated output
		String newContent = output.toString();
		input = new BufferedReader(new StringReader(newContent));
		RichSequence rereadSeq = null;
        try {
            RichStreamReader reader = new RichStreamReader(input, new GenbankFormat(), dna, RichSequenceBuilderFactory.FACTORY, defaultNs);
            rereadSeq = reader.nextRichSequence();
            assertNotNull(rereadSeq);
        } catch (Exception e) {
        	e.printStackTrace();
        	fail("Unexpected exception: "+e);
        }
        assertEquals(sequence.getAccession(), rereadSeq.getAccession());
        assertEquals(sequence.getName(), rereadSeq.getName());
        assertEquals(sequence.seqString(), rereadSeq.seqString());
    }
}
