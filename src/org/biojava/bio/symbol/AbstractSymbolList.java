/*
 * BioJava development code
 * 
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 * 
 * http://www.gnu.org/copyleft/lesser.html
 * 
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 * 
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 * 
 * http://www.biojava.org
 *
 */

package org.biojava.bio.symbol;

import java.util.*;
import java.lang.reflect.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.*;

/**  
 * Abstract helper implementation of the SymbolList core interface.
 * To produce a concrete SymbolList implementation, you need only
 * implement the <code>getAlphabet</code>, <code>length</code> and
 * <code>symbolAt</code> methods.  Iterators and sublists are
 * handled for you automatically.
 *
 * <P>
 * This class makes many custom SymbolList implementations
 * very quick to implement.
 * See org.biojava.bio.seq.tools.ComplementSymbolList
 * for an example of this.
 * </P>
 *
 * <p>
 * To make a mutable SymbolList, override the implementation of edit to perform
 * the apropreate edit. If your implementation of SymbolList is a view onto an
 * underlying SymbolList, then you must forward any apropreate edit requests
 * to that list, and forward all events from the underlying list to your
 * listeners.
 * </p>
 *
 * @author     Thomas Down 
 * @author     Matthew Pocock
 */


public abstract class AbstractSymbolList implements SymbolList {
  protected transient ChangeSupport changeSupport = null;
  
  protected AbstractSymbolList() {
  }

  public Iterator iterator() {
    return new SymbolIterator(1, length());
  }

  public SymbolList subList(int start, int end) {
    return new SubList(start, end);
  }

  public List toList() {
    return new ListView(this);
  }

  public String seqString() {
    return subStr(1, length());
  }

  public String subStr(int start, int end) {
    StringBuffer sb = new StringBuffer();
    for (int i = start; i <= end; i++) {
      sb.append(symbolAt(i).getToken());
    }

    return sb.toString();
  }
  
  public void edit(Edit edit)
  throws IndexOutOfBoundsException, IllegalAlphabetException,
  ChangeVetoException {
    throw new ChangeVetoException(
      "AbstractSymbolList is immutable"
    );
  }
  
  /**
   * If there is any work that you need to do when the ChangeSupport is
   * instantiated, override this method. You should emediately call
   * super.generateChangeSupport, and then create any event forarders and the
   * like.
   * <P>
   * You can use the changeType field to decide if event forwarders need to be
   * instantiated or not. If nobody is interested, then wait.
   *
   * @param changeType  the ChangeType that a ChangeListener is interested in or
   *        null if it interested in everything
   */

  protected void generateChangeSupport(ChangeType changeType) {
    if(changeSupport == null) {
      changeSupport = new ChangeSupport();
    }
  }
  
  public void addChangeListener(ChangeListener cl) {
    generateChangeSupport(null);

    synchronized(changeSupport) {
      changeSupport.addChangeListener(cl);
    }
  }
  
  public void addChangeListener(ChangeListener cl, ChangeType ct) {
    generateChangeSupport(ct);

    synchronized(changeSupport) {
      changeSupport.addChangeListener(cl, ct);
    }
  }
  
