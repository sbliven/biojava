package symbol;

import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

public class TestIndexedIntegers {
  public static void main(String[] args)
  throws Throwable {
	  SimpleAlphabet alph = new SimpleAlphabet();
    alph.setName("Protein Domains");

    for(int i = 0; i < 3000; i++) {
    	//iterate over alphabet names ...
      char token = (char) i;
      String name = String.valueOf(i);
      Annotation ann = Annotation.EMPTY_ANNOTATION;
    	AtomicSymbol symb = AlphabetManager.createSymbol(name, ann);
    	alph.addSymbol(symb);
    }
    
    AlphabetIndex alphindex = AlphabetManager.getAlphabetIndex(alph);
    
    System.out.println("Using indexer: " + alphindex);
    int c = 0;
    for(int i = 0; i < alph.size(); i++) {
      AtomicSymbol a = (AtomicSymbol) alphindex.symbolForIndex(i);
      int j = alphindex.indexForSymbol(a);
      if(i != j) {
        System.out.println(i + " -> " + a + " -> " + j);
      }
      c++;
    }
    System.out.println("Counted " + c + " of " + alph.size() + " symbols");
  }
}

