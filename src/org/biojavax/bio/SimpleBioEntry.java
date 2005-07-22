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
 
 * SimpleBioEntry.java
 
 *
 
 * Created on June 16, 2005, 10:29 AM
 
 */



package org.biojavax.bio;

import java.util.Collections;
import java.util.HashSet;

import java.util.Iterator;

import java.util.List;
import java.util.Set;

import java.util.Vector;

import org.biojava.bio.Annotatable;

import org.biojava.bio.Annotation;

import org.biojava.bio.BioException;

import org.biojava.bio.SimpleAnnotation;

import org.biojava.bio.seq.Feature;

import org.biojava.bio.seq.FeatureFilter;

import org.biojava.bio.seq.FeatureHolder;

import org.biojava.bio.seq.SimpleFeatureHolder;

import org.biojava.bio.seq.StrandedFeature;

import org.biojava.bio.symbol.Alphabet;

import org.biojava.bio.symbol.Edit;

import org.biojava.bio.symbol.IllegalAlphabetException;

import org.biojava.bio.symbol.Symbol;

import org.biojava.bio.symbol.SymbolList;

import org.biojava.ontology.AlreadyExistsException;

import org.biojava.utils.AbstractChangeable;

import org.biojava.utils.ChangeEvent;

import org.biojava.utils.ChangeForwarder;

import org.biojava.utils.ChangeSupport;

import org.biojava.utils.ChangeVetoException;

import org.biojavax.CrossRef;

import org.biojavax.Namespace;

import org.biojavax.bio.taxa.NCBITaxon;

import org.biojavax.bio.db.Persistent;

import org.biojavax.ontology.ComparableTerm;

import org.biojavax.LocatedDocumentReference;



/**
 *
 * Reference implementation of a BioEntry object which has no features or sequence. *
 *
 *
 *
 * Equality is the combination of namespace, name, accession and version.
 *
 *
 *
 * @author Richard Holland
 *
 * @author Mark Schreiber
 *
 */

public class SimpleBioEntry extends AbstractChangeable implements BioEntry {
    
    
    
    /**
     *
     * The comments for this entry.
     *
     */
    
    private Vector comments;
    
    /**
     *
     * The crossrefs for this entry.
     *
     */
    
    private Vector crossrefs;
    
    /**
     *
     * The docrefs for this entry.
     *
     */
    
    private Vector docrefs;
    
    /**
     *
     * The relationships for this entry.
     *
     */
    
    private Set relationships;
    
    /**
     *
     * The relationships for this entry.
     *
     */
    
    private String description;
    
    /**
     *
     * The division for this entry.
     *
     */
    
    private String division;
    
    /**
     *
     * The identifier for this entry.
     *
     */
    
    private String identifier;
    
    /**
     *
     * The name for this entry.
     *
     */
    
    private String name;
    
    /**
     *
     * The accession for this entry.
     *
     */
    
    private String accession;
    
    /**
     *
     * The version for this entry.
     *
     */
    
    private int version;
    
    /**
     *
     * The taxon for this entry.
     *
     */
    
    private NCBITaxon taxon;
    
    /**
     *
     * The namespace for this entry.
     *
     */
    
    private Namespace ns;
    
    /**
     *
     * The annotation for this entry.
     *
     */
    
    private Annotation ann;
    
    /**
     *
     * The event forwarder for this entry.
     *
     */
    
    private ChangeForwarder annFor;
    
    /**
     *
     * The sequences for this feature.
     *
     */
    
    private SymbolList symList;
    
    /**
     *
     * The features for this entry.
     *
     */
    
    private FeatureHolder features;
    
    /**
     *
     * Change forwarder for the features.
     *
     */
    
    private ChangeForwarder featForF;
    
    /**
     *
     * Change forwarder for the feature schemas.
     *
     */
    
    private ChangeForwarder featForS;
    
    /**
     *
     * The sequence version for this feature.
     *
     */
    
    private double symListVersion;
    
    
    
    /**
     *
     * Creates a new feature holding bioentry.
     *
     * @param ns The namespace for this new bioentry.
     *
     * @param name The name for this new bioentry.
     *
     * @param accession The accession for this new bioentry.
     *
     * @param version The version for this new bioentry.
     *
     * @param symList The symbol list for this new bioentry. If null, SymbolList.EMPTY_LIST is used instead.
     *
     * @param seqversion The sequence version for this new bioentry.
     *
     */
    
