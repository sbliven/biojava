package org.biojava.bio.symbol;

import java.util.*;

import org.biojava.bio.seq.*;

/**
 * @author Matthew Pocock
 */
public class PackingFactory {
  private final static Map packForAlpha;
  
  static {
    packForAlpha = new HashMap();
    
  }
  
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
  
  public static int primeWord(SymbolList symList, int wordLength, Packing packing) {
    int word = 0;
    for(int i = 0; i < wordLength; i++) {
      int p = packing.pack(symList.symbolAt(i+1));
      word |= (int) ((int) p << (int) (i * packing.wordSize()));
    }
    return word;
  }
  
  public static int nextWord(SymbolList symList, int word, int offset, int wordLength, Packing packing) {
    word = word >> (int) packing.wordSize();
    int p = packing.pack(symList.symbolAt(offset));
    word |= (int) p << ((int) wordLength * packing.wordSize() - 1);
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

