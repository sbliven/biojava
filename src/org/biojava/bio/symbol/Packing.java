package org.biojava.bio.symbol;

/**
 * @author Matthew Pocock
 */
public interface Packing
extends java.io.Serializable {
  FiniteAlphabet getAlphabet();
  byte pack(Symbol sym);
  Symbol unpack(byte packed)
  throws IllegalSymbolException;
  byte wordSize();
  boolean handlesAmbiguity();
}
