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

package org.biojavax.bio.seq;

import java.util.Iterator;
import java.util.List;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeForwarder;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.Namespace;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.SimpleBioEntry;
import org.biojavax.bio.seq.SimpleRichSequenceFeatureHolder;

/**
 *
 * @author Richard Holland
 */
public class SimpleRichSequence extends SimpleBioEntry implements RichSequence {
    
    /**
     *
     * The sequences for this feature.
     *
     */
    
    private SymbolList symList;
    
    /**
     *
     * The features for this entry.
     *
     */
    
    private FeatureHolder features;
    
    /**
     *
     * Change forwarder for the features.
     *
     */
    
    private ChangeForwarder featForF;
    
    /**
     *
     * Change forwarder for the feature schemas.
     *
     */
    
    private ChangeForwarder featForS;
    
    
    /**
     *
     * The sequence version for this feature.
     *
     */
    
    private double symListVersion;
    
    
    /** Creates a new instance of SimpleRichSequence */
    public SimpleRichSequence(Namespace ns, String name, String accession, int version, SymbolList symList, double seqversion) {
       
        super(ns,name,accession,version);
        
        if (symList==null) symList = SymbolList.EMPTY_LIST;
        
        this.symList = symList;
        
        this.symListVersion = seqversion;
        
        
        // set up features
        
        this.features = new SimpleRichSequenceFeatureHolder(this);
        
        // construct the forwarder so that it emits FeatureHolder ChangeEvents
        
        // for the FeatureHolder events it will listen for
        
        this.featForF = new ChangeForwarder.Retyper(this, super.getChangeSupport(FeatureHolder.FEATURES), FeatureHolder.FEATURES);
        
        this.featForS = new ChangeForwarder.Retyper(this, super.getChangeSupport(FeatureHolder.SCHEMA), FeatureHolder.SCHEMA);
        
        // connect the forwarder so it listens for FeatureHolder events
        
        this.features.addChangeListener(this.featForF, FeatureHolder.FEATURES);
        
        this.features.addChangeListener(this.featForS, FeatureHolder.SCHEMA);
    }
    
    
    
    /**
     *
     * The version of the associated symbol list.
     *
     * @return  the version
     *
     */
    
    public double getSeqVersion() {
        
        return this.symListVersion;
        
    }
    
    
    
    
    /**
     * Sets the version of the associated symbol list.
     * @param seqVersion the version to set.
     * @throws org.biojava.utils.ChangeVetoException in case the change is refused.
     */
    
    public void setSeqVersion(double seqVersion) throws ChangeVetoException {
        
        if(!this.hasListeners(BioEntry.RELATIONSHIP)) {
            
            this.symListVersion = seqVersion;
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    BioEntry.SEQVERSION,
                    
                    Double.valueOf(seqVersion),
                    
                    Double.valueOf(this.symListVersion)
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(BioEntry.RELATIONSHIP);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.symListVersion = seqVersion;
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
    }
    
    
    
    /**
     *
     * Return a new FeatureHolder that contains all of the children of this one
     *
     * that passed the filter fc.
     *
     *
     *
     * This method is scheduled for deprecation.  Use the 1-arg filter
     *
     * instead.
     *
     * @param fc the FeatureFilter to apply
     *
     * @param recurse true if all features-of-features should be scanned, and a
     *
     * single flat collection of features returned, or false if
     *
     * just immediate children should be filtered.
     *
     * @return a FeatureHolder.
     *
     */
    
    public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
        
        return this.features.filter(fc,recurse);
        
    }
    
    
    
    /**
     *
     * Create a new Feature, and add it to this FeatureHolder.  This
     *
     * method will generally only work on Sequences, and on some
     *
     * Features which have been attached to Sequences.
     *
     * @param ft The Template to create the feature from.
     *
     * @throws BioException if something went wrong during creating the feature
     *
     * @throws ChangeVetoException if this FeatureHolder does not support
     *
     * creation of new features, or if the change was vetoed
     *
     * @return the new feature.
     *
     */
    
    public Feature createFeature(Feature.Template ft) throws BioException, ChangeVetoException {
        
        return this.features.createFeature(ft);
        
    }
    
    
    
    /**
     *
     * Remove a feature from this FeatureHolder.
     *
     * @param f the feature to remove.
     *
     * @throws ChangeVetoException if this FeatureHolder does not support
     *
     * feature removal or if the change was vetoed
     *
     * @throws BioException if there was an error removing the feature
     *
     */
    
    public void removeFeature(Feature f) throws ChangeVetoException, BioException {
        
        this.features.removeFeature(f);
        
    }
    
    
    
