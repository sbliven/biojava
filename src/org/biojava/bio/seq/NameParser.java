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
 */
public class NameParser implements ResidueParser {
  private Alphabet alphabet;
  private Map nameToResidue;
  
  {
    nameToResidue = new HashMap();
  }
  
  public Alphabet alphabet() {
    return alphabet;
  }
  
  public ResidueList parse(String seq) throws IllegalResidueException {
    SimpleResidueList rList = new SimpleResidueList(alphabet());
    String [] names = (String []) nameToResidue.keySet().toArray(new String[0]);
    while(seq.length() > 0) {
      int chosen = -1;
      for(int n = 0; (chosen != -1) && (n < names.length); n++) {
        if(seq.startsWith(names[n])) {
          chosen = n;
          break;
        }
      }
      
      if(chosen == -1) {
        if(seq.length() > 10)
          seq = seq.substring(0, 10);
        throw new IllegalResidueException("Unable to find residue name matching from " + seq);
      }
      
      rList.addResidue(parseToken(names[chosen]));
      seq = seq.substring(names[chosen].length());
    }
    return rList;
  }
  
  public Residue parseToken(String token) throws IllegalResidueException {
    Residue res = (Residue) nameToResidue.get(token);
    if(res == null)
      throw new IllegalResidueException("No residue for token '" + token +
                                        "' found");
    return res;
  }
  
  public NameParser(Alphabet alpha) {
    this.alphabet = alpha;
    for(Iterator i = alpha.residues().iterator(); i.hasNext(); ) {
      Residue res = (Residue) i.next();
      nameToResidue.put(res.getName(), res);
    }
  }
  
  public NameParser(Map nameToResidue) {
    this.nameToResidue = nameToResidue;
  }
}
