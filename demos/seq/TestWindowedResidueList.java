import java.util.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

/**
 * Demonstration of the WindowedResidueList class.
 * <P>
 * This program generates a random DNA sequence. It then constructs
 * views of this sequence that are onto windows of width 1-5.
 * Each view is then printed out, so that you can check that the
 * WindowResidueList is the correct sequence.
 */
public class TestWindowedResidueList {
  public static void main(String [] args)
  throws Exception {
    ResidueList res = SeqTools.createResidueList(3*4*5);
    
    for(int w = 1; w <= 6; w++) {
      ResidueList view = new WindowedResidueList(res, w);
      System.out.println("Window " + w + " view:");
      for(int i = 1; i <= view.length(); i++) {
        System.out.println(i + ": " + view.residueAt(i).getName());
      }
      System.out.println("\n");
    }
  }
}
