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

/**
 * Encapsulates the mapping from a source to a destination alphabet.
 * <P>
 * A TranslationTable is in effect a map or function with the source domain
 * being getSourceAlphabet() and the target domain being getTargetAlphabet().
 * The method translate() maps a single symbol from source to target.
 * <P>
 * It is presumed that there will be some explicit declaration of the mapping
 * for attomic symbols, and that the mapping for all other symbols will be
 * infered from these.
 * <P>
 * If you
 * wish to translate every symbol in a symbol list then use TranslatedSymbolList
 * to automate the job. If you want to translate windowed regions then first
 * construct a WindowedSymbolList from the original sequence and then build a
 * TranslatedSymbolList from this windowed view.
 *
 * @author Matthew Pocock
 */
public interface TranslationTable {
  /**
   * The alphabet of Symbols that can be translated.
   *
   * @return the source Alphabet
   */
  public Alphabet getSourceAlphabet();
  
  /**
   * The alphabet of Symbols that will be produced.
   *
   * @return the target Alphabet
   */
  public Alphabet getTargetAlphabet();
  
  /**
   * Translate a single symbol from source alphabet to the target alphabet.
   *
   * @param sym the Symbol to translate (member of source alphabet)
   * @return the translated version of sym (member of target alphabet)
   * @throws IllegalSymbolException if sym is not a member of the source
   *         alphabet
   */
  public Symbol translate(Symbol sym) throws IllegalSymbolException;
}
