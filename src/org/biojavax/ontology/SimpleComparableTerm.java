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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.biojava.bio.Annotation;
import org.biojava.ontology.AbstractTerm;
import org.biojava.ontology.Ontology;
import org.biojava.ontology.Term;
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
public class SimpleComparableTerm extends AbstractTerm implements ComparableTerm {
    
    private String name;
    private String description;
    private Ontology ontology;
    private String identifier;
    private boolean obsolete;
    private Set synonyms = new HashSet();
    private Set rankedcrossrefs = new HashSet();
    
    /**
     * Creates a new instance of SimpleComparableTerm with synonyms.
     * @param ontology The ontology to put the term in.
     * @param name the name of the term.
     * @param description the description for the term.
     * @param synonyms a set of synonyms for the term.
     */
    public SimpleComparableTerm(ComparableOntology ontology, String name, String description, Object[] synonyms) {
        if (name == null) throw new NullPointerException("Name must not be null");
        if (description == null) throw new NullPointerException("Description must not be null");
        if (ontology == null) throw new NullPointerException("Ontology must not be null");
        
        this.name = name;
        this.description = description;
        this.ontology = ontology;
        this.identifier = null;
        this.obsolete = false;
        
        if (synonyms!=null) this.synonyms.addAll(Arrays.asList(synonyms));
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleComparableTerm() {}
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int value = 17;
        value = 37*value + this.getName().hashCode();
        value = 37*value + this.getOntology().hashCode();
        return value;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ComparableTerm)) return false;
        Term that = (Term) obj;
        return this.getOntology() == that.getOntology() &&
                this.getName() == that.getName();
    }
    
    /**
     * {@inheritDoc}
     */
    public int compareTo(Object o) {
        ComparableTerm them = (ComparableTerm)o;
        if (this.getOntology().equals(them.getOntology())) return ((ComparableOntology)this.getOntology()).compareTo(them.getOntology());
        return this.getName().compareTo(them.getName());
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
        this.synonyms.clear();
        for (Iterator i = synonyms.iterator(); i.hasNext(); ) {
            Object o = i.next();
            this.addSynonym(o);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Set getRankedCrossRefs() { return Collections.unmodifiableSet(this.rankedcrossrefs); }
    
    /**
     * {@inheritDoc}
     */
    public void setRankedCrossRefs(Set rankedcrossrefs) throws ChangeVetoException {
        this.rankedcrossrefs.clear();
        for (Iterator i = rankedcrossrefs.iterator(); i.hasNext(); ) {
            Object o = i.next();
            if (!(o instanceof RankedCrossRef)) throw new ChangeVetoException("Can only add RankedCrossRef objects as ranked crossrefs");
            this.addRankedCrossRef((RankedCrossRef)o);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void addRankedCrossRef(RankedCrossRef crossref) throws ChangeVetoException {
        if (crossref==null) throw new ChangeVetoException("Crossref cannot be null");
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
        if (crossref==null) throw new ChangeVetoException("Crossref cannot be null");
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
        if (description==null) throw new ChangeVetoException("Description cannot be null");
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
    public boolean getObsolete() { return this.obsolete; }
    
    /**
     * {@inheritDoc}
     */
    public void setObsolete(boolean obsolete) throws ChangeVetoException {
        if(!this.hasListeners(ComparableTerm.OBSOLETE)) {
            this.obsolete = obsolete;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    ComparableTerm.OBSOLETE,
                    Boolean.valueOf(obsolete),
                    Boolean.valueOf(this.obsolete)
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