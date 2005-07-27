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
 
 * SimpleRichSequenceFeature.java
 
 *
 
 * Created on June 16, 2005, 11:47 AM
 
 */



package org.biojavax.bio.seq;
import java.util.HashSet;
import java.util.Iterator;

import java.util.Set;

import org.biojava.bio.Annotatable;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;

import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.FilterUtils;
import org.biojava.bio.seq.RealizingFeatureHolder;

import org.biojava.bio.seq.Sequence;

import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.ontology.OntoTools;
import org.biojava.ontology.Term;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;

import org.biojava.utils.ChangeForwarder;
import org.biojava.utils.ChangeSupport;

import org.biojava.utils.ChangeVetoException;
import org.biojavax.bio.*;
import org.biojavax.ontology.ComparableTerm;






/**
 * A simple implementation of RichSequenceFeature.
 *
 * Equality is inherited from SimpleStrandedFeature.
 *
 * @author Richard Holland
 * @author Mark Schreiber
 */

public class SimpleRichSequenceFeature extends AbstractChangeable implements RichSequenceFeature {
    
    /**
     *
     * The annotation for this feature.
     *
     */
    
    private BioEntryAnnotation ann = new SimpleBioEntryAnnotation();
    
    /**
     *
     * The event forwarder for this feature.
     *
     */
    
    private ChangeForwarder annFor;
    private ChangeForwarder featFor;
    
    
    
    /**
     * Creates a new instance of SimpleRichSequenceFeature
     * @param sourceSeq The sequence to relate the feature to.
     * @param parent The parent feature holder, if any.
     * @param template The template to construct the feature from.
     */
    
    public SimpleRichSequenceFeature(Sequence sourceSeq, FeatureHolder parent, StrandedFeature.Template template) {
        
        if (template.location == null) throw new IllegalArgumentException("Location can not be null. Did you mean Location.EMPTY_LOCATION? " + template.toString());
        if(!(parent instanceof RichSequenceFeature) && !(parent instanceof RichSequence)) throw new IllegalArgumentException("Parent must be rich sequence or feature, not: " + parent.getClass() + " " + parent);
        
        this.parent = (RichSequenceFeatureHolder)parent;
        this.loc = template.location;
        this.typeTerm = template.typeTerm != null ? template.typeTerm : OntoTools.ANY;
        this.sourceTerm = template.sourceTerm != null ? template.sourceTerm : OntoTools.ANY;
        
        this.strand = template.strand;
        
        // transfer any existing annotations
        
        if (template.annotation!=null) {
            
            Set k = template.annotation.keys();
            
            for (Iterator i = k.iterator(); i.hasNext(); ) {
                
                Object key = i.next();
                
                Object value = template.annotation.getProperty(key);
                
                try {
                    
                    this.ann.setProperty(key,value);
                    
                } catch (ChangeVetoException c) {
                    
                    throw new IllegalArgumentException(c);
                    
                }
                
            }
            
        }
        
        // construct the forwarder so that it emits Annotatable.ANNOTATION ChangeEvents
        
        // for the Annotation.PROPERTY events it will listen for
        
        this.annFor = new ChangeForwarder.Retyper(this, this.getChangeSupport(Annotatable.ANNOTATION), Annotatable.ANNOTATION);
        this.featFor = new ChangeForwarder(this, this.getChangeSupport(FeatureHolder.FEATURES));
        
        // connect the forwarder so it listens for Annotation.PROPERTY events
        
        this.ann.addChangeListener(this.annFor, Annotation.PROPERTY);
        this.featureHolder.addChangeListener(this.featFor, FeatureHolder.FEATURES);
        
    }
    
    
    // Hibernate requirement - not for public use.
    private SimpleRichSequenceFeature() {}
    
    /**
     *
     * Should return the associated annotation object.
     *
     *
     *
     * @return an Annotation object, never null
     *
     */
    
    public Annotation getAnnotation() {
        
        return this.ann;
        
    }
    
