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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.SymbolList;

/**
 * JUnit test for SymbolListCharSequence.
 *
 * @author Ido Tamir
 * @author Keith James
 */
public class SymbolListCharSequenceTest extends TestCase
{
    public SymbolListCharSequenceTest(String name)
    {
        super(name);
    }

    public void testCharAt() throws Exception
    {
        CharSequence charSeq =
            new SymbolListCharSequence(DNATools.createDNA("GCAT"));

        assertEquals('g', charSeq.charAt(0));
        assertEquals('c', charSeq.charAt(1));
        assertEquals('a', charSeq.charAt(2));
        assertEquals('t', charSeq.charAt(3));
    }

    public void testLength() throws Exception
    {
        CharSequence charSeq =
            new SymbolListCharSequence(DNATools.createDNA("GCAT"));

        assertEquals(4, charSeq.length());
    }

    public void testSubSequence() throws Exception
    {
        CharSequence charSeq =
            new SymbolListCharSequence(DNATools.createDNA("GCAT"));

        assertEquals("g",  charSeq.subSequence(0, 1));
        assertEquals("t",  charSeq.subSequence(3, 4));
        assertEquals("gc", charSeq.subSequence(0, 2));
        assertEquals("ca", charSeq.subSequence(1, 3));
        assertEquals("at", charSeq.subSequence(2, 4));
    }
}
