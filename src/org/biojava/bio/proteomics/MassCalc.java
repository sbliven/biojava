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

import java.util.HashMap;
import java.util.Iterator;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.symbol.*;

/**
 * <code>MassCalc</code> calculates the mass of peptides which for our
 * purposes are <code>SymbolList</code>s which contain
 * <code>Symbol</code>sfrom the protein <code>Alphabet</code>. It uses
 * the mono-isotopic and average-isotopic masses identical to those
 * specified at www.micromass.co.uk
 *
 * @author M. Jones
 * @author Keith James (minor changes)
 */
public class MassCalc {
    //Possibly these should be put in a configurable table somewhere
    /**
     * Constant value of Carbon monoisotopic mass
     */
    public static final double Cmono = 12.00000;

    /**
     * Constant value of  Hydrogen monoisotopic mass
     */
    public static final double Hmono = 1.0078250;

    /**
     * Constant value of Nitrogen monoisotopic mass
     */
    public static final double Nmono = 14.0030740;

    /**
     * Constant value of Oxygen monoisotopic mass
     */
    public static final double Omono = 15.9949146;

    /**
     * Constant value of Carbon average mass
     */
    public static final double Cavg = 12.011;

    /**
     * Constant value of Hydrogen average mass
     */
    public static final double Havg = 1.00794;

    /**
     * Constant value of Nitrogen average mass
     */
    public static final double Navg = 14.00674;

    /**
     * Constant value of Oxygen average mass
     */
    public static final double Oavg = 15.9994;
    
    //Save values here so that modifications are not global
    private HashMap mSymbolPropertyHash;

    /*
     * If instance methods are being used this will store the
     * isotopically and MH_PLUS correct terminal mass to be created.
     * Need to be able to handle N and C term mods in the future.
     */
     private double termMass;
    
    /*
     * Creates new MassCalc.
     * @param isotopicType The type of isotopes to calculate. Either
     * mono isotopic or average isotopic. It realy is the name of
     * SymbolProperty table. Ex. SymbolPropertyTable.AVG_MASS or
     * SymbolPropertyTable.MONO_MASS */


    /**
     * Creates a new <code>MassCalc</code>.
     *
     * @param isotopicType a <code>String</code>. The type of isotopes
     * to calculate. Either mono isotopic or average
     * isotopic. Acceptable values are
     * <code>SymbolPropertyTable.AVG_MASS</code> or
     * <code>SymbolPropertyTable.MONO_MASS</code>.
     * @param MH_PLUS a <code>boolean</code>.
     */
    public MassCalc(String isotopicType, boolean MH_PLUS) {
        //Calculate hydroxyl mass
        termMass = calcTermMass(isotopicType, MH_PLUS); 
        mSymbolPropertyHash = new HashMap();

        SymbolPropertyTable symbolPropertyTable = 
            ProteinTools.getSymbolPropertyTable(isotopicType);
        
        Iterator symbolList = ProteinTools.getAlphabet().iterator();

        for(; symbolList.hasNext(); )
        {
            Symbol sym = (Symbol) symbolList.next();
            try {
                // System.out.println(sym.getName() + ":" + toke.tokenizeSymbol(sym));
                try {
                    Double value =
                        new Double(symbolPropertyTable.getDoubleValue(sym));
                    mSymbolPropertyHash.put(sym, value);
                } catch (NullPointerException npe) {
                    //This seems to be happening when a amino acid is 
                    //in the property table that doesn't have a residue value
                }
            }
            catch (IllegalSymbolException ise)
            {
                throw new BioError(ise, "Error setting properties for Symbol " + sym);
            }
        }
    }

    /**
     * Use this to set a post translational modification for the
     * <code>Symbol</code> represented by this character. It will only
     * affect the current <code>MassCalc</code> instance and will not
     * affect the static method.
     *
     * @param symbolToken a <code>char</code> representing a
     * <code>Symbol</code>.
     * @param mass a <code>double</code> to be the new mass of the
     * residue.
     *
     * @exception IllegalSymbolException if the <code>Symbol</code> is
     * not recognised.
     */
    public void setSymbolModification(char symbolToken, double mass) 
        throws IllegalSymbolException
    {
        Iterator list = ProteinTools.getAlphabet().iterator();
        SymbolTokenization toke;

        try {
            toke = ProteinTools.getAlphabet().getTokenization("token");
        } catch (BioException ex) {
            throw new BioError(ex, "Expected a tokenization");
        }

        Symbol sym = toke.parseToken("" + symbolToken);
        mSymbolPropertyHash.put(sym, new Double(mass));
    }
 
