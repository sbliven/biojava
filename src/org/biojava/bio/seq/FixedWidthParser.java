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

public class FixedWidthParser implements ResidueParser {
  private Alphabet alpha;
  private int tokenLength;
  private Map tokenToResidue;

  {
    tokenToResidue = new HashMap();
  }

  public Alphabet alphabet() {
    return alpha;
  }

  public ResidueList parse(String seq) throws IllegalResidueException {
    SimpleResidueList res = new SimpleResidueList(alpha);
    for(int i = 0; i < seq.length(); i+= tokenLength) {
      res.addResidue(parseToken(seq.substring(i, i+tokenLength)));
    }
    return res;
  }

  public Residue parseToken(String token) throws IllegalResidueException {
    Residue res = (Residue) tokenToResidue.get(token);
    if(res == null)
      throw new IllegalResidueException("No residue associated with token " + token);
    return res;
  }

  public void addTokenMap(String token, Residue residue)
         throws IllegalResidueException, IllegalArgumentException {
    alphabet().validate(residue);
    if(token.length() != tokenLength)
      throw new IllegalArgumentException("token '" + token +
                                         "' must be of length " + tokenLength);
    tokenToResidue.put(token, residue);
  }

  public FixedWidthParser(Alphabet alpha, int tokenLength) {
    this.alpha = alpha;
    this.tokenLength = tokenLength;
  }
}
