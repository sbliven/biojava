import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;
import org.biojava.bio.dp.*;

public class Dice
{
    public static void main(String[] args) throws Exception
    {
    	Residue[] rolls=new Residue[6];
	
    	//set up the dice alphabet
    	SimpleAlphabet diceAlphabet=new SimpleAlphabet();
    	diceAlphabet.setName("DiceAlphabet");
    
    	for(int i=1;i<7;i++)
    	{
	    rolls[i-1]= new SimpleResidue((char)('0'+i),""+i,Annotation.EMPTY_ANNOTATION);
	    diceAlphabet.addResidue(rolls[i-1]);
	}  
  
  int [] advance = { 1 };
	EmissionState fair   = StateFactory.DEFAULT.createState(diceAlphabet, advance, "fair");
	EmissionState loaded = StateFactory.DEFAULT.createState(diceAlphabet, advance, "loaded");
	
	SimpleMarkovModel casino=new SimpleMarkovModel(1, diceAlphabet);
	casino.addState(fair);
	casino.addState(loaded);
	
	//set up transitions between states.
	casino.createTransition(casino.magicalState(),fair);
	casino.createTransition(casino.magicalState(),loaded);
	casino.createTransition(fair,casino.magicalState());
	casino.createTransition(loaded,casino.magicalState());
	casino.createTransition(fair,loaded);
	casino.createTransition(loaded,fair);
	casino.createTransition(fair,fair);
	casino.createTransition(loaded,loaded);
	
	//set up emission probabilities.
	for(int i=0;i<rolls.length;i++)
	{
	    fair.setWeight(rolls[i],-Math.log(6));
	    loaded.setWeight(rolls[i],-Math.log(10));
	}
	loaded.setWeight(rolls[5],-Math.log(2));
	
	//set up transition scores.
	casino.setTransitionScore(casino.magicalState(),fair,  Math.log(0.8));
	casino.setTransitionScore(casino.magicalState(),loaded,Math.log(0.2));

	casino.setTransitionScore(fair,loaded,               Math.log(0.04));
	casino.setTransitionScore(fair,fair,                 Math.log(0.95));
	casino.setTransitionScore(fair,casino.magicalState(),Math.log(0.01));
	
	casino.setTransitionScore(loaded,fair,                 Math.log(0.09));
	casino.setTransitionScore(loaded,loaded,               Math.log(0.90));
	casino.setTransitionScore(loaded,casino.magicalState(),Math.log(0.01));
	
	DP dp=DPFactory.createDP(casino);
	StatePath obs_rolls = dp.generate(300);
	
	ResidueList roll_sequence = obs_rolls.residueListForLabel(StatePath.SEQUENCE);
	ResidueList[] res_array = {roll_sequence};
	StatePath v = dp.viterbi(res_array);
	
	//print out obs_sequence, output, state symbols.
	for(int i = 1; i <= obs_rolls.length(); i++) {
	  System.out.println(i + " " +
	    obs_rolls.residueAt(StatePath.SEQUENCE, i).getSymbol() + " " +
	    obs_rolls.residueAt(StatePath.STATES, i).getSymbol() + " " + 
	    v.residueAt(StatePath.STATES, i).getSymbol()
          );
	}	
    }
}
