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
import java.lang.reflect.*;
import java.io.*;

import org.biojava.bio.*;

/**
 * Abstract helper implementation of the SymbolList core interface.
 * To produce a concrete SymbolList implementation, you need only
 * implement the <code>alphabet</code>, <code>length</code> and
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
 * @author Thomas Down
 * @author Matthew Pocock
 */

public abstract class AbstractSymbolList implements SymbolList {
    public abstract Alphabet alphabet();

    public Iterator iterator() {
	return new SymbolIterator(1, length());
    }

    public abstract int length();

    public abstract Symbol symbolAt(int pos);

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
      for(int i = start; i <= end; i++) {
        sb.append( symbolAt(i).getToken() );
      }
      return sb.toString();
    }

    protected AbstractSymbolList() {}
    
    /**
     * An Iterator over each Symbol in a SymbolList.
     * <P>
     * Objects of this type are returned by 
     * <code>AbstractSymbolList.iterator</code>.
     *
     * @author Thomas Down
     */
    private class SymbolIterator implements Iterator, Serializable {
	private int min, max;
        private int pos;
	    
      protected SymbolIterator() {}
      
	public SymbolIterator(int min, int max) {
	    this.min = min;
	    this.max = max;
	    pos = min;
	}

	public boolean hasNext() {
	    return (pos <= max);
	}

	public Object next() {
	    if (pos > max)
		throw new NoSuchElementException();
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
     * @author Thomas Down
     */
    private class SubList implements SymbolList, Serializable {
	private int start, end;
  
	public SubList(int start, int end) {
	    this.start = start;
	    this.end = end;
	}

	public Alphabet alphabet() {
	    return AbstractSymbolList.this.alphabet();
	}

	public Iterator iterator() {
	    return new SymbolIterator(start, end);
	}

	public int length() {
	    return end - start + 1;
	}

	public Symbol symbolAt(int pos) {
	    return AbstractSymbolList.this.symbolAt(pos + start - 1);
	}

	public SymbolList subList(int sstart, int send) {
	    return new SubList(sstart + start - 1, send + start - 1);
	}

  public String seqString() {
    return subStr(1, length());
  }
  
  public String subStr(int start, int end) throws IndexOutOfBoundsException {
    if(start < 1 || start > this.end ||
       end < 1 || end > this.end ||
       start > end) {
      throw new IndexOutOfBoundsException();
    }
    return AbstractSymbolList.this.subStr(start + this.start, end + this.start);
  }
  
	public List toList() {
	    return new ListView(this);
	}
    }

    /**
     * Implements a list view of a SymbolList.
     * <P>
     * Objects of this type are instantiated by
     * <code>AbstractSymbolList.asList</code>.
     *
     * @author Thomas Down
     */
    private static class ListView extends AbstractList implements Serializable {
	private SymbolList rl;
  
	ListView(SymbolList rl) {
	    this.rl = rl;
	}

	public int size() {
	    return rl.length();
	}
	
	public Object get(int pos) {
	    return rl.symbolAt(pos + 1);
	}
    }
}
