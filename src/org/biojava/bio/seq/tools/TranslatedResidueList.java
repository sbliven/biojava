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

import org.biojava.bio.BioError;
import org.biojava.bio.seq.*;

/**
 * Provides a 'translated' view of an underlying ResidueList.
 * <P>
 * This class allows you to translate from one alphabet into another, so
 * for example, you could translate from DNA-triplets into amino-acids. You
 * could also translate from DNA-dinucleotide into the 'twist' structural
 * metric, or any other translation that takes your fancy.
 * <P>
 * The actual mapping from source to view Residue is encapsulated in a
 * TranslationTable object.
 * <P>
 * The TranslatedResidueList will be the same length as the source, and each
 * Residue in the view will correspond to a single Residue in the source.
 *
 * @author Matthew Pocock
 */
public class TranslatedResidueList extends AbstractResidueList {
  /**
   * The source residue list to translate.
   */
  private final ResidueList source;
  
  /**
   * The TranslationTable that will be used to translate source->view residues
   */
  private final TranslationTable transTable;
  
  public TranslationTable getTranslationTable() {
    return transTable;
  }
  
  public ResidueList getSource() {
    return source;
  }
  
  public TranslatedResidueList(ResidueList source, TranslationTable transTable)
  throws IllegalAlphabetException {
    if(transTable.getSourceAlphabet() != source.alphabet()) {
      throw new IllegalAlphabetException(
        "The source alphabet and translation table source alphabets don't match: " +
        source.alphabet().getName() + " and " +
        transTable.getSourceAlphabet().getName()
      );
    }
    
    this.source = source;
    this.transTable = transTable;
  }
  
  public int length() {
    return source.length();
  }
  
  public Residue residueAt(int indx) {
    try {
      return transTable.translate(source.residueAt(indx));
    } catch (IllegalResidueException ire) {
      throw new BioError(
        ire,
        "I thought that I had checked that the translation table was compatible with " +
        "my source, but apparently something has messed up."
      );
    }
  }
  
  public Alphabet alphabet() {
    return transTable.getTargetAlphabet();
  }
}
