package org.biojava.bio.symbol;

import org.biojava.bio.BioError;

/**
 * <p>
 * A SymbolList that stores symbols as bit-patterns in an array of longs.
 * </p>
 *
 * <p>
 * Bit-packed symbol lists are space efficient compared to the usual pointer
 * stoorage model employed by implementatinos like SimpleSymbolList. This
 * comes at the cost of encoding/decoding symbols from the stoorage. In
 * practice, the decrease in memory when stooring large sequences makes
 * applications go quicker because of issues like page swapping.
 * </p>
 *
 * <p>
 * Symbols can be mapped to and from bit-patterns. The Pattern interface
 * encapsulates this. A SymbolList can then be stored by writing these
 * bit-patterns into memory. This implementation stores the bits
 * in the long elements of an array. The first symbol will be packed into
 * bits 0 through packing.wordLength()-1 of the long at index 0.
 * <p>
 *
 * <h2>Example Usage</h2>
 * <pre>
 * SymbolList symL = ...;
 * SymbolList packed = new PackedSymbolList(
 *   PackingFactory.getPacking(symL.getAlphabet(), true),
 *   symL
 * );
 * </pre>
 *
 * @author Matthew Pocock
 */
public class PackedSymbolList
  extends
    AbstractSymbolList
  implements
    java.io.Serializable
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
  
  /**
   * <p>
   * Create a new PackedSymbolList directly from a bit pattern.
   * </p>
   *
   * <p>
   * <em>Warning:</em> This is a risky developer method.
   * You must be sure that the syms array is packed in a
   * way that is consistent with the packing. Also, it is your
   * responsibility to ensure that the length is sensible.</em>
   * </p>
   *
   * @param packing the Packing used
   * @param syms a long array containing already packed symbols
   * @param length the length of the sequence packed in symbols
   */
  public PackedSymbolList(Packing packing, long[] syms, int length) {
    this.symsPerElement = (byte) (bitsPerElement / packing.wordSize());
    this.packing = packing;
    this.syms = syms;
    this.length = length;
    this.mask = calcMask(packing);
  }
  
  /**
   * <p>
   * Create a new PackedSymbolList as a packed copy of another symbol list.
   * </p>
   *
   * <p>
   * This will create a new and independand symbol list that is a copy of
   * the symbols in symList. Both lists can be modified independantly.
   * </p>
   *
   * @param packing the way to bit-pack symbols
   * @param symList the SymbolList to copy
   */
  public PackedSymbolList(Packing packing, SymbolList symList)
  throws IllegalAlphabetException {
    if(packing.getAlphabet() != symList.getAlphabet()) {
      throw new IllegalAlphabetException(
        "Can't pack with alphabet " + packing.getAlphabet() +
        " and symbol list " + symList.getAlphabet()
      );
    }
    
    try {
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
    } catch (IllegalSymbolException ise) {
      throw new BioError(ise, "Assertion Failure: Symbol got lost somewhere");
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
  
  /**
   * Dump out the long as a binary string.
   *
   * @param l the long to print
   */
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
  
  /**
   * <p>
   * Return the long array within which the symbols are bit-packed.
   * </p>
   *
   * <p>
   * <em>Warning:</em> This is a risky developer method.
   * This is the actual array that this object uses to store the bits
   * representing symbols. You should not modify this in any way. If you do,
   * you will modify the symbols returned by symbolAt(). This methd is
   * provided primarily as an easy way for developers to extract the
   * bit pattern for stoorage in such a way as it could be fetched later and
   * fed into the apropreate constructor.
   * </p>
   *
   * @return the actual long array used to store bit-packed symbols
   */
  public long[] getSyms() {
    return syms;
  }
}
