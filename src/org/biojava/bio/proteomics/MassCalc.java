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


/*
 * MassCalc.java
 *
 * Created on January 11, 2001, 4:18 PM
 */

package org.biojava.bio.proteomics;


import org.biojava.bio.seq.*;
//import java.io.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.io.*;


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

    
    /**
     * If instance methods are being used this will store the 
     * isotopicly and MH_PLUS correct treminal mass to be created. 
     * Need to be able to handle N and C term mods in the future. 
     */
     private double termMass;

    
    /** Creates new MassCalc 
    public MassCalc() {
    }*/
    
     /** Creates new MassCalc
     * @param isotopicType The type of isotopes to calculate. Either mono isotopic
     * or average isotopic. It realy is the name of SymbolProperty
     * table. Ex. SymbolPropertyTable.AVG_MASS or SymbolPropertyTable.MONO_MASS
     */
    public MassCalc(String isotopicType, boolean MH_PLUS) {
        //Calculate hydroxyl mass
        termMass = calcTermMass(isotopicType,MH_PLUS); 
        mSymbolPropertyHash = new HashMap();
        
        SymbolPropertyTable symbolPropertyTable = 
                ProteinTools.getSymbolPropertyTable(isotopicType);
        
        //Iterator symbolList = ProteinTools.getAlphabet().symbols().iterator();
        
        Iterator symbolList = ProteinTools.getAlphabet().iterator();
	// SymbolTokenization toke = ProteinTools.getAlphabet().getTokenization("token");
        
        for(; symbolList.hasNext(); )
        {
            
            Symbol sym = (Symbol)symbolList.next();
            try{
                // System.out.println(sym.getName() + ":" + toke.tokenizeSymbol(sym));
                try{
                Double value = new Double(symbolPropertyTable.getDoubleValue(sym));
                
                mSymbolPropertyHash.put(sym, value);
                }catch (NullPointerException npe){
                   //This seems to be happening when a amino acid is 
                   //in the property table that doesn't have a residue value
                   
                }
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
        //Iterator list = ProteinTools.getAlphabet().symbols().iterator();
        
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
	                       throws IllegalSymbolException
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
            throw ise;
        }
        
        //Calculate hydroxyl mass
	double termMass = calcTermMass(isotopicType,MH_PLUS);
        
        if (pepMass != 0.0){
	    pepMass += termMass;
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
        
        HashMap symbolPropertyMap = getSymbolPropertyMap();

        for(Iterator it = proteinSeq.iterator(); it.hasNext(); ){
            Double mass = (Double)symbolPropertyMap.get((Symbol)it.next());
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
     * Use this method to double check the aded teminal masses. 
     *
     */
    public double getTermMass(){
	return termMass;
    }

    private static double calcTermMass(String isotopicType, boolean MH_PLUS){
	double termMass = 0.0;
	if(isotopicType.equals(SymbolPropertyTable.AVG_MASS)){
	    //Add the C-terminal OH and N-Term H
	    termMass += Havg + Oavg + Havg;
	    //Add the extra H
	    if(MH_PLUS){
	      termMass += Havg;	
	    }
        }
        else if(isotopicType.equals(SymbolPropertyTable.MONO_MASS)){
	    //Add the C-terminal OH and N-Term H
	    termMass += Hmono + Omono + Hmono;
	    //Add the extra H
	    if(MH_PLUS){
	      termMass += Hmono;
	    }
        }

	return termMass;
    }
}
