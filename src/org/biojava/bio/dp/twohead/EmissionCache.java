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


package org.biojava.bio.dp.twohead;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biojava.bio.dist.Distribution;
import org.biojava.bio.dp.EmissionState;
import org.biojava.bio.dp.ScoreType;
import org.biojava.bio.dp.State;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.utils.ListTools;

/**
 * Cache for columns of emission probabilities in pair-wise alignment
 * algorithms. 
 *
 * @author Matthew Pocock
 */
public class EmissionCache {
  private final Map eMap;
  private final Alphabet alpha;
  private final State[] states;
  private final int dsi;
  private final ScoreType scoreType;
  private final Symbol[] gap;
  
  public EmissionCache(
    Alphabet alpha,
    State[] states,
    int dsi,
    ScoreType scoreType
  ) {
    this.eMap = new HashMap();
    this.alpha = alpha;
    this.states = states;
    this.dsi = dsi;
    this.scoreType = scoreType;
    
    List alphas = alpha.getAlphabets();
    this.gap = new Symbol[alphas.size()];
    for(int i = 0; i < this.gap.length; i++) {
      this.gap[i] = ((Alphabet) alphas.get(i)).getGapSymbol();
    }
  }
  
  public final double [] getEmissions(List symList)
  throws IllegalSymbolException {
    double [] emission = (double []) eMap.get(symList);
    if(emission == null) {
      //System.out.println(".");
      Symbol sym[][] = new Symbol[2][2];
      List ll = ListTools.createList(symList);
      sym[0][0] = AlphabetManager.getGapSymbol();
      sym[1][1] = alpha.getSymbol(Arrays.asList(new Symbol [] {
        (Symbol) symList.get(0),
        (Symbol) symList.get(1)
      }));
      sym[1][0] = alpha.getSymbol(Arrays.asList(new Symbol [] {
        (Symbol) symList.get(0),
        gap[1]
      }));
      sym[0][1] = alpha.getSymbol(Arrays.asList(new Symbol [] {
        gap[0],
        (Symbol) symList.get(1)
      }));
      emission = new double[dsi];
      for(int i = 0; i < dsi; i++) {
        EmissionState es = (EmissionState) states[i];
        int [] advance = es.getAdvance();
        Distribution dis = es.getDistribution();
        Symbol s = sym[advance[0]][advance[1]]; 
        emission[i] = Math.log(scoreType.calculateScore(dis, s));
        /*System.out.println(
          advance[0] + ", " + advance[1] + " " +
          s.getName() + " " +
          es.getName() + " " +
          emission[i]
        );*/
      }
      eMap.put(ll, emission);
    } else {
      //System.out.print("-");
    }
    return emission;    
  }
  
  public void clear() {
    eMap.clear();
  }
}
