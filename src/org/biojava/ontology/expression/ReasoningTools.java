package org.biojava.ontology.expression;

import java.io.PushbackReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import org.biojava.ontology.format.triples.lexer.LexerException;
import org.biojava.ontology.format.triples.parser.ParserException;

/**
 *
 *
 * @author Matthew Pocock
 */
public class ReasoningTools {
  public static Namespace CORE;

  public static final Atom AND;
  public static final Atom OR;
  public static final Atom IMPLIES;
  public static final Expression ANYTHING;
  public static final Variable ANYTHING_VAR;
  public static final Atom ANY;
  public static final Atom INSTANCE_OF;

  static
  {
    try {
      CORE = loadNamespaceFromResource("org/biojava/ontology/core.pred");
    } catch (IOException e) {
      throw new ExceptionInInitializerError(e);
    } catch (LexerException e) {
      throw new ExceptionInInitializerError(e);
    } catch (ParserException e) {
      throw new ExceptionInInitializerError(e);
    }

    AND = CORE.getAtomByName("and");
    OR = CORE.getAtomByName("or");
    IMPLIES = CORE.getAtomByName("implies");
    ANY = CORE.getAtomByName("any");
    INSTANCE_OF = CORE.getAtomByName("instance_of");

    ANYTHING = CORE.getExpressionByName("everything_in_any");
    ANYTHING_VAR = (Variable) ANYTHING.getVariables().iterator().next();
  }

  private ReasoningTools() {}

  public static Namespace loadNamespaceFromResource(String resName)
          throws IOException, LexerException, ParserException
  {
    return loadNamespaceFromResource(resName,
                                     ReasoningTools.class.getClassLoader());
  }

  public static Namespace loadNamespaceFromResource(String resName,
                                                    ClassLoader classLoader)
          throws IOException, LexerException, ParserException
  {
    TriplesParser tp = new TriplesParser();
    return tp.parse(
            new PushbackReader(new BufferedReader(new InputStreamReader(
                    classLoader.getResourceAsStream(resName)))));
  }
}
