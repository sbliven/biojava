package dp;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.dist.*;
import org.biojava.bio.dp.*;

public class SearchProfile {
  public static Distribution nullModel;
  
  public static void main(String [] args) {
    try {
      File seqFile = new File(args[0]);

      FiniteAlphabet PROTEIN = ProteinTools.getAlphabet();
      nullModel = new UniformDistribution(PROTEIN);
      
      System.out.println("Loading sequences");
      SequenceDB seqDB = readSequenceDB(seqFile, PROTEIN);
      
      System.out.println("Creating profile HMM");
      ProfileHMM profile = createProfile(seqDB, PROTEIN);

    
      System.out.println("make dp object");
      DP dp = DPFactory.createDP(profile);
//      dumpDP(dp);

      Sequence [] seq1 = { seqDB.sequenceIterator().nextSequence() };
      System.out.println("Viterbi: " + dp.viterbi(seq1).getScore());
      System.out.println("Forward: " + dp.forward(seq1));
      System.out.println("Backward: " + dp.backward(seq1));
      
      System.out.println("Training whole profile");
      TrainingAlgorithm ta = new BaumWelchTrainer(dp);
      ta.train(seqDB, 5, new StoppingCriteria() {
        public boolean isTrainingComplete(TrainingAlgorithm ta) {
          System.out.println("Cycle " + ta.getCycle() + " completed");
          System.out.println("Score: " + ta.getCurrentScore());
          if(ta.getCycle() == 5) {
            return true;
          } else {
            return false;
          }
        }
      });

      System.out.println("Alignining sequences to the model");
      for(SequenceIterator si = seqDB.sequenceIterator(); si.hasNext(); ) {
        Sequence seq = si.nextSequence();
        SymbolList [] rl = { seq };
        StatePath statePath = dp.viterbi(rl);
        double fScore = dp.forward(rl);
        double bScore = dp.backward(rl);
      
        System.out.println(
          seq.getName() +
          " viterbi: " + statePath.getScore() +
          ", forwards: " + fScore +
          ", backwards: " + bScore
        );
        for(int i = 0; i <= statePath.length() / 60; i++) {
          for(int j = i*60; j < Math.min((i+1)*60, statePath.length()); j++) {
            System.out.print(statePath.symbolAt(StatePath.SEQUENCE, j+1).getToken()); 
          }
          System.out.print("\n");
          for(int j = i*60; j < Math.min((i+1)*60, statePath.length()); j++) {
            System.out.print(statePath.symbolAt(StatePath.STATES, j+1).getToken()); 
          }
          System.out.print("\n");
          System.out.print("\n");
        }
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }    
  }
  
  private static ProfileHMM createProfile(SequenceDB seqs, Alphabet alpha)
  throws Exception {
    double l = 0;
    for(SequenceIterator i = seqs.sequenceIterator(); i.hasNext(); ) {
      l+=Math.log(i.nextSequence().length());
    }
    l /= seqs.ids().size();
    int length = (int) Math.exp(l);
    
    System.out.println("Estimating alignment as having length " + length);
    ProfileHMM profile = new ProfileHMM(
      alpha, length,
      DistributionFactory.DEFAULT, DistributionFactory.DEFAULT
    );
    
    randomize(profile);
    
    return profile;
  }

  public static SequenceDB readSequenceDB(File seqFile, Alphabet alpha)
  throws Exception {
    HashSequenceDB seqDB = new HashSequenceDB(IDMaker.byName);
    
    SequenceFactory sFact = new SimpleSequenceFactory();
    FastaFormat fFormat = new FastaFormat();
    SequenceIterator stateI = null;

    for(
      SequenceIterator seqI = new StreamReader(
        new FileInputStream(seqFile),
        fFormat,
        alpha.getParser("symbol"),
        sFact
      );
      seqI.hasNext();
    ) {
      Sequence seq = seqI.nextSequence();
      seqDB.addSequence(seq);
    }
    
    return seqDB;
  }
  
  private static void randomize(MarkovModel model) throws Exception {
    ModelTrainer mt = new SimpleModelTrainer();
    mt.registerModel(model);
    mt.setNullModelWeight(5.0);
    
    for(Iterator i = model.stateAlphabet().symbols().iterator(); i.hasNext(); ) {
      State s = (State) i.next();
      if(s instanceof EmissionState && !(s instanceof MagicalState) ) {
        EmissionState es = (EmissionState) s;
        Distribution dis = es.getDistribution();
        FiniteAlphabet fa = (FiniteAlphabet) dis.getAlphabet();
        for(
          Iterator j = fa.iterator();
          j.hasNext();
        ) {
          Symbol r = (Symbol) j.next();
          mt.addCount(es.getDistribution(), r, Math.random());
        }
      }
      Distribution dist = model.getWeights(s);
      for(Iterator j = model.transitionsFrom(s).iterator(); j.hasNext(); ) {
        State t = (State) j.next();
        mt.addCount(dist, t, Math.random());
      }
    }
    
    mt.train();
    mt.clearCounts();
  }
  
  private static void dumpDP(DP dp) {
    State [] states = dp.getStates();
    
    System.out.print("states: ");
    for(int i = 0; i < states.length; i++) {
      System.out.print(" " + states[i].getName());
    }
    System.out.println("\n");
    
    int [][] forwardT = dp.getForwardTransitions();
    double [][] forwardTS = dp.getForwardTransitionScores();
    for(int i = 0; i < states.length; i++) {
      System.out.print("Transitions from " + i + ": ");
      for(int j = 0; j < forwardT[i].length; j++) {
        System.out.print(
          " " + forwardT[i][j] + "(" +
          forwardTS[i][j] + ")"
        );
      }
    }
  }
}
