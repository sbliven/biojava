package org.biojava.bio.ontology.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.biojava.bio.BioError;
import org.biojava.bio.ontology.AlreadyExistsException;
import org.biojava.bio.ontology.Ontology;
import org.biojava.bio.ontology.OntologyException;
import org.biojava.bio.ontology.OntologyFactory;
import org.biojava.bio.ontology.Term;
import org.biojava.bio.ontology.Triple;
import org.biojava.utils.ChangeVetoException;

/**
 * Parse tab-delimited ontology files into Ontology objects.
 *
 * <p>
 * The tab-delimited ontology files have three types of lines. Lines that are
 * pure white space can be discarded. Comment lines begin with a hash (#) and
 * can be discarded. The payload lines contain three fields seperated by tabs.
 * These are <code>subject</code>, <code>relation</code> and
 * <code>object</code>.
 * By convention, the content of each field contains no spaces.
 * </p>
 *
 * <p>
 * By convention, if there are comment lines beginning with <code>name:</code>
 * or <code>description:</code> and these appear before any relation
 * declarations then they become the name and description of the ontology.
 * Otherwise, the name and description will be the empty string.
 * </p>
 *
 * <p>
 * Term names normally will be just a term name like <code>relation</code> or
 * <code>person</code>. There are also terms that represent collections of
 * triples. For example, here is the declaration for the 'triple' type in
 * the core ontology.
 * </p>
 *
 * <code><pre>
 * ...
 * triple	is-a	any
 * triple	has-a	source
 * triple	has-a	target
 * triple	has-a	relation
 * (triple,has-a,any)	size	3
 * ...
 * </pre></code>
 *
 * <p>
 * The first four lines just associate triple with some type with a relation
 * (e.g. is-a or has-a). The fifth line says that something must have a size of
 * three. The 'something' is <code>(triple,has-a,any)	size	3</code> and is
 * short-hand for a collection of triples that state that the source must be
 * <code>triple</code>, the target must be <code>any</code> and the relation
 * must be <code>has-a</code>. This whole expression states that a triple
 * has exactly three has-a relationships; that is, exactly three properties.
 * </p>
 *
 * @author Matthew Pocock
 */
public class TabDelimParser {
  /**
   * Parse an ontology from a reader.
   * The reader will be emptied of text. It is the caller's responsibility to
   * close the reader.
   *
   * @param in  the BufferedReader to read from
   * @param of  an OntologyFactory used to create the Ontology instance
   * @return  a new Ontology
   * @throws IOException if there is some problem with the buffered reader
   * @throws OntologyException if it was not possible to instantiate a new
   *         ontology
   */
  public Ontology parse(BufferedReader in, OntologyFactory of)
  throws IOException, OntologyException {
    String name = "";
    String description = "";
    Ontology onto = null;
    
    for(
      String line = in.readLine();
      line != null;
      line = in.readLine()
    ) {
      line = line.trim();
      if(line.length() > 0) {
        if(line.startsWith("#")) {
          // comment line - let's try to pull out name or description
          
          if(line.startsWith("#name:")) {
            name = line.substring("#name:".length()).trim();
          } else if(line.startsWith("#description:")) {
            description = line.substring("#description:".length()).trim();
          }
        } else {
          try {
            // make sure we have an ontology
            if(onto == null) {
              onto = of.createOntology(name, description);
            }
            
            // build a tripple
            
            /*
            
            int t1 = line.indexOf("\t");
            int t2 = line.indexOf("\t", t1 + 1);
            
            String subject  = line.substring(0, t1);
            String relation = line.substring(t1 + 1, t2);
            String object   = line.substring(t2 + 1);
            
            */
            
            StringTokenizer toke = new StringTokenizer(line);
            String subject = toke.nextToken();
            String relation = toke.nextToken();
            String object = toke.nextToken();
            
            Term subT = resolveTerm(subject, onto);
            Term objT = resolveTerm(object, onto);
            Term relT = resolveTerm(relation, onto);
            
            Triple trip = resolveTriple(subT, objT, relT, onto);
          } catch (StringIndexOutOfBoundsException e) {
            throw new IOException("Could not parse line: " + line);
          }
        }
      }
    }
    
    return onto;
  }
  
  private Term resolveTerm(String termName, Ontology onto) {
    boolean isTrippleTerm = termName.startsWith("(") && termName.endsWith(")");
    
    if(onto.containsTerm(termName)) {
      return onto.getTerm(termName);
    } else {
      try {
        if(isTrippleTerm) {
          int c1 = termName.indexOf(",");
          int c2 = termName.indexOf(",", c1 + 1);
          
          String source = termName.substring(1, c1);
          String target = termName.substring(c2 + 1, termName.length() - 1);
          String relation = termName.substring(c1 + 1, c2);
          
          Term st = resolveTerm(source, onto);
          Term tt = resolveTerm(target, onto);
          Term rt = resolveTerm(relation, onto);
          
          return onto.createTripleTerm(st, tt, rt);
        } else {
          return onto.createTerm(termName, "");
        }
      } catch (AlreadyExistsException aee) {
        throw new BioError(aee, "Assertion Failure: Could not create term");
      } catch (ChangeVetoException cve) {
        throw new BioError(cve, "Assertion Failure: Could not create term");
      }
    }
  }
  
  private Triple resolveTriple(Term sub, Term obj, Term rel, Ontology onto) {
    if(onto.containsTriple(sub, obj, rel)) {
      return (Triple) onto.getTriples(sub, obj, rel).iterator().next();
    } else {
      try {
        return onto.createTriple(sub, obj, rel);
      } catch (AlreadyExistsException aee) {
        throw new BioError(aee, "Assertion Failure: Could not create triple");
      } catch (ChangeVetoException cve) {
        throw new BioError(cve, "Assertion Failure: Could not create triple");
      }
    }
  }
}
