package dp;

import java.io.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.dist.*;
import org.biojava.bio.dp.*;
import org.biojava.bio.dp.onehead.*;

public class PatternFinder {
  public static void main(String[] args)
  throws Throwable {
    if(args.length != 2) {
      System.err.println("Use: dp.PatternFinder seqFile length");
      System.exit(1);
    }

    File seqFile = new File(args[0]);

    if(!seqFile.exists()) {
      System.err.println("Sequence file must exist: " + seqFile);
      System.exit(1);
    }

    SequenceDB seqDB = loadSequenceDB(seqFile);

    int length = Integer.parseInt(args[1]);

    MarkovModel model = createModel(length);
    DP dp = new SingleDP(model);
    BaumWelchTrainer bwt = new BaumWelchTrainer(dp);
    
    StoppingCriteria sc = new StoppingCriteria() {
      public boolean isTrainingComplete(TrainingAlgorithm ta) {
        System.out.println("Cycle: " + ta.getCycle() + " score: " + ta.getCurrentScore() + " " + (ta.getCurrentScore() - ta.getLastScore()) );
        if(ta.getCycle() < 5) {
          return false;
        } else {
          return true;
        }
      }
    };

    bwt.train(seqDB, 5.0, sc);
  }

  private static SequenceDB loadSequenceDB(File seqFile)
  throws Throwable {
    SequenceDB seqDB = new HashSequenceDB();
    SequenceIterator si = SeqIOTools.readFastaDNA(
      new BufferedReader(
        new FileReader(
          seqFile)));

    while(si.hasNext()) {
      seqDB.addSequence(si.nextSequence());
    }

    return seqDB;
  }

  private static MarkovModel createModel(int length)
  throws Throwable {
    FiniteAlphabet alpha = DNATools.getDNA();
    DistributionFactory dFact = DistributionFactory.DEFAULT;
    int[] advance = new int[] { 1 };

    State[] pattern = new State[length];

    for(int i = 0; i < length; i++) {
      pattern[i] = new SimpleEmissionState(
        "e-" + i,
        Annotation.EMPTY_ANNOTATION,
        advance,
        dFact.createDistribution(alpha) );
    }

    State nullModel = new SimpleEmissionState(
      "nm",
      Annotation.EMPTY_ANNOTATION,
      advance,
      new UniformDistribution(alpha) );


    MarkovModel model = new SimpleMarkovModel(
      advance.length,
      alpha,
      "pattern finder");
    
    for(int i = 0; i < length; i++) {
      model.addState(pattern[i]);
    }
    model.addState(nullModel);

    model.createTransition(model.magicalState(), nullModel);
    model.createTransition(nullModel, model.magicalState());
    model.createTransition(nullModel, nullModel);
    model.createTransition(nullModel, pattern[0]);
    model.createTransition(pattern[length - 1], nullModel);

    for(int i = 1; i < length; i++) {
      model.createTransition(pattern[i-1], pattern[i]);
    }

    for(Iterator i = model.stateAlphabet().iterator(); i.hasNext(); ) {
      State s = (State) i.next();
      if(s instanceof EmissionState) {
        EmissionState es = (EmissionState) s;
        DistributionTools.randomizeDistribution(es.getDistribution());
      }
      DistributionTools.randomizeDistribution(model.getWeights(s));
    }
    
    return model;
  }
}
