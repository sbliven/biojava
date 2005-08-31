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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
 * An implementation of RichLocation which covers multiple positions, 
 * maybe even on different strands and/or on different sequences.
 * @author Richard Holland
 * @author Mark Schreiber
 */
public class CompoundRichLocation extends AbstractChangeable implements RichLocation {
    
    private ComparableTerm term;
    private List members;
    private static ComparableTerm JOIN_TERM = null;
    private static ComparableTerm ORDER_TERM = null;
    
    /**
     * Getter for the "join" term
     * @return the "join" term
     */
    public static ComparableTerm getJoinTerm() {
        if (JOIN_TERM==null) JOIN_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("join");
        return JOIN_TERM;
    }
    
    /**
     * Getter for the "order" term
     * @return the "order" term
     */
    public static ComparableTerm getOrderTerm() {
        if (ORDER_TERM==null) ORDER_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("order");
        return ORDER_TERM;
    }
    
    /**
     * Constructs a CompoundRichLocation from the given set of members, with
     * the default term of "join". Note that you really shouldn't use this if
     * you are unsure if your members set contains overlapping members. Use
     * RichLocation.Tools.construct() instead. The members collection
     * must only contain Location instances. Any that are not RichLocations will
     * be converted using RichLocation.Tools.enrich().
     * @param members the members to put into the compound location.
     * @see RichLocation.Tools
     */
    public CompoundRichLocation(Collection members) { this(getJoinTerm(), members); }
    
    /**
     * Constructs a CompoundRichLocation from the given set of members.
     * Note that you really shouldn't use this if
     * you are unsure if your members set contains overlapping members. Use
     * RichLocation.Tools.construct(members) instead. The members collection
     * must only contain Location instances. Any that are not RichLocations will
     * be converted using RichLocation.Tools.enrich().
     * @param term the term to use when describing the group of members.
     * @param members the members to put into the compound location.
     * @see RichLocation.Tools
     */
    public CompoundRichLocation(ComparableTerm term, Collection members) {
        if (term==null) throw new IllegalArgumentException("Term cannot be null");
        if (members==null || members.size()<2) throw new IllegalArgumentException("Must have at least two members");        
        this.term = term;
        this.members = new ArrayList();        
        for (Iterator i = members.iterator(); i.hasNext(); ) {
            // Convert each member into a RichLocation
            Object o = i.next();
            if (!(o instanceof RichLocation)) o = RichLocation.Tools.enrich((Location)o);
            // Add in member
            this.members.add(o);
        }
    }
                
    /**
     * {@inheritDoc} 
     * This method will only return the feature from the first member of this compound location.
     */
    public RichFeature getFeature() { return ((RichLocation)this.members.get(0)).getFeature(); }
    
    /**
     * {@inheritDoc} 
     * Passes the call on to each of its members in turn.
     */
    public void setFeature(RichFeature feature) throws ChangeVetoException {
        for (Iterator i = this.members.iterator(); i.hasNext(); ) ((RichLocation)i.next()).setFeature(feature);
    }
    
    /**
     * {@inheritDoc}
     */
    public CrossRef getCrossRef() { return null; }
    
    /**
     * {@inheritDoc}
     * ALWAYS RETURNS THE EMPTY ANNOTATION
     */
    public Annotation getAnnotation() { return RichAnnotation.EMPTY_ANNOTATION; }
    
    /**
     * {@inheritDoc}
     * ALWAYS RETURNS THE EMPTY ANNOTATION NOTE SET
     */
    public Set getNoteSet() { return RichAnnotation.EMPTY_ANNOTATION.getNoteSet(); }
    
    /**
     * {@inheritDoc}
     * NOT IMPLEMENTED
     * @throws ChangeVetoException ALWAYS
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
     * ALWAYS RETURNS ZERO
     */
    public int getCircularLength() { return 0; }
    
    /**
     * {@inheritDoc}
     * NOT IMPLEMENTED
     * @throws ChangeVetoException ALWAYS
     */
    public void setCircularLength(int sourceSeqLength) throws ChangeVetoException {
        throw new ChangeVetoException("CompoundRichLocations cannot be circular");
    }
    
    /**
     * {@inheritDoc}
     * ALWAYS RETURNS UNKNOWN_STRAND
     */
    public Strand getStrand() { return Strand.UNKNOWN_STRAND; }
    
    /**
     * {@inheritDoc}
     * ALWAYS RETURNS ZERO
     */
    public int getRank() { return 0; }
    
    /**
     * {@inheritDoc}
     * NOT IMPLEMENTED
     */
    public void setRank(int rank) throws ChangeVetoException {
        throw new ChangeVetoException("Cannot rank compound locations.");
    }
    
    /**
     * {@inheritDoc}
     * ALWAYS RETURNS ZERO
     */
    public int getMax() { return 0; }
    
    /**
     * {@inheritDoc}
     * ALWAYS RETURNS ZERO
     */
    public int getMin() { return 0; }
    
    /**
     * {@inheritDoc}
     * ALWAYS RETURNS THE EMPTY POSITION
     */
    public Position getMinPosition() { return Position.EMPTY_POSITION; }
    
    /**
     * {@inheritDoc}
     * ALWAYS RETURNS THE EMPTY POSITION
     */
    public Position getMaxPosition() { return Position.EMPTY_POSITION; }

    /**
     * {@inheritDoc}
     * Recursively applies this call to all members.
     */
    public void setPositionResolver(PositionResolver p) {
        for (Iterator i = this.members.iterator(); i.hasNext(); ) ((RichLocation)i.next()).setPositionResolver(p);
    }
    