    public SimpleBioEntry(Namespace ns, String name, String accession, int version, SymbolList symList, double seqversion) {
        
        if (name==null) throw new IllegalArgumentException("Name cannot be null");
        
        if (accession==null) throw new IllegalArgumentException("Accession cannot be null");
        
        if (ns==null) throw new IllegalArgumentException("Namespace cannot be null");
        
        if (version==Persistent.NULL_INTEGER) throw new IllegalArgumentException("Version cannot be null");
        
        if (symList==null) symList = SymbolList.EMPTY_LIST;
        
        this.comments = new Vector();
        
        this.crossrefs = new Vector();
        
        this.relationships = new HashSet();
        
        this.description = null;
        
        this.division = null;
        
        this.identifier = null;
        
        this.name = name;
        
        this.accession = accession;
        
        this.version = version;
        
        this.taxon = null;
        
        this.ns = ns;
        
        this.symList = symList;
        
        this.symListVersion = seqversion;
        
        // make the ann delegate
        
        this.ann = new SimpleAnnotation() {
            
            public void setProperty(Object key, Object value) throws ChangeVetoException {
                
                if (!(key instanceof ComparableTerm)) throw new ChangeVetoException("Can only annotate using ComparableTerm objects as keys");
                
                if (!(value instanceof String)) throw new ChangeVetoException("Can only annotate using single String objects as values");
                
                super.setProperty(key, value);
                
            }
            
        };
        
        // construct the forwarder so that it emits Annotatable.ANNOTATION ChangeEvents
        
        // for the Annotation.PROPERTY events it will listen for
        
        this.annFor = new ChangeForwarder.Retyper(this, super.getChangeSupport(Annotatable.ANNOTATION), Annotatable.ANNOTATION);
        
        // connect the forwarder so it listens for Annotation.PROPERTY events
        
        this.ann.addChangeListener(this.annFor, Annotation.PROPERTY);
        
        // set up features
        
        final BioEntry yomama = this;
        
        this.features = new SimpleFeatureHolder() {
            
            public Feature createFeature(Feature.Template f) throws ChangeVetoException {
                
                StrandedFeature.Template sft;
                
                if (f instanceof StrandedFeature.Template) {
                    
                    sft = (StrandedFeature.Template)f;
                    
                } else {
                    
                    sft = new StrandedFeature.Template();
                    
                    sft.annotation = f.annotation;
                    
                    sft.location = f.location;
                    
                    sft.source = f.source;
                    
                    sft.sourceTerm = f.sourceTerm;
                    
                    sft.type = f.type;
                    
                    sft.typeTerm = f.typeTerm;
                    
                    sft.strand = StrandedFeature.UNKNOWN;
                    
                }
                
                BioEntryFeature bef = new SimpleBioEntryFeature(yomama,this,sft);
                
                // make the feature a singleton
                
                if (this.containsFeature(bef)) for (Iterator i = this.features(); i.hasNext(); ) {
                    
                    BioEntryFeature bef2 = (BioEntryFeature)i.next();
                    
                    if (bef.equals(bef2)) return bef2;
                    
                }
                
                // or create a new feature
                
                this.addFeature(bef);
                
                return bef;
                
            }
            
            public void addFeature(Feature f) throws ChangeVetoException {
                
                if (!(f instanceof BioEntryFeature)) throw new ChangeVetoException("Can only add BioEntryFeature objects as features");
                
                super.addFeature(f);
                
            }
            
        };
        
        // construct the forwarder so that it emits FeatureHolder ChangeEvents
        
        // for the FeatureHolder events it will listen for
        
        this.featForF = new ChangeForwarder.Retyper(this, super.getChangeSupport(FeatureHolder.FEATURES), FeatureHolder.FEATURES);
        
        this.featForS = new ChangeForwarder.Retyper(this, super.getChangeSupport(FeatureHolder.SCHEMA), FeatureHolder.SCHEMA);
        
        // connect the forwarder so it listens for FeatureHolder events
        
        this.features.addChangeListener(this.featForF, FeatureHolder.FEATURES);
        
        this.features.addChangeListener(this.featForS, FeatureHolder.SCHEMA);
        
    }
    
    
    
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
    
    
    
    /**
     *
     * Searches for a crossref in the list of all crossrefs, and removes it if it was
     *
     * found.
     *
     * @return True if the crossref was found, false if the crossref was not found.
     *
     * @param crossref the crossref to search for and remove.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public boolean removeCrossRef(CrossRef crossref) throws ChangeVetoException {
        
        int index = this.crossrefs.indexOf(crossref);
        
        if (index>=0) {
            
            if(!this.hasListeners(BioEntry.CROSSREF)) {
                
                this.crossrefs.set(index,null);
                
            } else {
                
                ChangeEvent ce = new ChangeEvent(
                        
                        this,
                        
                        BioEntry.CROSSREF,
                        
                        null,
                        
                        crossref
                        
                        );
                
                ChangeSupport cs = this.getChangeSupport(BioEntry.CROSSREF);
                
                synchronized(cs) {
                    
                    cs.firePreChangeEvent(ce);
                    
                    this.crossrefs.set(index,null);
                    
                    cs.firePostChangeEvent(ce);
                    
                }
                
            }
            
            return true;
            
        } else {
            
            return false;
            
        }
        
    }
    
    
    
    /**
     *
     *
     *
     * Tests for the existence of a crossref in the list.
     *
     * @param crossref the crossref to look for.
     *
     * @return True if the crossref is in the list, false if not.
     *
     */
    
    public boolean containsCrossRef(CrossRef crossref) {
        
        return this.crossrefs.contains(crossref);
        
    }
    
    
    
    /**
     *
     * Adds the crossref to the end of the list of crossrefs, giving it the index of
     *
     * max(all other crossref index positions)+1.
     *
     * @return The position the crossref was added at.
     *
     * @param crossref New crossref to add.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     * @throws AlreadyExistsException if the crossref already exists at another index.
     *
     */
    
