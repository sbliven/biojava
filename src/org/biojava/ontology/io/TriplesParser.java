package org.biojava.ontology.io;

import java.io.PushbackReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.biojava.ontology.format.triples.lexer.Lexer;
import org.biojava.ontology.format.triples.lexer.LexerException;
import org.biojava.ontology.format.triples.parser.Parser;
import org.biojava.ontology.format.triples.parser.ParserException;
import org.biojava.ontology.format.triples.node.*;
import org.biojava.ontology.format.triples.analysis.DepthFirstAdapter;

import org.biojava.ontology.*;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.AssertionFailure;

/**
 *
 *
 * @author Matthew Pocock
 */
public class TriplesParser {
  public Ontology parse(PushbackReader reader,
                        OntologyFactory oFact,
                        ReasoningDomain domain)
  throws IOException, LexerException, ParserException  {
    Lexer lexer = new Lexer(reader);
    Parser parser = new Parser(lexer);
    Node ast = parser.parse();
    ast.apply(new ListRewriter());
    Processor processor = new Processor(oFact, domain);
    ast.apply(processor);
    return processor.getOntology();
  }

  public static class Processor
  extends DepthFirstAdapter {
    private final OntologyFactory oFact;
    private final ReasoningDomain domain;
    private Ontology onto;

    private int counter;
    private int depth;
    private final Map anonymousNode2Term;
    private final Map anonymousName2Term;

    public Processor(OntologyFactory oFact, ReasoningDomain domain) {
      this.oFact = oFact;
      this.domain = domain;
      anonymousNode2Term = new HashMap();
      anonymousName2Term = new HashMap();
    }

    public Ontology getOntology() {
      return onto;
    }

    public void inAProgram(AProgram aProgram) {
      counter = 0;
    }

    public void outANamespaceDecl(ANamespaceDecl aNamespaceDecl) {
      PFullName fullName = aNamespaceDecl.getFullName();
      String name = nameToString(fullName);
      String description = makeDescription(aNamespaceDecl.getComment());
      //System.err.println("Creating namespace name: " + name + " description: " + description);
      try {
        onto = oFact.createOntology(name, description);
      } catch (OntologyException e) {
        throw new RuntimeException("Can't create ontology for node: " + aNamespaceDecl, e);
      }
    }

    public void outAImportExpr(AImportExpr aImportExpr) {
      PFullName fullName = aImportExpr.getFullName();
      ACompoundFullName cfn = (ACompoundFullName) fullName;
      String ontoName = cfn.getName().getText();
      PFullName current = cfn.getFullName();

      while (current instanceof ACompoundFullName) {
        ACompoundFullName acfn = (ACompoundFullName) current;
        ontoName = ontoName + "." + acfn.getName().getText();
        current = acfn.getFullName();
      }
      String termName = ((ASimpleFullName) current).getName().getText();
      Term term = domain.getOntology(ontoName).getTerm(termName);

      String localName = aImportExpr.getName().getText();

      try {
        onto.importTerm(term, localName);
      } catch (ChangeVetoException e) {
        throw new RuntimeException(e);
      }
    }

    public void inAExpressionDecl(AExpressionDecl aExpressionDecl) {
      counter++;
      depth = 0;

      //System.err.println("Processing expression: " + counter + ": " + aExpressionDecl);
    }

    public void outAExpressionDecl(AExpressionDecl aExpressionDecl) {
      anonymousNode2Term.clear();
      anonymousName2Term.clear();
      //System.err.println("Leaving expression: " + counter + ": " + aExpressionDecl);
    }

    public void inASimpleExpression(ASimpleExpression aSimpleExpression) {
      depth++;
    }

    public void outASimpleExpression(ASimpleExpression aSimpleExpression) {
      PValue value = aSimpleExpression.getValue();
      String name = valueToString(value);
      String desc = makeDescription(aSimpleExpression.getComment());

      if(depth == 1) {

        try {
          Term term = onto.createTerm(name, desc);
          register(aSimpleExpression, name, term);
        } catch (AlreadyExistsException e) {
          throw new RuntimeException("Possible duplicate deffinition: " + aSimpleExpression, e);
        } catch (ChangeVetoException e) {
          throw new RuntimeException("Error defining expression: " + aSimpleExpression, e);
        }
      } else {
        try {
          Term term = resolve(value);
          register(aSimpleExpression, name, term);
        } catch (NoSuchElementException e) {
          throw new RuntimeException("Can't resolve identifier: " + name +
                                     " for " + value.getClass() + "@" + value.hashCode() + ":" + value,
                                     e);
        }
      }

      depth--;
    }

    public void inAComplexExpression(AComplexExpression aComplexExpression) {
      depth++;
    }

    public void outAComplexExpression(AComplexExpression aComplexExpression) {
      try {
        Term subject = resolve(aComplexExpression.getSubject());
        Term object = resolve(aComplexExpression.getObject());
        Term predicate = resolve(aComplexExpression.getPredicate());

        String desc = makeDescription(aComplexExpression.getComment());
        Triple triple = onto.createTriple(subject, object, predicate, null, desc);
        register(aComplexExpression, aComplexExpression.toString(), triple);
      } catch (AlreadyExistsException e) {
        Token tok = aComplexExpression.getLeftElipse();
        throw new RuntimeException(
                "Possible duplicate deffinition: [" +
                tok.getLine() + "," +
                tok.getPos() + "] '" +
                aComplexExpression.toString() + "'",
                e);
      } catch (ChangeVetoException e) {
        Token tok = aComplexExpression.getLeftElipse();
        throw new RuntimeException(
                "Could not process complex expression: [" +
                tok.getLine() + "," +
                tok.getPos() + "] '" +
                aComplexExpression.toString() + "'",
                e);
      } catch (NoSuchElementException e) {
        Token tok = aComplexExpression.getLeftElipse();
        throw new RuntimeException(
                "Could not process complex expression: [" +
                tok.getLine() + "," +
                tok.getPos() + "] '" +
                aComplexExpression.toString() + "'",
                e);
      }

      depth--;
    }

    public void outANamePredicate(ANamePredicate aNamePredicate) {
      //System.err.println("Predicate name: " + aNamePredicate);
      PFullName fullName = aNamePredicate.getFullName();

      try {
        String name = nameToString(fullName);
        Term term = onto.getTerm(name);
        register(aNamePredicate, name, term);
      } catch (NoSuchElementException e) {
        Token name = nid2Token(fullName);
        throw new RuntimeException("Can't resolve identifier: [" +
                                   name.getLine() + "," +
                                   name.getPos() + "] '" +
                                   fullName.toString() + "'");
      }
    }

    public void outAVariablePredicate(AVariablePredicate aVariablePredicate) {
      //System.err.println("Predicate variable: " + aVariablePredicate);
      TVariable tVariable = aVariablePredicate.getVariable();
      String name = tVariable.getText();
      name = name + ":" + counter;

      Term varTerm = resolveName(name);
      if(varTerm != null) {
        register(aVariablePredicate, name, varTerm);
        return;
      }

      try {
        varTerm = onto.createVariable(name, "");
        register(aVariablePredicate, name, varTerm);
      } catch (AlreadyExistsException e) {
        throw new RuntimeException(
                "Problem resloving variable: [" + tVariable.getLine() +
                "," + tVariable.getPos() +
                "] '" + tVariable.getText() + "'");
      } catch (ChangeVetoException e) {
        throw new RuntimeException(
                "Problem resloving variable: [" + tVariable.getLine() +
                "," + tVariable.getPos() +
                "] '" + tVariable.getText() + "'");
      }
    }

    public void outANameValue(ANameValue aNameValue) {
      //System.err.println("Value name: " + aNameValue);
      PFullName fullName = aNameValue.getFullName();

      if(depth == 1) {
        return;
      }

      try {
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

          Term term = domain.getOntology(ontoName).getTerm(termName);
          RemoteTerm remoteTerm = onto.importTerm(term, null);
          register(aNameValue, aNameValue.toString(), remoteTerm);
        } else {
          String name = nameToString(fullName);
          //System.err.println("Simple name: '" + name + "'");

          Term term = onto.getTerm(name);
          register(aNameValue, name, term);
        }
      } catch (NoSuchElementException e) {
        Token name = nid2Token(fullName);
        //System.err.println("No term for: '" + name.getText() + "'");
        if(name.getText().startsWith("list:")) {
          //System.err.println("Creating list term");
          try {
            Term term = onto.createTerm(name.getText(), "");
            register(aNameValue, name.getText(), term);
          } catch (AlreadyExistsException ae) {
            throw new AssertionFailure(ae);
          } catch (ChangeVetoException ce) {
            throw new AssertionFailure(ce);
          }
        } else {
          throw new RuntimeException("Can't resolve identifier: [" +
                                     name.getLine() + "," +
                                     name.getPos() + "] '" +
                                     fullName.toString() + "'", e);
        }
      } catch (ChangeVetoException e) {
        Token name = nid2Token(fullName);
        throw new RuntimeException("Can't resolve identifier: [" +
                                   name.getLine() + "," +
                                   name.getPos() + "] '" +
                                   fullName.toString() + "'", e);
      }
    }

