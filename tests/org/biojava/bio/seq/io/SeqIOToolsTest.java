/**
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
package org.biojava.bio.seq.io;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;

import java.util.*;
import java.io.*;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * JUnit test for SeqIOTools objects
 * @author David Huen
 * @since 1.3
 */
public class SeqIOToolsTest extends TestCase
{

    public SeqIOToolsTest(String name)
    {
        super(name);
    }

    private boolean compareSymbolLists(SymbolList sl0, SymbolList sl1)
    {
        // compare lengths
        if (sl0.length() != sl1.length()) return false;

        // compare symbols
        for (int i=1; i <= sl0.length(); i++) {
            if (sl0.symbolAt(i) != sl1.symbolAt(i)) return false;
        }

        return true;
    }

    public void testDNAReaders()
    {
        /******* test readFastaDNA *********/

        // get access to the test file
        InputStream inputS = this.getClass().getResourceAsStream("/files/AY069118.fa");
        assertNotNull(inputS);

        // get SequenceIterator
        SequenceIterator seqI = SeqIOTools.readFastaDNA(
            new BufferedReader(new InputStreamReader(inputS)));

        // get sequence
        assertTrue(seqI.hasNext());
        Sequence fastaDNASeq = null;
        try {
            fastaDNASeq = seqI.nextSequence();
        }
        catch (BioException be) {}

        assertNotNull(fastaDNASeq);

        // is its length correct?
        assertEquals("Fasta sequence AY069118.fa had incorrect length", fastaDNASeq.length(), 1502);

        /******* test readGenbank **********/

        // get access to the test file
        inputS = this.getClass().getResourceAsStream("/files/AY069118.gb");
        assertNotNull(inputS);

        // get SequenceIterator
        seqI = SeqIOTools.readGenbank(
            new BufferedReader(new InputStreamReader(inputS)));

        // get sequence
        assertTrue(seqI.hasNext());
        Sequence genbankDNASeq = null;
        try {
            genbankDNASeq = seqI.nextSequence();
        }
        catch (BioException be) {}

        assertNotNull(genbankDNASeq);

        // is its length correct?
        assertEquals("Genbank sequence AY069118.gb had incorrect length", genbankDNASeq.length(), 1502);

        // compare with fasta reference
        assertTrue(compareSymbolLists(fastaDNASeq, genbankDNASeq));

        /******* test readEmblNucleotide **********/

        // get access to the test file
        inputS = this.getClass().getResourceAsStream("/files/AY069118.em");
        assertNotNull(inputS);

        // get SequenceIterator
        seqI = SeqIOTools.readEmblNucleotide(
            new BufferedReader(new InputStreamReader(inputS)));

        // get sequence
        assertTrue(seqI.hasNext());
        Sequence emblDNASeq = null;
        try {
            emblDNASeq = seqI.nextSequence();
        }
        catch (BioException be) {}

        assertNotNull(emblDNASeq);

        // is its length correct?
        assertEquals("Genbank sequence AY069118.em had incorrect length", emblDNASeq.length(), 1502);

        // compare with fasta reference
        assertTrue(compareSymbolLists(fastaDNASeq, emblDNASeq));
    }

    public void testProteinReaders()
    {
        /******* test readFastaProtein *********/

        // get access to the test file
        InputStream inputS = this.getClass().getResourceAsStream("/files/AAL039263.fa");
        assertNotNull(inputS);

        // get SequenceIterator
        SequenceIterator seqI = SeqIOTools.readFastaProtein(
            new BufferedReader(new InputStreamReader(inputS)));

        // get sequence
        assertTrue(seqI.hasNext());
        Sequence fastaProteinSeq = null;
        try {
            fastaProteinSeq = seqI.nextSequence();
        }
        catch (BioException be) {}

        assertNotNull(fastaProteinSeq);

        // is its length correct?
        assertEquals("Fasta sequence AAL39263.fa had incorrect length", fastaProteinSeq.length(), 370);

        /******* test readGenbank **********/

        // get access to the test file
        inputS = this.getClass().getResourceAsStream("/files/AAL039263.gb");
        assertNotNull(inputS);

        // get SequenceIterator
        seqI = SeqIOTools.readGenpept(
            new BufferedReader(new InputStreamReader(inputS)));

        // get sequence
        assertTrue(seqI.hasNext());
        Sequence genbankProteinSeq = null;
        try {
            genbankProteinSeq = seqI.nextSequence();
        }
        catch (BioException be) {}

        assertNotNull(genbankProteinSeq);

        // is its length correct?
        assertEquals("Genbank sequence AAL39263.gb had incorrect length", genbankProteinSeq.length(), 370);

        // compare with fasta reference
        assertTrue(compareSymbolLists(fastaProteinSeq, genbankProteinSeq));

        /******* test readEmblNucleotide **********/

        // get access to the test file
        inputS = this.getClass().getResourceAsStream("/files/AAL039263.em");
        assertNotNull(inputS);

        // get SequenceIterator
        seqI = SeqIOTools.readSwissprot(
            new BufferedReader(new InputStreamReader(inputS)));

        // get sequence
        assertTrue(seqI.hasNext());
        Sequence emblProteinSeq = null;
        try {
            emblProteinSeq = seqI.nextSequence();
        }
        catch (BioException be) {}

        assertNotNull(emblProteinSeq);

        // is its length correct?
        assertEquals("Genbank sequence AAL39263.em had incorrect length", emblProteinSeq.length(), 370);

        // compare with fasta reference
        assertTrue(compareSymbolLists(fastaProteinSeq, emblProteinSeq));
    }

