package symbol;

import java.io.*;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

public class TestAlphabetIndexers {
  public static void main(String [] args) throws Exception {
    int size = 2;
    int extra = 2;
    System.out.println("Test the simple case of a fixed-size alphabet");
    
    FiniteAlphabet testAlpha = new SimpleAlphabet();
    //testAlpha.addChangeListener(ChangeListener.LOG_TO_OUT);
    
    System.out.println("Adding symbols");
    char c = 'a';
    for(int i = 0; i < size; i++) {
      System.out.println("Creating symbol " + (char) (c+i));
      testAlpha.addSymbol(AlphabetManager.createSymbol(
        (char) (c + i), "Symbol " + i, Annotation.EMPTY_ANNOTATION
      ));
    }
    
    System.out.println("Creating alphabet index");
    AlphabetIndex ai = AlphabetManager.getAlphabetIndex(testAlpha);
    ai.addChangeListener(ChangeListener.LOG_TO_OUT, AlphabetIndex.INDEX);
    
    for(int i = 0; i < size; i++) {
      System.out.println(i + " -> " + ai.symbolForIndex(i).getName());
    }
    
    for(Iterator si = testAlpha.iterator(); si.hasNext(); ) {
      Symbol s = (Symbol) si.next();
      System.out.println(ai.indexForSymbol(s) + " -> " + s.getName());
    }

    System.out.println("Test the complex case of a  variable-size alphabet");
    
    for(int ii = size; ii < size+extra; ii++) {
      testAlpha.addSymbol(AlphabetManager.createSymbol(
        (char) (c + ii), "Symbol " + ii, Annotation.EMPTY_ANNOTATION
      ));
      for(int i = 0; i < ii; i++) {
        System.out.println(i + " -> " + ai.symbolForIndex(i).getName());
      }
      
      for(Iterator si = testAlpha.iterator(); si.hasNext(); ) {
        Symbol s = (Symbol) si.next();
        System.out.println(ai.indexForSymbol(s) + " -> " + s.getName());
      }
    }
  }
}
