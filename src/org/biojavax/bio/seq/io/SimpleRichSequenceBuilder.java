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
 * SimpleRichSequenceBuilder.java
 *
 * Created on July 11, 2005, 1:51 PM
 */

package org.biojavax.bio.seq.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.SimpleAnnotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.io.ChunkedSymbolListFactory;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.seq.io.SequenceBuilder;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.SimpleSymbolListFactory;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.CrossRef;
import org.biojavax.Namespace;
import org.biojavax.LocatedDocumentReference;
import org.biojavax.bio.BioEntryComment;
import org.biojavax.bio.BioEntryRelationship;
import org.biojavax.bio.db.Persistent;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.SimpleRichSequence;
import org.biojavax.bio.taxa.NCBITaxon;
import org.biojavax.ontology.ComparableTerm;

/**
 * Constructs BioEntry objects by listening to events.
 * @author Richard Holland
 */
public class SimpleRichSequenceBuilder implements RichSeqIOListener,SequenceBuilder {
    
    /**
     * Creates a new instance of SimpleRichSequenceBuilder 
     */
    public SimpleRichSequenceBuilder() {
        this.reset();
    }
    
    /**
     * Sets the sequence info back to default values, ie. in order to start
     * constructing a new sequence from scratch.
     */
    private void reset() {
        this.version = 0;
        this.versionSeen = false;
        this.seqVersion = 0;
        this.seqVersionSeen = false;
        this.accessions.clear();
        this.description = null;
        this.division = null;
        this.identifier = null;
        this.name = null;
        this.crossRefs.clear();
        this.symbols = null;
        this.namespace = null;
        this.taxon = null;
        this.comments.clear();
        this.relations.clear();
        this.references.clear();
        this.rootFeatures.clear();
        this.featureStack.clear();
        this.ann = new SimpleAnnotation() {
            public void setProperty(Object key, Object value) throws ChangeVetoException {
                if (!(key instanceof ComparableTerm)) throw new ChangeVetoException("Can only annotate using ComparableTerm objects as keys");
                if (!(value instanceof String)) throw new ChangeVetoException("Can only annotate using single String objects as values");
                super.setProperty(key, value);
            }
        };
    }
    
    /**
     * Call back method so the event emitter can tell the listener
     * the version of the record being read. This method would typically
     * be called 0 or 1 times per entry. If it is not called the
     * Listener should assume the version is 0. If it is called more
     * than once per entry an exception should be thrown.
     * @param version the version of the record
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     */
    public void setVersion(String version) throws ParseException {
        if (this.versionSeen) throw new ParseException("Current BioEntry already has a version");
        if (version==null) this.version=0;
        else {
            try {
                this.version = Integer.parseInt(version);
                this.versionSeen = true;
            } catch (NumberFormatException e) {
                throw new ParseException("Could not parse version as an integer");
            }
        }
    }
    private int version;
    private boolean versionSeen;
    
    /**
     * Notify the listener of a URI identifying the current sequence or record.
     * The method is not commonly used. A typical URI might be a life science
     * identifier (LSID).
     * @param uri the URI of the record.
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     * @see #setName(String name)
     * @see #setAccession(String accession)
     * @see #setIdentifier(String identifier)
     */
    public void setURI(String uri) throws ParseException {
        throw new ParseException("We don't understand URIs");
    }
    
    /**
     * Call back method so the event emitter can tell the listener
     * the version of the sequence of the record being read. This method would typically
     * be called 0 or 1 times per entry. If it is not called the
     * Listener should assume the version is 0. If it is called more
     * than once per entry an exception should be thrown.
     * @param seqVersion the version to set the sequence to.
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     */
    public void setSeqVersion(String seqVersion) throws ParseException {
        if (this.seqVersionSeen) throw new ParseException("Current BioEntry already has a sequence version");
        if (seqVersion==null) this.seqVersion=Persistent.NULL_DOUBLE;
        else {
            try {
                this.seqVersion = Double.parseDouble(seqVersion);
                this.seqVersionSeen = true;
            } catch (NumberFormatException e) {
                throw new ParseException("Could not parse sequence version as a double");
            }
        }
    }
    private double seqVersion;
    private boolean seqVersionSeen;
    
