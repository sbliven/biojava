package org.biojava.bio.symbol;

/**
 * @author Matthew Pocock
 */
public class PackedSymbolList
  extends
    AbstractSymbolList
{
  private final byte bitsPerElement = 64;

  private final Packing packing;
  private final long[] syms;
  private final int length;
  private final byte symsPerElement;
  private final byte mask;
  
  public Alphabet getAlphabet() {
    return packing.getAlphabet();
  }
  
  public int length() {
    return length;
  }
  
  public PackedSymbolList(Packing packing, long[] syms, int length) {
    this.symsPerElement = (byte) (bitsPerElement / packing.wordSize());
    this.packing = packing;
    this.syms = syms;
    this.length = length;
    this.mask = calcMask(packing);
  }
  
  public PackedSymbolList(Packing packing, SymbolList symList)
  throws IllegalAlphabetException {
    if(packing.getAlphabet() != symList.getAlphabet()) {
      throw new IllegalAlphabetException(
        "Can't pack with alphabet " + packing.getAlphabet() +
        " and symbol list " + symList.getAlphabet()
      );
    }
    this.symsPerElement = (byte) (bitsPerElement / packing.wordSize());
    this.packing = packing;
    this.length = symList.length();
    this.syms = new long[
      length / symsPerElement +
      ((length % symsPerElement == 0) ? 0 : 1)
    ];
    this.mask = calcMask(packing);
    
    // pack the body of the sequence
    for(int i = 0; i < (syms.length - 1); i++) {
      int ii = i * symsPerElement;
      long l = 0;
      for(int j = 0; j < symsPerElement; j++) {
        int jj = j * packing.wordSize();
        long p = packing.pack(symList.symbolAt(ii + j + 1));
        l |= (long) ((long) p << (long) jj);
      }
      syms[i] = l;
    }
    // pack the final word
    if(syms.length > 0) {
      long l = 0;
      int ii = (syms.length - 1) * symsPerElement;
      int jMax = symList.length() % symsPerElement;
      if(jMax == 0) {
        jMax = symsPerElement;
      }
      for(int j = 0; j < jMax; j++) {
        int jj = j * packing.wordSize();
        long p = packing.pack(symList.symbolAt(ii + j + 1));
        l |= (long) ((long) p << (long) jj);
      }
      syms[syms.length - 1] = l;
    }
  }
  
  public Symbol symbolAt(int indx) {
    indx--;
    int word = indx / symsPerElement;
    int offset = indx % symsPerElement;
    
    long l = syms[word];
    int jj = offset * packing.wordSize();
    
    try {
      return packing.unpack((byte) ((l >> (long) jj) & mask));
    } catch (IllegalSymbolException ise) {
      throw new org.biojava.utils.NestedError(ise, "Could not unpack " + indx + " at " + word + "," + offset);
    }
  }
  
  private void binary(long l) {
    for(int i = 63; i >= 0; i--) {
      System.out.print( ((((l >> i) & 1) == 1) ? 1 : 0) );
    }
    System.out.println();
  }
  
  private static byte calcMask(Packing packing) {
    byte mask = 0;
    for(int i = 0; i < packing.wordSize(); i++) {
      mask |= 1 << i;
    }
    return mask;
  }
}
