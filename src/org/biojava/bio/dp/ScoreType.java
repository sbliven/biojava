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


package org.biojava.bio.dp;
import java.io.Serializable;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.*;

public interface ScoreType {
  /**
   * Calculate the score associated with a distribution and a symbol.
   */
  double calculateScore(Distribution dist, Symbol sym)
  throws IllegalSymbolException;
  
  public final static ScoreType PROBABILITY = new Probability();
  
  public static class Probability implements ScoreType {
    public double calculateScore(Distribution dist, Symbol sym)
    throws IllegalSymbolException {
      return dist.getWeight(sym);
    }
  };
  
  public final static ScoreType ODDS = new Odds();
  
  public static class Odds implements ScoreType {
    public double calculateScore(Distribution dist, Symbol sym)
    throws IllegalSymbolException {
      double d = dist.getWeight(sym);
      double n = dist.getNullModel().getWeight(sym);
      //System.out.println("Odds for " + sym.getName() + "\t= " + d + " / " + n);
      return d / n;
    }
  };
  
  public final static ScoreType NULL_MODEL = new NullModel();
  
  public static class NullModel implements ScoreType {
    public double calculateScore(Distribution dist, Symbol sym)
    throws IllegalSymbolException {
      return dist.getNullModel().getWeight(sym);
    }
  };
}

