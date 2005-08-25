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
import java.util.TreeSet;
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
    
    CompoundRichLocation(Collection members) {
        this(getJoinTerm(), members);
    }
    
    /**
     * Creates a new instance of SimpleRichSequenceLocation.
     * @param term such as the <code>JOIN_TERM</code> or the <code>ORDER_TERM</code>
     * @param members a collection of <code>RichLocations</code> that make up
     * this location.
     */
    CompoundRichLocation(ComparableTerm term, Collection members) {
        if (term==null) throw new IllegalArgumentException("Term cannot be null");
        if (members==null || members.size()<2) throw new IllegalArgumentException("Must have at least two members");
        
        this.term = term;
        this.members = new ArrayList();
        
        for (Iterator i = members.iterator(); i.hasNext(); ) {
            // Convert the object into a RichLocation
            Object o = i.next();
            if (!(o instanceof RichLocation)) o = RichLocation.Tools.enrich((Location)o);
            // Add in member
            this.members.add(o);
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
    public int getCircularLength() { return 0; }
    
    /**
     * {@inheritDoc}
     */
    public void setCircularLength(int sourceSeqLength) throws ChangeVetoException {
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
    
    public Position getMinPosition() { return Position.EMPTY_POSITION; }
    
    public Position getMaxPosition() { return Position.EMPTY_POSITION; }
    
    public void setPositionResolver(PositionResolver p) {
        for (Iterator i = this.members.iterator(); i.hasNext(); ) ((RichLocation)i.next()).setPositionResolver(p);
    }
    
    /**
     * {@inheritDoc}
     */
    public Iterator blockIterator() { return Collections.unmodifiableCollection(this.members).iterator(); }
    
    /**
     * {@inheritDoc}
     * Always returns false, else it wouldn't be a compound location would it.
     */
    public boolean isContiguous() { return false; }
    
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
        return new CompoundRichLocation(this.term,newmembers);
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
        if (!(l instanceof RichLocation)) l = RichLocation.Tools.enrich(l);
        if (l instanceof EmptyRichLocation) return this;
        else {
            List members = new ArrayList();
            members.addAll(RichLocation.Tools.flatten(this));
            members.addAll(RichLocation.Tools.flatten((RichLocation)l));
            return RichLocation.Tools.construct(RichLocation.Tools.merge(members));
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Location intersection(Location l) {
        if (!(l instanceof RichLocation)) l = RichLocation.Tools.enrich(l);
        if (l instanceof EmptyRichLocation) return l;
        else if (l instanceof SimpleRichLocation) {
            // Simple vs. ourselves
            Set results = new TreeSet();
            for (Iterator i = this.members.iterator(); i.hasNext(); ) {
                RichLocation member = (RichLocation)i.next();
                results.add(member.intersection(l));
            }
            return RichLocation.Tools.construct(RichLocation.Tools.merge(results));
        } else {
            Collection theirMembers = RichLocation.Tools.flatten((RichLocation)l);
            // for each of their members, record intersection results
            Set results = new TreeSet();
            for (Iterator i = theirMembers.iterator(); i.hasNext(); ) {
                RichLocation member = (RichLocation)i.next();
                results.add(this.intersection(member));
            }
            return RichLocation.Tools.construct(RichLocation.Tools.merge(results));
        }
    }
    
    /**
     * {@inheritDoc}
     * This function concatenates the symbols of all its child locations. Note that
     * if it is made up of overlapping sections, the results may not be any use.
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

