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

package org.biojava.bio.ontology.io; 
 
import java.util.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.ontology.*;
import org.biojava.bio.seq.io.ParseException;

/**
 * Simple parser for the Gene Ontology (GO) flatfile format.
 *
 * @author Thomas Down
 * @since 1.4
 */
 
public class GOParser {
    public Ontology parseGO(BufferedReader goFile, OntologyFactory factory)
        throws ParseException, IOException
    {
        try {
            Ontology onto = factory.createOntology("go", "");
            Term isa = onto.importTerm(OntoTools.IS_A);
            Term partof = onto.importTerm(OntoTools.PART_OF);
            List termStack = new ArrayList();
            String line;
            while ((line = goFile.readLine()) != null) {
                int leadSpaces = 0;
                while (line.charAt(leadSpaces) == ' ') {
                    ++leadSpaces;
                }
                line = line.trim();
                if (line.startsWith("!")) {
                    continue;
                } 
                
                StringTokenizer toke = new StringTokenizer(line, "%<$", true);
                String parentRel = toke.nextToken();
                Term term = parseTerm(onto, toke.nextToken());
                if (parentRel.equals("%")) {
                    safeAddTriple(onto, term, (Term) termStack.get(leadSpaces - 1), isa);
                } else if (parentRel.equals("<")) {
                    safeAddTriple(onto, term, (Term) termStack.get(leadSpaces - 1), partof);
                } 
                while (toke.hasMoreTokens()) {
                    String altRel = toke.nextToken();
                    Term altTerm = parseTerm(onto, toke.nextToken());
                    if (altRel.equals("%")) {
                        safeAddTriple(onto, term, altTerm, isa);
                    } else if (altRel.equals("<")) {
                        safeAddTriple(onto, term, altTerm, partof);
                    }
                }
                
                if (termStack.size() == leadSpaces) {
                    termStack.add(term);
                } else {
                    termStack.set(leadSpaces, term);
                }
            }
            return onto;
        } catch (AlreadyExistsException ex) {
            throw new ParseException(ex, "Duplication in ontology");
        } catch (OntologyException ex) {
            throw new ParseException(ex);
        } catch (ChangeVetoException ex) {
            throw new BioError(ex, "Error accessing newly created ontology");
        }
    }
    
    private void safeAddTriple(Ontology onto, Term s, Term o, Term p) 
        throws AlreadyExistsException, ChangeVetoException
    {
        if (!onto.containsTriple(s, o, p)) {
            onto.createTriple(s, o, p);
        }
    }
    
    private Term parseTerm(Ontology onto, String s) 
        throws ParseException, AlreadyExistsException, ChangeVetoException
    {
        int semi = s.indexOf(';');
        int semi2 = s.indexOf(';', semi + 1);
        if (semi < 0) {
            throw new ParseException("No semicolon in " + s);
        }
        String termDesc = s.substring(0, semi).trim();
        String termName;
        if (semi2 < 0) {
            termName = s.substring(semi + 1).trim();
        } else {
            termName = s.substring(semi + 1, semi2).trim();
        }
        StringTokenizer toke = new StringTokenizer(termName, ", ");
        termName = toke.nextToken();
        if (onto.containsTerm(termName)) {
            return onto.getTerm(termName);
        } else {
            Term t = onto.createTerm(termName, termDesc);
            if (toke.hasMoreTokens()) {
                List secondaries = new ArrayList();
                while (toke.hasMoreTokens()) {
                    secondaries.add(toke.nextToken());
                }
                t.getAnnotation().setProperty("go.secondary_ids", secondaries);
            }
            return t;
        }
    }
}
 
 