    public void outAVariableValue(AVariableValue aVariableValue) {
      //System.err.println("Variable value: " + aVariableValue);
      TVariable tVariable = aVariableValue.getVariable();
      String name = tVariable.getText();
      name = name + ":" + counter;

      Term varTerm = resolveName(name);
      if(varTerm != null) {
        register(aVariableValue, name, varTerm);
        return;
      }

      try {
        varTerm = onto.createVariable(name, "");
        register(aVariableValue, name, varTerm);
      } catch (AlreadyExistsException e) {
        throw new RuntimeException(
                "Problem resloving variable: [" + tVariable.getLine() +
                "," + tVariable.getPos() +
                "] '" + tVariable.getText() + "' as " + name);
      } catch (ChangeVetoException e) {
        throw new RuntimeException(
                "Problem resloving variable: [" + tVariable.getLine() +
                "," + tVariable.getPos() +
                "] '" + tVariable.getText() + "' as " + name);
      }
    }

    public void caseAIntegerLiteralValue(AIntegerLiteralValue aIntegerLiteralValue) {
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

    public void caseTComment(TComment tComment) {
      String comment = tComment.getText();
      comment = comment.substring(1);
      comment = comment.substring(0, comment.length() - 1);
      comment.trim();
      tComment.setText(comment);
    }

    private Term resolve(Node node) {
      Term term = (Term) anonymousNode2Term.get(node);

      if(term == null) {
        throw new NoSuchElementException("Could not find term for node: " + node.getClass() + "@" + node.hashCode() + ":" + node);
      }

      //System.err.println("Resolved: " + node.getClass() + "@" + node.hashCode() + ":" + node + " -> " + term.getClass() + "@" + term.hashCode() + ":" + term);
      return term;
    }

    private void register(Node node, String name, Term term) {
      anonymousNode2Term.put(node, term);
      anonymousName2Term.put(name, term);
      //System.err.println("Registered: " + node.getClass() + "@" + node.hashCode() + ":" + node + " -> " + term.getClass() + "@" + term.hashCode() + ":" + term + " as '" + name + "'");
    }

    private Term resolveName(String name) {
      return (Term) anonymousName2Term.get(name);
    }

    private Token nid2Token(PFullName fullName) {
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

    private String makeDescription(TComment comment) {
      if(comment == null) {
        return "";
      } else {
        return comment.getText();
      }
    }

    private String valueToString(PValue v) {
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

    private String nameToString(PFullName fn) {
      String name = "";
      while (fn instanceof ACompoundFullName) {
        ACompoundFullName cfn = (ACompoundFullName) fn;
        name = name + cfn.getName().getText() + ".";
        fn = cfn.getFullName();
      }

      name = name + ((ASimpleFullName) fn).getName().getText();

      return name;
    }

    private String integerLiteralToString(PIntegerLiteral il) {
      return ((AIntegerLiteral) il).getNumber().getText();
    }

    public String stringLiteralToString(PStringLiteral sl) {
      return ((AStringLiteral) sl).getQuotedString().getText();
    }
  }

  private static final class ListRewriter
  extends DepthFirstAdapter {
    private int listCounter;
    private final Map listToExpression;
    private final Map listToName;

    public ListRewriter() {
      listToExpression = new HashMap();
      listToName = new HashMap();
    }

    public void inAProgram(AProgram aProgram) {
      listCounter = 0;
    }

    public void outAListBody(AListBody aListBody) {
      PExpression head = aListBody.getExpression();
      PListTail lTail = aListBody.getListTail();
      PExpression tail;
      String tailName;
      if(lTail != null) {
        PListBody tailBody = ((AListTail) lTail).getListBody();
        tail = resolveExpression(tailBody);
        tailName = resolveName(tailBody);
      } else {
        tailName = "none";
        tail = new ASimpleExpression(new ANameValue(new ASimpleFullName(new TName(tailName))), null);
      }

      String listName = "list:" + listCounter;
      listCounter++;

      PExpression aList = new AComplexExpression(
              new ANamePredicate(new ASimpleFullName(new TName("instanceof"))),
              null,
              new TLeftElipse(),
              new ASimpleExpression(new ANameValue(new ASimpleFullName(new TName(listName))), null),
              new TComma(),
              new ASimpleExpression(new ANameValue(new ASimpleFullName(new TName("list"))), null),
              new TRightElipse() );
      PExpression listHead = new AComplexExpression(
              new ANamePredicate(new ASimpleFullName(new TName("head"))),
              null,
              new TLeftElipse(),
              new ASimpleExpression(new ANameValue(new ASimpleFullName(new TName(listName))), null),
              new TComma(),
              head,
              new TRightElipse() );
      PExpression andList_head = new AComplexExpression(
              new ANamePredicate(new ASimpleFullName(new TName("and"))),
              null,
              new TLeftElipse(),
              aList,
              new TComma(),
              listHead,
              new TRightElipse() );
      PExpression listTail = new AComplexExpression(
              new ANamePredicate(new ASimpleFullName(new TName("tail"))),
              null,
              new TLeftElipse(),
              new ASimpleExpression(new ANameValue(new ASimpleFullName(new TName(listName))), null),
              new TComma(),
              new ASimpleExpression(new ANameValue(new ASimpleFullName(new TName(tailName))), null),
              new TRightElipse() );
      PExpression andList_head_tail = new AComplexExpression(
              new ANamePredicate(new ASimpleFullName(new TName("and"))),
              null,
              new TLeftElipse(),
              andList_head,
              new TComma(),
              listTail,
              new TRightElipse() );

      PExpression expr;
      if(lTail == null) {
        expr = andList_head_tail;
      } else {
        expr = new AComplexExpression(
              new ANamePredicate(new ASimpleFullName(new TName("and"))),
              null,
              new TLeftElipse(),
              andList_head_tail,
              new TComma(),
              tail,
              new TRightElipse() );
      }

      register(aListBody, listName, expr);
    }

    public void outAFullList(AFullList aFullList) {
      PListBody list = aFullList.getListBody();
      PExpression expr = resolveExpression(list);
      String name = resolveName(list);

      register(aFullList, name, expr);
    }

    public void outAListExpression(AListExpression aListExpression) {
      PList list = aListExpression.getList();

      PExpression expr = resolveExpression(list);
      String name = resolveName(list);

      ASimpleExpression listRef = new ASimpleExpression(new ANameValue(new ASimpleFullName(new TName(name))), null);
      AComplexExpression parent = (AComplexExpression) aListExpression.parent();

      // swap this list expression out for a list reference to make mod-parent
      aListExpression.replaceBy(listRef);

      // create and(mod-parent, listDef)
      AComplexExpression newParent = new AComplexExpression(
              new ANamePredicate(new ASimpleFullName(new TName("and"))),
                null,
                new TLeftElipse(),
                (PExpression) parent.clone(),
                new TComma(),
                expr,
                new TRightElipse() );

      // swap and(mod-parent, listDef) into where parent was
      parent.replaceBy(newParent);
    }

    private PExpression resolveExpression(Node list) {
      PExpression res = (PExpression) listToExpression.get(list);

      if(res == null) {
        throw new NoSuchElementException("Can't resolve " + list.getClass().getName() + "@" + list.hashCode() + " " + list);
      }

      return res;
    }

    private String resolveName(Node list) {
      String name = (String) listToName.get(list);

      if(name == null) {
        throw new NoSuchElementException("Can't resolve " + list.getClass().getName() + "@" + list.hashCode() + " " + list);
      }

      return name;
    }

    private void register(Node list, String name, PExpression expression) {
      //System.err.println("Registering: " + list.getClass().getName() + "@" + list.hashCode() + " " + list + " to " + name + " and " + expression);
      listToExpression.put(list, expression);
      listToName.put(list, name);
    }
  }
}
