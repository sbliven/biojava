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
package dp;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.dist.*;
import org.biojava.bio.dp.*;

/**
 * This demo file is a simulation of the "The occasionally dishonest casino" example
 * from the book by R. Durbin, S. Eddy, A. Krogh, G. Mitchison,
 * "Biological Sequence Analysis",
 * Chapter 3 Markov Chains and hidden Markov models, Section 2, pp55-57.
 * <P>
 * Use: <code>Dice</code>
 * <p>
 * The output consists of three lines:  line 1 represents the output sequence generated
 * by the hidden markov model (f for fair and l for loaded).  Line 2 contains the name of
 * the die which emitted the corresponding output symbol.  Line 3 shows the state
 * sequence predicted by the Viterbi algorithm.
 * <P>
 *
 * @author Samiul Hasan
 */

public class Dice
{
  public static void main(String[] args) {
    try {
      MarkovModel casino = createCasino();
      DP dp=DPFactory.DEFAULT.createDP(casino);
      StatePath obs_rolls = dp.generate(300);

      SymbolList roll_sequence = obs_rolls.symbolListForLabel(StatePath.SEQUENCE);
      SymbolList[] res_array = {roll_sequence};
      StatePath v = dp.viterbi(res_array, ScoreType.PROBABILITY);

      //print out obs_sequence, output, state symbols.
      for(int i = 1; i <= obs_rolls.length()/60; i++) {
        for(int j=i*60; j<Math.min((i+1)*60, obs_rolls.length()); j++)  {
          System.out.print(obs_rolls.symbolAt(StatePath.SEQUENCE, j+1).getToken());
        }
        System.out.print("\n");
        for(int j=i*60; j<Math.min((i+1)*60, obs_rolls.length()); j++)  {
          System.out.print(obs_rolls.symbolAt(StatePath.STATES, j+1).getToken());
        }
        System.out.print("\n");
        for(int j=i*60; j<Math.min((i+1)*60, obs_rolls.length()); j++)  {
          System.out.print(v.symbolAt(StatePath.STATES, j+1).getToken());
        }
        System.out.print("\n\n");
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static MarkovModel createCasino() {
    Symbol[] rolls=new Symbol[6];

    //set up the dice alphabet
    SimpleAlphabet diceAlphabet=new SimpleAlphabet();
    diceAlphabet.setName("DiceAlphabet");

    for(int i=1;i<7;i++) {
      try {
        rolls[i-1]= AlphabetManager.createSymbol((char)('0'+i),""+i,Annotation.EMPTY_ANNOTATION);
        diceAlphabet.addSymbol(rolls[i-1]);
      } catch (Exception e) {
        throw new NestedError(
          e, "Can't create symbols to represent dice rolls"
        );
      }
    }

    int [] advance = { 1 };
    Distribution fairD;
    Distribution loadedD;
    try {
      fairD = DistributionFactory.DEFAULT.createDistribution(diceAlphabet);
      loadedD = DistributionFactory.DEFAULT.createDistribution(diceAlphabet);
    } catch (Exception e) {
      throw new NestedError(e, "Can't create distributions");
    }
    EmissionState fairS = new SimpleEmissionState("fair", Annotation.EMPTY_ANNOTATION, advance, fairD);
    EmissionState loadedS = new SimpleEmissionState("loaded", Annotation.EMPTY_ANNOTATION, advance, loadedD);

    SimpleMarkovModel casino = new SimpleMarkovModel(1, diceAlphabet, "Casino");
    try {
      casino.addState(fairS);
      casino.addState(loadedS);
    } catch (Exception e) {
      throw new NestedError(e, "Can't add states to model");
    }

    //set up transitions between states.
    try {
      casino.createTransition(casino.magicalState(),fairS);
      casino.createTransition(casino.magicalState(),loadedS);
      casino.createTransition(fairS,casino.magicalState());
      casino.createTransition(loadedS,casino.magicalState());
      casino.createTransition(fairS,loadedS);
      casino.createTransition(loadedS,fairS);
      casino.createTransition(fairS,fairS);
      casino.createTransition(loadedS,loadedS);
    } catch (Exception e) {
      throw new NestedError(e, "Can't create transitions");
    }

    //set up emission probabilities.
    try {
      for(int i=0;i<rolls.length;i++)	{
        fairD.setWeight(rolls[i],1.0/6.0);
        loadedD.setWeight(rolls[i], 0.1);
      }
      loadedD.setWeight(rolls[5],0.5);
    } catch (Exception e) {
      throw new NestedError(e, "Can't set emission probabilities");
    }

    //set up transition scores.
    try {
      Distribution dist;

      dist = casino.getWeights(casino.magicalState());
      dist.setWeight(fairS, 0.8);
      dist.setWeight(loadedS, 0.2);

      dist = casino.getWeights(fairS);
      dist.setWeight(loadedS,               0.04);
      dist.setWeight(fairS,                 0.95);
      dist.setWeight(casino.magicalState(), 0.01);

      dist = casino.getWeights(loadedS);
      dist.setWeight(fairS,                 0.09);
      dist.setWeight(loadedS,               0.90);
      dist.setWeight(casino.magicalState(), 0.01);
    } catch (Exception e) {
      throw new NestedError(e, "Can't set transition probabilities");
    }

    return casino;
  }
}
