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
        SequenceDBLite db = createFastaDB(SeqIOConstants.DNA, "dna",
                                          new String [] { "dna1.fasta",
                                                          "dna2.fasta" });
        Sequence seq1 = db.getSequence("id1");
        assertEquals("gatatcgatt", seq1.seqString());
        Sequence seq2 = db.getSequence("id2");
        assertEquals("ggcgcgcgcg", seq2.seqString());
        Sequence seq3 = db.getSequence("id3");
        assertEquals("ccccccccta", seq3.seqString());
        Sequence seq4 = db.getSequence("id4");
        assertEquals("tttttcgatt", seq4.seqString());
        Sequence seq5 = db.getSequence("id5");
        assertEquals("ggttcgcgcg", seq5.seqString());
        Sequence seq6 = db.getSequence("id6");
        assertEquals("nnnnnnttna", seq6.seqString());
    }

    public void testIndexFastaRNA() throws Exception
    {
        SequenceDBLite db = createFastaDB(SeqIOConstants.RNA, "rna",
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
        SequenceDBLite db = createFastaDB(SeqIOConstants.AA, "protein",
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

    public void testIndexEmblDNA() throws Exception
    {
        File [] files = getDBFiles(new String [] { "part1.embl",
                                                   "part2.embl" });
        IndexTools.indexEmblDNA(new File(location), files);

        SequenceDBLite db = new FlatSequenceDB(location, "embl");

        Sequence seq1 = db.getSequence("A16SRRNA");
        assertEquals(1497, seq1.length());
        Sequence seq2 = db.getSequence("A16STM112");
        assertEquals(1346, seq2.length());
        Sequence seq3 = db.getSequence("A16STM146");
        assertEquals(1352, seq3.length());

        Sequence seq4 = db.getSequence("AY080928");
        assertEquals(557, seq4.length());
        Sequence seq5 = db.getSequence("AY080929");
        assertEquals(556, seq5.length());
        Sequence seq6 = db.getSequence("AY080930");
        assertEquals(557, seq6.length());
    }

    public void testIndexGenbankDNA() throws Exception
    {
        File [] files = getDBFiles(new String [] { "part1.gb",
                                                   "part2.gb" });
        IndexTools.indexGenbankDNA(new File(location), files);

        SequenceDBLite db = new FlatSequenceDB(location, "genbank");

        Sequence seq1 = db.getSequence("");
        assertEquals(, seq1.length());
        Sequence seq2 = db.getSequence("");
        assertEquals(, seq2.length());
        Sequence seq3 = db.getSequence("");
        assertEquals(, seq3.length());

        Sequence seq4 = db.getSequence("");
        assertEquals(, seq4.length());
        Sequence seq5 = db.getSequence("");
        assertEquals(, seq5.length());
        Sequence seq6 = db.getSequence("");
        assertEquals(, seq6.length());
    }

    public void testIndexSwissprot() throws Exception
    {
        File [] files = getDBFiles(new String [] { "part1.swiss",
                                                   "part2.swiss" });
        IndexTools.indexSwissprot(new File(location), files);

        SequenceDBLite db = new FlatSequenceDB(location, "swiss");

        Sequence seq1 = db.getSequence("104K_THEPA");
        assertEquals(924, seq1.length());
        Sequence seq2 = db.getSequence("108_LYCES");
        assertEquals(102, seq2.length());
        Sequence seq3 = db.getSequence("10KD_VIGUN");
        assertEquals(75, seq3.length());
        Sequence seq4 = db.getSequence("110K_PLAKN");
        assertEquals(296, seq4.length());
        Sequence seq5 = db.getSequence("11S3_HELAN");
        assertEquals(493, seq5.length());
        Sequence seq6 = db.getSequence("11SB_CUCMA");
        assertEquals(480, seq6.length());
    }

    private SequenceDBLite createFastaDB(int alphabetId, String name,
                                         String [] fileNames)
        throws Exception
    {
        File [] files = getDBFiles(fileNames);

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

        return new FlatSequenceDB(location, name);
    }

    private File [] getDBFiles(String [] fileNames) throws Exception
    {
        File [] files = new File [fileNames.length];

        for (int i = 0; i < files.length; i++)
        {
            URL seqURL = getClass().getResource(fileNames[i]);
            files[i] = new File(seqURL.getFile());
        }

        return files;
    }            
}