  public void removeChangeListener(ChangeListener cl) {
    if(changeSupport != null) {
      synchronized(changeSupport) {
        changeSupport.removeChangeListener(cl);
      }
    }
  }
  
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
    if(changeSupport != null) {
      synchronized(changeSupport) {
        changeSupport.removeChangeListener(cl, ct);
      }
    }
  }


  /**
   *  
   * An Iterator over each Symbol in a range of a SymbolList.
   * <P>
   * Objects of this type are returned by
   * <code>AbstractSymbolList.iterator</code>.
   *
   * @author     Thomas Down 
   */

  private class SymbolIterator implements Iterator, Serializable {
    private int min, max;
    private int pos;

    /**
     * Construct a SymbolIterator object that will return the symbols from
     * min to max inclusive.
     *
     * @param  min  the first index to return
     * @param  max  the last index to return
     */

    public SymbolIterator(int min, int max) {
      this.min = min;
      this.max = max;
      pos = min;
    }

    protected SymbolIterator() {
    }

    public boolean hasNext() {
      return (pos <= max);
    }

    public Object next() {
      if (pos > max) {
        throw new NoSuchElementException();
      }
      return symbolAt(pos++);
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Implements a list view of a SymbolList.
   * <P>
   * Objects of this type are instantiated by
   * <code>AbstractSymbolList.subList</code>.
   *
   * @author     Thomas Down 
   * @author     Matthew Pocock
   */

  private class SubList implements SymbolList, Serializable {
    private int start, end;

    private transient ChangeSupport changeSupport = null;
    private transient EditTranslater editTranslater = null;
    private transient Annotatable.AnnotationForwarder annotationForwarder = null;

    public SubList(int start, int end) {
      this.start = start;
      this.end = end;
    }

    public Alphabet getAlphabet() {
      return AbstractSymbolList.this.getAlphabet();
    }

    public Iterator iterator() {
      return new SymbolIterator(start, end);
    }

    public int length() {
      return end - start + 1;
    }

    public Symbol symbolAt(int pos) {
      if (pos < 1 || pos > length()) {
        throw new IndexOutOfBoundsException(
            "Symbol index out of bounds " + length() + ":" + pos
            );
      }
      return AbstractSymbolList.this.symbolAt(pos + start - 1);
    }

    public SymbolList subList(int sstart, int send) {
      if (sstart < 1 || send > length() || send < 1 || send > length()) {
        throw new IndexOutOfBoundsException(
            "Sublist index out of bounds " + length() + ":" + start + "," + send
            );
      }

      if (send < sstart) {
        throw new IndexOutOfBoundsException(
            "send must not be lower than sstart: sstart=" + start + ", send=" + send
            );
      }

      return new SubList(sstart + start - 1, send + start - 1);
    }

    public String seqString() {
      return subStr(1, length());
    }

    public String subStr(int start, int end) throws IndexOutOfBoundsException {
      if (start < 1 || start > this.end || 
          end < 1 || end > this.end || 
          start > end) {
        throw new IndexOutOfBoundsException();
      }

      return AbstractSymbolList.this.subStr(start + this.start - 1, end + this.start - 1);
    }

    public List toList() {
      return new ListView(this);
    }

    // fixme: doesn't do range checking on edit object
    public void edit(Edit edit)
    throws IndexOutOfBoundsException, IllegalAlphabetException,
    ChangeVetoException {
      AbstractSymbolList.this.edit(new Edit(
        edit.pos + this.start - 1, edit.length, edit.replacement
      ));
    }
    
    private void generateChangeSupport(ChangeType changeType) {
      if(changeSupport == null) {
        changeSupport = new ChangeSupport();
      }
      
      if(
        ((changeType == null) || (changeType == SymbolList.EDIT)) &&
        (editTranslater == null)
      ) {
        editTranslater = new EditTranslater(this, changeSupport, start, end);
        AbstractSymbolList.this.addChangeListener(editTranslater, SymbolList.EDIT);
      }
            
      if(
        ((changeType == null) || (changeType == Annotation.PROPERTY)) &&
        (annotationForwarder == null)
      ) {
        annotationForwarder = new Annotatable.AnnotationForwarder(this, changeSupport);
        AbstractSymbolList.this.addChangeListener(annotationForwarder, Annotation.PROPERTY);
      }
    }
    
    public void addChangeListener(ChangeListener cl) {
      generateChangeSupport(null);
      
      synchronized(changeSupport) {
        changeSupport.addChangeListener(cl);
      }
    }
    
    public void addChangeListener(ChangeListener cl, ChangeType ct) {
      generateChangeSupport(ct);
      
      synchronized(changeSupport) {
        changeSupport.addChangeListener(cl, ct);
      }
    }
    
    public void removeChangeListener(ChangeListener cl) {
      if(changeSupport != null) {
        synchronized(changeSupport) {
          changeSupport.removeChangeListener(cl);
        }
      }
    }
    
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {
      if(changeSupport != null) {
        synchronized(changeSupport) {
          changeSupport.removeChangeListener(cl, ct);
        }
      }
    }
  }

  /**
   *  
   * Implements a list view of a SymbolList.
   * <P>
   * Objects of this type are instantiated by
   * <code>AbstractSymbolList.asList</code>.
   *
   * @author     Thomas Down 
   *
   */

  private static class ListView extends AbstractList implements Serializable {
    private SymbolList rl;

    /**
     *  Constructor for the ListView object 
     *
     * @param  rl  Description of Parameter 
     */

    ListView(SymbolList rl) {
      this.rl = rl;
    }

    /**
     *  Description of the Method 
     *
     * @param  pos  Description of Parameter 
     * @return      Description of the Returned Value 
     */

    public Object get(int pos) {
      return rl.symbolAt(pos + 1);
    }

    /**
     *  Description of the Method 
     *
     * @return    Description of the Returned Value 
     */

    public int size() {
      return rl.length();
    }
  }

  
  /**
   * This adapter screens all edit events to see if they overlap with a window
   * of interest. If they do, then a new edit event is built for the overlapping
   * region and pased on to all listeners.
   */
  public class EditScreener extends ChangeForwarder {
    protected final int min;
    protected final int max;
    
    public EditScreener(
      Object source, ChangeSupport cs,
      int min, int max
    ) {
      super(source, cs);
      this.min = min;
      this.max = max;
    }
    
    protected ChangeEvent generateEvent(ChangeEvent ce) {
      ChangeType ct = ce.getType();
      if(ct == SymbolList.EDIT) {
        Object change = ce.getChange();
        if( (change != null) && (change instanceof Edit) ) {
          Edit edit = (Edit) change;
          int start = edit.pos;
          int end = start + edit.length - 1; // inclusive
          if( (start <= max) && (end >= min) ) {
            // they overlap
            return new ChangeEvent(
              getSource(),
              ct,
              edit,
              null,
              ce
            );
          }
        }
      }
      return null;
    }
  }
    
  public class EditTranslater extends EditScreener {
    public EditTranslater(
      Object source, ChangeSupport cs,
      int min, int max
    ) {
      super(source, cs, min, max);
    }
    
    protected ChangeEvent generateEvent(ChangeEvent ce) {
      ce = super.generateEvent(ce);
      if(ce != null) {
        Edit edit = (Edit) ce.getChange();
        return new ChangeEvent(
          ce.getSource(),
          ce.getType(),
          new Edit(edit.pos - min, edit.length, edit.replacement),
          null,
          ce.getChainedEvent()
        );
      }
      return null;
    }
  }
}

