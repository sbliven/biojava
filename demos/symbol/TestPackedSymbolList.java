package symbol;

import java.util.*;

import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

public class TestPackedSymbolList {
  public static void main(String[] args)
  throws Throwable {
    Packing dnaAmb = new DNAAmbPack();
    Packing dnaNot = new DNANoAmbPack(DNATools.a());
    SymbolTokenization tok = DNATools.getDNA().getTokenization("token");
    
    for(int i = 0; i < 74; i++) {
      SymbolList test = createSymbolList(i);
      SymbolList amb = new PackedSequence(dnaAmb, test);
      SymbolList not = new PackedSequence(dnaNot, test);
      
      System.out.print(i + " ");
      for(int j = 1; j <= i; j++) {
        Symbol t = test.symbolAt(j);
        Symbol a = amb.symbolAt(j);
        Symbol n = not.symbolAt(j);
        
        if(
          t == a
            &&
          t == n
            &&
          a == n
        ) {
          System.out.print(tok.tokenizeSymbol(t));
        } else {
          System.out.print(
            "{" +
              tok.tokenizeSymbol(t) + "," +
              tok.tokenizeSymbol(a) + "," +
              tok.tokenizeSymbol(n) +
            "}"
          );
        }
      }
      System.out.println();
    }
  }
  
  private static SymbolList createSymbolList(int length)
  throws Exception {
    List l = new ArrayList(length);
    for(int i = 0; i < length; i++) {
      l.add(DNATools.forIndex((int) (4.0 * Math.random())));
    }
    return new SimpleSymbolList(DNATools.getDNA(), l);
  }
}
