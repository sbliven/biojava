/*
 * SymbolPropertyTable.java
 *
 * Created on December 20, 2000, 7:15 PM
 */

package org.biojava.bio.symbol;

/**
 *
 * @author  mjones
 * @version 
 */
public interface SymbolPropertyTable {
  
  public static String AVG_MASS = "avgMass";

  public static String MONO_MASS = "monoMass";

  // the name of the property e.g. "isotopic mass"
  public String getName();

  // the alphabet that this property is defined for e.g. PROTEIN
  public Alphabet getAlphabet();

  // the value of the property for a given symbol
  public double getDoubleValue(Symbol s) throws IllegalSymbolException;

 // public void setDoubleProperty(Symbol s, String value) throws IllegalSymbolException;

  // the value of the property for a given symbol
 // public void setDoubleProperty(Symbol s, String value) throws IllegalSymbolException;

}