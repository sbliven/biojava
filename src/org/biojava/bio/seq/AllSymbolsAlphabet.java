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


package org.biojava.bio.seq;

import java.util.*;

public class AllSymbolsAlphabet implements Alphabet {
  private Map symbolToResidue; // symbol->residue
  private Map nameToResidue; // name->residue
  private Set residues;
  private String name;
  
  private Annotation annotation;

  protected void addResidue(Residue r) {
    residues.add(r);
    Character symbol = new Character(r.getSymbol());
    if(!symbolToResidue.keySet().contains(symbol)) {
      symbolToResidue.put(symbol, r);
    }
    nameToResidue.put(r.getName(), r);
  }
  
  public Annotation getAnnotation() {
    if(annotation == null)
      annotation = new SimpleAnnotation();
    return annotation;
  }
  
  public boolean contains(Residue r) {
    return residues.contains(r);
  }
  
  public String getName() {
    return name;
  }
  
  public ResidueParser getParser(String name)
  throws NoSuchElementException {
    if(name.equals("name")) {
      return new NameParser(nameToResidue) {
        public Residue parseToken(String token) throws IllegalResidueException {
          Residue res = (Residue) nameToResidue.get(token);
          if(res == null) {
            res = new SimpleResidue(token.charAt(0), token, null);
            addResidue(res);
          }
          return res;
        }
      };
    } else if(name.equals("symbol")) {
      return new ResidueParser() {
        public Alphabet alphabet() {
          return AllSymbolsAlphabet.this;
        }
        public ResidueList parse(String seq) {
          List resList = new ArrayList(seq.length());
          for(int i = 0; i < seq.length(); i++)
            resList.add(parseToken(seq.substring(i, i+1)));
          return new SimpleResidueList(alphabet(), resList);
        }
        public Residue parseToken(String token) {
          char c = token.charAt(0);
          Character ch = new Character(c);
          Residue r = (Residue) symbolToResidue.get(ch);
          if(r == null) {
            r = new SimpleResidue(c, token, null);
            addResidue(r);
          }
          return r;
        }
      };
    } else {
      throw new NoSuchElementException("No parser for " + name +
      " known in alphabet " + getName());
    }
  }
  
  public ResidueList residues() {
    return new SimpleResidueList(this, new ArrayList(residues));
  }
  
  public int size() {
    return residues.size();
  }
  
  public void validate(Residue r)
  throws IllegalResidueException {
    if(contains(r))
      return;
    throw new IllegalResidueException("No residue " + r.getName() +
                                      " in alphabet " + getName());
  }
  
  public AllSymbolsAlphabet(String name) {
    this.name = name;
    this.residues = new HashSet();
    this.symbolToResidue = new HashMap();
    this.nameToResidue = new HashMap();
  }
}
