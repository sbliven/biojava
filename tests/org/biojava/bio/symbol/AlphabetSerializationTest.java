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
 */


package org.biojava.bio.symbol;

import java.io.*;
import junit.framework.*;

import org.biojava.bio.program.phred.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.*;
import java.util.*;

/**
 * <p>Title: AlphabetSerializationTest</p>
 * <p>Description: Tests Alphabets after Serialization</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: AgResearch</p>
 * @author Mark Schreiber
 * @version 1.0
 */

public class AlphabetSerializationTest extends TestCase {
    IntegerAlphabet integer;
    IntegerAlphabet.SubIntegerAlphabet subint;
    DoubleAlphabet doub;
    FiniteAlphabet phred;
    FiniteAlphabet dna;
    SimpleAlphabet custom;

    public AlphabetSerializationTest(String name){
        super(name);
    }

    protected void setUp() throws java.lang.Exception {
        super.setUp();

        integer = IntegerAlphabet.getInstance();
        subint = IntegerAlphabet.getSubAlphabet(20,99);
        doub = DoubleAlphabet.getInstance();
        phred = PhredTools.getPhredAlphabet();
        dna = DNATools.getDNA();

        //make a custom alphabet

        AtomicSymbol s0 = AlphabetManager.createSymbol("foo",Annotation.EMPTY_ANNOTATION);
        AtomicSymbol s1 = AlphabetManager.createSymbol("goo",Annotation.EMPTY_ANNOTATION);
        AtomicSymbol s2 = AlphabetManager.createSymbol("hoo",Annotation.EMPTY_ANNOTATION);
        Set set = new HashSet(3);
        set.add(s0); set.add(s1); set.add(s2);
        custom = new SimpleAlphabet(set,"custom_test");
    }

    protected void tearDown() throws java.lang.Exception {
        super.tearDown();
    }

    public void testIntegerSerialization()throws Exception{
        IntegerAlphabet integer2 = (IntegerAlphabet)serialize(integer);

        assertTrue(integer == integer2);
        assertTrue(integer.getInstance() == integer2.getInstance());
        assertTrue(integer.equals(integer2));
        assertTrue(integer.getSymbol(12) == integer2.getSymbol(12));
        assertTrue(integer.getSymbol(12).equals(integer2.getSymbol(12)));
        assertTrue(!(integer.getSymbol(12).equals(integer2.getSymbol(13))));

        //test tokenization
        int[] ints = {25,26,27,28,29};
        SymbolList sl = integer.fromArray(ints);
        SymbolList sl2 = integer2.fromArray(ints);
        assertEquals(sl.seqString(),sl2.seqString());
    }

    public void testDoubleSerialization()throws Exception{
        DoubleAlphabet doub2 = (DoubleAlphabet)serialize(doub);

        assertTrue(doub == doub2);
        assertTrue(doub.getInstance() == doub2.getInstance());
        assertTrue(doub.equals(doub2));
        assertTrue(doub.getSymbol(12.0) == doub2.getSymbol(12.0));
        assertTrue(doub.getSymbol(12.0).equals(doub2.getSymbol(12.0)));
        assertTrue(!(doub.getSymbol(12.0).equals(doub2.getSymbol(13.0))));
    }

    public void testSubInt()throws Exception{
        IntegerAlphabet.SubIntegerAlphabet subint2 =
            (IntegerAlphabet.SubIntegerAlphabet)serialize(subint);
        assertTrue(subint == subint2);
        assertTrue(subint.equals(subint2));
        assertTrue(integer.getSymbol(80)== subint2.getSymbol(80));
        assertTrue(subint.getSymbol(80)== subint2.getSymbol(80));
        assertTrue(subint.getSymbol(80).equals(subint2.getSymbol(80)));
        assertTrue(subint.getSymbol(81)!= subint2.getSymbol(80));
        assertTrue(subint.size() == subint2.size());
    }

    public void testDNASerialization()throws Exception{
        FiniteAlphabet dna2 = (FiniteAlphabet)serialize(dna);
        assertTrue(dna == dna2);
        assertTrue(dna.size() == dna2.size());
        assertTrue(equalSymbols(dna, dna2));

        //test tokenization
        assertTrue(dna.getTokenization("token").equals(dna2.getTokenization("token")));
    }

    public void testPhredSerialization()throws Exception{
        //this tests a specific instance of a compound alphabet
        FiniteAlphabet phred2 = (FiniteAlphabet)serialize(phred);
        assertTrue(phred == phred2);
        assertTrue(phred.size() == phred2.size());
        assertTrue(equalSymbols(phred, phred2));
    }

    public void testCustom() throws Exception{
        SimpleAlphabet custom2 = (SimpleAlphabet)custom;
        assertTrue(custom == custom2);
        assertTrue(custom.size() == custom.size());
        assertTrue(equalSymbols(custom, custom2));
    }

    private Object serialize(Object alpha) throws Exception{
        File f = File.createTempFile("biojava",".tmp");
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
        oos.writeObject(alpha);
        oos.flush();
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    private boolean equalSymbols(FiniteAlphabet alpha, FiniteAlphabet alpha2)
    {
        AlphabetIndex index1 = AlphabetManager.getAlphabetIndex(alpha);
        AlphabetIndex index2 = AlphabetManager.getAlphabetIndex(alpha2);

        int aSize = alpha.size();
        for (int i = 0; i < aSize; i++)
        {
            if (index1.symbolForIndex(i) != index2.symbolForIndex(i))
                return false;
        }

        return true;
    }
}
