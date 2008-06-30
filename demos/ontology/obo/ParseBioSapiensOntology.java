/*
 *                  BioJava development code
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
 * Created on Jan 18, 2008
 * 
 */

package ontology.obo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;

import org.biojava.ontology.Ontology;
import org.biojava.ontology.Term;
import org.biojava.ontology.io.OboParser;

public class ParseBioSapiensOntology {

	public static void main(String[] args){
		
		ParseBioSapiensOntology demo = new ParseBioSapiensOntology();
		
		demo.run();
	
	}
	
	public void run(){
		OboParser parser = new OboParser();
		InputStream inStream = this.getClass().getResourceAsStream("/files/ontology/biosapiens.obo");
		
		BufferedReader oboFile = new BufferedReader ( new InputStreamReader ( inStream ) );
		try {
			Ontology ontology = parser.parseOBO(oboFile, "BioSapiens", "the BioSapiens ontology");
						
			Set keys = ontology.getTerms();
			Iterator iter = keys.iterator();
			while (iter.hasNext()){
				Term term = (Term) iter.next();
				System.out.println("TERM: " + term.getName() + " " + term.getDescription());
				System.out.println(term.getAnnotation());
				Object[] synonyms =  term.getSynonyms();
				for ( Object syn : synonyms ) {
					System.out.println(syn);
				}
				
				
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
