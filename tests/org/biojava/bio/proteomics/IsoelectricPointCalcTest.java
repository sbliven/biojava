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
            double p1 = IsoelectricPointCalc.getIsoelectricPoint(pro,false,false);  
            pro = ProteinTools.createProtein("h-hhhhher");
            double p2 = IsoelectricPointCalc.getIsoelectricPoint(pro,false,false);
            pro = ProteinTools.createProtein("hxhhhhher");
            double p3 = IsoelectricPointCalc.getIsoelectricPoint(pro,false,false);
            assertTrue(p1==p2);
            assertTrue(p1==p3);
            
            //will fail in BinarySearch
            //pro = ProteinTools.createProtein("hhhhhh");
            //IsoelectricPointCalc.getIsoelectricPoint(pro,false,false);
            
        } catch (IllegalSymbolException ex) {
            fail(ex.getMessage());
        } catch (BioException ex) {
            fail(ex.getMessage());
        }
    }
}
