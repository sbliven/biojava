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

package org.biojava.bio.molbio;

import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;

public class RestrictionEnzymeManagerTest extends TestCase
{
    public RestrictionEnzymeManagerTest(String name)
    {
        super(name);
    }

    public void testGetAllEnzymes()
    {
        Set allEnz = RestrictionEnzymeManager.getAllEnzymes();
        // Is this number correct for REBASE withrefm.206?
        assertEquals(3361, allEnz.size());
    }

    public void testGetEnzyme() throws BioException
    {
        RestrictionEnzyme ecoRi = RestrictionEnzymeManager.getEnzyme("EcoRI");
        assertEquals("EcoRI", ecoRi.getName());

        try
        {
            RestrictionEnzyme invalid = RestrictionEnzymeManager.getEnzyme("xxxx");
        }
        catch (BioException be)
        {
            return;
        }

        fail("Expected BioException");
    }

    public void testGetIsoschizomers() throws BioException
    {
        Set isoAaaI = RestrictionEnzymeManager.getIsoschizomers("AaaI");
        assertEquals(10, isoAaaI.size());        

        RestrictionEnzyme   xmaIII = RestrictionEnzymeManager.getEnzyme("XmaIII");
        RestrictionEnzyme   bseX3I = RestrictionEnzymeManager.getEnzyme("BseX3I");
        RestrictionEnzyme    bsoDI = RestrictionEnzymeManager.getEnzyme("BsoDI");
        RestrictionEnzyme    bstZI = RestrictionEnzymeManager.getEnzyme("BstZI");
        RestrictionEnzyme     eagI = RestrictionEnzymeManager.getEnzyme("EagI");
        RestrictionEnzyme    eclXI = RestrictionEnzymeManager.getEnzyme("EclXI");
        RestrictionEnzyme   eco52I = RestrictionEnzymeManager.getEnzyme("Eco52I");
        RestrictionEnzyme senPT16I = RestrictionEnzymeManager.getEnzyme("SenPT16I");
        RestrictionEnzyme    tauII = RestrictionEnzymeManager.getEnzyme("TauII");
        RestrictionEnzyme  tsp504I = RestrictionEnzymeManager.getEnzyme("Tsp504I");

        assertTrue(isoAaaI.contains(xmaIII));
        assertTrue(isoAaaI.contains(bseX3I));
        assertTrue(isoAaaI.contains(bsoDI));
        assertTrue(isoAaaI.contains(bstZI));
        assertTrue(isoAaaI.contains(eagI));
        assertTrue(isoAaaI.contains(eclXI));
        assertTrue(isoAaaI.contains(eco52I));
        assertTrue(isoAaaI.contains(senPT16I));
        assertTrue(isoAaaI.contains(tauII));
        assertTrue(isoAaaI.contains(tsp504I));

        try
        {
            Set invalid = RestrictionEnzymeManager.getIsoschizomers("xxxx");
        }
        catch (BioException be)
        {
            return;
        }

        fail("Expected BioException");
    }

    public void testGetNCutters()
    {
        Set all6Cutters = RestrictionEnzymeManager.getNCutters(6);
        // Is this number correct for REBASE withrefm.206?
        assertEquals(1804, all6Cutters.size());

        for (Iterator ei = all6Cutters.iterator(); ei.hasNext();)
        {
            RestrictionEnzyme e = (RestrictionEnzyme) ei.next();
            assertEquals(6, e.getRecognitionSite().length());
        }
    }

    public void testGetPatterns() throws BioException
    {
        RestrictionEnzyme ecoRi = RestrictionEnzymeManager.getEnzyme("EcoRI");
        Pattern [] pat = RestrictionEnzymeManager.getPatterns(ecoRi);

        assertEquals("ga{2}t{2}c", pat[0].pattern());
        assertEquals("ga{2}t{2}c", pat[1].pattern());

        SymbolList site = null;
        try
        {
            site = DNATools.createDNA("a");
        }
        catch (IllegalSymbolException ise)
        {
            throw new BioError(ise, "Internal error in test");
        }

        RestrictionEnzyme custom = null;
        try
        {
            custom = new RestrictionEnzyme("custom", site, 1, 1);
        }
        catch (IllegalAlphabetException iae)
        {
            throw new BioError(iae, "Internal error in test");
        }

        try
        {
            pat = RestrictionEnzymeManager.getPatterns(custom);
        }
        catch (BioException be)
        {
            return;
        }

        fail("Expected BioException");
    }
}
