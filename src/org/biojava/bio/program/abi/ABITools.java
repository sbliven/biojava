package org.biojava.bio.program.abi;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

public class ABITools {
  public static final FiniteAlphabet QUALITY;
  public static final AtomicSymbol _0;
  public static final AtomicSymbol _1;
    
  static {
    try {
      IntegerAlphabet.SubIntegerAlphabet _01
      = IntegerAlphabet.getSubAlphabet(0, 1);
      _0 = _01.getSymbol(0);
      _1 = _01.getSymbol(1);
      
      List alphas = new ArrayList();
      alphas.add(DNATools.getDNA());
      alphas.add(_01);
      
      // naughty here - we know because we are insiders that the result of this
      // call will be an AbstractAlphabet impl
      AbstractAlphabet quality = (AbstractAlphabet) AlphabetManager.getCrossProductAlphabet(alphas);
      CharacterTokenization tok = new CharacterTokenization(quality, true);
      
      // all lower case characters go to sym,0
      // all upper case characters go to sym,1
      SymbolList sl = DNATools.createDNA("agctrymkswhbvdn");
      ListTools.Doublet pair = new ListTools.Doublet();
      SymbolTokenization dnaTok = DNATools.getDNA().getTokenization("token");
      for(Iterator i = sl.iterator(); i.hasNext(); ) {
        pair.setA((Symbol) i.next());
        String c = dnaTok.tokenizeSymbol((Symbol) pair.getA());
        
        pair.setB(_1);
        tok.bindSymbol(quality.getSymbol(pair), c.toUpperCase().charAt(0));
        
        pair.setB(_0);
        tok.bindSymbol(quality.getSymbol(pair), c.toLowerCase().charAt(0));
      }
      
      quality.putTokenization("token", tok);
      QUALITY = quality;
    } catch (Exception e) {
      throw new BioError(e, "Could not initialize ABI quality alphabet");
    }
  }
}
