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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.biojava.bio.Annotation;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.CrossRef;
import org.biojavax.RichAnnotation;
import org.biojavax.bio.db.RichObjectFactory;
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
public class CompoundRichLocation extends AbstractChangeable implements RichLocation {
        
    private static ComparableTerm JOIN_TERM = null;
    /**
     * Getter for the "join" term
     * @return the "join" term
     */
    public static ComparableTerm getJoinTerm() {
        if (JOIN_TERM==null) JOIN_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("join");  
        return JOIN_TERM;
    }
    private static ComparableTerm ORDER_TERM = null;
    /**
     * Getter for the "order" term
     * @return the "order" term
     */
    public static ComparableTerm getOrderTerm() {
        if (ORDER_TERM==null) ORDER_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("order");    
        return ORDER_TERM;
    }
    
    private ComparableTerm term;
    private Collection members;
    
    /**
     * Creates a new instance of SimpleRichSequenceLocation.
     * @param term such as the <code>JOIN_TERM</code> or the <code>ORDER_TERM</code>
     * @param members a collection of <code>RichLocations</code> that make up
     * this location.
     */
    public CompoundRichLocation(ComparableTerm term, Collection members) {
        if (term==null) throw new IllegalArgumentException("Term cannot be null");
        if (members==null || members.size()<2) throw new IllegalArgumentException(
                "Members collection must have at least 2 members. Term: "+term+
                " number of members: "+members.size());
        this.members = members;
        this.term = term;
        for (Iterator i = this.members.iterator(); i.hasNext(); ) {
            if (!(i.next() instanceof RichLocation)) throw new IllegalArgumentException("All members must be RichLocations");
        }
    }
    
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
    
    /**
     * {@inheritDoc}
     */
    public void setNoteSet(Set notes) throws ChangeVetoException {
        throw new ChangeVetoException("Cannot annotate compound locations.");
    }
    
    /**
     * {@inheritDoc}
     */
    public ComparableTerm getTerm() { return this.term; }
    
    /**
     * {@inheritDoc}
     */
    public void setTerm(ComparableTerm term) throws ChangeVetoException {
        if(!this.hasListeners(RichLocation.TERM)) {
            this.term = term;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichLocation.TERM,
                    term,
                    this.term
                    );
            ChangeSupport cs = this.getChangeSupport(RichLocation.TERM);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.term = term;
                cs.firePostChangeEvent(ce);
            }
        }
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
        throw new ChangeVetoException("Cannot rank compound locations.");
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
            
    public void setPositionResolver(PositionResolver p) {
        for (Iterator i = this.members.iterator(); i.hasNext(); ) ((RichLocation)i.next()).setPositionResolver(p);
    }
    
    /**
     * {@inheritDoc}
     */
    public Iterator blockIterator() { return Collections.unmodifiableCollection(this.members).iterator(); }
    
    /**
     * {@inheritDoc}
     */
    public boolean contains(Location l) {
        for (Iterator i = this.members.iterator(); i.hasNext(); ) if (((RichLocation)i.next()).contains(l)) return true;
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean overlaps(Location l) {
        for (Iterator i = this.members.iterator(); i.hasNext(); ) if (((RichLocation)i.next()).overlaps(l)) return true;
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isContiguous() {
        Iterator i = this.members.iterator();
        RichLocation prev = (RichLocation)i.next(); // we're guaranteed two locations at least
        do {
            RichLocation curr = (RichLocation)i.next();
            if (!prev.overlaps(curr)) return false;
            prev = curr;
        } while (i.hasNext());
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean contains(int p) {
        for (Iterator i = this.members.iterator(); i.hasNext(); ) if (((RichLocation)i.next()).contains(p)) return true;
        return false;
    }
    
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
    public Location translate(int dist) {
        List newmembers = new ArrayList();
        for (Iterator i = this.members.iterator(); i.hasNext(); ) {
            RichLocation rl = (RichLocation)i.next();
            newmembers.add(rl.translate(dist));
        }
        return new CompoundRichLocation(getJoinTerm(),newmembers);
    }
    
    /**
     * {@inheritDoc}
     */
    public Location union(Location l) {
        if (l==null) throw new IllegalArgumentException("Location cannot be null");
        if (!(l instanceof RichLocation)) throw new IllegalArgumentException("Location cannot be a non-RichLocation");
        List newmembers = new ArrayList();
        newmembers.addAll(this.members);
        newmembers.add(l);
        return new CompoundRichLocation(getJoinTerm(),newmembers);
    }
    
    /**
     * {@inheritDoc}
     */
    public Location intersection(Location l) {
        if (l==null) throw new IllegalArgumentException("Location cannot be null");
        if (!(l instanceof RichLocation)) throw new IllegalArgumentException("Location cannot be a non-RichLocation");
        RichLocation them = (RichLocation)l;
        
        if (!this.overlaps(l)) return RichLocation.EMPTY_LOCATION;
        
        List newmembers = new ArrayList();
        for (Iterator i = this.members.iterator(); i.hasNext(); ) {
            RichLocation rl = (RichLocation)i.next();
            if (rl.overlaps(l)) newmembers.add(rl.intersection(l));
        }
        if (newmembers.size()>1) return new CompoundRichLocation(getJoinTerm(),newmembers);
        else return (RichLocation)newmembers.get(0);
    }
    
    /**
     * {@inheritDoc}
     */
    public SymbolList symbols(SymbolList seq) {
        if (seq==null) throw new IllegalArgumentException("Sequence cannot be null");
        
        List res = new ArrayList();
        for (Iterator i = this.members.iterator(); i.hasNext(); ) {
            RichLocation l = (RichLocation) i.next();
            res.addAll(l.symbols(seq).toList());
        }
        
        try {
            return new SimpleSymbolList(seq.getAlphabet(), res);
        } catch (IllegalSymbolException ex) {
            throw new RuntimeException("Could not build compound sequence string",ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        code = 31*code + this.term.hashCode();
        for (Iterator i = this.members.iterator(); i.hasNext(); ) code = 31*i.next().hashCode();
        return code;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (! (o instanceof CompoundRichLocation)) return false;
        CompoundRichLocation fo = (CompoundRichLocation) o;
        if (!this.term.equals(fo.getTerm())) return false;
        return this.members == fo.members;
    }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        CompoundRichLocation fo = (CompoundRichLocation) o;
        if (!this.term.equals(fo.getTerm())) return this.term.compareTo(fo.getTerm());
        if (this.members.equals(fo.members)) return 0;
        else return -1; // not much else we can do really - you can't compare collections
    }
    
}

