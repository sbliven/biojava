package org.biojava.bio.program.hmmer;

import org.biojava.bio.dp.*;
import org.biojava.bio.dist.*;
import org.biojava.bio.symbol.*;
import org.biojava.utils.*;
import java.util.*; 
import org.biojava.bio.*;

public class ProfileEmissionState extends SimpleEmissionState{
	
	public ProfileEmissionState(String str, Annotation ann, int[] adv, Distribution dis){
	  super(str, ann,adv, dis);
	}
	
	public double logProb(Symbol sym) throws IllegalSymbolException{
	 return log2(getDistribution().getWeight(sym)); 
	}
	
	protected static double log2(double x){
		return Math.log(x)/Math.log(2);
	}
    
}
