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

import org.biojava.bio.seq.*;

/**
 * @author Matthew Pocock
 * @author Thomas Down
 */  
public class DNANoAmbPack
  implements
    Packing,
    java.io.Serializable
{
  final byte placeHolder;
    

    public DNANoAmbPack(byte placeHolder) {
	this.placeHolder = placeHolder;
    }
  
    public DNANoAmbPack(Symbol placeHolderSymbol) {
	this.placeHolder = pack(placeHolderSymbol);
    }

    public FiniteAlphabet getAlphabet() {
	return DNATools.getDNA();
    }
  
  public byte pack(Symbol sym) {
    if(false) {
    } else if(sym == DNATools.a()) {
      return 0;
    } else if(sym == DNATools.g()) {
      return 1;
    } else if(sym == DNATools.c()) {
      return 2;
    } else if(sym == DNATools.t()) {
      return 3;
    }
    
    return placeHolder;
  }
  
  public Symbol unpack(byte b)
  throws IllegalSymbolException {
    if(false) {
    } else if(b == 0) {
      return DNATools.a();
    } else if(b == 1) {
      return DNATools.g();
    } else if(b == 2) {
      return DNATools.c();
    } else if(b == 3) {
      return DNATools.t();
    }
    
    throw new IllegalSymbolException("Can't unpack: " + b);
  }
  
  public byte wordSize() {
    return 2;
  }
  
  public boolean handlesAmbiguity() {
    return false;
  }
}

