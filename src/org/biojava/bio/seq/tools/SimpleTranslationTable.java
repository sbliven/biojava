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
import org.biojava.bio.seq.*;

public class SimpleTranslationTable implements TranslationTable {
  private final Map transMap;
  private final FiniteAlphabet source;
  private final Alphabet target;
  
  public Alphabet getSourceAlphabet() {
    return source;
  }
  
  public Alphabet getTargetAlphabet() {
    return target;
  }
  
  public Residue translate(Residue res)
  throws IllegalResidueException {
    Residue r = (Residue) transMap.get(res);
    if(r == null) {
      source.validate(res);
      throw new IllegalResidueException(
        "Unable to map " + res.getName()
      );
    }
    return r;
  }
  
  public void setTranslation(Residue from, Residue to)
  throws IllegalResidueException {
    source.validate(from);
    target.validate(to);
    transMap.put(from, to);
  }
  
  public SimpleTranslationTable(FiniteAlphabet source, Alphabet target) {
    this.source = source;
    this.target = target;
    this.transMap = new HashMap();
  }

  public SimpleTranslationTable(
    FiniteAlphabet source, Alphabet target, Map transMap
  ) {
    this.source = source;
    this.target = target;
    this.transMap = transMap;
  }
}
