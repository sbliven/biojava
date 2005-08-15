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
 * SimpleComparableTerm.java
 *
 * Created on July 13, 2005, 10:22 AM
 */

package org.biojavax.ontology;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.biojava.bio.Annotation;
import org.biojava.ontology.Ontology;
import org.biojava.ontology.Term;
import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.RankedCrossRef;



/**
 * A Term object that can be compared and thus sorted.
 *
 * Equality is inherited from Term.Impl.
 *
 * @author Richard Holland
 */
public class SimpleComparableTerm extends AbstractChangeable implements ComparableTerm {
    
    private String name;
    private String description;
    private ComparableOntology ontology;
    private String identifier;
    private Boolean obsolete;
    private Set synonyms = new TreeSet();
    private Set rankedcrossrefs = new TreeSet();
    
    /**
     * Creates a new instance of SimpleComparableTerm with synonyms.
     * @param ontology The ontology to put the term in.
     * @param name the name of the term.
     * @param synonyms a set of synonyms for the term.
     */
    public SimpleComparableTerm(ComparableOntology ontology, String name, Object[] synonyms) {
        if (name == null) throw new IllegalArgumentException("Name must not be null");
        if (ontology == null) throw new IllegalArgumentException("Ontology must not be null");
        
        this.name = name;
        this.description = null;
        this.ontology = ontology;
        this.identifier = null;
        
        if (synonyms!=null) this.synonyms.addAll(Arrays.asList(synonyms));
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleComparableTerm() {}
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int value = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.ontology==null) return value;
        // Normal comparison
        value = 37*value + this.name.hashCode();
        value = 37*value + this.ontology.hashCode();
        return value;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ComparableTerm)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.ontology==null) return false;
        // Normal comparison
        Term that = (Term) obj;
        return this.ontology.equals(that.getOntology()) &&
                this.name.equals(that.getName());
    }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        // Hibernate comparison - we haven't been populated yet
        if (this.ontology==null) return -1;
        // Normal comparison
        ComparableTerm them = (ComparableTerm)o;
        if (!this.ontology.equals(them.getOntology())) return this.ontology.compareTo(them.getOntology());
        return this.name.compareTo(them.getName());
    }
    
    /**
     * {@inheritDoc}
     */
    public void addSynonym(Object synonym) { this.synonyms.add(synonym); }
    
    /**
     * {@inheritDoc}
     */
    public void removeSynonym(Object synonym) { this.synonyms.remove(synonym); }
    
    public Object[] getSynonyms() { return this.synonyms.toArray(); }
    
    // Hibernate requirement - not for public use.
    private Set getSynonymSet() { return this.synonyms; }
    
    // Hibernate requirement - not for public use.
    private void setSynonymSet(Set synonyms) {
        Set newsyns = new TreeSet();
        if (synonyms!=null) for (Iterator i = synonyms.iterator(); i.hasNext(); ) {
            Object o = i.next();
            newsyns.add(o);
        }
        this.synonyms.clear();
        for (Iterator i = newsyns.iterator(); i.hasNext(); ) this.addSynonym(i.next());
    }
    
    /**
     * {@inheritDoc}
     */
    public Set getRankedCrossRefs() { return Collections.unmodifiableSet(this.rankedcrossrefs); }
    
    /**
     * {@inheritDoc}
     */
    public void setRankedCrossRefs(Set rankedcrossrefs) throws ChangeVetoException {
        Set newrc = new TreeSet();
        if (rankedcrossrefs!=null) for (Iterator i = rankedcrossrefs.iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (!(o instanceof RankedCrossRef)) throw new ChangeVetoException("Can only add RankedCrossRef objects as ranked crossrefs");
            newrc.add(o);
        }
        this.rankedcrossrefs.clear();
        for (Iterator i = newrc.iterator(); i.hasNext(); ) this.addRankedCrossRef((RankedCrossRef)i.next());
    }
    
    /**
     * {@inheritDoc}
     */
    public void addRankedCrossRef(RankedCrossRef crossref) throws ChangeVetoException {
        if (crossref==null) throw new IllegalArgumentException("Crossref cannot be null");
        if(!this.hasListeners(ComparableTerm.RANKEDCROSSREF)) {
            this.rankedcrossrefs.add(crossref);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    ComparableTerm.RANKEDCROSSREF,
                    crossref,
                    null
                    );
            ChangeSupport cs = this.getChangeSupport(ComparableTerm.RANKEDCROSSREF);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.rankedcrossrefs.add(crossref);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeRankedCrossRef(RankedCrossRef crossref) throws ChangeVetoException {
        if (crossref==null) throw new IllegalArgumentException("Crossref cannot be null");
        if(!this.hasListeners(ComparableTerm.RANKEDCROSSREF)) {
            this.rankedcrossrefs.remove(crossref);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    ComparableTerm.RANKEDCROSSREF,
                    null,
                    crossref
                    );
            ChangeSupport cs = this.getChangeSupport(ComparableTerm.RANKEDCROSSREF);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.rankedcrossrefs.remove(crossref);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName() { return this.name; }
    
    // Hibernate requirement - not for public use.
    private void setName(String name) { this.name = name; }
    
    /**
     * {@inheritDoc}
     */
    public String getDescription() { return this.description; }
    
    /**
     * {@inheritDoc}
     */
    public void setDescription(String description) throws ChangeVetoException {
        if(!this.hasListeners(ComparableTerm.DESCRIPTION)) {
            this.description = description;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    ComparableTerm.DESCRIPTION,
                    description,
                    this.description
                    );
            ChangeSupport cs = this.getChangeSupport(ComparableTerm.DESCRIPTION);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.description = description;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Ontology getOntology() { return this.ontology; }
    
    // Hibernate requirement - not for public use.
    private void setOntology(ComparableOntology ontology) { this.ontology = ontology; }
    
    /**
     * {@inheritDoc}
     */
    public String toString() { return this.name; }
    
    /**
     * {@inheritDoc}
     * ALWAYS RETURNS AN EMPTY ANNOTATION OBJECT
     */
    public Annotation getAnnotation() { return Annotation.EMPTY_ANNOTATION; }
    
    /**
     * {@inheritDoc}
     */
    public String getIdentifier() { return this.identifier; }
    
    /**
     * {@inheritDoc}
     */
    public void setIdentifier(String identifier) throws ChangeVetoException {
        if(!this.hasListeners(ComparableTerm.IDENTIFIER)) {
            this.identifier = identifier;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    ComparableTerm.IDENTIFIER,
                    identifier,
                    this.identifier
                    );
            ChangeSupport cs = this.getChangeSupport(ComparableTerm.IDENTIFIER);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.identifier = identifier;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Boolean getObsolete() { return this.obsolete; }
    
    /**
     * {@inheritDoc}
     */
    public void setObsolete(Boolean obsolete) throws ChangeVetoException {
        if(!this.hasListeners(ComparableTerm.OBSOLETE)) {
            this.obsolete = obsolete;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    ComparableTerm.OBSOLETE,
                    obsolete,
                    this.obsolete
                    );
            ChangeSupport cs = this.getChangeSupport(ComparableTerm.OBSOLETE);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.obsolete = obsolete;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    // Hibernate requirement - not for public use.
    private Long id;
    
    // Hibernate requirement - not for public use.
    private Long getId() { return this.id; }
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) { this.id = id; }
}