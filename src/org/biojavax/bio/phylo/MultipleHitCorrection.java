
import java.io.*;
import java.lang.*;
import java.util.*;

import org.biojavax.bio.phylo.io.nexus.*;

public class MultipleHitCorrection {
	
	 public static double JukesCantor(String taxa1, String taxa2){
			
		taxa1 = taxa1.replace(" ", "");
		taxa2 = taxa2.replace(" ", "");
		
		int length = taxa1.length();
		
		if(length == taxa2.length()){
		
			double counter = 0.0;	
			for( int i = 0 ; i < length; i++){
				if(taxa1.charAt(i) != taxa2.charAt(i))
					counter++;
			}
							
			double p = counter/ (double) length;	
			return (-0.75 * Math.log(1.0-(4.0/3.0)*p));
		}else{
			System.out.println("Error: Sequence Length dose not match!\n");
			return 0.0;
		}
	}	

	public static double KimuraTwoParameter(String taxa1, String taxa2){
		
		taxa1 = taxa1.replace(" ","");
		taxa2 = taxa2.replace(" ","");

		int length = taxa1.length();

		if(length == taxa2.length()){
		
			double counter1 = 0.0;
			double counter2 = 0.0;

			for( int i = 0; i < length; i++){
			
				if(taxa1.charAt(i) != taxa2.charAt(i)){
					if((taxa1.charAt(i) == 'A' && taxa2.charAt(i) == 'G') || (taxa1.charAt(i) == 'G' && taxa2.charAt(i) == 'A')){
						counter1++;
					}else if((taxa1.charAt(i) == 'T' && taxa2.charAt(i) == 'C') || (taxa1.charAt(i) == 'C' && taxa2.charAt(i) == 'T')){
						counter1++;
					}else{
						counter2++;
					}
				}
			}	
			double p = counter1 / (double) length;
			double q = counter2 / (double) length;

			return ( (0.5)*Math.log(1.0/(1.0 - 2.0*p - q)) + (0.25)*Math.log(1.0/(1.0 - 2.0*q)));	
		}else{
			System.out.println("Error: Sequence Length dose not match!\n");
			return 0.0;
		}
	}

}

