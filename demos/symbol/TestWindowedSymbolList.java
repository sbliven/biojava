package symbol;

import java.util.*;

import org.biojava.bio.symbol.*;

/**
 * Demonstration of the WindowedSymbolList class.
 * <P>
 * This program generates a random DNA sequence. It then constructs
 * views of this sequence that are onto windows of width 1-5.
 * Each view is then printed out, so that you can check that the
 * WindowSymbolList is the correct sequence.
 */
public class TestWindowedSymbolList {
  public static void main(String [] args)
  throws Exception {
    SymbolList res = Tools.createSymbolList(3*4*5);
    
    for(int w = 1; w <= 6; w++) {
      SymbolList view = SymbolListViews.windowedSymbolList(res, w);
      System.out.println("Window " + w + " view:");
      for(int i = 1; i <= view.length(); i++) {
        System.out.println(i + ": " + view.symbolAt(i).getName());
      }
      System.out.println("\n");
    }
  }
}
