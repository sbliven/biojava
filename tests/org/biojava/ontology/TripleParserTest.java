package org.biojava.ontology;

import java.io.*;
import java.util.*;

import junit.framework.TestCase;

import org.biojava.ontology.io.TriplesParser;
import org.biojava.ontology.format.triples.lexer.LexerException;
import org.biojava.ontology.format.triples.parser.ParserException;
import org.biojava.utils.ChangeVetoException;

/**
 *
 *
 * @author Matthew Pocock
 */
public class TripleParserTest extends TestCase {
  public void testParser() throws IOException, LexerException, ParserException, ChangeVetoException {
    ReasoningDomain domain = new ReasoningDomain.Impl();
    domain.addOntology(OntoTools.getIntegerOntology());

    TriplesParser tp = new TriplesParser();
    Ontology onto = tp.parse(
            new PushbackReader(new BufferedReader(new FileReader(
                    new File("C:\\devel\\biojava-live\\resources\\org\\biojava\\ontology\\core.pred")))),
            OntoTools.getDefaultFactory(),
            domain);
    System.err.println("Terms: " + onto.getTerms().size());
    for(Iterator i = onto.getTerms().iterator(); i.hasNext(); ) {
      System.err.println("\t" + i.next());
    }
    System.err.println("Triples: " + onto.getTriples(null, null, null).size());
    for(Iterator i = onto.getTriples(null, null, null).iterator(); i.hasNext(); ) {
      System.err.println("\t" + i.next());
    }
  }
}
