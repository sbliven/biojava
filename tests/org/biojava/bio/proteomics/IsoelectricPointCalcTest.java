/*
 * IsoelectricPointCalcTest.java
 *
 * Created on April 2, 2007, 4:01 PM
 *
 */

package org.biojava.bio.proteomics;

import junit.framework.TestCase;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

/**
 *
 * @author George Waldon - testGetIsoelectricPointTest
 */
public class IsoelectricPointCalcTest extends TestCase {
    
    public void testGetIsoelectricPointTest() {

        try {
            SymbolList pro = ProteinTools.createProtein("hhhhhher");
            double p1 = IsoelectricPointCalc.getIsoelectricPoint(pro); 
            pro = ProteinTools.createProtein("h-hhhhher");
            double p2 = IsoelectricPointCalc.getIsoelectricPoint(pro);
            pro = ProteinTools.createProtein("hxhhhhher");
            double p3 = IsoelectricPointCalc.getIsoelectricPoint(pro);
            assertTrue(p1==p2);
            assertTrue(p1==p3);

            pro = ProteinTools.createProtein("cdehkry");
            double p4 = IsoelectricPointCalc.getIsoelectricPoint(pro);
            assert(p4==6.74);
            pro = ProteinTools.createProtein("da");
            p4 = IsoelectricPointCalc.getIsoelectricPoint(pro);
            assert(p4==3.80);
            pro = ProteinTools.createProtein("ad");
            p4 = IsoelectricPointCalc.getIsoelectricPoint(pro);
            assert(p4==4.30);
            
        } catch (IllegalSymbolException ex) {
            fail(ex.getMessage());
        } catch (BioException ex) {
            fail(ex.getMessage());
        }
    }
}
