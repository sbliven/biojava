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

package org.biojava.bio.program.indexdb;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.db.SequenceDBLite;
import org.biojava.bio.seq.db.flat.FlatSequenceDB;
import org.biojava.bio.seq.io.SeqIOConstants;

public class IndexToolsTest extends TestCase
{
    protected String location;

    public IndexToolsTest(String name)
    {
        super(name);
    }

    protected void setUp() throws IOException
    {
        location = System.getProperty("java.io.tmpdir")
            + System.getProperty("file.separator")
            + "IndexToolsTest."
            + System.currentTimeMillis()
            + ".idx";
    }

    public void testIndexFastaDNA() throws Exception
    {
        SequenceDBLite db = createFastaDB(SeqIOConstants.DNA,
                                          new String [] { "dna1.fasta",
                                                          "dna2.fasta" });
        Sequence seq1 = db.getSequence("id1");
        assertEquals("gatatcgatt", seq1.seqString());
        Sequence seq2 = db.getSequence("id2");
        assertEquals("ggcgcgcgcg", seq2.seqString());
        Sequence seq3 = db.getSequence("id3");
        assertEquals("tttttcgatt", seq3.seqString());
        Sequence seq4 = db.getSequence("id4");
        assertEquals("ggttcgcgcg", seq4.seqString());
    }

    public void testIndexFastaRNA() throws Exception
    {
        SequenceDBLite db = createFastaDB(SeqIOConstants.RNA,
                                          new String [] { "rna1.fasta",
                                                          "rna2.fasta" });
        Sequence seq1 = db.getSequence("id1");
        assertEquals("gauaucgauu", seq1.seqString());
        Sequence seq2 = db.getSequence("id2");
        assertEquals("ggcgcgcgcg", seq2.seqString());
        Sequence seq3 = db.getSequence("id3");
        assertEquals("uuuuucgauu", seq3.seqString());
        Sequence seq4 = db.getSequence("id4");
        assertEquals("gguucgcgcg", seq4.seqString());
    }

    public void testIndexFastaProtein() throws Exception
    {
        SequenceDBLite db = createFastaDB(SeqIOConstants.AA,
                                          new String [] { "protein1.fasta",
                                                          "protein2.fasta" });
        Sequence seq1 = db.getSequence("id1");
        assertEquals("MTTSRGGGGG", seq1.seqString());
        Sequence seq2 = db.getSequence("id2");
        assertEquals("VVLLLLDDTN", seq2.seqString());
        Sequence seq3 = db.getSequence("id3");
        assertEquals("MVVVLNNGGG", seq3.seqString());
        Sequence seq4 = db.getSequence("id4");
        assertEquals("NGGDEEFDTN", seq4.seqString());
    }

    private SequenceDBLite createFastaDB(int alphabetId, String [] fileNames)
        throws FileNotFoundException, IOException, BioException
    {
        File [] files = new File [fileNames.length];

        for (int i = 0; i < files.length; i++)
        {
            URL seqURL = getClass().getResource(fileNames[i]);
            files[i] = new File(seqURL.getFile());
            
        }

        switch (alphabetId)
        {
            case SeqIOConstants.DNA:
                IndexTools.indexFastaDNA(new File(location), files);
                break;
            case SeqIOConstants.RNA:
                IndexTools.indexFastaRNA(new File(location), files);
                break;
            case SeqIOConstants.AA:
                IndexTools.indexFastaProtein(new File(location), files);
                break;

            default:
                throw new IllegalArgumentException("Unknown alphabet ID '"
                                                   + alphabetId
                                                   + "'");
        }


        return new FlatSequenceDB(location, "test");
    }
}
