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

/**
 * This uses residue symbols to parse characters into residues.
 *
 * @author Matthew Pocock
 */
public class SymbolParser implements ResidueParser {
  /**
   * The alphabet to parse into.
   */
  private Alphabet alphabet;
  /**
   * The map from symbol to residue.
   */
  private Map symbolToResidue;
  
  /**
   * Initialize symbolToResidue.
   */
  {
    symbolToResidue = new HashMap();
  }
  
  public Alphabet alphabet() {
    return alphabet;
  }
  
  public ResidueList parse(String seq) throws IllegalResidueException {
    List rList = new ArrayList(seq.length());
    for(int i = 0; i < seq.length(); i++) {
      rList.add(parseToken(seq.substring(i, i+1)));
    }
    return new SimpleResidueList(alphabet(), rList);
  }
  
  public Residue parseToken(String token) throws IllegalResidueException {
    Residue res = (Residue) symbolToResidue.get(token);
    if(res == null) {
      throw new IllegalResidueException(
        "No residue for token '" + token +
        "' found in alphabet " + alphabet.getName()
      );
    }
    return res;
  }
  
  /**
   * Generate a new SymbolParser for an alphabet.
   * <P>
   * It will be correct at the time of creation. If the alphabet is edited in
   * any way that affects the residues contained, this parser becomes invalid.
   *
   * @param alpha the Alphabet to parse symbols into
   */
  public SymbolParser(Alphabet alpha) {
    this.alphabet = alpha;
    for(Iterator i = alpha.residues().iterator(); i.hasNext(); ) {
      Residue res = (Residue) i.next();
      char c = res.getSymbol();
      symbolToResidue.put(Character.toLowerCase(c) + "", res);
      symbolToResidue.put(Character.toUpperCase(c) + "", res);
    }
  }
}