    /**
     *
     * Check if the feature is present in this holder.
     *
     *
     *
     * @since 1.2
     *
     * @param f the Feature to check
     *
     * @return true if f is in this set
     *
     */
    
    public boolean containsFeature(Feature f) {
        
        return this.features.containsFeature(f);
        
    }
    
    
    
    /**
     *
     * Query this set of features using a supplied <code>FeatureFilter</code>.
     *
     *
     *
     * @param filter the <code>FeatureFilter</code> to apply.
     *
     * @return all features in this container which match <code>filter</code>.
     *
     */
    
    public FeatureHolder filter(FeatureFilter filter) {
        
        return this.features.filter(filter);
        
    }
    
    
    
    /**
     *
     * Return a schema-filter for this <code>FeatureHolder</code>.  This is a filter
     *
     * which all <code>Feature</code>s <em>immediately</em> contained by this <code>FeatureHolder</code>
     *
     * will match.  It need not directly match their child features, but it can (and should!) provide
     *
     * information about them using <code>FeatureFilter.OnlyChildren</code> filters.  In cases where there
     *
     * is no feature hierarchy, this can be indicated by including <code>FeatureFilter.leaf</code> in
     *
     * the schema filter.
     *
     *
     *
     * <p>
     *
     * For the truly non-informative case, it is possible to return <code>FeatureFilter.all</code>.  However,
     *
     * it is almost always possible to provide slightly more information that this.  For example, <code>Sequence</code>
     *
     * objects should, at a minimum, return <code>FeatureFilter.top_level</code>.  <code>Feature</code> objects
     *
     * should, as a minimum, return <code>FeatureFilter.ByParent(new FeatureFilter.ByFeature(this))</code>.
     *
     * </p>
     *
     *
     *
     * @since 1.3
     *
     * @return the schema filter
     *
     */
    
    public FeatureFilter getSchema() {
        
        return this.features.getSchema();
        
    }
    
    
    
    /**
     *
     * Iterate over the features in no well defined order.
     *
     *
     *
     * @return  an Iterator
     *
     */
    
    public Iterator features() {
        
        return this.features.features();
        
    }
    
    
    
    /**
     *
     * Count how many features are contained.
     *
     *
     *
     * @return  a positive integer or zero, equal to the number of features
     *
     *          contained
     *
     */
    
    public int countFeatures() {
        
        return this.features.countFeatures();
        
    }
    
    
    /**
     *
     * Apply an edit to the SymbolList as specified by the edit object.
     *
     *
     *
     * <h2>Description</h2>
     *
     *
     *
     * <p>
     *
     * All edits can be broken down into a series of operations that change
     *
     * contiguous blocks of the sequence. This represent a one of those operations.
     *
     * </p>
     *
     *
     *
     * <p>
     *
     * When applied, this Edit will replace 'length' number of symbols starting a
     *
     * position 'pos' by the SymbolList 'replacement'. This allow to do insertions
     *
     * (length=0), deletions (replacement=SymbolList.EMPTY_LIST) and replacements
     *
     * (length>=1 and replacement.length()>=1).
     *
     * </p>
     *
     *
     *
     * <p>
     *
     * The pos and pos+length should always be valid positions on the SymbolList
     *
     * to:
     *
     * <ul>
     *
     * <li>be edited (between 0 and symL.length()+1).</li>
     *
     * <li>To append to a sequence, pos=symL.length()+1, pos=0.</li>
     *
     * <li>To insert something at the beginning of the sequence, set pos=1 and
     *
     * length=0.</li>
     *
     * </ul>
     *
     * </p>
     *
     *
     *
     * <h2>Examples</h2>
     *
     *
     *
     * <code><pre>
     *
     * SymbolList seq = DNATools.createDNA("atcaaaaacgctagc");
     *
     * System.out.println(seq.seqString());
     *
     *
     *
     * // delete 5 bases from position 4
     *
     * Edit ed = new Edit(4, 5, SymbolList.EMPTY_LIST);
     *
     * seq.edit(ed);
     *
     * System.out.println(seq.seqString());
     *
     *
     *
     * // delete one base from the start
     *
     * ed = new Edit(1, 1, SymbolList.EMPTY_LIST);
     *
     * seq.edit(ed);
     *
     *
     *
     * // delete one base from the end
     *
     * ed = new Edit(seq.length(), 1, SymbolList.EMPTY_LIST);
     *
     * seq.edit(ed);
     *
     * System.out.println(seq.seqString());
     *
     *
     *
     * // overwrite 2 bases from position 3 with "tt"
     *
     * ed = new Edit(3, 2, DNATools.createDNA("tt"));
     *
     * seq.edit(ed);
     *
     * System.out.println(seq.seqString());
     *
     *
     *
     * // add 6 bases to the start
     *
     * ed = new Edit(1, 0, DNATools.createDNA("aattgg");
     *
     * seq.edit(ed);
     *
     * System.out.println(seq.seqString());
     *
     *
     *
     * // add 4 bases to the end
     *
     * ed = new Edit(seq.length() + 1, 0, DNATools.createDNA("tttt"));
     *
     * seq.edit(ed);
     *
     * System.out.println(seq.seqString());
     *
     *
     *
     * // full edit
     *
     * ed = new Edit(3, 2, DNATools.createDNA("aatagaa");
     *
     * seq.edit(ed);
     *
     * System.out.println(seq.seqString());
     *
     * </pre></code>
     *
     *
     *
     * @param edit the Edit to perform
     *
     * @throws IndexOutOfBoundsException if the edit does not lie within the
     *
     *         SymbolList
     *
     * @throws IllegalAlphabetException if the SymbolList to insert has an
     *
     *         incompatible alphabet
     *
     * @throws ChangeVetoException  if either the SymboList does not support the
     *
     *         edit, or if the change was vetoed
     *
     */
    
