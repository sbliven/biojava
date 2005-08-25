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

/*
 * RangeRichLocation.java
 *
 * Created on June 16, 2005, 11:47 AM
 */

package org.biojavax.bio.seq;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import org.biojava.bio.Annotation;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.Unchangeable;
import org.biojavax.CrossRef;
import org.biojavax.RichAnnotation;
import org.biojavax.bio.seq.RichLocation.Strand;
import org.biojavax.ontology.ComparableTerm;

/**
 * An Empty implementation of RichLocation. This class is intended to 
 * act as a place holder for events like the intersection of two locations
 * that do not overlap so that null need not be returned.
 *
 * @author Richard Holland
 * @author Mark Schreiber
 */
public class EmptyRichLocation extends Unchangeable implements RichLocation {
            
    /**
     * {@inheritDoc} An <code>EmptyRichLocation</code> does not have cross-refs.
     * @return null
     */
    public CrossRef getCrossRef() { return null; }
    
    /**
     * {@inheritDoc} An <code>EmptyRichLocation</code> contains only an
     * <code>RichAnnotation.EMPTY_ANNOTATION</code>.
     * @return <code>RichAnnotation.EMPTY_ANNOTATION</code>
     */
    public Annotation getAnnotation() { return RichAnnotation.EMPTY_ANNOTATION; }
    
    /**
     * {@inheritDoc} An empty set
     * @return <code>Collections.EMPTY_SET</code>
     */
    public Set getNoteSet() { return RichAnnotation.EMPTY_ANNOTATION.getNoteSet(); }
    
    /**
     * You cannot annotate the empty location.
     * @throws ChangeVetoException everytime this method is called.
     */
    public void setNoteSet(Set notes) throws ChangeVetoException {
        throw new ChangeVetoException("Cannot annotate the empty location");
    }
    
    /**
     * {@inheritDoc} The empty location has no terms
     * @return null
     */
    public ComparableTerm getTerm() { return null; }
    
    /**
     * Cannot give a term to the empty location.
     * @throws ChangeVetoException everytime this method is called.
     */
    public void setTerm(ComparableTerm term) throws ChangeVetoException {
        throw new ChangeVetoException("Cannot give a term to the empty location");
    }
    
    /**
     * {@inheritDoc}
     */
    public int getCircularLength() { return 0; }
    
    /**
     * {@inheritDoc}
     */
    public void setCircularLength(int sourceSeqLength) throws ChangeVetoException {
        throw new ChangeVetoException("Cannot make empty locations circular");
    }
    
    /**
     * {@inheritDoc} The empty_location has no defined strand
     * @return Strand.UNKNOWN_STRAND
     */
    public Strand getStrand() { return Strand.UNKNOWN_STRAND; }
        
    /**
     * {@inheritDoc} The empty location  has a rank of 0
     * @return 0
     */
    public int getRank() { return 0; }
    
    /**
     * Cannot give a rank to the empty location.
     * @throws ChangeVetoException everytime this method is called.
     */
    public void setRank(int rank) throws ChangeVetoException {
        throw new ChangeVetoException("Cannot give a rank to the empty location");
    }
    
    /**
     * {@inheritDoc}
     * @return 0
     */
    public int getMax() { return 0; }
        
    /**
     * {@inheritDoc}
     * @return 0
     */
    public int getMin() { return 0; }
    
    /**
     * {@inheritDoc}
     * @return a <code>SimplePosition</code> based around 0
     */ 
    public Position getMinPosition() { return Position.EMPTY_POSITION; }
    
    /**
     * {@inheritDoc}
     * @return a <code>SimplePosition</code> based around 0
     */ 
    public Position getMaxPosition() { return Position.EMPTY_POSITION; }
    
    /**
     * {@inheritDoc} This method is ignored in the empty location because positions
     * are fixed an cannot be modified.
     */
    public void setPositionResolver(PositionResolver p) {} // ignore
    
    /**
     * {@inheritDoc}
     * @return an interator over <code>Collections.EMPTY_SET</code>
     */
    public Iterator blockIterator() { return Collections.EMPTY_SET.iterator(); }
    
    /**
     * {@inheritDoc}
     * @return true (always)
     */
    public boolean isContiguous() { return true; }
        
    /**
     * {@inheritDoc}
     * @return false (always)
     */
    public boolean contains(int p) { return false; }
    
    /**
     * {@inheritDoc}
     * @return null
     */
    public Location getDecorator(Class decoratorClass) { return null; }
    
    /**
     * {@inheritDoc}
     */
    public Location newInstance(Location loc) { return loc; }
    
    /**
     * {@inheritDoc}
     * @return the same object, empty translated is still empty
     */
    public Location translate(int dist) { return this; }  
    
    /**
     * {@inheritDoc}
     * @return false (always)
     */
    public boolean contains(Location l) { return false; }
    
    /**
     * {@inheritDoc}
     * @return false (always)
     */
    public boolean overlaps(Location l) { return false; }
    
    /**
     * {@inheritDoc} The union of an empty location and another location (l) is
     * l.
     * @return l
     */
    public Location union(Location l) {
        if (l==null) throw new IllegalArgumentException("Location cannot be null");
        if (!(l instanceof RichLocation)) l = RichLocation.Tools.enrich(l);
        return l;
    }
    
    /**
     * {@inheritDoc}
     * @return an empty location
     */
    public Location intersection(Location l) {
        if (l==null) throw new IllegalArgumentException("Location cannot be null");
        return this;
    }
    
    /**
     * {@inheritDoc} the empty location covers no symbols
     * @return <code>SymbolList.EMPTY_LIST</code>
     */
    public SymbolList symbols(SymbolList seq) {
        if (seq==null) throw new IllegalArgumentException("Sequence cannot be null");
        return SymbolList.EMPTY_LIST;
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() { return 17; }
    
    /**
     * {@inheritDoc}
     * @return true only if <code>o</code> is an EmptyRichLocation
     */
    public boolean equals(Object o) {
        if (o instanceof EmptyRichLocation) return true;
        return false;
    }
    
    /**
     * {@inheritDoc}
     * @return 0 if <code>o</code> is an instance of <code>EmptyRichLocation</code>
     *   otherwise -1
     */
    public int compareTo(Object o) {
        if (o instanceof EmptyRichLocation) return 0;
        else return -1;
    }
}

