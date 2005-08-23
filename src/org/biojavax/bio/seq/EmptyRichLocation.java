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
 * A simple implementation of RichLocation.
 *
 * Equality is based on parent, min and max, strand, and rank.
 *
 * @author Richard Holland
 * @author Mark Schreiber
 */
public class EmptyRichLocation extends Unchangeable implements RichLocation {
            
    /**
     * {@inheritDoc}
     */
    public CrossRef getCrossRef() { return null; }
    
    /**
     * {@inheritDoc}
     */
    public Annotation getAnnotation() { return RichAnnotation.EMPTY_ANNOTATION; }
    
    /**
     * {@inheritDoc}
     */
    public Set getNoteSet() { return RichAnnotation.EMPTY_ANNOTATION.getNoteSet(); }
    
    public void setNoteSet(Set notes) throws ChangeVetoException {
        throw new ChangeVetoException("Cannot annotate the empty location");
    }
    
    /**
     * {@inheritDoc}
     */
    public ComparableTerm getTerm() { return null; }
    
    /**
     * {@inheritDoc}
     */
    public void setTerm(ComparableTerm term) throws ChangeVetoException {
        throw new ChangeVetoException("Cannot give a term to the empty location");
    }
    
    /**
     * {@inheritDoc}
     */
    public Strand getStrand() { return Strand.UNKNOWN_STRAND; }
        
    /**
     * {@inheritDoc}
     */
    public int getRank() { return 0; }
    
    /**
     * {@inheritDoc}
     */
    public void setRank(int rank) throws ChangeVetoException {
        throw new ChangeVetoException("Cannot give a rank to the empty location");
    }
    
    /**
     * {@inheritDoc}
     */
    public int getMax() { return 0; }
        
    /**
     * {@inheritDoc}
     */
    public int getMin() { return 0; }
        
    public Position getMinPosition() { return new SimplePosition(false,false,0); }
    
    public Position getMaxPosition() { return new SimplePosition(false,false,0); }
    
    public void setPositionResolver(PositionResolver p) {} // ignore
    
    /**
     * {@inheritDoc}
     */
    public Iterator blockIterator() { return Collections.EMPTY_SET.iterator(); }
    
    /**
     * {@inheritDoc}
     */
    public boolean contains(Location l) { return false; }
    
    /**
     * {@inheritDoc}
     */
    public boolean overlaps(Location l) { return false; }
    
    /**
     * {@inheritDoc}
     */
    public boolean isContiguous() { return true; }
    
    /**
     * {@inheritDoc}
     */
    public boolean contains(int p) { return false; }
    
    /**
     * {@inheritDoc}
     */
    public Location getDecorator(Class decoratorClass) { return null; }
    
    /**
     * {@inheritDoc}
     */
    public Location newInstance(Location loc) { return loc; }
    
    /**
     * {@inheritDoc}
     */
    public Location translate(int dist) { return this; }
    
    /**
     * {@inheritDoc}
     */
    public Location union(Location l) {
        if (l==null) throw new IllegalArgumentException("Location cannot be null");
        if (!(l instanceof RichLocation)) l = RichLocation.Tools.enrich(l);
        return l;
    }
    
    /**
     * {@inheritDoc}
     */
    public Location intersection(Location l) {
        if (l==null) throw new IllegalArgumentException("Location cannot be null");
        return this;
    }
    
    /**
     * {@inheritDoc}
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
     */
    public boolean equals(Object o) {
        if (o instanceof EmptyRichLocation) return true;
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        if (o instanceof EmptyRichLocation) return 0;
        else return -1;
    }
}