    public void edit(Edit edit) throws IndexOutOfBoundsException, IllegalAlphabetException, ChangeVetoException {
        
        this.symList.edit(edit);
        
    }
    
    
    
    /**
     *
     * Return the symbol at index, counting from 1.
     *
     *
     *
     * @param index the offset into this SymbolList
     *
     * @return  the Symbol at that index
     *
     * @throws IndexOutOfBoundsException if index is less than 1, or greater than
     *
     *                                   the length of the symbol list
     *
     */
    
    public Symbol symbolAt(int index) throws IndexOutOfBoundsException {
        
        return this.symList.symbolAt(index);
        
    }
    
    
    
    /**
     *
     * Returns a List of symbols.
     *
     * <p>
     *
     * This is an immutable list of symbols. Do not edit it.
     *
     *
     *
     * @return  a List of Symbols
     *
     */
    
    public List toList() {
        
        return this.symList.toList();
        
    }
    
    
    
    /**
     *
     * Return a region of this symbol list as a String.
     *
     * <p>
     *
     * This should use the same rules as seqString.
     *
     *
     *
     * @param start  the first symbol to include
     *
     * @param end the last symbol to include
     *
     * @return the string representation
     *
     * @throws IndexOutOfBoundsException if either start or end are not within the
     *
     *         SymbolList
     *
     */
    
    public String subStr(int start, int end) throws IndexOutOfBoundsException {
        
        return this.symList.subStr(start, end);
        
    }
    
    
    
    /**
     *
     * Return a new SymbolList for the symbols start to end inclusive.
     *
     *
     *
     * The resulting SymbolList will count from 1 to (end-start + 1) inclusive, and
     *
     * refer to the symbols start to end of the original sequence.
     *
     * @param start the first symbol of the new SymbolList
     *
     * @param end the last symbol (inclusive) of the new SymbolList
     *
     * @throws java.lang.IndexOutOfBoundsException if the symbol list doesn't have the coordinates given.
     *
     * @return the sublist.
     *
     */
    
    public SymbolList subList(int start, int end) throws IndexOutOfBoundsException {
        
        return this.symList.subList(start, end);
        
    }
    
    
    
    /**
     *
     * Stringify this symbol list.
     *
     * <p>
     *
     * It is expected that this will use the symbol's token to render each
     *
     * symbol. It should be parsable back into a SymbolList using the default
     *
     * token parser for this alphabet.
     *
     *
     *
     * @return  a string representation of the symbol list
     *
     */
    
    public String seqString() {
        
        return this.symList.seqString();
        
    }
    
    
    
    /**
     *
     * The number of symbols in this SymbolList.
     *
     *
     *
     * @return  the length
     *
     */
    
    public int length() {
        
        return this.symList.length();
        
    }
    
    
    
    /**
     *
     * An Iterator over all Symbols in this SymbolList.
     *
     * <p>
     *
     * This is an ordered iterator over the Symbols. It cannot be used
     *
     * to edit the underlying symbols.
     *
     *
     *
     * @return  an iterator
     *
     */
    
    public Iterator iterator() {
        
        return this.symList.iterator();
        
    }
    
    
    
    /**
     *
     * The alphabet that this SymbolList is over.
     *
     * <p>
     *
     * Every symbol within this SymbolList is a member of this alphabet.
     *
     * <code>alphabet.contains(symbol) == true</code>
     *
     * for each symbol that is within this sequence.
     *
     *
     *
     * @return  the alphabet
     *
     */
    
    public Alphabet getAlphabet() {
        
        return this.symList.getAlphabet();
        
    }
}
