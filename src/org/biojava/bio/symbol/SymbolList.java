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
import java.io.*;
import java.lang.reflect.*;

import org.biojava.utils.*;

/**
 * A sequence of symbols that belong to an alphabet.
 * <P>
 * This uses biological coordinates (1 to length).
 *
 * @author Matthew Pocock
 */
public interface SymbolList extends Changeable {
  /**
   * Signals that the SymbolList is being edited. The getChange field of the
   * event should contain the SymbolList.Edit object describing the change.
   */
  public static final ChangeType EDIT = new ChangeType(
    "the SymbolList has been edited",
    "org.biojava.bio.symbol.SymbolList",
    "EDIT"
  );
  
  /**
   * The alphabet that this SymbolList is over.
   * <P>
   * Every symbol within this SymbolList is a member of this alphabet.
   * <code>alphabet.contains(symbol) == true</code>
   * for each symbol that is within this sequence.
   *
   * @return  the alphabet
   */
  Alphabet getAlphabet();
  
  /**
   * The number of symbols in this SymbolList.
   *
   * @return  the length
   */
  int length();

  /**
   * Return the symbol at index, counting from 1.
   *
   * @param index the offset into this SymbolList
   * @return  the Symbol at that index
   * @throws IndexOutOfBoundsException if index is less than 1, or greater than
   *                                   the length of the symbol list
   */
  Symbol symbolAt(int index) throws IndexOutOfBoundsException;
  
  /**
   * Returns a List of symbols.
   * <p>
   * This is an immutable list of symbols. Do not edit it.
   *
   * @return  a List of Symbols
   */
  List toList();
  
  /**
   * An Iterator over all Symbols in this SymbolList.
   * <p>
   * This is an ordered iterator over the Symbols. It cannot be used
   * to edit the underlying symbols.
   *
   * @return  an iterator
   */
  Iterator iterator();
  
  /**
   * Return a new SymbolList for the symbols start to end inclusive.
   * <P>
   * The resulting SymbolList will count from 1 to (end-start + 1) inclusive, and
   * refer to the symbols start to end of the original sequence.
   *
   * @param start the first symbol of the new SymbolList
   * @param end the last symbol (inclusive) of the new SymbolList
   */
  SymbolList subList(int start, int end) throws IndexOutOfBoundsException;
    
  /**
   * Stringify this symbol list.
   * <P>
   * It is expected that this will use the symbol's token to render each
   * symbol. It should be parsable back into a SymbolList using the default
   * token parser for this alphabet.
   *
   * @return  a string representation of the symbol list
   */
  String seqString();
  
  /**
   * Return a region of this symbol list as a String.
   * <P>
   * This should use the same rules as seqString.
   *
   * @param start  the first symbol to include
   * @param end the last symbol to include
   * @return the string representation
   * @throws IndexOutOfBoundsException if either start or end are not within the
   *         SymbolList
   */
  String subStr(int start, int end) throws IndexOutOfBoundsException;
  
  /**
   * Aply an edit to the SymbolList as specified by the edit object.
   *
   * @param edit the Edit to perform
   * @throws IndexOutOfBoundsException if the edit does not lie within the
   *         SymbolList
   * @throws IllegalAlphabetException if the SymbolList to insert has an
   *         incompatible alphabet
   * @throws ChangeVetoException  if either the SymboList does not support the
   *         edit, or if the change was vetoed
   */
  void edit(Edit edit)
  throws IndexOutOfBoundsException, IllegalAlphabetException,
  ChangeVetoException;
  
  /**
   * A useful object that represents an empty symbol list, to avoid returning
   * null.
   *
   * @author Matthew Pocock
   */
  static final SymbolList EMPTY_LIST = new EmptySymbolList();
    
  /**
   * The empty immutable implementation.
   */
  class EmptySymbolList implements SymbolList, Serializable {
    public Alphabet getAlphabet() {
      return Alphabet.EMPTY_ALPHABET;
    }
    
    public int length() {
      return 0;
    }
    
    public Symbol symbolAt(int index) throws IndexOutOfBoundsException {
      throw new IndexOutOfBoundsException("Attempted to retrieve symbol from empty list at " + index);
    }
    
    public List toList() {
      return Collections.EMPTY_LIST;
    }
    
    public Iterator iterator() {
      return Collections.EMPTY_LIST.iterator();
    }
    
    public SymbolList subList(int start, int end) throws IndexOutOfBoundsException {
      Collections.EMPTY_LIST.subList(start-1, end);
      return SymbolList.EMPTY_LIST;
    }
    
    public String seqString() {
      return "";
    }
    
    public String subStr(int start, int end) throws IndexOutOfBoundsException {
      throw new IndexOutOfBoundsException(
        "You can not retrieve part of an empty symbol list"
      );
    }

    public void edit(Edit edit)
    throws IndexOutOfBoundsException, ChangeVetoException {
      throw new ChangeVetoException(
        "You can't edit the empty symbol list"
      );
    }
    
    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}

    private Object writeReplace() throws ObjectStreamException {
      try {
        return new StaticMemberPlaceHolder(SymbolList.class.getField("EMPTY_LIST"));
      } catch (NoSuchFieldException nsfe) {
        throw new NotSerializableException(nsfe.getMessage());
      }
    }

  }
}
