/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package dist;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;

/**
 * Demonstration of the using training to obtain the distribution of a n-mer.
 * <P>
 * This program generates a random DNA sequence. It then constructs
 * views of this sequence of the required order and collates the frequencies
 * of given n-mers.
 *
 * @author David Huen, who cobbled it together from code by all and sundry.
 */
public class TestOrderNAlphabet {
  public static void main(String [] args) {
    try {
        // verify arguments
        if (args.length != 2) {
            System.out.println("Usage: java dist/TestOrderNDistribution <test seq length> <order>");
            System.exit(1);
        }

        SymbolList res = Tools.createSymbolList(Integer.parseInt(args[0]));
        int order = Integer.parseInt(args[1]);
    
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
