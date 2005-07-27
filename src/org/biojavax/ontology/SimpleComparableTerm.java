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
import java.util.HashSet;
import java.util.Set;
import org.biojava.bio.Annotation;
import org.biojava.ontology.AbstractTerm;
import org.biojava.ontology.Ontology;
import org.biojava.ontology.Term;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.bio.BioEntryAnnotation;
import org.biojavax.bio.SimpleBioEntryAnnotation;

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
    private Annotation annotation;
    private Set synonyms = new HashSet();
    
    private String identifier;
    
    private boolean obsolete;
    
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
        
        if (synonyms!=null) this.synonyms.addAll(Arrays.asList(synonyms));
        
        this.identifier = null;
        this.obsolete = false;
    }
    
    // Hibernate requirement - not for public use.
    private SimpleComparableTerm() {}
    
    public int hashCode() {
        int value = 17;
        if(getName() != null)
            value *= 31 * getName().hashCode();
        return 17 * value;
    }
    
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof Term)) return false;
        
        Term that = (Term) obj;
        
        return this.getOntology() == that.getOntology() &&
                this.getName() == that.getName();
    }
    
    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * @return a negative integer, zero, or a positive integer as this object
     * 		is less than, equal to, or greater than the specified object.
     * @param o the Object to be compared.
     */
    public int compareTo(Object o) {
        ComparableTerm them = (ComparableTerm)o;
        if (this.getOntology().equals(them.getOntology())) return ((ComparableOntology)this.getOntology()).compareTo(them.getOntology());
        return this.getName().compareTo(them.getName());
    }
    
    public void addSynonym(Object synonym) {
        this.synonyms.add(synonym);
    }
    
    public void removeSynonym(Object synonym) {
        this.synonyms.remove(synonym);
    }
    
    public Object[] getSynonyms() {
        return this.synonyms.toArray();
    }
    
    // Hibernate requirement - not for public use.
    private Set getSynonymSet() { return this.synonyms; }
    
    // Hibernate requirement - not for public use.
    private void setSynonymSet(Set synonyms) { this.synonyms = synonyms; }
    
    public String getName() {
        return this.name;
    }
    
    // Hibernate requirement - not for public use.
    private void setName(String name) { this.name = name; }
    
    public String getDescription() {
        return this.description;
    }
    
    // Hibernate requirement - not for public use.
    private void setDescription(String description) { this.description = description; }
    
    public Ontology getOntology() {
        return this.ontology;
    }
    
    // Hibernate requirement - not for public use.
    private void setOntology(ComparableOntology ontology) { this.ontology = ontology; }
    
    public String toString() {
        return this.name;
    }
    
    public Annotation getAnnotation() {
        if (this.annotation == null) this.annotation = new SimpleBioEntryAnnotation();
        return this.annotation;
    }
    
    // Hibernate requirement - not for public use.
    private void setAnnotation(BioEntryAnnotation annotation) { this.annotation = annotation; }
    
    public String getIdentifier() {
        return this.identifier;
    }
    
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
    
    public boolean getObsolete() {
        return this.obsolete;
    }
    
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
    private Long getId() {
        
        return this.id;
    }
    
    
    // Hibernate requirement - not for public use.
    private void setId(Long id) {
        
        this.id = id;
    }
}