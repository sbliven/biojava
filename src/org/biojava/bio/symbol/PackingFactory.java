package org.biojava.bio.symbol;

import java.util.*;

import org.biojava.bio.seq.*;

/**
 * <p>
 * A factory that is used to maintain associations between alphabets and
 * preferred bit-packings for them.
 * </p>
 *
 * <p>
 * There are many ways to pack the symbols for an alphabet as binary.
 * Different applications will wish to have different representations for
 * reasons such as integration with external formats, wether to store
 * ambiguity or not and what algorithms may be used on the bit-packed
 * representation. Also, it has utility methods to arrange the bit-strings
 * for symbols within a Java int primative.
 * </p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */
public class PackingFactory {
  private final static Map packForAlpha;
  
  static {
    packForAlpha = new HashMap();
    
  }
  
  /**
   * Get the default packing for an alphabet.
   *
   * @param alpha  the FiniteAlphabet that will be bit-packed
   * @param ambiguity  true if the packing should store ambiguity and false
   *                   if it can discard ambiguity information
   **/
  public static Packing getPacking(FiniteAlphabet alpha, boolean ambiguity)
  throws IllegalAlphabetException {
    Packing pack = (Packing) packForAlpha.get(alpha);
    if(pack == null) {
      if(alpha == DNATools.getDNA()) {
        if(ambiguity) {
          pack = new DNAAmbPack();
        } else {
          pack = new DNANoAmbPack(DNATools.a());
        }
      } else {
        throw new IllegalAlphabetException();
      }
    }
    return pack;
  }
  
  public static int primeWord(
    SymbolList symList,
    int wordLength,
    Packing packing
  ) throws IllegalSymbolException {
    int word = 0;
    for(int i = 0; i < wordLength; i++) {
      int p = packing.pack(symList.symbolAt(i+1));
      word |= (int) ((int) p << (int) (i * packing.wordSize()));
    }
    return word;
  }
  
  public static int nextWord(
    SymbolList symList,
    int word,
    int offset,
    int wordLength,
    Packing packing
  ) throws IllegalSymbolException {
    word = word >> (int) packing.wordSize();
    int p = packing.pack(symList.symbolAt(offset));
    word |= (int) p << ((int) (wordLength - 1) * packing.wordSize());
    return word;
  }
  
  public static void binary(long val) {
    for(int i = 63; i >= 0; i--) {
      System.out.print( ((((val >> i) & 1) == 1) ? 1 : 0) );
    }
    System.out.println();
  }
  public static void binary(int val) {
    for(int i = 31; i >= 0; i--) {
      System.out.print( ((((val >> i) & 1) == 1) ? 1 : 0) );
    }
    System.out.println();
  }
}

