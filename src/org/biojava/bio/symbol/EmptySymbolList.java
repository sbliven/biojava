package org.biojava.bio.symbol;

import org.biojava.utils.Unchangeable;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.StaticMemberPlaceHolder;

import java.io.Serializable;
import java.io.ObjectStreamException;
import java.io.NotSerializableException;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;

/**
 * The empty immutable implementation.
 */
class EmptySymbolList
extends Unchangeable
implements SymbolList, Serializable {
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

  private Object writeReplace() throws ObjectStreamException {
    try {
      return new StaticMemberPlaceHolder(SymbolList.class.getField("EMPTY_LIST"));
    } catch (NoSuchFieldException nsfe) {
      throw new NotSerializableException(nsfe.getMessage());
    }
  }

}
