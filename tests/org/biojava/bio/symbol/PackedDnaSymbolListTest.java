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
package org.biojava.bio.symbol;

import org.biojava.bio.seq.*;
import org.biojava.bio.dist.*;

import java.util.*;
import junit.framework.TestCase;

public class PackedDnaSymbolListTest extends TestCase
{
    /*
     * tests should be done with full cache lines,
     * a part-filled cache line with only one symbol,
     * ditto with full storage unit and ditto with 
     * full storage unit plus one symbol
     * for a byte-sized storage unit and a 16 symbol
     * cache, appropriate lengths will be 16, 17, 18 and 19.
     */

    // SymbolList lengths to run tests at.
    int testLengths[] = {16,17,18,19};
    // number of times to repeat each test to deal with chance
    // matches in last symbol.
    int noRepeats = 8;

    public PackedDnaSymbolListTest(String string)
    {
        super(string);
    }

    private SymbolList createRandomSymbolList(int length)
        throws Exception
    {
        FiniteAlphabet alpha = DNATools.getDNA();
        Distribution random = new UniformDistribution(alpha);

        List rl = new ArrayList();
        while (rl.size() < length) {
            rl.add(random.sampleSymbol());
        }
        return new SimpleSymbolList(alpha, rl);
    }
    
    private boolean compareSymbolLists(SymbolList list0, SymbolList list1)
    {
        // lists must be identical in length
        int length = list0.length();
        if (length != list1.length()) return false;

        // compare symbol lists across length
        for (int i =1; i <= length; i++) {
            if (list0.symbolAt(i) != list1.symbolAt(i)) return false;
        }

        return true;
    }

    /**
     * runs repeated tests for the constructor
     * that takes a SymbolList argument
     */
    private boolean runSymbolListConstructorTest()
        throws Exception
    {
        for (int i=0; i < testLengths.length; i++) {

            // setup test for specified length
            int length = testLengths[i];

            for (int j=0; j < noRepeats; j++ ) {
                // create random sequence
                SymbolList sl = createRandomSymbolList(length);

                // create PackedDnaSymbolList from it
                PackedDnaSymbolList packedSymbolList = new PackedDnaSymbolList(sl);

                if (!compareSymbolLists(sl, packedSymbolList)) return false;
            }
        }
        return true;
    }

    /**
     * tests the the constructor
     * that takes a SymbolList argument.
     */
    public void testSymbolListConstructor()
        throws Exception
    {
        assertTrue(runSymbolListConstructorTest());
    }


    private boolean runByteBufferConstructor()
        throws Exception
    {
        // create random sequence
        SymbolList sl = createRandomSymbolList(200);

        // create packed symbol list
        PackedDnaSymbolList packedSymbolList = new PackedDnaSymbolList(sl);

        // create new packed list from the byte buffer of this list
        SymbolList arraySymbolList = new PackedDnaSymbolList(packedSymbolList.length(), packedSymbolList.getArray());

        // validate new list
        for (int i =1; i <= packedSymbolList.length(); i++) {
            if (sl.symbolAt(i) != packedSymbolList.symbolAt(i)) return false;
        }

        return true;
    }

    public void testByteBufferConstructor()
        throws Exception
    {
        assertTrue(runByteBufferConstructor());
    }

}
