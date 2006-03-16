/*
 * RichSequenceHandler.java
 *
 * Created on March 7, 2006, 3:12 PM
 */

package org.biojavax.bio.seq;

import java.util.Iterator;
import java.util.List;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeVetoException;

/**
 * An interface for classes that know how to handle subsequence operations.
 * Implementations may be optimized so that they perform more efficiently in
 * certain conditions. For example a subsequence operation on a huge BioSQL
 * backed <code>RichSequence</code> could be optimized so that the operation
 * is performed more efficiently than dragging the whole sequence to memory and
 * then doing the operation.
 *
 * Implementations of <code>RichSequence</code> should generally delegate
 * <code>symbolAt(int index)</code>, <code>subStr(int start, int end)</code>,
 * <code>subList(int start, int end)</code> and subSequence(int start, int end)
 * to some implementation of this interface.
 *
 * @author Mark Schreiber
 * @author Richard Holland
 */
public interface RichSequenceHandler {
    /**
     * {@inheritDoc}
     */
    public void edit(RichSequence seq, Edit edit) throws IndexOutOfBoundsException, IllegalAlphabetException, ChangeVetoException;
    
    /**
     * {@inheritDoc}
     */
    public Symbol symbolAt(RichSequence seq, int index) throws IndexOutOfBoundsException;
    
    /**
     * {@inheritDoc}
     */
    public List toList(RichSequence seq);
    
    /**
     * {@inheritDoc}
     */
    public String subStr(RichSequence seq, int start, int end) throws IndexOutOfBoundsException;
    
    /**
     * {@inheritDoc}
     */
    public SymbolList subList(RichSequence seq, int start, int end) throws IndexOutOfBoundsException;
    
    /**
     * {@inheritDoc}
     */
    public String seqString(RichSequence seq);
    
    /**
     * {@inheritDoc}
     */
    public Iterator iterator(RichSequence seq);
}
