import java.util.*;

import org.biojava.bio.symbol.*;

/**
 * Demonstration of the OrderNSymbolList class.
 * <P>
 * This program generates a random DNA sequence of length 10. It then constructs
 * views of this sequence that contain di, tri, quad and penta -nucleotides.
 * Each view is then printed out, so that you can check that the
 * OrderNSymbolList is the correct sequence.
 */
public class TestOrderNSymbolList {
  public static void main(String [] args) {
    try {
    SymbolList res = Tools.createSymbolList(10);
    
    for(int o = 1; o < 6; o++) {
      SymbolList view = new OrderNSymbolList(res, o);
      System.out.println("Order " + o + " view:");
      for(int i = 1; i <= view.length(); i++) {
        System.out.println(i + ": " + view.symbolAt(i).getName());
      }
      System.out.println("\n");
    }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
