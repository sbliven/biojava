package org.biojava.bio.proteomics;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.io.SymbolTokenization;

public final class StructureTools {
  private static final FiniteAlphabet struct;
  
  private static final AtomicSymbol _;
  private static final AtomicSymbol c;
  private static final AtomicSymbol h;
  private static final AtomicSymbol g;
  private static final AtomicSymbol i;
  private static final AtomicSymbol e;
  private static final AtomicSymbol b;
  private static final AtomicSymbol t;
  private static final AtomicSymbol s;
  
  static {
    try {
      struct = (FiniteAlphabet) AlphabetManager.alphabetForName("STRUCTURE");
      
      SymbolTokenization sTok = struct.getTokenization("token");
      
      _ = (AtomicSymbol) sTok.parseToken(" ");
      c = (AtomicSymbol) sTok.parseToken("c");
      h = (AtomicSymbol) sTok.parseToken("h");
      g = (AtomicSymbol) sTok.parseToken("g");
      i = (AtomicSymbol) sTok.parseToken("i");
      e = (AtomicSymbol) sTok.parseToken("e");
      b = (AtomicSymbol) sTok.parseToken("b");
      t = (AtomicSymbol) sTok.parseToken("t");
      s = (AtomicSymbol) sTok.parseToken("s");
    } catch (Throwable t) {
      throw new BioError(t, "Could not initialise structure alphabet");
    }
  }
  
  public FiniteAlphabet getStructure() {
    return struct;
  }
  
  public AtomicSymbol get_() {
    return _;
  }
  
  public AtomicSymbol getC() {
    return c;
  }
  
  public AtomicSymbol getH() {
    return h;
  }
  
  public AtomicSymbol getG() {
    return g;
  }
  
  public AtomicSymbol getI() {
    return i;
  }
  
  public AtomicSymbol getE() {
    return e;
  }
  
  public AtomicSymbol getB() {
    return b;
  }
  
  public AtomicSymbol getT() {
    return t;
  }
  
  public AtomicSymbol getS() {
    return s;
  }
}