    /**
     * Call back method so the event emitter can tell the listener
     * the accession of the record being read. It is possible that some
     * records have more than one accession. As a guide the first one
     * sent to the listener should be the primary one.
     * @param accession The accession of the record
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     *
     * @see #setName(String name)
     * @see #setURI(String uri)
     * @see #setIdentifier(String identifier)
     *
     */
    public void setAccession(String accession) throws ParseException {
        if (accession==null) throw new ParseException("Accession cannot be null");
        if (!this.accessions.contains(accession)) this.accessions.add(accession);
    }
    private List accessions = new ArrayList();
    
    /**
     * Call back method so the event emitter can tell the listener
     * the description of the record being read. For example the description
     * line of a FASTA format file would be the description. This method
     * would typically be called 0 or 1 times and may cause an exception
     * if it is called more than once per entry.
     * @param description The description of the record
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     */
    public void setDescription(String description) throws ParseException {
        if (description==null) throw new ParseException("Description cannot be null");
        if (this.description!=null) throw new ParseException("Current BioEntry already has a description");
        this.description = description;
    }
    private String description;
    
    /**
     * Call back method so the event emitter can tell the listener
     * the division of the record being read. If the source of the
     * calls back is a GenBank parser the division will be a Genbank division.
     * This method would typically be called 0 or 1 times. It should
     * not be called more than once per entry and an exception could be thrown
     * if it is.
     * @param division The division the entry belongs too.
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     */
    public void setDivision(String division) throws ParseException {
        if (division==null) throw new ParseException("Division cannot be null");
        if (this.division!=null) throw new ParseException("Current BioEntry already has a division");
        this.division = division;
    }
    private String division;
    
    /**
     * Call back method so the event emitter can tell the listener
     * the identifier of the record being read. There should be
     * zero or one identifier per bioentry. If there is more
     * than one the Listener should consider throwing an exception.
     * For some formats like fasta the identifier may not exist. For others
     * like GenBank the identifier best maps to the GI.
     * @param identifier The identifier of the Bioentry.
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     * @see #setName(String name)
     * @see #setAccession(String accession)
     * @see #setURI(String uri)
     */
    public void setIdentifier(String identifier) throws ParseException {
        if (identifier==null) throw new ParseException("Identifier cannot be null");
        if (this.identifier!=null) throw new ParseException("Current BioEntry already has a identifier");
        this.identifier = identifier;
    }
    private String identifier;
    
    /**
     * Notify the listener that the current sequence is generally known
     * by a particular name. This method should be called once per entry. An exception
     * may be thrown if it is called more or less frequently.
     *
     * @param name the String that should be returned by getName for the sequence
     * being parsed.
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     * @see #setURI(String uri)
     * @see #setAccession(String accession)
     * @see #setIdentifier(String identifier)
     */
    public void setName(String name) throws ParseException {
        if (name==null) throw new ParseException("Name cannot be null");
        if (this.name!=null) throw new ParseException("Current BioEntry already has a name");
        this.name = name;
    }
    private String name;
    
    /**
     * Call back method so the event emitter can tell the listener about a cross reference.
     * This could be called zero or more times per entry.
     * @param crossRef the cross reference
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     */
    public void setCrossReference(CrossRef crossRef) throws ParseException {
        if (crossRef==null) throw new ParseException("Name cannot be null");
        this.crossRefs.add(crossRef);
    }
    private Set crossRefs = new HashSet();
    
