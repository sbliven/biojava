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


package org.biojava.bio.symbol;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.*;

/**
 * An implementation of FiniteAlphabet that grows the alphabet to accomodate all
 * the characters seen while parsing a file.
 * <P>
 * The contains and validate methods will still work as for other alphabets, but
 * the parsers will generate new symbol objects for each token or name seen.
 * <P>
 * This is particularly useful when reading in arbitrary alphabet files where
 * you don't want to invest the time and effort writing a formal alphabet.
 *
 * @author Matthew Pocock
 */
public class AllTokensAlphabet implements FiniteAlphabet, Serializable {
  private Map tokenToSymbol; // token->Symbol
  private Map nameToSymbol; // name->Symbol
  private Set symbols;
  private String name;
  private Annotation annotation;
  
  protected transient ChangeSupport changeSupport = null;
  protected transient Annotatable.AnnotationForwarder annotationForwarder = null;

  /**
   * Adds a symbol to the alphabet
   *
   * @param r the symbol to add
   */
  public void addSymbol(Symbol s) {
    symbols.add(s);
    Character token = new Character(s.getToken());
    if(!tokenToSymbol.keySet().contains(token)) {
      tokenToSymbol.put(token, s);
    }
    nameToSymbol.put(s.getName(), s);
  }

  public Iterator iterator() {
    return symbols.iterator();
  }
  
  public Annotation getAnnotation() {
    if(annotation == null)
      annotation = new SimpleAnnotation();
    return annotation;
  }
  
  public boolean contains(Symbol s) {
    return symbols.contains(s);
  }
  
  public String getName() {
    return name;
  }
  
  public SymbolParser getParser(String name)
  throws NoSuchElementException {
    if(name.equals("name")) {
      return new NameParser(nameToSymbol) {
        public Symbol parseToken(String token) throws IllegalSymbolException {
          Symbol sym = (Symbol) nameToSymbol.get(token);
          if(sym == null) {
            sym = new SimpleAtomicSymbol(token.charAt(0), token, null);
            addSymbol(sym);
          }
          return sym;
        }
      };
    } else if(name.equals("token")) {
      return new SymbolParser() {
        public Alphabet getAlphabet() {
          return AllTokensAlphabet.this;
        }
        public SymbolList parse(String seq) {
          List symList = new ArrayList(seq.length());
          for(int i = 0; i < seq.length(); i++)
            symList.add(parseToken(seq.substring(i, i+1)));
	  try {
	      return new SimpleSymbolList(getAlphabet(), symList);
	  } catch (IllegalSymbolException ex) {
	      throw new BioError(ex);
	  }
        }
        public Symbol parseToken(String token) {
          char c = token.charAt(0);
          Character ch = new Character(c);
          Symbol s = (Symbol) tokenToSymbol.get(ch);
          if(s == null) {
            s = new SimpleAtomicSymbol(c, token, null);
            addSymbol(s);
          }
          return s;
        }
      };
    } else {
      throw new NoSuchElementException("No parser for " + name +
      " known in alphabet " + getName());
    }
  }
  
  public SymbolList symbols() {
      try {
	  return new SimpleSymbolList(this, new ArrayList(symbols));
      } catch (IllegalSymbolException ex) {
	  throw new BioError(ex);
      }
  }
  
  public int size() {
    return symbols.size();
  }
  
  public void validate(Symbol s)
  throws IllegalSymbolException {
    if(contains(s))
      return;
    throw new IllegalSymbolException("No symbol " + s.getName() +
                                      " in alphabet " + getName());
  }
  
  public void removeSymbol(Symbol sym) throws IllegalSymbolException {
    throw new IllegalSymbolException(
      "Can't remove symbols from alphabet: " + sym.getName() +
      " in " + getName()
    );
  }
  
  protected void generateChangeSupport(ChangeType changeType) {
    if(changeSupport == null) {
      changeSupport = new ChangeSupport();
    }
    
    if(
      ((changeType == null) || (changeType == Annotation.PROPERTY)) &&
      (annotationForwarder == null)
    ) {
      annotationForwarder = new Annotatable.AnnotationForwarder(this, changeSupport);
      annotation.addChangeListener(annotationForwarder, Annotation.PROPERTY);
    }
  }
  
  public void addChangeListener(ChangeListener cl) {
    generateChangeSupport(null);

    synchronized(changeSupport) {
      changeSupport.addChangeListener(cl);
    }
  }
  
  public void addChangeListener(ChangeListener cl, ChangeType ct) {
    generateChangeSupport(ct);

    synchronized(changeSupport) {
      changeSupport.addChangeListener(cl, ct);
    }
  }
  
  public void removeChangeListener(ChangeListener cl) {
    if(changeSupport != null) {
      synchronized(changeSupport) {
        changeSupport.removeChangeListener(cl);
      }
    }
  }
  
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
    if(changeSupport != null) {
      synchronized(changeSupport) {
        changeSupport.removeChangeListener(cl, ct);
      }
    }
  }  

  public AllTokensAlphabet(String name) {
    this.name = name;
    this.symbols = new HashSet();
    this.tokenToSymbol = new HashMap();
    this.nameToSymbol = new HashMap();
  }
}
