import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;
import org.biojava.bio.dp.*;

public class Dice
{
    public static void main(String[] args) throws Exception
    {
    	Residue[] rolls=new Residue[6];
    
    	SimpleAlphabet diceAlphabet=new SimpleAlphabet();
    	diceAlphabet.setName("DiceAlphabet");
    
    	for(int i=1;i<7;i++)
    	{
	    rolls[i-1]= new SimpleResidue((char)('0'+i),""+i,Annotation.EMPTY_ANNOTATION);
	    diceAlphabet.addResidue(rolls[i-1]);
	}  
  
  int [] advance = { 1 };
	AbstractState fair   = StateFactory.createState(diceAlphabet, advance, "fair");
	AbstractState loaded = StateFactory.createState(diceAlphabet, advance, "loaded");
	
	SimpleMarkovModel casino=new SimpleMarkovModel(1, diceAlphabet);
	casino.addState(fair);
	casino.addState(loaded);
	
	casino.createTransition(casino.magicalState(),fair);
	casino.createTransition(casino.magicalState(),loaded);
	casino.createTransition(fair,casino.magicalState());
	casino.createTransition(loaded,casino.magicalState());
	casino.createTransition(fair,loaded);
	casino.createTransition(loaded,fair);
	casino.createTransition(fair,fair);
	casino.createTransition(loaded,loaded);
	
	for(int i=1;i<rolls.length;i++)
	{
	    fair.setWeight(rolls[i],-Math.log(6));
	    loaded.setWeight(rolls[i],-Math.log(10));
	}
	loaded.setWeight(rolls[5],-Math.log(2));
	
	casino.setTransitionScore(fair,loaded,Math.log(0.04));
	casino.setTransitionScore(loaded,fair,Math.log(0.09));
	casino.setTransitionScore(loaded,loaded,Math.log(0.9));
	casino.setTransitionScore(fair,fair,Math.log(0.95));
	
	casino.setTransitionScore(casino.magicalState(),fair,Math.log(0.8));
	casino.setTransitionScore(casino.magicalState(),loaded,Math.log(0.2));
	casino.setTransitionScore(fair,casino.magicalState(),Math.log(0.01));
	casino.setTransitionScore(loaded,casino.magicalState(),Math.log(0.01));
	
	DP dp=DPFactory.createDP(casino);
	StatePath obs_rolls = dp.generate(300);
	
	for(int i = 1; i <= obs_rolls.length(); i++) {
	  System.out.println(i + " " +
	    obs_rolls.residueAt(StatePath.SEQUENCE, i).getSymbol() + " " +
	    obs_rolls.residueAt(StatePath.STATES, i).getSymbol()
          );
	}
	
    }
}
