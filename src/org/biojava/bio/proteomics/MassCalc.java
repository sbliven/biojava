/*
 * MassCalc.java
 *
 * Created on January 11, 2001, 4:18 PM
 */

package org.biojava.bio.proteomics;


import org.biojava.bio.seq.*;
//import java.io.*;

import org.biojava.bio.symbol.*;


import java.util.Iterator;
import java.util.HashMap;

/**
 *
 * @author  mjones
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

    
    /** Creates new MassCalc */
    public MassCalc() {
    }
    
     /** Creates new MassCalc
     * @param isotopicType The type of isotopes to calculate. Either mono isotopic
     * or average isotopic. It realy is the name of SymbolProperty
     * table. Ex. SymbolPropertyTable.AVG_MASS or SymbolPropertyTable.MONO_MASS
     */
    public MassCalc(String isotopicType) {
        mSymbolPropertyHash = new HashMap();
        
        SymbolPropertyTable symbolPropertyTable = 
                ProteinTools.getSymbolPropertyTable(isotopicType);
        
        Iterator symbolList = ProteinTools.getAlphabet().symbols().iterator();
        
        for(; symbolList.hasNext(); )
        {
            
            Symbol sym = (Symbol)symbolList.next();
            try{
                Double value = new Double(symbolPropertyTable.getDoubleValue(sym));
                mSymbolPropertyHash.put(sym, value);
            }
            catch(IllegalSymbolException ise)
            {
                //Should most likley not be thrown
                ise.printStackTrace(System.err);
            }
        }
    }

    /** Use this to set a post translational modification for this Symbol. It will only
     * the current MassCalc instance and will not affect the static method.
     * @param symbolToken The one char id for this residue
     * @param mass the mass to change the residue to
     * @throws IllegalSymbolException
     */
    public void setSymbolModification(char symbolToken, double mass) 
                                        throws IllegalSymbolException
    {
        Iterator list = ProteinTools.getAlphabet().symbols().iterator();
        for(; list.hasNext(); )
        {
            Symbol sym = (Symbol)list.next();
            if(sym.getToken() == symbolToken) 
                    mSymbolPropertyHash.put(sym, new Double(mass));
        }
        
    }
    
 
    /** May need something like this if a user wants to add a non standard
     * amino acid to a specific location on a peptide. Not very usefull
     * for woking with large batches of sequences. Currently this is not implemented
     * @param residue
     * @param mass
     * @throws IllegalSymbolException
     */
    public void addSymbolModification(Symbol residue, double mass) 
                                        throws IllegalSymbolException
    {
     //   mSymbolPropertyTable.setDoubleProperty(residue, Double.toString(mass));
    }
    
    /** Get the Mass of this peptide. This only works for the values in the
     * ResidueProperties.xml. It is probably slightly faster but it does not
     * handle Post Translational Modifications.
     * @param proteinSeq The sequence
     * @param isotopicType The type of table containing the mass values. Ex. SymbolPropertyTable.MONO_MASS
     * @param MH_PLUS True if the value needed is the MH+ mass
     * @return The mass of the peptide
     */
    public static final double getMass(SymbolList proteinSeq, 
                                        String isotopicType, 
                                        boolean MH_PLUS)
    {
        if(!proteinSeq.getAlphabet().getName().equals("PROTEIN")){
            //throw exception
        }
        
        double pepMass = 0.0;
        
        SymbolPropertyTable sPT = 
                ProteinTools.getSymbolPropertyTable(isotopicType);
        try
        {
            for(Iterator it = proteinSeq.iterator(); it.hasNext(); ){
                pepMass += sPT.getDoubleValue((Symbol)it.next());
            }
        }
        catch(IllegalSymbolException ise)
        {
            //Throw
            ise.printStackTrace();
        }
        
        if (pepMass != 0.0)
        {
                if (MH_PLUS)
                {
                   pepMass = pepMass + 3 * Havg + Oavg;
                }
                else
                {
                   pepMass = pepMass + 2 * Havg + Oavg;
                }
        }

        return pepMass;
    }
    
    /** Get the Mass of this peptide. Use this if you want to set fixed modifications
     * and have created an instance of MassCalc. The static method may be faster.
     * @param proteinSeq The sequence for mass calculation
     * @param MH_PLUS true if you want to calculate MH+ values
     * @return The mass of the sequence
     */
    public double getMass(SymbolList proteinSeq, boolean MH_PLUS)
    {
        if(!proteinSeq.getAlphabet().getName().equals("PROTEIN")){
            //throw exception
        }
        
        double pepMass = 0.0;
        
      //  SymbolPropertyTable sPT = getSymbolPropertyTable();
        HashMap symbolPropertyMap = getSymbolPropertyMap();

        for(Iterator it = proteinSeq.iterator(); it.hasNext(); ){
            Double mass = (Double)symbolPropertyMap.get((Symbol)it.next());
            pepMass += mass.doubleValue();
        }
        
        if (pepMass != 0.0)
        {
                if (MH_PLUS)
                {
                   pepMass = pepMass + 3 * Havg + Oavg;
                }
                else
                {
                   pepMass = pepMass + 2 * Havg + Oavg;
                }
        }

        return pepMass;
    }
    
    private HashMap getSymbolPropertyMap()
    {
        return mSymbolPropertyHash;// = ProteinTools.getSymbolPropertyTable(name);
    }
}
