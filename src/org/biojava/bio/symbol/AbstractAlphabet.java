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

/**
 * An abstract implementation of FiniteAlphabet.
 * <P>
 * This provides the frame-work for maintaining the SymbolParser <-> name
 * mappings.
 * <P>
 * This class is for developers to derive from, not for use directly.
 *
 * @author Matthew Pocock
 */
public abstract class AbstractAlphabet implements FiniteAlphabet {
  private Map parserByName;
  {
    parserByName = new HashMap();
  }
  
  public void putParser(String name, SymbolParser parser) {
    parserByName.put(name, parser);
  }

  public SymbolParser getParser(String name)
         throws NoSuchElementException {
    SymbolParser parser = (SymbolParser) parserByName.get(name);
    if(parser == null) {
      if(name.equals("token")) {
        parser = new TokenParser(this);
        putParser(name, parser);
      } else if(name.equals("name")) {
        parser = new NameParser(this);
        putParser(name, parser);
      } else {
        throw new NoSuchElementException("There is no parser '" + name +
                                         "' defined in alphabet " + getName());
      }
    }
    return parser;
  }
}
