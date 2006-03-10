/*
 * RichSubSequenceHandler.java
 *
 * Created on March 7, 2006, 3:12 PM
 */

package org.biojavax.bio.seq;

import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojavax.Namespace;

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
 */
public interface RichSubSequenceHandler {
    /**
     * Return the <code>Symbol</code> at <code>index</code>, counting from 1.
     * @return the <code>Symbol</code> at that index
     * @param sl The target <code>SymbolList</code>
     * @param index the offset into the <code>SymbolList</code>
     * @throws IndexOutOfBoundsException if index is less than 1, or greater than
     *                                   the length of the symbol list
     */
    public Symbol symbolAt(int index, SymbolList sl) throws IndexOutOfBoundsException;
    
    
    /**
     * Return a region of a SymbolList as a <code>String</code>. Typically an
     * implemetation would perform a <code>subList(int start, int end) operation
     * and then tokenize the results.
     * @return the <code>String</code> representation
     * @param sl The target <code>SymbolList</code>
     * @param start the first <code>Symbol</code> to include
     * @param end the last <code>Symbol</code> to include
     * @throws IndexOutOfBoundsException if either start or end are not within the
     *         <code>SymbolList</code>
     */
    public String subStr(int start, int end, SymbolList sl) throws IndexOutOfBoundsException;
    
    /**
     * Return a new <code>SymbolList</code> for the <code>Symbol</code>s <code>start</code> to
     * <code>end</code> inclusive.
     * <p>
     * The resulting <code>SymbolList</code> will count from 1 to (end-start + 1)
     * inclusive, and
     * refer to the symbols start to end of the original sequence. Implementations
     * should consider the possibility that <code>sl</code>
     * might be a the parent <code>RichSequence</code> and 
     * may be circular with a call to <code>getCircular();</code>
     * @param sl The target <code>SymbolList</code>
     * @param start the first symbol of the new SymbolList
     * @param end the last symbol (inclusive) of the new SymbolList
     * @throws java.lang.IndexOutOfBoundsException If the coordinates are not within the 
     * symbollist
     * @return the resulting <code>SymbolList</code>
     */
    public SymbolList subList(int start, int end, SymbolList sl) throws IndexOutOfBoundsException;
    
    /**
     * <p>Creates a new sequence from a subregion of another sequence. The sequence is not a view.
     * The sequence can be given a new Namespace, Accession, Name, Identifier etc. or you can
     * copy over the old values. For unique identification in databases we recommend you change
     * at least the name and identifier.</p>
     * <p>The new sequence will retain all features that are fully contained by the new
     *  subsequence, the note set (annotation), Taxon, and
     * description, modified to reflect the subsequence as follows:
     * <pre>
     *      seq.setDescription("subsequence ("+from+":"+to+") of "+s.getDescription());
     * </pre>
     * No other properties are copied.
     * @return A new <CODE>RichSequence</CODE>
     * @param newVersion the new version number
     * @param seqVersion the new sequence version
     * @param s the original <code>RichSequence</code>.
     * @param from the 1st subsequence coordinate (inclusive)
     * @param to the last subsequence coordinate (inclusive)
     * @param newNamespace the new <code>Namespace</code>
     * @param newName the new name
     * @param newAccession the new accession number
     * @param newIdentifier the new identifier
     */
    public RichSequence subSequence(RichSequence s,
            int from,
            int to,
            Namespace newNamespace,
            String newName,
            String newAccession,
            String newIdentifier,
            int newVersion,
            Double seqVersion);
}