    public int addCrossRef(CrossRef crossref) throws AlreadyExistsException,ChangeVetoException {
        
        if (this.crossrefs.contains(crossref)) throw new AlreadyExistsException("Cross reference has already been made");
        
        int index = this.crossrefs.size();
        
        if(!this.hasListeners(BioEntry.CROSSREF)) {
            
            this.crossrefs.ensureCapacity(index+1);
            
            this.crossrefs.add(crossref);
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    BioEntry.CROSSREF,
                    
                    crossref,
                    
                    this.crossrefs.contains(crossref)?crossref:null
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(BioEntry.CROSSREF);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.crossrefs.ensureCapacity(index+1);
                
                this.crossrefs.add(crossref);
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
        return index;
        
    }
    
    
    /**
     *
     * Setter for property taxon.
     *
     * @param taxon New value of property taxon.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public void setTaxon(NCBITaxon taxon) throws ChangeVetoException {
        
        if(!this.hasListeners(BioEntry.TAXON)) {
            
            this.taxon = taxon;
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    BioEntry.TAXON,
                    
                    taxon,
                    
                    this.taxon
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(BioEntry.TAXON);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.taxon = taxon;
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
    }
    
    
    
    /**
     *
     * Overwrites the list of comments at the given index position with the comment
     *
     * supplied. It will overwrite anything already at that position.
     *
     * @param comment New comment to write at that position.
     *
     * @param index Position to write comment at.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     * @throws AlreadyExistsException if the comment already exists at another index.
     *
     */
    
    public void setComment(String comment, int index) throws AlreadyExistsException,ChangeVetoException {
        
        if (this.comments.contains(comment)) throw new AlreadyExistsException("Comment has already been made");
        
        if(!this.hasListeners(BioEntry.COMMENTS)) {
            
            this.comments.ensureCapacity(index+1);
            
            this.comments.set(index,comment);
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    BioEntry.COMMENTS,
                    
                    comment,
                    
                    this.comments.get(index)
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(BioEntry.COMMENTS);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.comments.ensureCapacity(index+1);
                
                this.comments.set(index,comment);
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
    }
    
    
    
    /**
     *
     * Setter for property identifier.
     *
     * @param identifier New value of property identifier.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public void setIdentifier(String identifier) throws ChangeVetoException {
        
        if(!this.hasListeners(BioEntry.IDENTIFIER)) {
            
            this.identifier = identifier;
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    BioEntry.IDENTIFIER,
                    
                    identifier,
                    
                    this.identifier
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(BioEntry.IDENTIFIER);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.identifier = identifier;
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
    }
    
    
    
    /**
     *
     * Setter for property division.
     *
     * @param division New value of property division.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public void setDivision(String division) throws ChangeVetoException {
        
        if(!this.hasListeners(BioEntry.DIVISION)) {
            
            this.division = division;
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    BioEntry.DIVISION,
                    
                    division,
                    
                    this.division
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(BioEntry.DIVISION);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.division = division;
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
    }
    
    
    
    /**
     *
     * Setter for property description.
     *
     * @param description New value of property description.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public void setDescription(String description) throws ChangeVetoException {
        
        if(!this.hasListeners(BioEntry.DESCRIPTION)) {
            
            this.description = description;
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    BioEntry.DESCRIPTION,
                    
                    description,
                    
                    this.description
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(BioEntry.DESCRIPTION);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.description = description;
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
    }
    
    
    
    /**
     *
     * Removes the crossref at a given index. If the index position already had no
     *
     * crossref associated, it returns false. Else, it returns true.
     *
     * @return True if a crossref was found at that position and removed.
     *
     * @param index the index position to remove the crossref from.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     * @throws IndexOutOfBoundsException if the index position was invalid.
     *
     */
    
    public boolean removeCrossRef(int index) throws IndexOutOfBoundsException,ChangeVetoException {
        
        if (this.crossrefs.get(index)==null) return false;
        
        else {
            
            if(!this.hasListeners(BioEntry.CROSSREF)) {
                
                this.crossrefs.set(index,null);
                
            } else {
                
                ChangeEvent ce = new ChangeEvent(
                        
                        this,
                        
                        BioEntry.CROSSREF,
                        
                        null,
                        
                        this.crossrefs.get(index)
                        
                        );
                
                ChangeSupport cs = this.getChangeSupport(BioEntry.CROSSREF);
                
                synchronized(cs) {
                    
                    cs.firePreChangeEvent(ce);
                    
                    this.crossrefs.set(index,null);
                    
                    cs.firePostChangeEvent(ce);
                    
                }
                
            }
            
            return true;
            
        }
        
    }
    
    
    
    /**
     *
     * Removes the comment at a given index. If the index position already had no
     *
     * comment associated, it returns false. Else, it returns true.
     *
     * @return True if a comment was found at that position and removed.
     *
     * @param index the index position to remove the comment from.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     * @throws IndexOutOfBoundsException if the index position was invalid.
     *
     */
    
    public boolean removeComment(int index) throws IndexOutOfBoundsException,ChangeVetoException {
        
        if (this.comments.get(index)==null) return false;
        
        else {
            
            if(!this.hasListeners(BioEntry.COMMENTS)) {
                
                this.comments.set(index,null);
                
            } else {
                
                ChangeEvent ce = new ChangeEvent(
                        
                        this,
                        
                        BioEntry.COMMENTS,
                        
                        null,
                        
                        this.comments.get(index)
                        
                        );
                
                ChangeSupport cs = this.getChangeSupport(BioEntry.COMMENTS);
                
                synchronized(cs) {
                    
                    cs.firePreChangeEvent(ce);
                    
                    this.comments.set(index,null);
                    
                    cs.firePostChangeEvent(ce);
                    
                }
                
            }
            
            return true;
            
        }
        
    }
    
    
    /**
     *
     * Returns the comment at a given index. If the index is valid but no comment is
     *
     * found at that position, it will return null. If the index is invalid,
     *
     * an exception will be thrown.
     *
     * @param index the index of the comment to retrieve.
     *
     * @return The comment at that index position.
     *
     * @throws IndexOutOfBoundsException if an invalid index is specified.
     *
     */
    
    public String getComment(int index) throws IndexOutOfBoundsException {
        
        return (String)this.comments.get(index);
        
    }
    
    
    
    /**
     *
     * Returns the crossref at a given index. If the index is valid but no crossref is
     *
     * found at that position, it will return null. If the index is invalid,
     *
     * an exception will be thrown.
     *
     * @param index the index of the crossref to retrieve.
     *
     * @return The crossref at that index position.
     *
     * @throws IndexOutOfBoundsException if an invalid index is specified.
     *
     */
    
    public CrossRef getCrossRef(int index) throws IndexOutOfBoundsException {
        
        return (CrossRef)this.crossrefs.get(index);
        
    }
    
    
    
    /**
     *
     * Searches for a comment in the list of all comments, and removes it if it was
     *
     * found.
     *
     * @return True if the comment was found, false if the comment was not found.
     *
     * @param comment the comment to search for and remove.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public boolean removeComment(String comment) throws ChangeVetoException {
        
        int index = this.comments.indexOf(comment);
        
        if (index>=0) {
            
            if(!this.hasListeners(BioEntry.COMMENTS)) {
                
                this.comments.set(index,null);
                
            } else {
                
                ChangeEvent ce = new ChangeEvent(
                        
                        this,
                        
                        BioEntry.COMMENTS,
                        
                        null,
                        
                        comment
                        
                        );
                
                ChangeSupport cs = this.getChangeSupport(BioEntry.COMMENTS);
                
                synchronized(cs) {
                    
                    cs.firePreChangeEvent(ce);
                    
                    this.comments.set(index,null);
                    
                    cs.firePostChangeEvent(ce);
                    
                }
                
            }
            
            return true;
            
        } else {
            
            return false;
            
        }
        
    }
    
    
    
    /**
     *
     *
     *
     * Tests for the existence of a comment in the list.
     *
     * @param comment the comment to look for.
     *
     * @return True if the comment is in the list, false if not.
     *
     */
    
    public boolean containsComment(String comment) {
        
        return this.comments.contains(comment);
        
    }
    
    
    
    /**
     *
     * Adds the comment to the end of the list of comments, giving it the index of
     *
     * max(all other comment index positions)+1.
     *
     * @return The position the comment was added at.
     *
     * @param comment New comment to add.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     * @throws AlreadyExistsException if the comment already exists at another index.
     *
     */
    
    public int addComment(String comment) throws AlreadyExistsException,ChangeVetoException {
        
        if (comment==null) throw new ChangeVetoException("Comment cannot be null");
        
        if (this.comments.contains(comment)) throw new AlreadyExistsException("Comment has already been added");
        
        int index = this.comments.size();
        
        if(!this.hasListeners(BioEntry.COMMENTS)) {
            
            this.comments.ensureCapacity(index+1);
            
            this.comments.add(index,comment);
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    BioEntry.COMMENTS,
                    
                    comment,
                    
                    null
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(BioEntry.COMMENTS);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.comments.ensureCapacity(index+1);
                
                this.comments.add(index,comment);
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
        return index;
        
    }
    
    
    
    /**
     *
     * Overwrites the list of crossrefs at the given index position with the crossref
     *
     * supplied. It will overwrite anything already at that position.
     *
     * @param crossref New crossref to write at that position.
     *
     * @param index Position to write crossref at.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     * @throws AlreadyExistsException if the crossref already exists at another index.
     *
     */
    
    public void setCrossRef(CrossRef crossref, int index) throws AlreadyExistsException,ChangeVetoException {
        
        if (this.crossrefs.contains(crossref)) throw new AlreadyExistsException("Cross-reference has already been added");
        
        if(!this.hasListeners(BioEntry.CROSSREF)) {
            
            this.crossrefs.ensureCapacity(index+1);
            
            this.crossrefs.set(index,crossref);
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    BioEntry.CROSSREF,
                    
                    crossref,
                    
                    this.crossrefs.get(index)
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(BioEntry.CROSSREF);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.crossrefs.ensureCapacity(index+1);
                
                this.crossrefs.set(index,crossref);
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
    }
    
    
    
    /**
     *
     * Searches for a relationship in the list of all relationships, and removes it if it was
     *
     * found.
     *
     * @return True if the relationship was found, false if the relationship was not found.
     *
     * @param relationship the relationship to search for and remove.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public boolean removeBioEntryRelationship(BioEntryRelationship relationship) throws ChangeVetoException {
        
        boolean result;
        
        if(!this.hasListeners(BioEntry.RELATIONSHIP)) {
            
            result = this.relationships.remove(relationship);
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    BioEntry.RELATIONSHIP,
                    
                    null,
                    
                    relationship
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(BioEntry.RELATIONSHIP);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                result = this.relationships.remove(relationship);
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
        return result;
    }
    
    
    
    /**
     *
     *
     *
     * Tests for the existence of a relationship in the list.
     *
     * @param relationship the relationship to look for.
     *
     * @return True if the relationship is in the list, false if not.
     *
     */
    
    public boolean containsBioEntryRelationship(BioEntryRelationship relationship) {
        
        return this.relationships.contains(relationship);
        
    }
    
    
    
    /**
     *
     * Adds the relationship to the end of the list of relationships, giving it the index of
     *
     * max(all other relationship index positions)+1.
     *
     * @return The position the relationship was added at.
     *
     * @param relationship New relationship to add.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     * @throws AlreadyExistsException if the relationship already exists at another index.
     *
     */
    
    public void addBioEntryRelationship(BioEntryRelationship relationship) throws AlreadyExistsException,ChangeVetoException {
        
        if (this.relationships.contains(relationship)) throw new AlreadyExistsException("Relationship has already been added");
        
        if(!this.hasListeners(BioEntry.RELATIONSHIP)) {
            
            this.relationships.add(relationship);
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    BioEntry.RELATIONSHIP,
                    
                    relationship,
                    
                    null
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(BioEntry.RELATIONSHIP);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.relationships.add(relationship);
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
    }
    
    
    
    /**
     *
     * Returns a list of all relationships associated with this bioentry. This
     *
     * list is not mutable. If no relationships are associated, you will get back an
     *
     * empty list. If the relationships have indexes that are not consecutive, then the
     *
     * list will contain nulls at the indexes corresponding to the gaps between
     *
     * the extant relationships. eg. If there are only two relationships A and B at positions 10
     *
     * and 20 respectively, then the List returned will be of size 20, with nulls
     *
     * at index positions 0-9 and 11-19.
     *
     * @return Value of property relationships.
     *
     */
    
    public Set getBioEntryRelationships() {
        
        return Collections.unmodifiableSet(this.relationships);
        
    }
    
    
    
    /**
     *
     * Getter for property accession.
     *
     * @return Value of property accession.
     *
     */
    
    public String getAccession() {
        
        return this.accession;
        
    }
    
    
    
    /**
     *
     * Returns a list of all comments associated with this bioentry. This
     *
     * list is not mutable. If no comments are associated, you will get back an
     *
     * empty list. If the comments have indexes that are not consecutive, then the
     *
     * list will contain nulls at the indexes corresponding to the gaps between
     *
     * the extant comments. eg. If there are only two comments A and B at positions 10
     *
     * and 20 respectively, then the List returned will be of size 20, with nulls
     *
     * at index positions 0-9 and 11-19.
     *
     * @return Value of property comments.
     *
     */
    
    public List getComments() {
        
        return Collections.unmodifiableList(this.comments);
        
    }
    
    
    
    /**
     *
     * Returns a list of all crossrefs associated with this bioentry. This
     *
     * list is not mutable. If no crossrefs are associated, you will get back an
     *
     * empty list. If the crossrefs have indexes that are not consecutive, then the
     *
     * list will contain nulls at the indexes corresponding to the gaps between
     *
     * the extant crossrefs. eg. If there are only two crossrefs A and B at positions 10
     *
     * and 20 respectively, then the List returned will be of size 20, with nulls
     *
     * at index positions 0-9 and 11-19.
     *
     * @return Value of property crossrefs.
     *
     */
    
    public List getCrossRefs() {
        
        return Collections.unmodifiableList(this.crossrefs);
        
    }
    
    
    
    /**
     *
     * Getter for property description.
     *
     * @return Value of property description.
     *
     */
    
    public String getDescription() {
        
        return this.description;
        
    }
    
    
    
    /**
     *
     * Getter for property division.
     *
     * @return Value of property division.
     *
     */
    
    public String getDivision() {
        
        return this.division;
        
    }
    
    
    
    /**
     *
     * Getter for property identifier.
     *
     * @return Value of property identifier.
     *
     */
    
    public String getIdentifier() {
        
        return this.identifier;
        
    }
    
    
    
    /**
     *
     * Getter for property name.
     *
     * @return Value of property name.
     *
     */
    
    public String getName() {
        
        return this.name;
        
    }
    
    
    
    /**
     *
     * Getter for property namespace.
     *
     * @return Value of property namespace.
     *
     */
    
    public Namespace getNamespace() {
        
        return this.ns;
        
    }
    
    
    
    /**
     *
     * Getter for property taxon.
     *
     * @return Value of property taxon.
     *
     */
    
    public NCBITaxon getTaxon() {
        
        return this.taxon;
        
    }
    
    
    
    /**
     *
     * Getter for property version.
     *
     * @return Value of property version.
     *
     */
    
    public int getVersion() {
        
        return this.version;
        
    }
    
    
    
    /**
     *
     * Indicates whether some other object is "equal to" this one. Equality is
     *
     * the combination of namespace, name, accession and version.
     *
     * @param   obj   the reference object with which to compare.
     *
     * @return  <code>true</code> if this object is the same as the obj
     *
     *          argument; <code>false</code> otherwise.
     *
     * @see     #hashCode()
     *
     * @see     java.util.Hashtable
     *
     */
    
    public boolean equals(Object obj) {
        
        if (this == obj) return true;
        
        if (obj==null || !(obj instanceof BioEntry)) return false;
        
        else {
            
            BioEntry them = (BioEntry)obj;
            
            return (this.getNamespace().equals(them.getNamespace()) &&
                    
                    this.getName().equals(them.getName()) &&
                    
                    this.getAccession().equals(them.getAccession()) &&
                    
                    this.getVersion()==them.getVersion());
            
        }
        
    }
    
    
    
    /**
     *
     * Compares this object with the specified object for order.  Returns a
     *
     * negative integer, zero, or a positive integer as this object is less
     *
     * than, equal to, or greater than the specified object.
     *
     * @return a negative integer, zero, or a positive integer as this object
     *
     * 		is less than, equal to, or greater than the specified object.
     *
     * @param o the Object to be compared.
     *
     */
    
    public int compareTo(Object o) {
        
        BioEntry them = (BioEntry)o;
        
        if (!this.getNamespace().equals(them.getNamespace())) return this.getNamespace().compareTo(them.getNamespace());
        
        if (!this.getName().equals(them.getName())) return this.getName().compareTo(them.getName());
        
        if (!this.getAccession().equals(them.getAccession())) return this.getAccession().compareTo(them.getAccession());
        
        return this.getVersion()-them.getVersion();
        
    }
    
    
    
    /**
     *
     * Returns a hash code value for the object. This method is
     *
     * supported for the benefit of hashtables such as those provided by
     *
     * <code>Hashtable</code>.
     *
     * @return  a hash code value for this object.
     *
     * @see     java.lang.Object#equals(java.lang.Object)
     *
     * @see     java.util.Hashtable
     *
     */
    
    public int hashCode() {
        
        int code = 17;
        
        code = 37*code + this.getNamespace().hashCode();
        
        code = 37*code + this.getName().hashCode();
        
        code = 37*code + this.getAccession().hashCode();
        
        code = 37*code + this.getVersion();
        
        return code;
        
    }
    
    
    
    /**
     *
     * Returns a string representation of the object of the form
     *
     * <code>this.getNamespace()+": "+this.getName()+"/"+this.getAccession()+" v."+this.getVersion();</code>
     *
     * @return  a string representation of the object.
     *
     */
    
    public String toString() {
        
        return this.getNamespace()+": "+this.getName()+"/"+this.getAccession()+" v."+this.getVersion();
        
    }
    
    
    
    /**
     *
     * Counts a list not including nulls.
     *
     * @return size of list
     *
     * @param input the list to measure.
     *
     */
    
    protected int countList(List input) {
        
        int count = 0;
        
        for (Iterator i = input.iterator();i.hasNext();) {
            
            if (i.next()!=null) count++;
            
        }
        
        return count;
        
    }
    
    
    
    /**
     *
     * Counts relationships, not including nulls.
     *
     * @return the number of relationships.
     *
     */
    
    public int countBioEntryRelationships() {
        
        return this.relationships.size();
        
    }
    
    
    
    /**
     *
     * Counts cross refs, not including nulls.
     *
     * @return the number of cross refs.
     *
     */
    
    public int countCrossRefs() {
        
        return this.countList(this.crossrefs);
        
    }
    
    
    
    /**
     *
     * Counts comments, not including nulls.
     *
     * @return the number of comments.
     *
     */
    
    public int countComments() {
        
        return this.countList(this.comments);
        
    }
    
    
    
    /**
     *
     * Return a new FeatureHolder that contains all of the children of this one
     *
     * that passed the filter fc.
     *
     *
     *
     * This method is scheduled for deprecation.  Use the 1-arg filter
     *
     * instead.
     *
     * @param fc the FeatureFilter to apply
     *
     * @param recurse true if all features-of-features should be scanned, and a
     *
     * single flat collection of features returned, or false if
     *
     * just immediate children should be filtered.
     *
     * @return a FeatureHolder.
     *
     */
    
    public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
        
        return this.features.filter(fc,recurse);
        
    }
    
    
    
    /**
     *
     * Create a new Feature, and add it to this FeatureHolder.  This
     *
     * method will generally only work on Sequences, and on some
     *
     * Features which have been attached to Sequences.
     *
     * @param ft The Template to create the feature from.
     *
     * @throws BioException if something went wrong during creating the feature
     *
     * @throws ChangeVetoException if this FeatureHolder does not support
     *
     * creation of new features, or if the change was vetoed
     *
     * @return the new feature.
     *
     */
    
    public Feature createFeature(Feature.Template ft) throws BioException, ChangeVetoException {
        
        return this.features.createFeature(ft);
        
    }
    
    
    
    /**
     *
     * Remove a feature from this FeatureHolder.
     *
     * @param f the feature to remove.
     *
     * @throws ChangeVetoException if this FeatureHolder does not support
     *
     * feature removal or if the change was vetoed
     *
     * @throws BioException if there was an error removing the feature
     *
     */
    
    public void removeFeature(Feature f) throws ChangeVetoException, BioException {
        
        this.features.removeFeature(f);
        
    }
    
    
    
    /**
     *
     * Check if the feature is present in this holder.
     *
     *
     *
     * @since 1.2
     *
     * @param f the Feature to check
     *
     * @return true if f is in this set
     *
     */
    
    public boolean containsFeature(Feature f) {
        
        return this.features.containsFeature(f);
        
    }
    
    
    
    /**
     *
     * Query this set of features using a supplied <code>FeatureFilter</code>.
     *
     *
     *
     * @param filter the <code>FeatureFilter</code> to apply.
     *
     * @return all features in this container which match <code>filter</code>.
     *
     */
    
    public FeatureHolder filter(FeatureFilter filter) {
        
        return this.features.filter(filter);
        
    }
    
    
    
    /**
     *
     * Return a schema-filter for this <code>FeatureHolder</code>.  This is a filter
     *
     * which all <code>Feature</code>s <em>immediately</em> contained by this <code>FeatureHolder</code>
     *
     * will match.  It need not directly match their child features, but it can (and should!) provide
     *
     * information about them using <code>FeatureFilter.OnlyChildren</code> filters.  In cases where there
     *
     * is no feature hierarchy, this can be indicated by including <code>FeatureFilter.leaf</code> in
     *
     * the schema filter.
     *
     *
     *
     * <p>
     *
     * For the truly non-informative case, it is possible to return <code>FeatureFilter.all</code>.  However,
     *
     * it is almost always possible to provide slightly more information that this.  For example, <code>Sequence</code>
     *
     * objects should, at a minimum, return <code>FeatureFilter.top_level</code>.  <code>Feature</code> objects
     *
     * should, as a minimum, return <code>FeatureFilter.ByParent(new FeatureFilter.ByFeature(this))</code>.
     *
     * </p>
     *
     *
     *
     * @since 1.3
     *
     * @return the schema filter
     *
     */
    
    public FeatureFilter getSchema() {
        
        return this.features.getSchema();
        
    }
    
    
    
    /**
     *
     * Iterate over the features in no well defined order.
     *
     *
     *
     * @return  an Iterator
     *
     */
    
    public Iterator features() {
        
        return this.features.features();
        
    }
    
    
    
    /**
     *
     * Count how many features are contained.
     *
     *
     *
     * @return  a positive integer or zero, equal to the number of features
     *
     *          contained
     *
     */
    
    public int countFeatures() {
        
        return this.features.countFeatures();
        
    }
    
    
    
    /**
     *
     * Apply an edit to the SymbolList as specified by the edit object.
     *
     *
     *
     * <h2>Description</h2>
     *
     *
     *
     * <p>
     *
     * All edits can be broken down into a series of operations that change
     *
     * contiguous blocks of the sequence. This represent a one of those operations.
     *
     * </p>
     *
     *
     *
     * <p>
     *
     * When applied, this Edit will replace 'length' number of symbols starting a
     *
     * position 'pos' by the SymbolList 'replacement'. This allow to do insertions
     *
     * (length=0), deletions (replacement=SymbolList.EMPTY_LIST) and replacements
     *
     * (length>=1 and replacement.length()>=1).
     *
     * </p>
     *
     *
     *
     * <p>
     *
     * The pos and pos+length should always be valid positions on the SymbolList
     *
     * to:
     *
     * <ul>
     *
     * <li>be edited (between 0 and symL.length()+1).</li>
     *
     * <li>To append to a sequence, pos=symL.length()+1, pos=0.</li>
     *
     * <li>To insert something at the beginning of the sequence, set pos=1 and
     *
     * length=0.</li>
     *
     * </ul>
     *
     * </p>
     *
     *
     *
     * <h2>Examples</h2>
     *
     *
     *
     * <code><pre>
     *
     * SymbolList seq = DNATools.createDNA("atcaaaaacgctagc");
     *
     * System.out.println(seq.seqString());
     *
     *
     *
     * // delete 5 bases from position 4
     *
     * Edit ed = new Edit(4, 5, SymbolList.EMPTY_LIST);
     *
     * seq.edit(ed);
     *
     * System.out.println(seq.seqString());
     *
     *
     *
     * // delete one base from the start
     *
     * ed = new Edit(1, 1, SymbolList.EMPTY_LIST);
     *
     * seq.edit(ed);
     *
     *
     *
     * // delete one base from the end
     *
     * ed = new Edit(seq.length(), 1, SymbolList.EMPTY_LIST);
     *
     * seq.edit(ed);
     *
     * System.out.println(seq.seqString());
     *
     *
     *
     * // overwrite 2 bases from position 3 with "tt"
     *
     * ed = new Edit(3, 2, DNATools.createDNA("tt"));
     *
     * seq.edit(ed);
     *
     * System.out.println(seq.seqString());
     *
     *
     *
     * // add 6 bases to the start
     *
     * ed = new Edit(1, 0, DNATools.createDNA("aattgg");
     *
     * seq.edit(ed);
     *
     * System.out.println(seq.seqString());
     *
     *
     *
     * // add 4 bases to the end
     *
     * ed = new Edit(seq.length() + 1, 0, DNATools.createDNA("tttt"));
     *
     * seq.edit(ed);
     *
     * System.out.println(seq.seqString());
     *
     *
     *
     * // full edit
     *
     * ed = new Edit(3, 2, DNATools.createDNA("aatagaa");
     *
     * seq.edit(ed);
     *
     * System.out.println(seq.seqString());
     *
     * </pre></code>
     *
     *
     *
     * @param edit the Edit to perform
     *
     * @throws IndexOutOfBoundsException if the edit does not lie within the
     *
     *         SymbolList
     *
     * @throws IllegalAlphabetException if the SymbolList to insert has an
     *
     *         incompatible alphabet
     *
     * @throws ChangeVetoException  if either the SymboList does not support the
     *
     *         edit, or if the change was vetoed
     *
     */
    
    public void edit(Edit edit) throws IndexOutOfBoundsException, IllegalAlphabetException, ChangeVetoException {
        
        this.symList.edit(edit);
        
    }
    
    
    
    /**
     *
     * Return the symbol at index, counting from 1.
     *
     *
     *
     * @param index the offset into this SymbolList
     *
     * @return  the Symbol at that index
     *
     * @throws IndexOutOfBoundsException if index is less than 1, or greater than
     *
     *                                   the length of the symbol list
     *
     */
    
    public Symbol symbolAt(int index) throws IndexOutOfBoundsException {
        
        return this.symList.symbolAt(index);
        
    }
    
    
    
    /**
     *
     * Returns a List of symbols.
     *
     * <p>
     *
     * This is an immutable list of symbols. Do not edit it.
     *
     *
     *
     * @return  a List of Symbols
     *
     */
    
    public List toList() {
        
        return this.symList.toList();
        
    }
    
    
    
    /**
     *
     * Return a region of this symbol list as a String.
     *
     * <p>
     *
     * This should use the same rules as seqString.
     *
     *
     *
     * @param start  the first symbol to include
     *
     * @param end the last symbol to include
     *
     * @return the string representation
     *
     * @throws IndexOutOfBoundsException if either start or end are not within the
     *
     *         SymbolList
     *
     */
    
    public String subStr(int start, int end) throws IndexOutOfBoundsException {
        
        return this.symList.subStr(start, end);
        
    }
    
    
    
    /**
     *
     * Return a new SymbolList for the symbols start to end inclusive.
     *
     *
     *
     * The resulting SymbolList will count from 1 to (end-start + 1) inclusive, and
     *
     * refer to the symbols start to end of the original sequence.
     *
     * @param start the first symbol of the new SymbolList
     *
     * @param end the last symbol (inclusive) of the new SymbolList
     *
     * @throws java.lang.IndexOutOfBoundsException if the symbol list doesn't have the coordinates given.
     *
     * @return the sublist.
     *
     */
    
    public SymbolList subList(int start, int end) throws IndexOutOfBoundsException {
        
        return this.symList.subList(start, end);
        
    }
    
    
    
    /**
     *
     * Stringify this symbol list.
     *
     * <p>
     *
     * It is expected that this will use the symbol's token to render each
     *
     * symbol. It should be parsable back into a SymbolList using the default
     *
     * token parser for this alphabet.
     *
     *
     *
     * @return  a string representation of the symbol list
     *
     */
    
    public String seqString() {
        
        return this.symList.seqString();
        
    }
    
    
    
    /**
     *
     * The number of symbols in this SymbolList.
     *
     *
     *
     * @return  the length
     *
     */
    
    public int length() {
        
        return this.symList.length();
        
    }
    
    
    
    /**
     *
     * An Iterator over all Symbols in this SymbolList.
     *
     * <p>
     *
     * This is an ordered iterator over the Symbols. It cannot be used
     *
     * to edit the underlying symbols.
     *
     *
     *
     * @return  an iterator
     *
     */
    
    public Iterator iterator() {
        
        return this.symList.iterator();
        
    }
    
    
    
    /**
     *
     * A <a href="http://www.rfc-editor.org/rfc/rfc2396.txt">Uniform
     *
     * Resource Identifier</a> (URI) which identifies the sequence
     *
     * represented by this object.  For sequences in well-known
     *
     * database, this may be a URN, e.g.
     *
     *
     *
     * <pre>
     *
     * urn:sequence/embl:AL121903
     *
     * </pre>
     *
     *
     *
     * It may also be a URL identifying a specific resource, either
     *
     * locally or over the network
     *
     *
     *
     * <pre>
     *
     * file:///home/thomas/myseq.fa|seq22
     *
     * http://www.mysequences.net/chr22.seq
     *
     * </pre>
     *
     *
     *
     * In this implementation, URN just equals Name.
     *
     *
     *
     * @return the URI as a String
     *
     */
    
    public String getURN() {
        
        return this.getName();
        
    }
    
    
    
    /**
     *
     * The alphabet that this SymbolList is over.
     *
     * <p>
     *
     * Every symbol within this SymbolList is a member of this alphabet.
     *
     * <code>alphabet.contains(symbol) == true</code>
     *
     * for each symbol that is within this sequence.
     *
     *
     *
     * @return  the alphabet
     *
     */
    
    public Alphabet getAlphabet() {
        
        return this.symList.getAlphabet();
        
    }
    
    
    
    /**
     * Sets the version of the associated symbol list.
     * @param seqVersion the version to set.
     * @throws org.biojava.utils.ChangeVetoException in case the change is refused.
     */
    
    public void setSeqVersion(double seqVersion) throws ChangeVetoException {
        
        if(!this.hasListeners(BioEntry.RELATIONSHIP)) {
            
            this.symListVersion = seqVersion;
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    BioEntry.SEQVERSION,
                    
                    Double.valueOf(seqVersion),
                    
                    Double.valueOf(this.symListVersion)
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(BioEntry.RELATIONSHIP);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.symListVersion = seqVersion;
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
    }
    
    
    
    /**
     *
     * The version of the associated symbol list.
     *
     * @return  the version
     *
     */
    
    public double getSeqVersion() {
        
        return this.symListVersion;
        
    }
    
    
    
    /**
     *
     * Searches for a docref in the list of all docrefs, and removes it if it was
     *
     * found.
     *
     * @return True if the docref was found, false if the docref was not found.
     *
     * @param docref the docref to search for and remove.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     */
    
    public boolean removeDocRef(LocatedDocumentReference docref) throws ChangeVetoException {
        
        int index = this.docrefs.indexOf(docref);
        
        if (index>=0) {
            
            if(!this.hasListeners(BioEntry.DOCREF)) {
                
                this.docrefs.set(index,null);
                
            } else {
                
                ChangeEvent ce = new ChangeEvent(
                        
                        this,
                        
                        BioEntry.DOCREF,
                        
                        null,
                        
                        docrefs
                        
                        );
                
                ChangeSupport cs = this.getChangeSupport(BioEntry.DOCREF);
                
                synchronized(cs) {
                    
                    cs.firePreChangeEvent(ce);
                    
                    this.docrefs.set(index,null);
                    
                    cs.firePostChangeEvent(ce);
                    
                }
                
            }
            
            return true;
            
        } else {
            
            return false;
            
        }
        
    }
    
    
    
    /**
     *
     * Tests for the existence of a docref in the list.
     *
     * @param docref the docref to look for.
     *
     * @return True if the docref is in the list, false if not.
     *
     */
    
    public boolean containsDocRef(LocatedDocumentReference docref) {
        
        return this.docrefs.contains(docref);
        
    }
    
    
    
    /**
     *
     * Adds the docref to the end of the list of docrefs, giving it the index of
     *
     * max(all other docref index positions)+1.
     *
     * @return The position the docref was added at.
     *
     * @param docref New docref to add.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     * @throws AlreadyExistsException if the docref already exists at another index.
     *
     */
    
    public int addDocRef(LocatedDocumentReference docref) throws AlreadyExistsException, ChangeVetoException {
        
        if (this.docrefs.contains(docref)) throw new AlreadyExistsException("Document reference has already been made");
        
        int index = this.docrefs.size();
        
        if(!this.hasListeners(BioEntry.DOCREF)) {
            
            this.docrefs.ensureCapacity(index+1);
            
            this.docrefs.add(docref);
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    BioEntry.DOCREF,
                    
                    docref,
                    
                    this.docrefs.contains(docref)?docref:null
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(BioEntry.DOCREF);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.docrefs.ensureCapacity(index+1);
                
                this.docrefs.add(docref);
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
        return index;
        
    }
    
    
    
    /**
     *
     * Overwrites the list of docrefs at the given index position with the docref
     *
     * supplied. It will overwrite anything already at that position.
     *
     * @param docref New docref to write at that position.
     *
     * @param index Position to write docref at.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     * @throws AlreadyExistsException if the docref already exists at another index.
     *
     */
    
    public void setDocRef(LocatedDocumentReference docref, int index) throws AlreadyExistsException, ChangeVetoException {
        
        if (this.docrefs.contains(docref)) throw new AlreadyExistsException("Document reference has already been added");
        
        if(!this.hasListeners(BioEntry.CROSSREF)) {
            
            this.docrefs.ensureCapacity(index+1);
            
            this.docrefs.set(index,docref);
            
        } else {
            
            ChangeEvent ce = new ChangeEvent(
                    
                    this,
                    
                    BioEntry.DOCREF,
                    
                    docref,
                    
                    this.docrefs.get(index)
                    
                    );
            
            ChangeSupport cs = this.getChangeSupport(BioEntry.DOCREF);
            
            synchronized(cs) {
                
                cs.firePreChangeEvent(ce);
                
                this.docrefs.ensureCapacity(index+1);
                
                this.docrefs.set(index,docref);
                
                cs.firePostChangeEvent(ce);
                
            }
            
        }
        
    }
    
    
    
    /**
     *
     * Removes the docref at a given index. If the index position already had no
     *
     * docref associated, it returns false. Else, it returns true.
     *
     * @return True if a docref was found at that position and removed.
     *
     * @param index the index position to remove the docref from.
     *
     * @throws org.biojava.utils.ChangeVetoException in case of objections.
     *
     * @throws IndexOutOfBoundsException if the index position was invalid.
     *
     */
    
    public boolean removeDocRef(int index) throws IndexOutOfBoundsException, ChangeVetoException {
        
        if (this.docrefs.get(index)==null) return false;
        
        else {
            
            if(!this.hasListeners(BioEntry.DOCREF)) {
                
                this.docrefs.set(index,null);
                
            } else {
                
                ChangeEvent ce = new ChangeEvent(
                        
                        this,
                        
                        BioEntry.DOCREF,
                        
                        null,
                        
                        this.docrefs.get(index)
                        
                        );
                
                ChangeSupport cs = this.getChangeSupport(BioEntry.DOCREF);
                
                synchronized(cs) {
                    
                    cs.firePreChangeEvent(ce);
                    
                    this.docrefs.set(index,null);
                    
                    cs.firePostChangeEvent(ce);
                    
                }
                
            }
            
            return true;
            
        }
        
    }
    
    
    
    /**
     *
     * Returns the docref at a given index. If the index is valid but no docref is
     *
     * found at that position, it will return null. If the index is invalid,
     *
     * an exception will be thrown.
     *
     * @param index the index of the docref to retrieve.
     *
     * @return The docref at that index position.
     *
     * @throws IndexOutOfBoundsException if an invalid index is specified.
     *
     */
    
    public LocatedDocumentReference getDocRef(int index) throws IndexOutOfBoundsException {
        
        return (LocatedDocumentReference)this.docrefs.get(index);
        
    }
    
    
    
    /**
     *
     * Returns a list of all docrefs associated with this bioentry. This
     *
     * list is not mutable. If no docrefs are associated, you will get back an
     *
     * empty list. If the docrefs have indexes that are not consecutive, then the
     *
     * list will contain nulls at the indexes corresponding to the gaps between
     *
     * the extant docrefs. eg. If there are only two docrefs A and B at positions 10
     *
     * and 20 respectively, then the List returned will be of size 20, with nulls
     *
     * at index positions 0-9 and 11-19.
     *
     * @return Value of property docrefs.
     *
     */
    
    public List getDocRefs() {
        
        return Collections.unmodifiableList(this.docrefs);
        
    }
    
    
    
    /**
     *
     * Counts doc refs, not including nulls.
     *
     * @return the number of doc refs.
     *
     */
    
    public int countDocRefs() {
        
        return this.countList(this.docrefs);
        
    }
    
    
    
}