    /**
     * Notify the listener of symbol data.  All symbols passed to
     * this method are guarenteed to be contained within the
     * specified alphabet.  Generally all calls to a given Listener
     * should have the same alphabet -- if not, the listener implementation
     * is likely to throw an exception. This method may be called zero or more
     * times for an entry. Obviously if you want to build a <code>Sequence</code>
     * object you would need to call it at least once. If your only making a
     * BioEntry you probably wouldn't call it at all (or the Listener would ignore
     * it).
     *
     * @param alpha The alphabet of the symbol data
     * @param syms An array containing symbols
     * @param start The start offset of valid data within the array
     * @param length The number of valid symbols in the array
     *
     * @throws IllegalAlphabetException if we can't cope with this
     *                                  alphabet.
     */
    public void addSymbols(Alphabet alpha, Symbol[] syms, int start, int length) throws IllegalAlphabetException {
        if (this.symbols==null) this.symbols = new ChunkedSymbolListFactory(new SimpleSymbolListFactory());
        this.symbols.addSymbols(alpha, syms, start, length);
    }
    private ChunkedSymbolListFactory symbols;
    
    /**
     * Call back method so the event emitter can tell the listener
     * about a comment in the record being read. The comment is typically
     * one or more comment lines relevant to the record as a whole and
     * bundled in a <code>Commment</code> object.
     * This method may be called zero or one times per entry. It can be called zero or more times.
     * @param comment The comment
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     */
    public void setComment(String comment) throws ParseException {
        if (comment==null) throw new ParseException("Comment cannot be null");
        this.comments.add(comment);
    }
    private Set comments = new HashSet();
    
    /**
     * Call back method so the event emitter can tell the listener
     * the namespace of the record being read. The method can be called
     * zero or one time. If it is called more than once an exception
     * may be thrown.<p>
     * The namespace is a concept from the BioSQL schema that enables
     * Bioentries to be disambiguated. It is possible in BioSQL and should be possible
     * in other collections of BioEntries to have records that have the same
     * name, accession and version but different namespaces. This method would be
     * expected to be called if you are reading a sequence from a biosql database or
     * if you are implementing a listener that knows how to write to a biosql database.
     * If you give a sequence a namespace and it is persited to biosql at somepoint in it's
     * life you could expect it to be persisted to that namespace (if possible).
     *
     * @param namespace The namespace of the entry.
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     */
    public void setNamespace(Namespace namespace) throws ParseException {
        if (namespace==null) throw new ParseException("Namespace cannot be null");
        if (this.namespace!=null) throw new ParseException("Current BioEntry already has a namespace");
        this.namespace = namespace;
    }
    private Namespace namespace;
    
    /**
     * Notify the listener that a new feature object is starting.
     * Every call to startFeature should have a corresponding call
     * to endFeature.  If the listener is concerned with a hierarchy
     * of features, it should maintain a stack of `open' features.
     * <p>
     * The key difference between Features and Sequence properties is that Features
     * are localized to a region of Sequence. Thus BioEntrys wouldn't have Features
     * but Sequences could. This method could be called zero or more times per
     * entry but shouldn't be called or should be ignored if the implementation
     * is only building bioentries.
     * @param templ The template of the feature to be created.
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     *
     * @see #addFeatureProperty(Object key, Object value)
     * @see #endFeature()
     */
    public void startFeature(Feature.Template templ) throws ParseException {
        TemplateWithChildren t2 = new TemplateWithChildren();
        t2.template = templ;
        if(templ.annotation == Annotation.EMPTY_ANNOTATION) templ.annotation = new SimpleAnnotation() {
            public void setProperty(Object key, Object value) throws ChangeVetoException {
                if (!(key instanceof ComparableTerm)) throw new ChangeVetoException("Can only annotate using ComparableTerm objects as keys");
                if (!(value instanceof String)) throw new ChangeVetoException("Can only annotate using single String objects as values");
                super.setProperty(key, value);
            }
        };
        if (this.featureStack.size() == 0) this.rootFeatures.add(t2);
        else {
            TemplateWithChildren parent = (TemplateWithChildren) this.featureStack.get(this.featureStack.size() - 1);
            if (parent.children == null) parent.children = new HashSet();
            parent.children.add(t2);
        }
        this.featureStack.add(t2);
    }
    private Set rootFeatures = new HashSet();
    private List featureStack = new ArrayList();
    
