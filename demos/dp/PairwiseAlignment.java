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
        0.5, 0.8,
        0.2, 0.8
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
          Sequence [] seqs = new Sequence [] {
            sourceSeq, targetSeq
          };
          System.out.println(
            "Aligning " + sourceSeq.getName() + ":" + targetSeq.getName()
          );

          // tests minimal memory forwards
          double forwardMin;
          System.out.println("Forwards-:");
          forwardMin = aligner.forward(seqs, DP.PROBABILITY);
          System.out.println("\t" + forwardMin);
          forwardMin = aligner.forward(seqs, DP.ODDS);
          System.out.println("\t" + forwardMin);
          forwardMin = aligner.forward(seqs, DP.NULL_MODEL);
          System.out.println("\t" + forwardMin);
          
          // uncomment to test explicit memory forwards
          //double forwardMax = aligner.forwardMatrix(seqs).getScore();
          //System.out.println("Forwards+: " + forwardMax);

          // tests explicit memory backwards
          double backward;
          System.out.println("Backwards:");
          backward = aligner.backward(seqs, DP.PROBABILITY);
          System.out.println("\t" + backward);
          backward = aligner.backward(seqs, DP.ODDS);
          System.out.println("\t" + backward);
          backward = aligner.backward(seqs, DP.NULL_MODEL);
          System.out.println("\t" + backward);
          
          // tests minimal memory viterbi
          StatePath result;
          System.out.println("Viterbi:");
          result = aligner.viterbi(seqs, DP.PROBABILITY);
          System.out.println("\t" + result.getScore());
          result = aligner.viterbi(seqs, DP.ODDS);
          System.out.println("\t" + result.getScore());
          result = aligner.viterbi(seqs, DP.NULL_MODEL);
          System.out.println("\t" + result.getScore());
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
    
    Distribution nullModel = new UniformDistribution(dna);
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
    
    Distribution dist;
    
    model.createTransition(model.magicalState(), hub);
    
    model.createTransition(hub, match);
    model.createTransition(hub, insert1);
    model.createTransition(hub, insert2);
    model.createTransition(hub, model.magicalState());

    model.createTransition(match, match);
    model.createTransition(match, hub);

    model.createTransition(insert1, insert1);
    model.createTransition(insert1, hub);

    model.createTransition(insert2, insert2);
    model.createTransition(insert2, hub);

    model.getWeights(model.magicalState()).setWeight(hub, 1.0);

    dist = model.getWeights(hub);
    dist.setWeight(match, pMatch);
    dist.setWeight(insert1, pGap);
    dist.setWeight(insert2, pGap);
    dist.setWeight(model.magicalState(), pEnd);

    dist = model.getWeights(match);
    dist.setWeight(match, pExtendMatch);
    dist.setWeight(hub, pEndMatch);

    dist = model.getWeights(insert1);    
    dist.setWeight(insert1, pExtendGap);
    dist.setWeight(hub, pEndGap);

    dist = model.getWeights(insert2);    
    dist.setWeight(insert2, pExtendGap);
    dist.setWeight(hub, pEndGap);
    
    return DPFactory.createDP(model);
  }
  
  private static Distribution generateMatchDist(FiniteAlphabet dna2)
  throws Exception {
    Distribution dist = DistributionFactory.DEFAULT.createDistribution(dna2);
    int size = dna2.size();
    int matches = (int) Math.sqrt(size);
    
    double pMatch = 0.15;
    
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