    /* May need something like this if a user wants to add a non standard
     * amino acid to a specific location on a peptide. Not very usefull
     * for woking with large batches of sequences. Currently this is not implemented
     * @param residue
     * @param mass
     * @throws IllegalSymbolException
     */

    
    /**
     * <code>addSymbolModification</code> may be needed if a user
     * wants to add a non standard amino acid to a specific location
     * on a peptide. <strong>Currently this is not implemented</strong>.
     *
     * @param residue a <code>Symbol</code>.
     * @param mass a <code>double</code>.
     *
     * @exception IllegalSymbolException if the <code>Symbol</code> is
     * not recognised.
     */
    public void addSymbolModification(Symbol residue, double mass) 
        throws IllegalSymbolException
    {
        //   mSymbolPropertyTable.setDoubleProperty(residue, Double.toString(mass));
    }

    /**
     * <code>getMass</code> calculates the mass of this peptide. This
     * only works for the values in the ResidueProperties.xml
     * configuration file. It is probably slightly faster than the
     * instance method, but it does not handle post-translational
     * modifications.
     *
     * @param proteinSeq a <code>SymbolList</code> whose mass is to be
     * calculated. This should use the protein alphabet.
     * @param isotopicType a <code>String</code> The type of isotopes
     * to calculate. Either mono isotopic or average
     * isotopic. Acceptable values are
     * <code>SymbolPropertyTable.AVG_MASS</code> or
     * <code>SymbolPropertyTable.MONO_MASS</code>.
     * @param MH_PLUS a <code>boolean</code> true if the value needed
     * is the MH+ mass.
     *
     * @return a <code>double</code> mass of the peptide.
     *
     * @exception IllegalSymbolException if the
     * <code>SymbolList</code> contains illegal
     * <code>Symbol</code>s.
     */
    public static final double getMass(SymbolList proteinSeq, 
                                       String isotopicType, 
                                       boolean MH_PLUS)
        throws IllegalSymbolException
    {
        if (! proteinSeq.getAlphabet().getName().equals("PROTEIN")) {
            throw new IllegalSymbolException("The SymbolList was not using the protein alphabet");
        }

        double pepMass = 0.0;

        SymbolPropertyTable sPT = 
            ProteinTools.getSymbolPropertyTable(isotopicType);

        for (Iterator it = proteinSeq.iterator(); it.hasNext(); ) {
            pepMass += sPT.getDoubleValue((Symbol) it.next());
        }

        //Calculate hydroxyl mass
        double termMass = calcTermMass(isotopicType, MH_PLUS); 

        if (pepMass != 0.0) {
            pepMass += termMass;
        }

        return pepMass;
    }
    
    /**
     * Get the Mass of this peptide. Use this if you want to set fixed
     * modifications and have created an instance of MassCalc. The
     * value is calculated using the value of MH_PLUS defined in the
     * constructor. The static method may be faster.
     *
     * @param proteinSeq The sequence for mass calculation
     *
     * @return The mass of the sequence */
    public double getMass(SymbolList proteinSeq)
        throws IllegalSymbolException
    {
        if (! proteinSeq.getAlphabet().getName().equals("PROTEIN")){
            throw new IllegalSymbolException("The SymbolList was not using the protein alphabet");
        }

        double pepMass = 0.0;

        HashMap symbolPropertyMap = getSymbolPropertyMap();

        for (Iterator it = proteinSeq.iterator(); it.hasNext(); ) {
            Double mass = (Double) symbolPropertyMap.get((Symbol) it.next());
            pepMass += mass.doubleValue();
        }
        pepMass += getTermMass();

        return pepMass;
    }
    
    private HashMap getSymbolPropertyMap()
    {
        return mSymbolPropertyHash;// = ProteinTools.getSymbolPropertyTable(name);
    }

    /**
     * <code>getTermMass</code> returns the terminal mass being used
     * by the instance methods.
     *
     * @return a <code>double</code> mass.
     */
    public double getTermMass(){
        return termMass;
    }

    private static double calcTermMass(String isotopicType, boolean MH_PLUS) {
        double termMass = 0.0;
        if (isotopicType.equals(SymbolPropertyTable.AVG_MASS)) {
            //Add the C-terminal OH and N-Term H
            termMass += Havg + Oavg + Havg;
            //Add the extra H
            if (MH_PLUS) {
                termMass += Havg;	
            }
        }
        else if (isotopicType.equals(SymbolPropertyTable.MONO_MASS)) {
            //Add the C-terminal OH and N-Term H
            termMass += Hmono + Omono + Hmono;
            //Add the extra H
            if (MH_PLUS) {
                termMass += Hmono;	
            }
        }
        return termMass;
    }
}
