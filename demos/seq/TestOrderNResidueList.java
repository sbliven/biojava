import java.util.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

/**
 * Demonstration of the OrderNResidueList class.
 * <P>
 * This program generates a random DNA sequence of length 10. It then constructs
 * views of this sequence that contain di, tri, quad and penta -nucleotides.
 * Each view is then printed out, so that you can check that the
 * OrderNResidueList is the correct sequence.
 */
public class TestOrderNResidueList {
  public static void main(String [] args)
  throws Exception {
    ResidueList res = SeqTools.createResidueList(10);
    
    for(int o = 1; o < 6; o++) {
      ResidueList view = new OrderNResidueList(res, o);
      System.out.println("Order " + o + " view:");
      for(int i = 1; i <= view.length(); i++) {
        System.out.println(i + ": " + view.residueAt(i).getName());
      }
      System.out.println("\n");
    }
  }
}
