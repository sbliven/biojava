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

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.dist.*;
import org.biojava.bio.dp.*;

/**
 * This demo file is a simple implementation of pairwise-alignment.
 *
 * @author Matthew Pocock
 */

public class PairwiseAlignment {
  public static void main(String [] args) {
    try {
      if(args.length != 2) {
        throw new Exception("Use: PairwiseAlignment sourceSeqFile targetSeqFile");
      }
      
      File sourceSeqFile = new File(args[0]);
      File targetSeqFile = new File(args[1]);
      FiniteAlphabet alpha = ProteinTools.getAlphabet();
      
      DP aligner = generateAligner(
        alpha,
        0.5, 0.5,
        0.5, 0.5
      );
      
      SymbolParser rParser = alpha.getParser("token");
      SequenceFactory sFact = new SimpleSequenceFactory();
      SequenceFormat sFormat = new FastaFormat();
    
      InputStream sourceIS = new FileInputStream(sourceSeqFile);
      SequenceIterator sourceI = new StreamReader(
        sourceIS,
        sFormat,
        rParser,
        sFact
      );
      while(sourceI.hasNext()) {
        Sequence sourceSeq = sourceI.nextSequence();
        
        InputStream targetIS = new FileInputStream(targetSeqFile);
        SequenceIterator targetI = new StreamReader(
          targetIS,
          sFormat,
          rParser,
          sFact
        );
        while(targetI.hasNext()) {
          Sequence targetSeq = targetI.nextSequence();
          StatePath result = aligner.viterbi(new Sequence [] {
            sourceSeq, targetSeq
          });
          System.out.println(
            "Score " + sourceSeq.getName() + ":" + targetSeq.getName() +
            " = " + result.getScore()
          );
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  private static DP generateAligner(
    FiniteAlphabet alpha,
    double pMatch, double pExtendMatch,
    double pGap, double pExtendGap
  ) throws Exception {
    double pEndMatch = 1.0 - pExtendMatch;
    double pEndGap = 1.0 - pExtendGap;
    double pEnd = 1.0 - pMatch - 2.0*pGap; 
    
    FiniteAlphabet dna = alpha;
    CrossProductAlphabet dna2 =
      AlphabetManager.getCrossProductAlphabet(
        Collections.nCopies(2, dna)
      );
      
    MarkovModel model = new SimpleMarkovModel(2, dna2, "pair-wise aligner");
    
    Distribution nullModel = UniformDistribution.createInstance(dna);
    Distribution gap = new GapDistribution(dna);
    Distribution matchDist = generateMatchDist((FiniteAlphabet) dna2);
    Distribution nullModel2 = new PairDistribution(nullModel, nullModel);
    Distribution insert1Dist = new PairDistribution(nullModel, gap);
    Distribution insert2Dist = new PairDistribution(gap, nullModel);
    
    DotState hub = new SimpleDotState("hub");
    EmissionState match = new SimpleEmissionState(
      "match",
      Annotation.EMPTY_ANNOTATION,
      new int [] { 1, 1 },
      matchDist
    );
    
    EmissionState insert1 = new SimpleEmissionState(
      "insert1",
      Annotation.EMPTY_ANNOTATION,
      new int [] { 1, 0 },
      insert1Dist
    );
    
    EmissionState insert2 = new SimpleEmissionState(
      "insert2",
      Annotation.EMPTY_ANNOTATION,
      new int [] { 0, 1 },
      insert2Dist
    );
    
    model.addState(hub);
    model.addState(match);
    model.addState(insert1);
    model.addState(insert2);
    
    model.createTransition(model.magicalState(), hub);
    model.setTransitionScore(model.magicalState(), hub, 0.0);
    
    model.createTransition(hub, match);
    model.setTransitionScore(hub, match, pMatch);
    model.createTransition(hub, insert1);
    model.setTransitionScore(hub, insert1, pGap);
    model.createTransition(hub, insert2);
    model.setTransitionScore(hub, insert2, pGap);
    model.createTransition(hub, model.magicalState());
    model.setTransitionScore(hub, model.magicalState(), pEnd);
    
    model.createTransition(match, match);
    model.setTransitionScore(match, match, pExtendMatch);
    model.createTransition(match, hub);
    model.setTransitionScore(match, hub, pEndMatch);
    
    model.createTransition(insert1, insert1);
    model.setTransitionScore(insert1, insert1, pExtendGap);
    model.createTransition(insert1, hub);
    model.setTransitionScore(insert1, hub, pEndGap);
    
    model.createTransition(insert2, insert2);
    model.setTransitionScore(insert2, insert2, pExtendGap);
    model.createTransition(insert2, hub);
    model.setTransitionScore(insert2, hub, pEndGap);
    
    return DPFactory.createDP(model);
  }
  
  private static Distribution generateMatchDist(FiniteAlphabet dna2)
  throws Exception {
    Distribution dist = DistributionFactory.DEFAULT.createDistribution(dna2);
    int size = dna2.size();
    int matches = (int) Math.sqrt(size);
    
    double pMatch = 0.5;
    
    double matchWeight = pMatch / matches;
    double missWeigth = (1.0 - pMatch) / (size - matches);
    
    for(Iterator i = dna2.iterator(); i.hasNext(); ) {
      CrossProductSymbol cps = (CrossProductSymbol) i.next();
      List sl = cps.getSymbols();
      if(sl.get(0) == sl.get(1)) {
        dist.setWeight(cps, matchWeight);
      } else {
        dist.setWeight(cps, missWeigth);
      }
    }
    
    return dist;
  }
}