    /**
     * Call back method so the event emitter can tell the listener
     * the Taxon of the record being read. This method may be called
     * zero or one times. An exception may be thrown if it is called
     * more than once. As a design decision NCBI's taxon model was chosen as it
     * is commonly used and is supported by the BioSQL schema. The setting of
     * an NCBI taxon should be considered entirely optional.
     *
     * @param taxon The taxon information relevant to this entry.
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     */
    public void setTaxon(NCBITaxon taxon) throws ParseException {
        if (taxon==null) throw new ParseException("Taxon cannot be null");
        if (this.taxon!=null) throw new ParseException("Current BioEntry already has a taxon");
        this.taxon = taxon;
    }
    private NCBITaxon taxon;
    
    /**
     * Call back method so the event emitter can tell the listener
     * about a relationship between the bioentry or sequence in the
     * record being read and another bioentry. This may be called zero
     * or more times.
     * @param relationship The relationship
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     */
    public void setRelationship(BioEntryRelationship relationship) throws ParseException {
        if (relationship==null) throw new ParseException("Relationship cannot be null");
        this.relations.add(relationship);
    }
    private Set relations = new HashSet();
    
    /**
     * Call back method so the event emitter can tell the listener
     * about a literature reference in the record being read. This method
     * may be called zero or more times.
     * @param ref A literature reference contained in the entry.
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     */
    public void setBioEntryDocRef(LocatedDocumentReference ref) throws ParseException {
        if (ref==null) throw new ParseException("Reference cannot be null");
        this.references.add(ref);
    }
    private Set references = new HashSet();
    
    /**
     * Start the processing of a sequence or bioentry.
     * This method exists primarily to enforce the life-cycles of records that
     * stream multiple sequence or bioentries in a single record eg a file
     * with mulitple sequences in FASTA format.
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     * @see #endSequence()
     */
    public void startSequence() throws ParseException {
        this.reset();
    }
    
    /**
     * Notify the listener of a feature property. A feature property is very
     * much like a sequence property but is only relevant to the region covered
     * by that feature. Feature properties go into the features annotation bundle.
     * <p>
     * If the listener wants to enforce an ontology or restricted vocabulary it
     * may want to throw an exception if the <code>key</code> and or
     * <code>value</code> are not <code>Term</code>s from that ontology.
     * This method could be called zero or more times per call to <code>
     * startFeature(Feature.Template templ)</code> and should never be called
     * unless between startFeature and endFeature events.
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     * @param key the key (often an <code>org.biojava.ontology.Term</code>
     * @param value the value associated with the key, typically a <code>String</code>
     */
    public void addFeatureProperty(Object key, Object value) throws ParseException {
        if (this.featureStack.size() == 0) throw new ParseException("Assertion failed: Not within a feature");
        TemplateWithChildren top = (TemplateWithChildren) this.featureStack.get(this.featureStack.size() - 1);
        try {
            top.template.annotation.setProperty(key, value);
        } catch (ChangeVetoException e) {
            throw new ParseException(e); // Simple conversion
        }
    }
    
    /**
     * Notify the listener of a sequence-wide property.  This might
     * be stored as an entry in the sequence's annotation bundle. If the listener
     * wants to enforce an ontology or restricted vocabulary it may want to
     * throw an exception if the <code>key</code> and or <code>value</code> are
     * not <code>Term</code>s from that ontology. This method could be called
     * zero or more times per entry.
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     * @param key the key (often an <code>org.biojava.ontology.Term</code>
     * @param value the value associated with the key, typically a <code>String</code>
     */
    public void addSequenceProperty(Object key, Object value) throws ParseException {
        try {
            this.ann.setProperty(key,value);
        } catch (ChangeVetoException e) {
            throw new ParseException(e); // just wrap it, the message is self-explanatory
        }
    }
    private Annotation ann;
    