    /**
     * {@inheritDoc}
     */
    public Iterator blockIterator() { return Collections.unmodifiableCollection(this.members).iterator(); }
    
    /**
     * {@inheritDoc}
     * ALWAYS RETURNS FALSE
     */
    public boolean isContiguous() { return false; }
    
    /**
     * {@inheritDoc}
     * Recursively applies this call to all members.
     */
    public boolean contains(int p) {
        for (Iterator i = this.members.iterator(); i.hasNext(); ) if (((RichLocation)i.next()).contains(p)) return true;
        return false;
    }
    
    /**
     * {@inheritDoc}
     * ALWAYS RETURNS NULL
     */
    public Location getDecorator(Class decoratorClass) { return null; }
    
    /**
     * {@inheritDoc}
     * ALWAYS RETURNS SELF
     */
    public Location newInstance(Location loc) { return loc; }
    
    /**
     * {@inheritDoc}
     * Recursively translates all members of this location.
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
     * Recursively applies this call to all members. If passed a Location 
     * which is not a RichLocation, it converts it first using 
     * RichLocation.Tools.enrich().
     * @see RichLocation.Tools
     * @return true if an only if one of the members of this <code>Location</code>
     * wholey contains <code>l</code>.
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
     * Recursively applies this call to all members.
     * @return true if and only if at least on of the members overlaps <code>l</code>
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
     * If passed a Location which is not a RichLocation, it converts it first 
     * using RichLocation.Tools.enrich().
     * The resulting location may or may not be a compound location. If it is
     * a compound location, its contents will be a set of simple locations.
     * Regions that overlap will be merged into a single location.
     * @see RichLocation.Tools
     * @return a <code>CompoundLocation</code> if the components of the union
     * cannot be merged else a <code>SimpleRichLocation</code>
     */
    public Location union(Location l) {
        if (!(l instanceof RichLocation)) l = RichLocation.Tools.enrich(l);
        if (l instanceof EmptyRichLocation) return this;
        else {
            // Easy - construct a new location based on the members of both
            // ourselves and the location passed as a parameter
            List members = new ArrayList();
            members.addAll(RichLocation.Tools.flatten(this));
            members.addAll(RichLocation.Tools.flatten((RichLocation)l));
            return RichLocation.Tools.construct(RichLocation.Tools.merge(members));
        }
    }
    
    /**
     * {@inheritDoc}
     * If passed a Location which is not a RichLocation, it converts it first 
     * using RichLocation.Tools.enrich().
     * The resulting location may or may not be a compound location. If it is
     * a compound location, its contents will be a set of simple locations.
     * @return a <code>CompoundLocation</code> if there is more than one region
     * of intersection that cannot be merged. Else a <code>SimpleRichLocation</code>.
     */
    public Location intersection(Location l) {
        if (!(l instanceof RichLocation)) l = RichLocation.Tools.enrich(l);
        if (l instanceof EmptyRichLocation) return l;
        else if (l instanceof SimpleRichLocation) {
            // Simple vs. ourselves
            // For every member of ourselves, intersect with the location
            // passed as a parameter. Then construct a new location from the
            // results of all the intersections.
            Set results = new TreeSet();
            for (Iterator i = this.members.iterator(); i.hasNext(); ) {
                RichLocation member = (RichLocation)i.next();
                results.add(member.intersection(l));
            }
            return RichLocation.Tools.construct(RichLocation.Tools.merge(results));
        } else {
            Collection theirMembers = RichLocation.Tools.flatten((RichLocation)l);
            // For every member of the location passed as a parameter, intersect 
            // with ourselves. Then construct a new location from the
            // results of all the intersections.
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
     * This function concatenates the symbols of all its child locations. If
     * the components of the location are from the negative strand the Symbols
     * will be reverse complemented. If there are mixed strands the results may
     * not be sensible but it is still possible.
     * <p>
     * The most obvious application of this method to a <code>CompoundRichLocation</code>
     * is the contatenation of the components of a gene with multiple exons.
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
     * Compound locations are only equal to other Locations if all their
     * components are equal.
     */
    public boolean equals(Object o) {
        if (o==this) return true;
        if (! (o instanceof Location)) return false;
        Location them = (Location) o;
        
        if(them.isContiguous()) return false; //because this is not!
        
        // ok - both compound. The blocks returned from blockIterator should each be
        // equivalent.
        Iterator i1 = this.blockIterator();
        Iterator i2 = them.blockIterator();

        // while there are more pairs to check...
        while(i1.hasNext() && i2.hasNext()) {
            // check that this pair is equivalent
            Location l1 = (Location) i1.next();
            Location l2 = (Location) i2.next();

            if(!(l1.equals(l2))) // not equivalent blocks so not equal
                return false;
        }
        if(i1.hasNext() || i2.hasNext()) {
            // One of the locations had more blocks than the other
            return false;
        }
        // Same number of blocks, all equivalent. Must be equal.
        return true;
    }
    
    /**
     * {@inheritDoc}
     * 
     */
    public int compareTo(Object o) {
        Location fo = (Location) o;
        if (this.equals(fo)) return 0;
        else return this.getMin() - fo.getMin();
    }
    
    /**
     * {@inheritDoc}
     * Form: "term:[location,location...]"
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.term);
        sb.append(":[");
        for (Iterator i = this.blockIterator(); i.hasNext(); ) {
            sb.append(i.next());
            if (i.hasNext()) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}

