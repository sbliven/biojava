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
import java.util.HashSet;
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
    private boolean singleSource;
    
    /**
     * Creates a new instance of SimpleRichSequenceLocation.
     * @param term such as the <code>JOIN_TERM</code> or the <code>ORDER_TERM</code>
     * @param members a collection of <code>RichLocations</code> that make up
     * this location.
     */
    public CompoundRichLocation(ComparableTerm term, Collection members) {
        // TODO - when circular and unions etc. done properly, change default flag to false
        this(term,members,true);
    }
    
    public CompoundRichLocation(ComparableTerm term, Collection members, boolean doNotMerge) {
        if (term==null) throw new IllegalArgumentException("Term cannot be null");
        if (members==null) throw new IllegalArgumentException("Members cannot be null");
        
        this.term = term;
        
        Set sources = new HashSet();
        
        if (doNotMerge) {
            this.members = new ArrayList();
            for (Iterator i = members.iterator(); i.hasNext(); ) {
                // Convert the object into a RichLocation
                Object o = i.next();
                if (!(o instanceof RichLocation)) o = RichLocation.Tools.enrich((Location)o);
                // Count the crossrefs
                CrossRef cr = ((RichLocation)o).getCrossRef();
                if (cr!=null) sources.add(cr);
                // Add in member
                this.members.add(o);
            }
        } else {
            this.members = Collections.emptyList(); // for temporary use
            Location mergedLocation = null;
            for (Iterator i = members.iterator(); i.hasNext(); ) {
                // Convert the object into a RichLocation
                Object o = i.next();
                if (!(o instanceof RichLocation)) o = RichLocation.Tools.enrich((Location)o);
                // Count the crossrefs
                CrossRef cr = ((RichLocation)o).getCrossRef();
                if (cr!=null) sources.add(cr);
                // Merge using the union operation
                mergedLocation = this.union((Location)o);
            }
            // Assign merged members to ourselves
            if (mergedLocation instanceof CompoundRichLocation) {
                // steal their members!
                this.members = new ArrayList(((CompoundRichLocation)mergedLocation).members);
            } else {
                this.members = Collections.singleton(mergedLocation);
            }
        }
        // Count sources
        this.singleSource = (sources.size()<=1);
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
    public boolean getCircular() { return false; }
    
    /**
     * {@inheritDoc}
     */
    public void setCircular(boolean circular) throws ChangeVetoException {
        throw new ChangeVetoException("CompoundRichLocations cannot be circular");
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
    public boolean isContiguous() { return this.members.size()<=1; }
    
    /**
     * {@inheritDoc}
     * Returns true if any of the member locations is adjacent to the query location.
     */
    public boolean isAdjacent(RichLocation loc) {
        for (Iterator i = this.members.iterator(); i.hasNext(); ) {
            RichLocation rl = (RichLocation)i.next();
            if (rl.isAdjacent(loc)) return true;
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean fromSingleSource() { return this.singleSource; }
    
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
        if (this.members.isEmpty()) return this;
        List newmembers = new ArrayList();
        for (Iterator i = this.members.iterator(); i.hasNext(); ) {
            RichLocation rl = (RichLocation)i.next();
            newmembers.add(rl.translate(dist));
        }
        return new CompoundRichLocation(getJoinTerm(),newmembers);
    }
    
    /**
     * {@inheritDoc}
     * If any of the member locations in this compound location contain the given location,
     * this function will return true.
     */
    public boolean contains(Location l) {
        if (!(l instanceof RichLocation)) l = RichLocation.Tools.enrich(l);
        if (l instanceof EmptyRichLocation) {
            return l.contains(this); // let them do the hard work!
        } else {
            for (Iterator i = this.members.iterator(); i.hasNext(); ) if (((RichLocation)i.next()).contains(l)) return true;
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     * Returns true if the any of this location's member locations overlap the
     * query location.
     */
    public boolean overlaps(Location l) {
        for (Iterator i = this.members.iterator(); i.hasNext(); ) {
            RichLocation rl = (RichLocation)i.next();
            if (rl.overlaps(l)) return true;
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public Location union(Location l) {
        if (this.members.isEmpty()) return l; // Simple case for constructor use
        if (!(l instanceof RichLocation)) l = RichLocation.Tools.enrich(l);
        if (l instanceof EmptyRichLocation) {
            return l.union(this); // let them do the hard work!
        } else {
            if (l instanceof CompoundRichLocation) {
                // Compound vs. Compound
                Location result = this;
                for (Iterator i = l.blockIterator(); i.hasNext(); ) {
                    // Do a simple union on each member in turn!
                    result = result.union((Location)i.next());
                }
                return result;
            } else {
                // TODO - Compound vs. simple (possibly circular)
                // Delete from here on in this clause when above is complete.
                List newmembers = new ArrayList();
                newmembers.addAll(this.members);
                newmembers.add(l);
                return new CompoundRichLocation(getJoinTerm(),newmembers,true);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Location intersection(Location l) {
        if (!(l instanceof RichLocation)) l = RichLocation.Tools.enrich(l);
        if (l instanceof EmptyRichLocation) {
            return l.intersection(this); // let them do the hard work!
        } else {
            if (!this.overlaps(l)) return RichLocation.EMPTY_LOCATION;
            if (l instanceof CompoundRichLocation) {
                // TODO - Compound vs. compound
            } else {
                // TODO - Compound vs. simple (possibly circular)
            }
            // Delete from here on when above is complete.
            List newmembers = new ArrayList();
            for (Iterator i = this.members.iterator(); i.hasNext(); ) {
                RichLocation rl = (RichLocation)i.next();
                if (rl.overlaps(l)) newmembers.add(rl.intersection(l));
            }
            if (newmembers.size()>1) return new CompoundRichLocation(getJoinTerm(),newmembers,true);
            else return (RichLocation)newmembers.get(0);
        }
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
        if (o==this) return true;
        return false; // not much else we can do really
    }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        Location fo = (Location) o;
        return -1; // not much else we can do really
    }
    
}

