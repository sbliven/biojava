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

package org.biojava.bio.program.phred;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.utils.*;
import org.biojava.bio.symbol.*;

/**
 * Title:        PhredSequence.java<p>
 * Description:  An extension of SimpleSequence that implements Qualitative to hold Phred quality scores.
 *               <p>
 * Copyright:    Copyright (c) 2001<p>
 * Company:      AgResearch<p>
 * @author Mark Schreiber
 * @since 1.1
 */

public class PhredSequence extends SimpleSequence implements Qualitative{

  /**
   * Constructs a new PhredSequence.
   * @param phredSequence - a SymbolList over the Phred Alphabet.
   * @param name - the name for the sequence.
   * @param urn - the URN for the sequence.
   * @param anno - the Annotation object for the sequence.
   */
  public PhredSequence(SymbolList phredSequence, String name, String urn, Annotation anno)
  throws IllegalAlphabetException{
    super(phredSequence,urn,name,anno);
    if(this.getAlphabet() != PhredTools.getPhredAlphabet()){
      throw new IllegalAlphabetException(
        "Cannot build a PhredSequence using a "+
        phredSequence.getAlphabet().getName()+
        " SymbolList.");
    }
  }

  /**
   * Extracts the quality part if the Phred Alphabet and returns it as a SymbolList
   * over the Integer SubAlphabet from 0..99.
   */
  public SymbolList getQuality(){
    SimpleSymbolList qual = new SimpleSymbolList(IntegerAlphabet.getSubAlphabet(0,99));
    for(int i = 1; i < this.length(); i++){
      try{
        qual.addSymbol(PhredTools.integerSymbolFromPhred(this.symbolAt(i)));
      }catch(IllegalSymbolException ise){
        throw new BioError(ise,
        "PhredTools.integerSymbolFromPhred() has returned a symbol not in this SymbolLists alphabet");
      }catch(ChangeVetoException cve){
        throw new BioError(cve, "Cannot construct symbol list as it has becomed locked?");
      }
    }
    return qual;
  }

  /**
   * Extracts the DNA part of the PhredAlpahbet SymbolList and returns it as a SymbolList
   */
  public SymbolList getDNA(){
    SimpleSymbolList dna = new SimpleSymbolList(DNATools.getDNA());
    for(int i = 1; i < this.length(); i++){
      try{
        dna.addSymbol(PhredTools.dnaSymbolFromPhred(this.symbolAt(i)));
      }catch(ChangeVetoException cve){
        throw new BioError(cve, "Cannot construct symbol list as it has becomed locked?");
      }catch(IllegalSymbolException ise){
        throw new BioError(ise,
        "PhredTools.dnaSymbolFromPhred() has returned a symbol not in the DNA alphabet");
      }
    }
    return dna;
  }

  public Symbol getQualityAt(int index) throws IndexOutOfBoundsException{
    try{
      return PhredTools.integerSymbolFromPhred(this.symbolAt(index));
    }catch(IllegalSymbolException ise){
      throw new BioError(ise, "Something has gone badly wrong with the Phred Alphabet!");
    }
  }

  public Symbol getDNAAt(int index) throws IndexOutOfBoundsException{
    try{
      return PhredTools.dnaSymbolFromPhred(this.symbolAt(index));
    }catch(IllegalSymbolException ise){
      throw new BioError(ise, "Something has gone badly wrong with the Phred Alphabet!");
    }
  }
}