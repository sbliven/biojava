package org.biojava.bio.symbol;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.seq.*;
  
public class DNAAmbPack
  implements
    Packing,
    java.io.Serializable
{
  private final Symbol[] syms;
  
  public DNAAmbPack() {
    this.syms = new Symbol[16];
    for(byte i = 0; i < 16; i++) {
      syms[i] = _unpack(i);
    }
  }
  
  public FiniteAlphabet getAlphabet() {
    return DNATools.getDNA();
  }
  
  public byte pack(Symbol sym) {
    if(false) {
    } else if(sym == DNATools.a()) {
      return 1;
    } else if(sym == DNATools.g()) {
      return 2;
    } else if(sym == DNATools.c()) {
      return 4;
    } else if(sym == DNATools.t()) {
      return 8;
    } else if(sym == DNATools.n()) {
      return 15;
    }
    
    byte p = 0;
    for(Iterator i = DNATools.getDNA().iterator(); i.hasNext(); ) {
      p |= pack((AtomicSymbol) i.next());
    }
    return p;
  }
  
  public Symbol unpack(byte b) {
    return syms[b];
  }
  
  private Symbol _unpack(byte b) {
    Set syms = new SmallSet();
    if(false) {
    } else if(0 != (b & 1)) {
      syms.add(DNATools.a());
    } else if(0 != (b & 2)) {
      syms.add(DNATools.g());
    } else if(0 != (b & 4)) {
      syms.add(DNATools.c());
    } else if(0 != (b & 8)) {
      syms.add(DNATools.t());
    }
    try {
      return DNATools.getDNA().getAmbiguity(syms);
    } catch (IllegalSymbolException ise) {
      throw new NestedError(ise, "Assertion failure: couldn't get DNA ambiguity from DNA: " + syms);
    }
  }
  
  public byte wordSize() {
    return 4;
  }
  
  public boolean handlesAmbiguity() {
    return true;
  }
}