    // Hibernate requirement - not for public use.
    private class Note {
        private ComparableTerm term;
        private String value;
        private int rank;
        private RichSequenceFeature f;
        private Note() {}
        private Note(ComparableTerm term, String value, int rank, RichSequenceFeature f) {
            this.term = term;
            this.value = value;
            this.rank = rank;
            this.f = f;
        }
        private void setTerm(ComparableTerm term) { this.term = term; }
        private ComparableTerm getTerm() { return this.term; }
        private void setValue(String value) { this.value = value; }
        private String getValue() { return this.value; }
        private void setRank(int rank) { this.rank = rank; }
        private int getRank() { return this.rank; }
        private void setRichSequenceFeature(RichSequenceFeature f) { this.f = f; }
        private RichSequenceFeature getRichSequenceFeature() { return this.f; }
    }
    // Hibernate requirement - not for public use.
    private void setAnnotationSet(Set ann) throws ChangeVetoException { 
        this.ann.clear();
        for (Iterator i = ann.iterator(); i.hasNext(); ) {
            Note n = (Note)i.next();
            this.ann.setProperty(new SimpleBioEntryAnnotation.RankedTerm(n.getTerm(),n.getRank()),n.getValue());
        }
    }
    // Hibernate requirement - not for public use.
    private Set getAnnotationSet() { 
        Set ns = new HashSet();
        for (Iterator i = this.ann.keys().iterator(); i.hasNext(); ) {
            SimpleBioEntryAnnotation.RankedTerm rt = (SimpleBioEntryAnnotation.RankedTerm)i.next();
            ComparableTerm ct = rt.getTerm();
            int rank = rt.getRank();
            String v = (String)this.ann.getProperty(ct);
            Note n = new Note(ct,v,rank,this);
            ns.add(n);
        }
        return ns;
    }
    
    /**
     *
     * Sets the source of the feature.
     *
     * @param source the new source.
     *
     * @throws ChangeVetoException If the source is unacceptable.
     *
     */
    
    public void setSource(String source) throws ChangeVetoException {
        
        throw new ChangeVetoException("Source can only be set using setSourceTerm");
        
    }
    
    /**
     *
     * Sets the source of the feature.
     *
     * @param source the new source.
     *
     * @throws ChangeVetoException If the source is unacceptable.
     *
     */
    
    public void setType(String source) throws ChangeVetoException {
        
        throw new ChangeVetoException("Type can only be set using setTypeTerm");
        
    }
    
    public String getSource() {
        throw new RuntimeException("Not implemented - use getSourceTerm instead.");
    }
    
    public String getType() {
        throw new RuntimeException("Not implemented - use getTypeTerm instead.");
    }
    
    
    private StrandedFeature.Strand strand;
    
    public StrandedFeature.Strand getStrand() {
        return this.strand;
    }
    
