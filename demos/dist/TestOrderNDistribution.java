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
 * Demonstration of the using OrderNDistribution
 * <P>
 * This sequence constructs a random sequence.  it then creates an
 * OrderNDistribution to which consists of an (N-1)th order crossproduct
 * alphabet for the conditioning alphabet and an ordinary alphabet as
 * the conditioned alphabet.
 *
 * @author David Huen, who cobbled it together from code by all and sundry.
 */
public class TestOrderNDistribution {
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

        // create a crossproduct alphabet of order N-1.
        List alfaList = Collections.nCopies(order-1, DNATools.getDNA());
        FiniteAlphabet NlessOneAlfa = (FiniteAlphabet) AlphabetManager.getCrossProductAlphabet(alfaList);

        // now create alphabet of (DNA)[N-1]th x DNA
        alfaList = new Vector();
        alfaList.add(NlessOneAlfa);
        alfaList.add(DNATools.getDNA());
        FiniteAlphabet NAlfa = (FiniteAlphabet) AlphabetManager.getCrossProductAlphabet(alfaList);

        // create a distribution training context for this job and register it for training
        DistributionTrainerContext dtc = new SimpleDistributionTrainerContext();
        OrderNDistribution orderNDistribution = (OrderNDistribution) OrderNDistributionFactory.DEFAULT.createDistribution(NAlfa);
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
        SymbolTokenization orderNTokenizer = orderNDistribution.getConditioningAlphabet().getTokenization("name");
        SymbolTokenization tokenizer = orderNDistribution.getConditionedAlphabet().getTokenization("name");
        FiniteAlphabet conditioningAlfa = (FiniteAlphabet) orderNDistribution.getConditioningAlphabet();
        FiniteAlphabet conditionedAlfa = (FiniteAlphabet) orderNDistribution.getConditionedAlphabet();

        // now print out the observed distribution
        for (Iterator i = conditioningAlfa.iterator(); i.hasNext();) {
            Symbol s = (Symbol) i.next();

            System.out.print(orderNTokenizer.tokenizeSymbol(s));

            // get the conditioned distribution
            Distribution conditionedDist = orderNDistribution.getDistribution(s);

            for (Iterator j = conditionedAlfa.iterator(); j.hasNext();) {
                Symbol s1 = (Symbol) j.next();
                System.out.print("\t" + tokenizer.tokenizeSymbol(s1) + "\t" + conditionedDist.getWeight(s1));
            }

            System.out.println("");
        }

    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
}