    /**
     * Mark the end of data associated with one specific feature. This should
     * be called once per call to <code>startFeature(Feature.Template templ)</code>
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     * @see #startFeature(Feature.Template templ)
     */
    public void endFeature() throws ParseException {
        if (this.featureStack.size() == 0) throw new ParseException("Assertion failed: Not within a feature");
        this.featureStack.remove(this.featureStack.size() - 1);
    }
    
    /**
     * Notify the listener that processing of the current
     * sequence or bioentry is complete.
     * @throws ParseException If the Listener cannot understand the event, is unable
     * to deal with the event or is not expecting the event.
     * @see #startSequence()
     */
    public void endSequence() throws ParseException {
        if (this.name==null) throw new ParseException("Name has not been supplied");
        if (this.namespace==null) throw new ParseException("Namespace has not been supplied");
        if (this.accessions.isEmpty()) throw new ParseException("No accessions have been supplied");
    }
    
    /**
     * Return the Sequence object which has been constructed
     * by this builder.  This method is only expected to succeed
     * after the endSequence() notifier has been called.
     * @throws org.biojava.bio.BioException in case the object could not be constructed.
     * @return the constructed object.
     */
    public Sequence makeSequence() throws BioException {
        this.endSequence(); // Check our input.
        // build our symbollist
        SymbolList syms = this.symbols.makeSymbolList();
        // deal with extra accessions by ignoring them.
        String accession = (String)this.accessions.get(0);
        // make our basic object
        RichSequence be = new SimpleRichSequence(this.namespace,this.name,accession,this.version,syms,this.seqVersion);
        // set misc stuff
        try {
            be.setSeqVersion(this.seqVersion);
            be.setDescription(this.description);
            be.setDivision(this.division);
            be.setIdentifier(this.identifier);
            be.setTaxon(this.taxon);
            for (Iterator i = this.crossRefs.iterator(); i.hasNext(); ) be.getCrossRefs().add((CrossRef)i.next());
            for (Iterator i = this.relations.iterator(); i.hasNext(); ) be.getBioEntryRelationships().add((BioEntryRelationship)i.next());
            for (Iterator i = this.references.iterator(); i.hasNext(); ) be.getDocRefs().add((LocatedDocumentReference)i.next());
            for (Iterator i = this.comments.iterator(); i.hasNext(); ) be.getComments().add((BioEntryComment)i.next());
            // set annotations
            for (Iterator i = this.ann.keys().iterator(); i.hasNext(); ) {
                Object key = i.next();
                Object value = this.ann.getProperty(key);
                if (value instanceof Collection) {
                    for (Iterator j = ((Collection)value).iterator(); j.hasNext(); ) be.getAnnotation().setProperty(key, j.next());
                } else {
                    be.getAnnotation().setProperty(key,value);
                }
            }
            // set features
            for (Iterator i = rootFeatures.iterator(); i.hasNext(); ) {
                TemplateWithChildren twc = (TemplateWithChildren) i.next();
                Feature f = be.createFeature(twc.template);
                if (twc.children != null) this.makeChildFeatures(f, twc.children);
            }
        } catch (Exception e) {
            throw new ParseException(e); // Convert them all to parse exceptions.
        }
        // return the object
        return be;
    }
    
    /**
     * Constructs features into BioEntryFeatures.
     * @param parent the parent feature holder.
     * @param children the set of features to add.
     * @throws java.lang.Exception in case it couldn't add them.
     */
    private void makeChildFeatures(Feature parent, Set children) throws Exception {
        for (Iterator i = children.iterator(); i.hasNext(); ) {
            TemplateWithChildren twc = (TemplateWithChildren) i.next();
            Feature f = parent.createFeature(twc.template);
            if (twc.children != null) {
                makeChildFeatures(f, twc.children);
            }
        }
    }
    
    /**
     * A template feature holder that has a hierarchy of children.
     */
    private static class TemplateWithChildren {
        public Feature.Template template;
        public Set children;
    }
    
}