    public void setStrand(Strand strand)
    throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(STRAND);
            synchronized(cs) {
                ChangeEvent ce =
                        new ChangeEvent(this, STRAND, strand, this.strand);
                cs.firePreChangeEvent(ce);
                this.strand = strand;
                cs.firePostChangeEvent(ce);
            }
        } else {
            this.strand = strand;
        }
    }
    
    public SymbolList getSymbols() {
        SymbolList symList = this.getLocation().symbols(this.getSequence());
        if (this.getStrand() == NEGATIVE) {
            try {
                symList = DNATools.reverseComplement(symList);
            } catch (IllegalAlphabetException iae) {
                throw new BioError(
                        "Could not retrieve symbols for feature as " +
                        "the alphabet can not be complemented.", iae
                        );
            }
        }
        return symList;
    }
    
    public Feature.Template makeTemplate() {
        StrandedFeature.Template ft = new StrandedFeature.Template();
        this.fillTemplate(ft);
        return ft;
    }
    
    protected void fillTemplate(StrandedFeature.Template ft) {
        ft.location = this.getLocation();
        ft.type = this.getType();
        ft.source = this.getSource();
        ft.annotation = this.getAnnotation();
        ft.sourceTerm = this.getSourceTerm();
        ft.typeTerm = this.getTypeTerm();
        ft.strand = this.getStrand();
    }
    
    public String toString() {
        String pm;
        if(getStrand() == POSITIVE) {
            pm = "+";
        } else {
            pm = "-";
        }
        return super.toString() + " " + pm;
    }
    
    private SimpleRichSequenceFeatureHolder featureHolder = new SimpleRichSequenceFeatureHolder();
    private Location loc;
    private RichSequenceFeatureHolder parent;
    
    private Term typeTerm;
    private Term sourceTerm;
    
    public Location getLocation() {
        return this.loc;
    }
    
    public void setLocation(Location loc) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(LOCATION);
            synchronized(cs) {
                ChangeEvent ce = new ChangeEvent(this, LOCATION, loc, this.loc);
                cs.firePreChangeEvent(ce);
                this.loc = loc;
                cs.firePostChangeEvent(ce);
            }
        } else {
            this.loc = loc;
        }
    }
    
    public Term getTypeTerm() {
        return this.typeTerm;
    }
    
    public void setTypeTerm(Term t) throws ChangeVetoException {
        if(this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(TYPE);
            synchronized (cs) {
                ChangeEvent ce_term = new ChangeEvent(this, TYPETERM, t, this.getTypeTerm());
                ChangeEvent ce_name = new ChangeEvent(this, TYPE, t.getName(), this.getType());
                cs.firePreChangeEvent(ce_term);
                cs.firePreChangeEvent(ce_name);
                this.typeTerm = typeTerm;
                cs.firePostChangeEvent(ce_term);
                cs.firePostChangeEvent(ce_name);
            }
        } else {
            this.typeTerm = typeTerm;
        }
    }
    
    public Term getSourceTerm() {
        return this.sourceTerm;
    }
    
    public void setSourceTerm(Term t) throws ChangeVetoException {
        if (this.hasListeners()) {
            ChangeSupport cs = this.getChangeSupport(TYPE);
            synchronized (cs) {
                ChangeEvent ce_term = new ChangeEvent(this, SOURCETERM, t, this.getSourceTerm());
                ChangeEvent ce_name = new ChangeEvent(this, SOURCE, t.getName(), this.getSource());
                cs.firePreChangeEvent(ce_term);
                cs.firePreChangeEvent(ce_name);
                this.sourceTerm = t;
                cs.firePostChangeEvent(ce_term);
                cs.firePostChangeEvent(ce_name);
            }
        } else {
            this.sourceTerm = sourceTerm;
        }
    }
    
    public Sequence getSequence() {
        FeatureHolder fh = this;
        while (fh instanceof Feature) {
            fh = ((Feature) fh).getParent();
        }
        try {
            return (Sequence) fh;
        } catch (ClassCastException ex) {
            throw new BioError("Feature doesn't seem to have a Sequence ancestor: " + fh);
        }
    }
    
    
    public FeatureHolder getParent() {
        return this.parent;
    }
    
    // Hibernate requirement - not for public use.
    private void setParent(RichSequenceFeatureHolder parent) { this.parent = parent; }
    
    public int countFeatures() {
        return this.featureHolder.countFeatures();
    }
    
    public Iterator features() {
        return this.featureHolder.features();
    }
    
    public void removeFeature(Feature f) throws ChangeVetoException {
        this.featureHolder.removeFeature(f);
    }
    
    public boolean containsFeature(Feature f) {
        return this.featureHolder.containsFeature(f);
    }
    
    
    public FeatureHolder filter(FeatureFilter ff) {
        FeatureFilter childFilter = new FeatureFilter.Not(FeatureFilter.top_level);
        
        if (FilterUtils.areDisjoint(ff, childFilter)) {
            return FeatureHolder.EMPTY_FEATURE_HOLDER;
        } else {
            return this.featureHolder.filter(ff);
        }
    }
    
    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
        return this.featureHolder.filter(ff, recurse);
    }
    
    public Feature realizeFeature(FeatureHolder fh, Feature.Template templ) throws BioException {
        try {
            RealizingFeatureHolder rfh = (RealizingFeatureHolder) this.getParent();
            return rfh.realizeFeature(fh, templ);
        } catch (ClassCastException ex) {
            throw new BioException("Couldn't propagate feature creation request.");
        }
    }
    
    public Feature createFeature(Feature.Template temp) throws BioException, ChangeVetoException {
        Feature f = this.realizeFeature(this, temp);
        this.featureHolder.addFeature(f);
        return f;
    }
    
    // Hibernate requirement - not for public use.
    private Set getFeatureSet() {
        return this.featureHolder.getFeatureSet();
    }
    
    // Hibernate requirement - not for public use.
    private void setFeatureSet(Set features) throws ChangeVetoException {
        this.featureHolder.setFeatureSet(features);
    }
    
    public FeatureFilter getSchema() {
        return new FeatureFilter.ByParent(new FeatureFilter.ByFeature(this));
    }
    
    public int hashCode() {
        return this.makeTemplate().hashCode();
    }
    
    public boolean equals(Object o) {
        if (! (o instanceof Feature)) return false;
        
        Feature fo = (Feature) o;
        if (! fo.getSequence().equals(this.getSequence())) return false;
        
        return this.makeTemplate().equals(fo.makeTemplate());
    }
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    
    // Hibernate requirement - not for public use.
    private Long getId() {
        
        return this.id;
    }
    
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) {
        
        this.id = id;
    }
}

