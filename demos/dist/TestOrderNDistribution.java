package dist;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

/**
 * Demonstration of the OrderNDistribution class.
 * <P>
 * This program generates a random DNA sequence of length 100. It then constructs
 * views of this sequence of the required order
 */
public class TestOrderNDistribution {
  public static void main(String [] args) {
    try {
        SymbolList res = Tools.createSymbolList(100);
        int order = Integer.parseInt(args[0]);
    
        // generate the Nth order view of this sequence
        SymbolList view = SymbolListViews.orderNSymbolList(res, order);

        // create a cross product Alphabet of required order
        List alphas = Collections.nCopies(order, DNATools.getDNA());
        FiniteAlphabet orderNAlfa = (FiniteAlphabet) AlphabetManager.getCrossProductAlphabet(alphas);

        // create a distribution training context for this job and register it for training
        DistributionTrainerContext dtc = new SimpleDistributionTrainerContext();
        Distribution orderNDistribution = DistributionFactory.DEFAULT.createDistribution(orderNAlfa);
        dtc.registerDistribution(orderNDistribution);
        dtc.clearCounts();

        // now iterate thru' the order n symbol list view and accumulate counts
        for (int i=1; i <= view.length(); i++) {
            dtc.addCount(orderNDistribution, view.symbolAt(i), 1.0);
        }

        // go normalise the whole shebang!
        try {
            dtc.train();
        }
        catch (ChangeVetoException cve) {
            throw new NestedError("couldn't train distribution");
        }

        // we have to be able to tokenise the symbols!
        SymbolTokenization tokenizer = orderNAlfa.getTokenization("name");

        // now print out the observed distribution
        for (Iterator i = orderNAlfa.iterator(); i.hasNext();) {
            Symbol s = (Symbol) i.next();

            // print the weights
            System.out.println(
                tokenizer.tokenizeSymbol(s) + "\t" +
                orderNDistribution.getWeight(s)
            );
        }

    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
