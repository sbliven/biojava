package org.biojava.ontology.expression;

import org.biojava.ontology.format.triples.lexer.LexerException;
import org.biojava.ontology.format.triples.lexer.Lexer;
import org.biojava.ontology.format.triples.parser.ParserException;
import org.biojava.ontology.format.triples.parser.Parser;
import org.biojava.ontology.format.triples.node.*;
import org.biojava.ontology.format.triples.analysis.DepthFirstAdapter;

import java.io.PushbackReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 *
 *
 * @author Matthew Pocock
 */
public class TriplesParser {
  public Namespace parse(PushbackReader reader)
          throws IOException, LexerException, ParserException
  {
    Lexer lexer = new Lexer(reader);
    Parser parser = new Parser(lexer);
    Node ast = parser.parse();
    ast.apply(new org.biojava.ontology.io.TriplesParser.ListRewriter());
    Processor processor = new Processor();
    ast.apply(processor);
    return processor.getNamespace();
  }

  public static class Processor
          extends DepthFirstAdapter {
    private Namespace namespace;

    private int counter;
    private int depth;
    private final Map anonymousNode2Term;
    private final Map anonymousName2Term;

    private String exprName;

    public Processor()
    {
      anonymousNode2Term = new HashMap();
      anonymousName2Term = new HashMap();
    }

    public Namespace getNamespace()
    {
      return namespace;
    }

    public void inAProgram(AProgram aProgram)
    {
      counter = 0;
    }

    public void outANamespaceDecl(ANamespaceDecl aNamespaceDecl)
    {
      PFullName fullName = aNamespaceDecl.getFullName();
      String name = nameToString(fullName);
      String description = makeDescription(aNamespaceDecl.getComment());
      //System.err.println("Creating namespace name: " + name + " description: " + description);
      //try {
        namespace = new NamespaceImpl(name, description);
      //} catch (OntologyException e) {
      //  throw new RuntimeException("Can't create ontology for node: " + aNamespaceDecl, e);
      //}
    }

    public void outAImportExpr(AImportExpr aImportExpr)
    {
      PFullName fullName = aImportExpr.getFullName();
      ACompoundFullName cfn = (ACompoundFullName) fullName;
      String ontoName = cfn.getName().getText();
      PFullName current = cfn.getFullName();

      while(current instanceof ACompoundFullName) {
        ACompoundFullName acfn = (ACompoundFullName) current;
        ontoName = ontoName + "." + acfn.getName().getText();
        current = acfn.getFullName();
      }
      String termName = ((ASimpleFullName) current).getName().getText();
      Atom term = namespace.createImportedAtom(termName, ontoName);
      namespace.addAtom(term);
    }

    public void inAExpressionDecl(AExpressionDecl aExpressionDecl)
    {
      counter++;
      depth = 0;

      exprName = null;
      if(aExpressionDecl.getExpressionName() != null) {
        AExpressionName aExprName
                = (AExpressionName) aExpressionDecl.getExpressionName();
        exprName = aExprName.getName().getText();
      }
      //System.err.println("Processing expression: " + counter + ": " + aExpressionDecl);
    }

    public void outAExpressionDecl(AExpressionDecl aExpressionDecl)
    {
      anonymousNode2Term.clear();
      anonymousName2Term.clear();
      //System.err.println("Leaving expression: " + counter + ": " + aExpressionDecl);
    }

    public void inASimpleExpression(ASimpleExpression aSimpleExpression)
    {
      depth++;
    }

    public void outASimpleExpression(ASimpleExpression aSimpleExpression)
    {
      PValue value = aSimpleExpression.getValue();
      String name = valueToString(value);
      String desc = makeDescription(aSimpleExpression.getComment());

      if(depth == 1) {
        Atom term = namespace.createAtom(name, desc);
        namespace.addAtom(term);
        register(aSimpleExpression, name, term);
      } else {
        ExpressionPart term = (ExpressionPart) resolve(value);
        register(aSimpleExpression, name, term);
      }

      depth--;
    }

    public void inAComplexExpression(AComplexExpression aComplexExpression)
    {
      depth++;
    }

    public void outAComplexExpression(AComplexExpression aComplexExpression)
    {
      ExpressionPart subject = (ExpressionPart) resolve(aComplexExpression.getSubject());
      ExpressionPart object = (ExpressionPart) resolve(aComplexExpression.getObject());
      Terminal predicate = (Terminal) resolve(aComplexExpression.getPredicate());

      Triple triple;

      if(depth == 1) {
        String name = exprName;
        String desc = makeDescription(aComplexExpression.getComment());
        Expression expression = namespace.createExpression(
                subject, object, predicate, name, desc);
        namespace.addExpression(expression);
        triple = expression;
      } else {
        triple = namespace.createSubExpression(subject, object, predicate);
      }

      register(aComplexExpression, aComplexExpression.toString(), triple);

      depth--;
    }

    public void outANamePredicate(ANamePredicate aNamePredicate)
    {
      //System.err.println("Predicate name: " + aNamePredicate);
      PFullName fullName = aNamePredicate.getFullName();

      try {
        String name = nameToString(fullName);
        Term term = namespace.getAtomByName(name);

        if(term == null) {
          throw new NoSuchElementException("Term not found: " + name);
        }

        register(aNamePredicate, name, term);
      } catch (NoSuchElementException e) {
        Token name = nid2Token(fullName);
        throw new RuntimeException("Can't resolve identifier: [" +
                                   name.getLine() + "," +
                                   name.getPos() + "] '" +
                                   fullName.toString() + "'");
      }
    }

    public void outAVariablePredicate(AVariablePredicate aVariablePredicate)
    {
      //System.err.println("Predicate variable: " + aVariablePredicate);
      TVariable tVariable = aVariablePredicate.getVariable();
      String name = tVariable.getText();
      name = name + ":" + counter;

      Term varTerm = resolveName(name);
      if(varTerm != null) {
        register(aVariablePredicate, name, varTerm);
        return;
      }

      varTerm = namespace.createVariable(name);
      register(aVariablePredicate, name, varTerm);
    }

    public void outANameValue(ANameValue aNameValue)
    {
      //System.err.println("Value name: " + aNameValue);
      PFullName fullName = aNameValue.getFullName();

      if(depth == 1) {
        return;
      }

      if(fullName instanceof ACompoundFullName) {
        ACompoundFullName cfn = (ACompoundFullName) fullName;
        String ontoName = cfn.getName().getText();
        PFullName current = cfn.getFullName();

        while(current instanceof ACompoundFullName) {
          ACompoundFullName acfn = (ACompoundFullName) current;
          ontoName = ontoName + "." + acfn.getName().getText();
          current = acfn.getFullName();
        }

        String termName = ((ASimpleFullName) current).getName().getText();

        //System.err.println("Remote name: '" + termName + "' in '" + ontoName + "'");

        ImportedAtom term = namespace.createImportedAtom(termName, ontoName);
        register(aNameValue, aNameValue.toString(), term);
      } else {
        String name = nameToString(fullName);
        //System.err.println("Simple name: '" + name + "'");

        Term term = namespace.getAtomByName(name);
        register(aNameValue, name, term);
      }
    }

    public void outAVariableValue(AVariableValue aVariableValue)
    {
      //System.err.println("Variable value: " + aVariableValue);
      TVariable tVariable = aVariableValue.getVariable();
      String name = tVariable.getText();
      name = name + ":" + counter;

      Term varTerm = resolveName(name);
      if(varTerm != null) {
        register(aVariableValue, name, varTerm);
        return;
      }

      varTerm = namespace.createVariable(name);
      register(aVariableValue, name, varTerm);
    }

    public void caseAIntegerLiteralValue(AIntegerLiteralValue aIntegerLiteralValue)
    {
      AIntegerLiteral aIntegerLiteral =
              (AIntegerLiteral) aIntegerLiteralValue.getIntegerLiteral();

      // integer literal goes to remote term in ontology core.integer
      TNumber number = aIntegerLiteral.getNumber();
      ACompoundFullName fullName = new ACompoundFullName(
              new TName("core"),
              new TDot(),
              new ACompoundFullName(
                      new TName("integer"),
                      new TDot(),
                      new ASimpleFullName(
                              new TName(number.getText(),
                                        number.getLine(),
                                        number.getPos()))
              )
      );

      ANameValue aNameValue = new ANameValue(fullName);
      aIntegerLiteralValue.replaceBy(aNameValue);

      aNameValue.apply(this);
    }

    public void caseTComment(TComment tComment)
    {
      String comment = tComment.getText();
      comment = comment.substring(1);
      comment = comment.substring(0, comment.length() - 1);
      comment.trim();
      tComment.setText(comment);
    }

    private Term resolve(Node node)
    {
      Term term = (Term) anonymousNode2Term.get(node);

      if(term == null) {
        throw new NoSuchElementException("Could not find term for node: " + node.getClass() + "@" + node.hashCode() + ":" + node);
      }

      //System.err.println("Resolved: " + node.getClass() + "@" + node.hashCode() + ":" + node + " -> " + term.getClass() + "@" + term.hashCode() + ":" + term);
      return term;
    }

    private void register(Node node, String name, Term term)
    {
      if(node == null || name == null || term == null) {
        throw new NullPointerException("Registration must not be for null: " +
                                       " node: " + node +
                                       " name: " + name +
                                       " term: " + term);
      }

      anonymousNode2Term.put(node, term);
      anonymousName2Term.put(name, term);
      //System.err.println("Registered: " + /*node.getClass() + "@" + node.hashCode() + ":" +*/ node + " -> " + term.getClass() + "@" + term.hashCode() + ":" + term + " as '" + name + "'");
    }

    private Term resolveName(String name)
    {
      Term res = (Term) anonymousName2Term.get(name);
      //System.err.println("Retrieving: " + name + " -> " + res);
      return res;
    }

    private Token nid2Token(PFullName fullName)
    {
      TName name = null;
      if(fullName instanceof ASimpleFullName) {
        ASimpleFullName fn = (ASimpleFullName) fullName;
        name = fn.getName();
      } else if(fullName instanceof ACompoundFullName) {
        ACompoundFullName fn = (ACompoundFullName) fullName;
        name = fn.getName();
      }

      return name;
    }

    private String makeDescription(TComment comment)
    {
      if(comment == null) {
        return "";
      } else {
        return comment.getText();
      }
    }

    private String valueToString(PValue v)
    {
      if(v instanceof AIntegerLiteralValue) {
        return integerLiteralToString(((AIntegerLiteralValue) v).getIntegerLiteral());
      } else if(v instanceof AStringLiteralValue) {
        return stringLiteralToString(((AStringLiteralValue) v).getStringLiteral());
      } else if(v instanceof ANameValue) {
        return nameToString(((ANameValue) v).getFullName());
      } else if(v instanceof AVariableValue) {
        return ((AVariableValue) v).getVariable().getText();
      }

      throw new AssertionError("Unreachabel code");
    }

    private String nameToString(PFullName fn)
    {
      String name = "";
      while(fn instanceof ACompoundFullName) {
        ACompoundFullName cfn = (ACompoundFullName) fn;
        name = name + cfn.getName().getText() + ".";
        fn = cfn.getFullName();
      }

      name = name + ((ASimpleFullName) fn).getName().getText();

      return name;
    }

    private String integerLiteralToString(PIntegerLiteral il)
    {
      return ((AIntegerLiteral) il).getNumber().getText();
    }

    public String stringLiteralToString(PStringLiteral sl)
    {
      return ((AStringLiteral) sl).getQuotedString().getText();
    }
  }
}
