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


package org.biojava.bio.seq.tools;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;


public class CrossProductResidueNameParser implements ResidueParser {
  private final CrossProductAlphabet alpha;
  private final ResidueParser [] parser;
  
  public Alphabet alphabet() {
    return alpha;
  }
  
  public ResidueList parse(String seq)
  throws IllegalResidueException {
    List residues = new ArrayList();
    int i = 0;
    
   OUTER:
    while(i < seq.length()) {
      while(seq.charAt(i) == ' ') {
        i++;
        if(i >= seq.length()) {
          break OUTER;
        }
      }
      if(seq.charAt(i) != '(') {
        throw new BioError(
          "Could not find opeing bracket at " + i +
          " in " + seq
        );
      }
      int depth = 1;
      int j = i+1;
      while(j < seq.length() && depth > 0) {
        char c = seq.charAt(j);
        if(c == '(') {
          depth++;
        } else if(c == ')') {
          depth--;
        }
        j++;
      }
      if(depth == 0) {
        residues.add(parseToken(seq.substring(i, j)));
      }  else {
        throw new BioError(
          "Error parsing sequence string: could not find matching bracket\n" +
          seq.substring(i)
        );
      }
    }
    return new SimpleResidueList(alpha, residues);
  }
  
  public Residue parseToken(String token)
  throws IllegalResidueException {
    if(!token.startsWith("(") || !token.endsWith(")")) {
      throw new IllegalResidueException(
        "Can't parse " + token + " as it is not bracketed"
      );
    }
    
    token = token.substring(1, token.length()-1).trim();
    
    List rList = new ArrayList();
    int i = 0;
    while(i < token.length()) {
      if(token.charAt(i) == '(') {
        int depth = 1;
        int j = i+1;
        while(j < token.length() && depth > 0) {
          char c = token.charAt(j);
          if(c == '(') {
            depth++;
          } else if(c == ')') {
            depth--;
          }
          j++;
        }
        if(depth == 0) {
          rList.add(parser[rList.size()].parseToken(
            token.substring(i, j)
          ));
        } else {
          throw new BioError(
            "Error parsing residue name: could not find matching bracket\n" +
            token.substring(i)
          );
        }
      } else {
        int j = token.indexOf(", ", i);
        if(j < 0) {
          rList.add(parser[rList.size()].parseToken(
            token.substring(i)
          ));
          i = token.length();
        } else {
          rList.add(parser[rList.size()].parseToken(
            token.substring(i, j)
          ));
          i = j + ", ".length();
        }
      }
    }
    
    return alpha.getResidue(rList);
  }
  
  public CrossProductResidueNameParser(CrossProductAlphabet alpha)
  throws SeqException {
    this.alpha = alpha;
    List alphas = alpha.getAlphabets();
    this.parser = new ResidueParser[alphas.size()];
    int c = 0;
    for(Iterator i = alphas.iterator(); i.hasNext(); ) {
      Alphabet a = (Alphabet) i.next();
      try {
        parser[c++] = a.getParser("name");
      } catch (NoSuchElementException e) {
        throw new SeqException(
          e,
          "Couldn't create CrossProductResidueNameParser for " +
          alpha.getName()
        );
      }
    }
  }
}