    public void testBigDNA()
    {

        /****** read big sequence as fasta ***********/
        // get access to the test file
        InputStream inputS = this.getClass().getResourceAsStream("/files/NC_004432.fa");
        assertNotNull(inputS);

        // get SequenceIterator
        SequenceIterator seqI = SeqIOTools.readFastaDNA(
            new BufferedReader(new InputStreamReader(inputS)));

        // get sequence
        assertTrue(seqI.hasNext());
        Sequence bigFastaDNASeq = null;
        try {
            bigFastaDNASeq = seqI.nextSequence();
        }
        catch (BioException be) {}

        assertNotNull(bigFastaDNASeq);

        // is its length correct?
        assertEquals("Fasta sequence NC_004432.fa had incorrect length", bigFastaDNASeq.length(), 1358633);

        /****** read big sequence as genbank ***********/
        // get access to the test file
        inputS = this.getClass().getResourceAsStream("/files/NC_004432.gb");
        assertNotNull(inputS);

        // get SequenceIterator
        seqI = SeqIOTools.readGenbank(
            new BufferedReader(new InputStreamReader(inputS)));

        // get sequence
        assertTrue(seqI.hasNext());
        Sequence bigGenbankDNASeq = null;
        try {
            bigGenbankDNASeq = seqI.nextSequence();
        }
        catch (BioException be) {}

        assertNotNull(bigGenbankDNASeq);

        // is its length correct?
        assertEquals("Genbank sequence NC_004432.gb had incorrect length", bigGenbankDNASeq.length(), 1358633);

        // compare with fasta reference
        assertTrue(compareSymbolLists(bigFastaDNASeq, bigGenbankDNASeq));      
    }

    public void testIdentifyFormat()
    {
        // Test formats which may be in any alphabet
        String [] formats = new String [] { "raw", "fasta",
                                            "nbrf", "ig",
                                            "embl", "genbank",
                                            "refseq", "gcg",
                                            "gff" };

        int [] formatIds = new int [] { SeqIOConstants.RAW, SeqIOConstants.FASTA,
                                        SeqIOConstants.NBRF, SeqIOConstants.IG,
                                        SeqIOConstants.EMBL, SeqIOConstants.GENBANK,
                                        SeqIOConstants.REFSEQ, SeqIOConstants.GCG,
                                        SeqIOConstants.GFF };

        String [] alphas = new String [] { "dna", "rna",
                                           "aa", "protein" };

        int [] alphaIds = new int [] { SeqIOConstants.DNA, SeqIOConstants.RNA,
                                       SeqIOConstants.AA, SeqIOConstants.AA };

        for (int i = 0; i < formats.length; i++)
        {
            for (int j = 0; j < alphas.length; j++)
            {
                assertEquals((formatIds[i] | alphaIds[j]),
                             SeqIOTools.identifyFormat(formats[i], alphas[j]));
            }
        }

        // Test formats which throw exceptions unless in a specific
        // alphabet
        assertEquals(SeqIOConstants.SWISSPROT,
                     SeqIOTools.identifyFormat("swissprot", "protein"));
        assertEquals(SeqIOConstants.SWISSPROT,
                     SeqIOTools.identifyFormat("swissprot", "aa"));
        assertEquals(SeqIOConstants.SWISSPROT,
                     SeqIOTools.identifyFormat("swiss", "protein"));
        assertEquals(SeqIOConstants.SWISSPROT,
                     SeqIOTools.identifyFormat("swiss", "aa"));

        assertEquals(SeqIOConstants.GENPEPT,
                     SeqIOTools.identifyFormat("genpept", "protein"));
        assertEquals(SeqIOConstants.GENPEPT,
                     SeqIOTools.identifyFormat("genpept", "aa"));

        assertEquals(SeqIOConstants.PDB,
                     SeqIOTools.identifyFormat("pdb", "protein"));
        assertEquals(SeqIOConstants.PDB,
                     SeqIOTools.identifyFormat("pdb", "aa"));

        assertEquals(SeqIOConstants.PHRED,
                     SeqIOTools.identifyFormat("phred", "dna"));
    }

    // creates a suite
    public static Test suite()
    {
        TestSuite suite = new TestSuite(SeqIOToolsTest.class);

        return suite;
    }

    // harness for tests
    public static void main(String [] args)
    {
        junit.textui.TestRunner.run(suite());
    }
}
