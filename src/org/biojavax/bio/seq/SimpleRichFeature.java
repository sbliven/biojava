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
 * SimpleRichFeature.java
 *
 * Created on June 16, 2005, 11:47 AM
 */

package org.biojavax.bio.seq;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.FilterUtils;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SimpleFeatureHolder;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.ontology.InvalidTermException;
import org.biojava.ontology.Term;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.RankedCrossRef;
import org.biojavax.RichAnnotation;
import org.biojavax.SimpleRichAnnotation;
import org.biojavax.ontology.ComparableTerm;

/**
 * A simple implementation of RichFeature.
 *
 * Equality is based on all type, source, parent and location fields.
 *
 * @author Richard Holland
 * @author Mark Schreiber
 */
public class SimpleRichFeature extends AbstractChangeable implements RichFeature {
    
    private RichAnnotation notes = new SimpleRichAnnotation();
    private ComparableTerm typeTerm;
    private ComparableTerm sourceTerm;
    private FeatureHolder parent;
    private RichLocation location;
    private Set crossrefs = new HashSet();
    private Set relations = new HashSet();
    private String name;
    private int rank;
    
    /**
     * Creates a new instance of SimpleRichFeature
     * @param parent The parent feature holder.
     * @param templ The template to construct the feature from.
     */
    public SimpleRichFeature(FeatureHolder parent, Feature.Template templ) throws ChangeVetoException {
        if (parent==null) throw new IllegalArgumentException("Parent cannot be null");
        if (templ==null) throw new IllegalArgumentException("Template cannot be null");
        if (templ.typeTerm==null) throw new IllegalArgumentException("Template type term cannot be null");
        if (templ.sourceTerm==null) throw new IllegalArgumentException("Template source term cannot be null");
        if (templ.location==null) throw new IllegalArgumentException("Template location cannot be null");
        this.parent = parent;
        this.typeTerm = (ComparableTerm)templ.typeTerm;
        this.sourceTerm = (ComparableTerm)templ.sourceTerm;
        
        if (templ.annotation instanceof RichAnnotation) {
            this.notes.setNoteSet(((RichAnnotation)templ.annotation).getNoteSet());
        } else {
            this.notes = new SimpleRichAnnotation();
            for (Iterator i = templ.annotation.keys().iterator(); i.hasNext(); ) {
                Object key = i.next();
                this.notes.setProperty(i.next(), templ.annotation.getProperty(key));
            }
        }
        
        if (templ.location instanceof RichLocation) {
            this.location.setBlocks(((RichLocation)templ.location).getBlocks());
        } else {
            Location u = templ.location;
            int counter = 0;
            RichLocation r = new SimpleRichLocation(0,0,counter++);
            Set blocks = new HashSet();
            for (Iterator i = u.blockIterator(); i.hasNext(); ) {
                Location b = (Location)i.next();
                if (b instanceof RichLocation) blocks.add(b);
                else blocks.add(new SimpleRichLocation(b.getMin(), b.getMax(),counter++));
            }
            r.setBlocks(blocks);
            this.location = r;
        }
        
        if (templ instanceof RichFeature.Template) {
            this.setRankedCrossRefs(((RichFeature.Template)templ).rankedCrossRefs);
            this.setFeatureRelationshipSet(((RichFeature.Template)templ).featureRelationshipSet);
        }
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleRichFeature() {}
    
    /**
     * {@inheritDoc}
     */
    public Feature.Template makeTemplate() {
        RichFeature.Template templ = new RichFeature.Template();
        templ.annotation = this.notes;
        templ.featureRelationshipSet = this.relations;
        templ.rankedCrossRefs = this.crossrefs;
        templ.location = this.location;
        templ.sourceTerm = this.sourceTerm;
        templ.source = this.sourceTerm.getName();
        templ.typeTerm = this.typeTerm;
        templ.type = this.typeTerm.getName();
        return templ;
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
    public void setNoteSet(Set notes) throws ChangeVetoException { this.notes.setNoteSet(notes); }
    
    /**
     * {@inheritDoc}
     */
    public Set getLocationSet() { return this.location.getBlocks(); }
    
    /**
     * {@inheritDoc}
     */
    public void setLocationSet(Set locs) throws ChangeVetoException {
        this.location = new SimpleRichLocation(0,0,0);
        this.location.setBlocks(locs);
    }
    
    /**
     * {@inheritDoc}
     */
    public void setName(String name) throws ChangeVetoException {
        if(!this.hasListeners(RichFeature.NAME)) {
            this.name = name;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichFeature.NAME,
                    name,
                    null
                    );
            ChangeSupport cs = this.getChangeSupport(RichFeature.NAME);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.name = name;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName() { return this.name; }
    
    /**
     * {@inheritDoc}
     */
    public void setRank(int rank) throws ChangeVetoException {
        if(!this.hasListeners(RichFeature.RANK)) {
            this.rank = rank;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichFeature.RANK,
                    Integer.valueOf(rank),
                    Integer.valueOf(this.rank)
                    );
            ChangeSupport cs = this.getChangeSupport(RichFeature.RANK);
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
    public int getRank() { return this.rank; }
    
    /**
     * {@inheritDoc}
     */
    public Sequence getSequence() {
        FeatureHolder p = this.parent;
        while (p instanceof Feature) p = ((Feature)p).getParent();
        return (Sequence)p;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getSource() { return this.sourceTerm.getName(); }
    
    /**
     * {@inheritDoc}
     */
    public void setSource(String source) throws ChangeVetoException { throw new ChangeVetoException("Must use term"); }
    
    /**
     * {@inheritDoc}
     */
    public Term getSourceTerm() { return this.sourceTerm; }
    
    /**
     * {@inheritDoc}
     */
    public void setSourceTerm(Term t) throws ChangeVetoException, InvalidTermException {
        if (t==null) throw new IllegalArgumentException("Term cannot be null");
        if (!(t instanceof ComparableTerm)) throw new IllegalArgumentException("Term must be a ComparableTerm");
        if(!this.hasListeners(RichFeature.SOURCETERM)) {
            this.sourceTerm = (ComparableTerm)t;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichFeature.SOURCETERM,
                    t,
                    this.sourceTerm
                    );
            ChangeSupport cs = this.getChangeSupport(RichFeature.SOURCETERM);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.sourceTerm = (ComparableTerm)t;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String getType() { return this.typeTerm.getName(); }
    
    /**
     * {@inheritDoc}
     */
    public void setType(String type) throws ChangeVetoException { throw new ChangeVetoException("Must use term"); }
    
    /**
     * {@inheritDoc}
     */
    public Term getTypeTerm() { return this.typeTerm; }
    
    /**
     * {@inheritDoc}
     */
    public void setTypeTerm(Term t) throws ChangeVetoException, InvalidTermException {
        if (t==null) throw new IllegalArgumentException("Term cannot be null");
        if (!(t instanceof ComparableTerm)) throw new IllegalArgumentException("Term must be a ComparableTerm");
        if(!this.hasListeners(RichFeature.TYPETERM)) {
            this.typeTerm = (ComparableTerm)t;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichFeature.TYPETERM,
                    t,
                    this.typeTerm
                    );
            ChangeSupport cs = this.getChangeSupport(RichFeature.TYPETERM);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.typeTerm = (ComparableTerm)t;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public SymbolList getSymbols() { return this.location.symbols(this.getSequence()); }
    
    /**
     * {@inheritDoc}
     */
    public Location getLocation() { return this.location; }
    
    /**
     * {@inheritDoc}
     */
    public void setLocation(Location loc) throws ChangeVetoException {
        if (loc==null) throw new IllegalArgumentException("Location cannot be null");
        if (!(loc instanceof RichLocation)) throw new IllegalArgumentException("Location must be a RichLocation");
        if(!this.hasListeners(RichFeature.LOCATION)) {
            this.location = (RichLocation)loc;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichFeature.LOCATION,
                    loc,
                    this.location
                    );
            ChangeSupport cs = this.getChangeSupport(RichFeature.LOCATION);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.location = (RichLocation)loc;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public FeatureHolder getParent() { return this.parent; }
    
    /**
     * {@inheritDoc}
     */
    public void setParent(RichSequence parent) throws ChangeVetoException {
        if (parent==null) throw new IllegalArgumentException("Parent cannot be null");
        if(!this.hasListeners(RichFeature.PARENT)) {
            this.parent = (RichSequence)parent;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichFeature.PARENT,
                    parent,
                    this.parent
                    );
            ChangeSupport cs = this.getChangeSupport(RichFeature.PARENT);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.parent = (RichSequence)parent;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Set getRankedCrossRefs() { return Collections.unmodifiableSet(this.crossrefs); }
    
    /**
     * {@inheritDoc}
     */
    public void setRankedCrossRefs(Set crossrefs) throws ChangeVetoException {
        this.crossrefs.clear();
        if (crossrefs==null) return;
        for (Iterator i = crossrefs.iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (!(o instanceof RankedCrossRef)) throw new ChangeVetoException("Found a non-RankedCrossRef object");
            this.addRankedCrossRef((RankedCrossRef)o);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void addRankedCrossRef(RankedCrossRef crossref) throws ChangeVetoException {
        if (crossref==null) throw new IllegalArgumentException("Crossref cannot be null");
        if(!this.hasListeners(RichFeature.CROSSREF)) {
            this.crossrefs.add(crossref);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichFeature.CROSSREF,
                    crossref,
                    null
                    );
            ChangeSupport cs = this.getChangeSupport(RichFeature.CROSSREF);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.crossrefs.add(crossref);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeRankedCrossRef(RankedCrossRef crossref) throws ChangeVetoException {
        if (crossref==null) throw new IllegalArgumentException("Crossref cannot be null");
        if(!this.hasListeners(RichFeature.CROSSREF)) {
            this.crossrefs.remove(crossref);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichFeature.CROSSREF,
                    null,
                    crossref
                    );
            ChangeSupport cs = this.getChangeSupport(RichFeature.CROSSREF);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.crossrefs.remove(crossref);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Set getFeatureRelationshipSet() { return Collections.unmodifiableSet(this.relations); }
    
    /**
     * {@inheritDoc}
     */
    public void setFeatureRelationshipSet(Set relationships) throws ChangeVetoException {
        this.relations.clear();
        if (relationships==null) return;
        for (Iterator i = relationships.iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (!(o instanceof RichFeatureRelationship)) throw new ChangeVetoException("Found a non-RichFeatureRelationship object");
            this.addFeatureRelationship((RichFeatureRelationship)o);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void addFeatureRelationship(RichFeatureRelationship relationship) throws ChangeVetoException {
        if (relationship==null) throw new IllegalArgumentException("Relationship cannot be null");
        if(!this.hasListeners(RichFeature.RELATION)) {
            this.relations.add(relationship);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichFeature.RELATION,
                    relationship,
                    null
                    );
            ChangeSupport cs = this.getChangeSupport(RichFeature.RELATION);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.relations.add(relationship);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeFeatureRelationship(RichFeatureRelationship relationship) throws ChangeVetoException {
        if (relationship==null) throw new IllegalArgumentException("Relationship cannot be null");
        if(!this.hasListeners(RichFeature.RELATION)) {
            this.relations.remove(relationship);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichFeature.RELATION,
                    null,
                    relationship
                    );
            ChangeSupport cs = this.getChangeSupport(RichFeature.RELATION);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.relations.remove(relationship);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    private Set relationsToFeatureSet() {
        Set features = new HashSet();
        for (Iterator i = this.relations.iterator(); i.hasNext(); ) {
            RichFeatureRelationship r = (RichFeatureRelationship)i.next();
            features.add(r.getSubject());
        }
        return features;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean containsFeature(Feature f) { return this.relationsToFeatureSet().contains(f); }
    
    /**
     * {@inheritDoc}
     */
    public int countFeatures() { return this.relationsToFeatureSet().size(); }
    
    /**
     * {@inheritDoc}
     */
    public Feature createFeature(Feature.Template ft) throws BioException, ChangeVetoException {
        if (ft==null) throw new IllegalArgumentException("Template cannot be null");
        RichFeature f = new SimpleRichFeature(this.parent, ft);
        this.addFeatureRelationship(
                new SimpleRichFeatureRelationship(f, RichFeatureRelationship.DEFAULT_FEATURE_RELATIONSHIP_TERM, 0)
                );
        return f;
    }
    
    /**
     * {@inheritDoc}
     */
    public Iterator features() { return this.relationsToFeatureSet().iterator(); }
    
    /**
     * {@inheritDoc}
     */
    public FeatureHolder filter(FeatureFilter filter) {
        boolean recurse = !FilterUtils.areProperSubset(filter, FeatureFilter.top_level);
        return this.filter(filter, recurse);
    }
    
    /**
     * {@inheritDoc}
     */
    public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
        SimpleFeatureHolder fh = new SimpleFeatureHolder();
        for (Iterator i = this.features(); i.hasNext(); ) {
            Feature f = (RichFeature)i.next();
            try {
                if (fc.accept(f)) fh.addFeature(f);
            } catch (ChangeVetoException e) {
                throw new RuntimeException("Aaargh! Our feature was rejected!",e);
            }
        }
        return fh;
    }
    
    /**
     * {@inheritDoc}
     */
    public FeatureFilter getSchema() { return FeatureFilter.all; }
    
    /**
     * {@inheritDoc}
     */
    public void removeFeature(Feature f) throws ChangeVetoException, BioException {
        Set features = new HashSet();
        for (Iterator i = this.relations.iterator(); i.hasNext(); ) {
            RichFeatureRelationship r = (RichFeatureRelationship)i.next();
            if (r.getSubject().equals(f)) i.remove();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.parent==null) return code;
        // Normal comparison
        code = 31*code + this.parent.hashCode();
        code = 31*code + this.sourceTerm.hashCode();
        code = 31*code + this.typeTerm.hashCode();
        return code;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (! (o instanceof RichFeature)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.parent==null) return false;
        // Normal comparison
        RichFeature fo = (RichFeature) o;
        if (! this.parent.equals(fo.getParent())) return false;
        if (! this.typeTerm.equals(fo.getTypeTerm())) return false;
        return this.sourceTerm.equals(fo.getSourceTerm());
    }
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    // Hibernate requirement - not for public use.
    private Long getId() { return this.id; }
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) { this.id = id; }
}

