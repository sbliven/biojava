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
 * SimpleRichLocation.java
 *
 * Created on June 16, 2005, 11:47 AM
 */

package org.biojavax.bio.seq;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.biojava.bio.Annotation;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.StrandedFeature.Strand;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.LocationTools;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolListViews;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.CrossRef;
import org.biojavax.Note;
import org.biojavax.RichAnnotation;
import org.biojavax.SimpleRichAnnotation;
import org.biojavax.ontology.ComparableTerm;

/**
 * A simple implementation of RichLocation.
 *
 * Equality is based on parent, min and max, strand, and rank.
 *
 * @author Richard Holland
 * @author Mark Schreiber
 */
public class SimpleRichLocation extends AbstractChangeable implements RichLocation {
    
    private Set blocks = new TreeSet();
    private CrossRef crossRef;
    private RichAnnotation notes = new SimpleRichAnnotation();
    private ComparableTerm term;
    private int min;
    private int max;
    private Strand strand;
    private int rank;
    
    /**
     * Creates a new instance of SimpleRichSequenceLocation.
     *
     * @param min Min location position.
     * @param max Max location position.
     * @param rank Rank of location.
     */
    public SimpleRichLocation(int min, int max, int rank) {
        this.min = min;
        this.max = max;
        this.rank = rank;
        this.strand = StrandedFeature.UNKNOWN;
        this.blocks.add(this);
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleRichLocation() {}
    
    /**
     * {@inheritDoc}
     */
    public CrossRef getCrossRef() { return this.crossRef; }
    
    /**
     * {@inheritDoc}
     */
    public void setCrossRef(CrossRef crossRef) throws ChangeVetoException {
        if(!this.hasListeners(RichLocation.CROSSREF)) {
            this.crossRef = crossRef;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichLocation.CROSSREF,
                    crossRef,
                    this.crossRef
                    );
            ChangeSupport cs = this.getChangeSupport(RichLocation.CROSSREF);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.crossRef = crossRef;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Annotation getAnnotation() { return this.notes; }
    
    /**
     * {@inheritDoc}
     */
    public Set getNoteSet() { return this.notes.getNoteSet(); }
    
    /**
     * {@inheritDoc}
     */
    public void setNoteSet(Set notes) throws ChangeVetoException {
        this.notes.clear();
        if (notes==null) return;
        for (Iterator i = notes.iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (!(o instanceof Note)) throw new ChangeVetoException("Note set must only have notes in");
            if(!this.hasListeners(RichLocation.NOTE)) {
                this.notes.addNote((Note)o);
            } else {
                ChangeEvent ce = new ChangeEvent(
                        this,
                        RichLocation.NOTE,
                        o,
                        null
                        );
                ChangeSupport cs = this.getChangeSupport(RichLocation.NOTE);
                synchronized(cs) {
                    cs.firePreChangeEvent(ce);
                    this.notes.addNote((Note)o);
                    cs.firePostChangeEvent(ce);
                }
            }
        }
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
    public Strand getStrand() { return this.strand; }
    
    /**
     * {@inheritDoc}
     */
    public void setStrand(Strand strand) throws ChangeVetoException {
        if (strand==null) throw new IllegalArgumentException("Strand cannot be null");
        if(!this.hasListeners(RichLocation.STRAND)) {
            this.strand = strand;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichLocation.STRAND,
                    term,
                    this.term
                    );
            ChangeSupport cs = this.getChangeSupport(RichLocation.STRAND);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.strand = strand;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    // Hibernate requirement - not for public use.
    private String getStrandChar() { return ""+this.strand.getToken(); }
    
    // Hibernate requirement - not for public use.
    private void setStrandChar(String token) {
        if (token==null) {
            this.strand = StrandedFeature.UNKNOWN;
            return;
        }
        char t = token.charAt(0);
        switch (t) {
            case '+':
                this.strand = StrandedFeature.POSITIVE;
                break;
            case '-':
                this.strand = StrandedFeature.NEGATIVE;
                break;
            default:
                this.strand = StrandedFeature.UNKNOWN;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int getRank() { return this.rank; }
    
    /**
     * {@inheritDoc}
     */
    public void setRank(int rank) throws ChangeVetoException {
        if(!this.hasListeners(RichLocation.RANK)) {
            this.rank = rank;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichLocation.RANK,
                    new Integer(rank),
                    new Integer(this.rank)
                    );
            ChangeSupport cs = this.getChangeSupport(RichLocation.RANK);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.rank = rank;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int getMax() { return this.max; }
    
    /**
     * {@inheritDoc}
     */
    public void setMax(int max) throws ChangeVetoException {
        if(!this.hasListeners(RichLocation.MAX)) {
            this.max = max;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichLocation.MAX,
                    new Integer(max),
                    new Integer(this.max)
                    );
            ChangeSupport cs = this.getChangeSupport(RichLocation.MAX);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.max = max;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int getMin() { return this.min; }
    
    /**
     * {@inheritDoc}
     */
    public void setMin(int min) throws ChangeVetoException {
        if(!this.hasListeners(RichLocation.MIN)) {
            this.min = min;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichLocation.MIN,
                    new Integer(min),
                    new Integer(this.min)
                    );
            ChangeSupport cs = this.getChangeSupport(RichLocation.MIN);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.min = min;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Iterator blockIterator() { return Collections.unmodifiableSet(this.blocks).iterator(); }
    
    /**
     * {@inheritDoc}
     */
    public boolean contains(Location l) { return LocationTools.contains(this,l); }
    
    /**
     * {@inheritDoc}
     */
    public boolean overlaps(Location l) { return LocationTools.overlaps(this,l); }
    
    /**
     * {@inheritDoc}
     */
    public boolean isContiguous() { return this.blocks.size()==1; }
    
    /**
     * {@inheritDoc}
     */
    public boolean contains(int p) { return this.getMin()<=p && p<=this.getMax(); }
    
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
        try {
            SimpleRichLocation l = new SimpleRichLocation(this.min+dist,this.max+dist,this.rank);
            l.setStrand(this.getStrand());
            l.setCrossRef(this.getCrossRef());
            l.setNoteSet(this.getNoteSet());
            l.setTerm(this.getTerm());
            if (!this.isContiguous()) {
                l.blocks.clear();
                for (Iterator i = this.blocks.iterator(); i.hasNext(); ) {
                    Location b = (Location)i.next();
                    l.blocks.add(b.translate(dist));
                }
            }
            return l;
        } catch (ChangeVetoException e) {
            throw new RuntimeException("Someone doesn't like us changing things!",e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Location union(Location l) {
        try {
            if (l==null) throw new IllegalArgumentException("Location cannot be null");
            Location u = LocationTools.union(this,l);
            SimpleRichLocation r = new SimpleRichLocation(u.getMin(),u.getMax(),this.rank);
            r.setStrand(this.getStrand());
            r.setCrossRef(this.getCrossRef());
            r.setNoteSet(this.getNoteSet());
            r.setTerm(this.getTerm());
            if (!u.isContiguous()) {
                r.blocks.clear();
                int counter = 1;
                for (Iterator i = u.blockIterator(); i.hasNext(); ) {
                    Location b = (Location)i.next();
                    RichLocation r2 = new SimpleRichLocation(b.getMin(), b.getMax(), counter++);
                    if (b instanceof RichLocation) {
                        RichLocation b2 = (RichLocation)b;
                        r2.setStrand(b2.getStrand());
                        r2.setCrossRef(b2.getCrossRef());
                        r2.setNoteSet(b2.getNoteSet());
                        r2.setTerm(b2.getTerm());
                    }
                    r.blocks.add(r2);
                }
            }
            return r;
        } catch (ChangeVetoException e) {
            throw new RuntimeException("Someone doesn't like us changing things!",e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Location intersection(Location l) {
        try {
            if (l==null) throw new IllegalArgumentException("Location cannot be null");
            Location u = LocationTools.intersection(this,l);
            SimpleRichLocation r = new SimpleRichLocation(u.getMin(),u.getMax(),this.rank);
            r.setStrand(this.getStrand());
            r.setCrossRef(this.getCrossRef());
            r.setNoteSet(this.getNoteSet());
            r.setTerm(this.getTerm());
            if (!u.isContiguous()) {
                r.blocks.clear();
                int counter = 1;
                for (Iterator i = u.blockIterator(); i.hasNext(); ) {
                    Location b = (Location)i.next();
                    RichLocation r2 = new SimpleRichLocation(b.getMin(), b.getMax(), counter++);
                    if (b instanceof RichLocation) {
                        RichLocation b2 = (RichLocation)b;
                        r2.setStrand(b2.getStrand());
                        r2.setCrossRef(b2.getCrossRef());
                        r2.setNoteSet(b2.getNoteSet());
                        r2.setTerm(b2.getTerm());
                    }
                    r.blocks.add(r2);
                }
            }
            return r;
        } catch (ChangeVetoException e) {
            throw new RuntimeException("Someone doesn't like us changing things!",e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public SymbolList symbols(SymbolList seq) {
        if (seq==null) throw new IllegalArgumentException("Sequence cannot be null");
        SymbolList seq2 = seq.subList(this.getMin(),this.getMax());
        if (this.strand==StrandedFeature.NEGATIVE) return SymbolListViews.reverse(seq2);
        else return seq2;
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.term==null) return code;
        // Normal comparison
        code = 31*code + this.term.hashCode();
        code = 31*code + this.min;
        code = 31*code + this.max;
        code = 31*code + this.strand.hashCode();
        code = 31*code + this.rank;
        return code;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (! (o instanceof RichLocation)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.term==null) return false;
        // Normal comparison
        RichLocation fo = (RichLocation) o;
        if (!this.term.equals(fo.getTerm())) return false;
        if (this.min!=fo.getMin()) return false;
        if (this.max!=fo.getMax()) return false;
        if (!this.strand.equals(fo.getStrand())) return false;
        return this.rank==fo.getRank();
    }
    
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    // Hibernate requirement - not for public use.
    private Long getId() { return this.id; }
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) { this.id = id; }
}

