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

package org.biojava.bio.proteomics;

import junit.framework.TestCase;

import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolPropertyTable;

/**
 * <code>MassCalcTest</code> tests molecular mass calculation.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 */
public class MassCalcTest extends TestCase
{
    protected double monoH = 1.0078250;
    protected double  avgH = 1.00794;

    protected double monoO = 15.9949146;
    protected double  avgO = 15.9994;

    protected double monoAla = 71.03711;
    protected double  avgAla = 71.0788;

    protected SymbolList syms1;

    protected void setUp() throws IllegalSymbolException
    {
        syms1 = ProteinTools.createProtein("A");
    }

    public MassCalcTest(String name)
    {
        super(name);
    }

    /**
     * <code>testStaticGetMass</code> tests the static
     * <code>getMass</code> method.
     *
     * @exception IllegalSymbolException if an error occurs.
     */
    public void testStaticGetMass() throws IllegalSymbolException
    {
        double  mass = 0.0;
        double delta = 0.0;

        mass = MassCalc.getMass(syms1,
                                SymbolPropertyTable.MONO_MASS,
                                false);
        assertEquals(monoAla + (monoO + monoH) + monoH,
                     mass, delta);

        mass = MassCalc.getMass(syms1,
                                SymbolPropertyTable.MONO_MASS,
                                true);
        assertEquals(monoAla + (monoO + monoH) + monoH + monoH,
                     mass, delta);

        mass = MassCalc.getMass(syms1,
                                SymbolPropertyTable.AVG_MASS,
                                false);
        assertEquals(avgAla + (avgO + avgH) + avgH,
                     mass, delta);

        mass = MassCalc.getMass(syms1,
                                SymbolPropertyTable.AVG_MASS,
                                true);
        assertEquals(avgAla + (avgO + avgH) + avgH + avgH,
                     mass, delta);
    }

    /**
     * <code>testGetMass</code> tests the non-static
     * <code>getMass</code> method.
     *
     * @exception IllegalSymbolException if an error occurs.
     */
    public void testGetMass() throws IllegalSymbolException
    {
        double delta = 0.0;
        MassCalc mCalc;

        mCalc = new MassCalc(SymbolPropertyTable.MONO_MASS, false);
        assertEquals(monoAla + (monoO + monoH) + monoH,
                     mCalc.getMass(syms1, false), delta);
        assertEquals(monoAla + (monoO + monoH) + monoH + monoH,
                     mCalc.getMass(syms1, true), delta);

        mCalc = new MassCalc(SymbolPropertyTable.MONO_MASS, true);
        assertEquals(monoAla + (monoO + monoH) + monoH,
                     mCalc.getMass(syms1, false), delta);
        assertEquals(monoAla + (monoO + monoH) + monoH + monoH,
                     mCalc.getMass(syms1, true), delta);

        mCalc = new MassCalc(SymbolPropertyTable.AVG_MASS, false);
        assertEquals(avgAla + (avgO + avgH) + avgH,
                     mCalc.getMass(syms1, false), delta);
        assertEquals(avgAla + (avgO + avgH) + avgH + avgH,
                     mCalc.getMass(syms1, true), delta);

        mCalc = new MassCalc(SymbolPropertyTable.AVG_MASS, true);
        assertEquals(avgAla + (avgO + avgH) + avgH,
                     mCalc.getMass(syms1, false), delta);
        assertEquals(avgAla + (avgO + avgH) + avgH + avgH,
                     mCalc.getMass(syms1, true), delta);
    }

    /**
     * <code>testGetTermMass</code> which returns the terminal mass
     * being added by the instance.
     */
    public void testGetTermMass() throws IllegalSymbolException
    {
        double delta = 0.0;
        MassCalc mCalc;

        mCalc = new MassCalc(SymbolPropertyTable.MONO_MASS, false);
        assertEquals(monoO + monoH + monoH,
                     mCalc.getTermMass(), delta);

        mCalc = new MassCalc(SymbolPropertyTable.MONO_MASS, true);
        assertEquals(monoO + monoH + monoH + monoH,
                     mCalc.getTermMass(), delta);

        mCalc = new MassCalc(SymbolPropertyTable.AVG_MASS, false);
        assertEquals(avgO + avgH + avgH,
                     mCalc.getTermMass(), delta);

        mCalc = new MassCalc(SymbolPropertyTable.AVG_MASS, true);
        assertEquals(avgO + avgH + avgH + avgH,
                     mCalc.getTermMass(), delta);
    }

    /**
     * <code>testSetSymbolModification</code> which allows a user
     * defined mass to be set for a residue.
     *
     * @exception IllegalSymbolException if an error occurs.
     */
    public void testSetSymbolModification() throws IllegalSymbolException
    {
        double delta = 0.0;
        double newAla = 1.0;
        MassCalc mCalc;

        mCalc = new MassCalc(SymbolPropertyTable.MONO_MASS, false);
        mCalc.setSymbolModification('A', newAla);
        assertEquals(newAla + (monoO + monoH) + monoH,
                     mCalc.getMass(syms1, false), delta);
        assertEquals(newAla + (monoO + monoH) + monoH + monoH,
                     mCalc.getMass(syms1, true), delta);

        mCalc = new MassCalc(SymbolPropertyTable.MONO_MASS, true);
        mCalc.setSymbolModification('A', newAla);
        assertEquals(newAla + (monoO + monoH) + monoH,
                     mCalc.getMass(syms1, false), delta);
        assertEquals(newAla + (monoO + monoH) + monoH + monoH,
                     mCalc.getMass(syms1, true), delta);

        mCalc = new MassCalc(SymbolPropertyTable.AVG_MASS, false);
        mCalc.setSymbolModification('A', newAla);
        assertEquals(newAla + (avgO + avgH) + avgH,
                     mCalc.getMass(syms1, false), delta);
        assertEquals(newAla + (avgO + avgH) + avgH + avgH,
                     mCalc.getMass(syms1, true), delta);

        mCalc = new MassCalc(SymbolPropertyTable.AVG_MASS, true);
        mCalc.setSymbolModification('A', newAla);
        assertEquals(newAla + (avgO + avgH) + avgH,
                     mCalc.getMass(syms1, false), delta);
        assertEquals(newAla + (avgO + avgH) + avgH + avgH,
                     mCalc.getMass(syms1, true), delta);
    }
}
