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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.biojava.bio.Annotation;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolListViews;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.CrossRef;
import org.biojavax.RichAnnotation;
import org.biojavax.SimpleRichAnnotation;
import org.biojavax.bio.seq.Position.ExactPosition;
import org.biojavax.bio.seq.PositionResolver.AverageResolver;
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
    
    private CrossRef crossRef;
    private RichAnnotation notes = new SimpleRichAnnotation();
    private ComparableTerm term;
    private Position min;
    private Position max;
    private PositionResolver pr = new AverageResolver();
    private Strand strand;
    private int rank;
    
    /**
     * Creates a new instance of SimpleRichSequenceLocation.
     *
     * @param min Min location position.
     * @param max Max location position.
     * @param rank Rank of location.
     */
    public SimpleRichLocation(Position min, Position max, int rank) {
        this(min,max,rank,RichLocation.POSITIVE_STRAND);
    }
    public SimpleRichLocation(Position min, Position max, int rank, Strand strand) {
        this(min,max,rank,strand,null);
    }
    public SimpleRichLocation(Position min, Position max, int rank, Strand strand, CrossRef crossRef) {
        this.min = min;
        this.max = max;
        this.rank = rank;
        this.strand = strand;
        this.crossRef = crossRef;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleRichLocation() {}
    
    /**
     * {@inheritDoc}
     */
    public CrossRef getCrossRef() { return this.crossRef; }
    
    
    // Hibernate requirement - not for public use.
    private void setCrossRef(CrossRef crossRef) { this.crossRef = crossRef; }
    
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
    public void setNoteSet(Set notes) throws ChangeVetoException { this.notes.setNoteSet(notes); }
    
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
    
    // Hibernate requirement - not for public use.
    private int getStrandNum() { return (this.strand==RichLocation.NEGATIVE_STRAND?-1:1); }
    
    // Hibernate requirement - not for public use.
    private void setStrandNum(int token) {
        switch (token) {
            case 1:
                this.strand = RichLocation.POSITIVE_STRAND;
                break;
            case -1:
                this.strand = RichLocation.NEGATIVE_STRAND;
                break;
            default:
                this.strand = RichLocation.UNKNOWN_STRAND;
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
    public int getMax() { return this.pr.getMax(this.max); }
        
    // Hibernate requirement - not for public use.
    private void setMax(int max) {  this.max = new ExactPosition(false,false,max); }
    
    /**
     * {@inheritDoc}
     */
    public int getMin() { return this.pr.getMin(this.min); }
    
    // Hibernate requirement - not for public use.
    private void setMin(int min) {  this.min = new ExactPosition(false,false,min); }
    
    public Position getMinPos() { return this.min; }
    
    public Position getMaxPos() { return this.max; }
            
    public void setPositionResolver(PositionResolver p) { this.pr = p; }
    
    /**
     * {@inheritDoc}
     */
    public Iterator blockIterator() { return Collections.singleton(this).iterator(); }
    
    /**
     * {@inheritDoc}
     */
    public boolean contains(Location l) {
        if (!(l instanceof RichLocation)) throw new IllegalArgumentException("Location cannot be a non-RichLocation");
        if (!this.overlaps(l)) return false; // check strand etc.
        if (l instanceof CompoundRichLocation) {
            for (Iterator i = l.blockIterator(); i.hasNext(); ) if (!this.contains((RichLocation)i.next())) return false;
            return true;
        } else {
            return (this.getMin() <= l.getMin() && this.getMax() >=l.getMax());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean overlaps(Location l) {
        if (!(l instanceof RichLocation)) throw new IllegalArgumentException("Location cannot be a non-RichLocation");
        RichLocation rl = (RichLocation)l;
        if (rl.getStrand()!=this.strand) return false;
        if (rl.getCrossRef()!=null || this.crossRef!=null) {
            if (rl.getCrossRef()!=null && this.crossRef!=null) {
                if (!this.crossRef.equals(rl.getCrossRef())) return false;
            } else return false;
        }
        return (rl.getMin()<this.getMax() && rl.getMax()>this.getMin());
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isContiguous() { return true; }
    
    /**
     * {@inheritDoc}
     */
    public boolean contains(int p) { return (p>=this.getMin() && p<=this.getMax()); }
    
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
        return new SimpleRichLocation(this.min.translate(dist),this.max.translate(dist),0,this.strand,this.crossRef);
    }
    
    /**
     * {@inheritDoc}
     */
    public Location union(Location l) {
        if (l==null) throw new IllegalArgumentException("Location cannot be null");
        if (!(l instanceof RichLocation)) throw new IllegalArgumentException("Location cannot be a non-RichLocation");
        RichLocation rl = (RichLocation)l;
        if (this.overlaps(rl)) {
            return new SimpleRichLocation(this.posmin(this.min,rl.getMinPos()),this.posmax(this.max,rl.getMaxPos()),0,this.strand,this.crossRef);
        } else {
            List s = new ArrayList();
            s.add(this);
            s.add(rl);
            return new CompoundRichLocation(CompoundRichLocation.getJoinTerm(),s);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Location intersection(Location l) {
        if (l==null) throw new IllegalArgumentException("Location cannot be null");
        if (!(l instanceof RichLocation)) throw new IllegalArgumentException("Location cannot be a non-RichLocation");
        RichLocation them = (RichLocation)l;
        
        if (!this.overlaps(l)) return RichLocation.EMPTY_LOCATION;
        
        return new SimpleRichLocation(this.posmax(this.min,them.getMinPos()),this.posmin(this.max,them.getMaxPos()),0,this.strand,this.crossRef);
    }
    
    private Position posmin(Position a, Position b) {
        int ar = this.pr.getMin(a);
        int br = this.pr.getMin(b);
        if (ar<=br) return a;
        else return b;
    }
    
    private Position posmax(Position a, Position b) {
        int ar = this.pr.getMax(a);
        int br = this.pr.getMax(b);
        if (ar>br) return a;
        else return b;
    }
    
    /**
     * {@inheritDoc}
     */
    public SymbolList symbols(SymbolList seq) {
        if (seq==null) throw new IllegalArgumentException("Sequence cannot be null");
        
        if ((this.getMax()-this.getMin())<1) return SymbolList.EMPTY_LIST;
        
        SymbolList seq2 = seq.subList(this.getMin(),this.getMax());
        
        try {
            if (this.strand==RichLocation.NEGATIVE_STRAND) {
                Alphabet a = seq.getAlphabet();
                if (a==AlphabetManager.alphabetForName("DNA")) {
                    seq2 = DNATools.reverseComplement(seq);
                } else if (a==AlphabetManager.alphabetForName("RNA")) {
                    seq2 = RNATools.reverseComplement(seq);
                } else {
                    seq2 = SymbolListViews.reverse(seq2);// no complement as no such thing
                }
            }
        } catch (IllegalAlphabetException e) {
            throw new IllegalArgumentException("Could not understand alphabet of passed sequence", e);
        }
        
        return seq2;
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.strand==null) return code;
        // Normal comparison
        if (this.term!=null) code = 31*code + this.term.hashCode();
        code = 31*code + this.getMin();
        code = 31*code + this.getMax();
        code = 31*code + this.strand.hashCode();
        code = 31*code + this.rank;
        if (this.crossRef!=null) code = 31*code + this.crossRef.hashCode();
        return code;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (! (o instanceof RichLocation)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.strand==null) return false;
        // Normal comparison
        RichLocation fo = (RichLocation) o;
        if (!this.term.equals(fo.getTerm())) return false;
        if (this.getMin()!=fo.getMin()) return false;
        if (this.getMax()!=fo.getMax()) return false;
        if (!this.strand.equals(fo.getStrand())) return false;
        if (this.crossRef!=null || fo.getCrossRef()!=null) {
            if (this.crossRef!=null && fo.getCrossRef()!=null) {
                return this.crossRef.equals(fo.getCrossRef());
            } else return false;
        }
        return this.rank==fo.getRank();
    }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        // Hibernate comparison - we haven't been populated yet
        if (this.strand==null) return -1;
        // Normal comparison
        RichLocation fo = (RichLocation) o;
        if (this.rank!=fo.getRank()) return this.rank-fo.getRank();
        if (this.crossRef!=null || fo.getCrossRef()!=null) {
            if (this.crossRef!=null && fo.getCrossRef()!=null) {
                return this.crossRef.compareTo(fo.getCrossRef());
            } else return -1;
        }
        if (!this.strand.equals(fo.getStrand())) return this.strand==RichLocation.NEGATIVE_STRAND?-1:1;
        if (!this.term.equals(fo.getTerm())) return this.term.compareTo(fo.getTerm());
        if (this.getMin()!=fo.getMin()) return this.getMin()-fo.getMin();
        return this.getMax()-fo.getMax();
    }
    
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    // Hibernate requirement - not for public use.
    private Long getId() { return this.id; }
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) { this.id = id; }
}

