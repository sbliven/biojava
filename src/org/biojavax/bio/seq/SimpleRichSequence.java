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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.FilterUtils;
import org.biojava.bio.seq.SimpleFeatureHolder;
import org.biojava.bio.seq.io.ChunkedSymbolListFactory;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.PackedSymbolListFactory;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.ontology.InvalidTermException;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.Namespace;
import org.biojavax.bio.SimpleBioEntry;
import org.biojavax.bio.seq.io.SimpleRichSequenceBuilder;


/**
 * A simple implementation of RichSequence.
 * @author Richard Holland
 */
public class SimpleRichSequence extends SimpleBioEntry implements RichSequence {
    
    private SymbolList symList;
    private Set features = new TreeSet();
    private Double symListVersion;
    private boolean circular;
    
    
    /**
     * Creates a new instance of SimpleRichSequence.
     * @param ns the namespace for this sequence.
     * @param name the name of the sequence.
     * @param accession the accession of the sequence.
     * @param version the version of the sequence.
     * @param symList the symbols for the sequence.
     * @param seqversion the version of the symbols for the sequence.
     */
    public SimpleRichSequence(Namespace ns, String name, String accession, int version, SymbolList symList, Double seqversion) {
        super(ns,name,accession,version);
        if (symList==null) this.symList = SymbolList.EMPTY_LIST;
        else this.symList = symList;
        this.symListVersion = seqversion;
        this.circular = false;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleRichSequence() {}
    
    /**
     * {@inheritDoc}
     */
    public Double getSeqVersion() { return this.symListVersion; }
    
    /**
     * {@inheritDoc}
     */
    public void setSeqVersion(Double seqVersion) throws ChangeVetoException {
        if(!this.hasListeners(RichSequence.SYMLISTVERSION)) {
            this.symListVersion = seqVersion;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichSequence.SYMLISTVERSION,
                    seqVersion,
                    this.symListVersion
                    );
            ChangeSupport cs = this.getChangeSupport(RichSequence.SYMLISTVERSION);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.symListVersion = seqVersion;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    public void setCircular(boolean circular) throws ChangeVetoException {
        if(!this.hasListeners(RichSequence.CIRCULAR)) {
            this.circular = circular;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichSequence.CIRCULAR,
                    new Boolean(circular),
                    new Boolean(this.circular)
                    );
            ChangeSupport cs = this.getChangeSupport(RichSequence.CIRCULAR);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.circular = circular;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    public boolean getCircular() {
        return this.circular;
    }
    
    /**
     * {@inheritDoc}
     */
    public void edit(Edit edit) throws IndexOutOfBoundsException, IllegalAlphabetException, ChangeVetoException {
        this.symList.edit(edit);
    }
    
    /**
     * {@inheritDoc}
     */
    public Symbol symbolAt(int index) throws IndexOutOfBoundsException {
        if (this.getCircular()) index = RichLocation.Tools.modulateCircularIndex(index,this.length());
        return this.symList.symbolAt(index);
    }
    
    /**
     * {@inheritDoc}
     */
    public List toList() { return this.symList.toList();}
    
    /**
     * {@inheritDoc}
     */
    public String subStr(int start, int end) throws IndexOutOfBoundsException {
        return this.symList.subList(start, end).seqString();
    }
    
    /**
     * {@inheritDoc}
     */
    public SymbolList subList(int start, int end) throws IndexOutOfBoundsException {
        if (this.getCircular()) {
            try {
                int[] modLocation = RichLocation.Tools.modulateCircularLocation(start,end,this.length());
                int modStart = modLocation[0];
                int modEnd = modLocation[1];
                int modLength = modLocation[2];
                int seqLength = this.length();
                if (modStart==0) modStart = seqLength;
                if (modEnd==0) modEnd = seqLength;
                // Use the packed symbol factory
                ChunkedSymbolListFactory symsf = new ChunkedSymbolListFactory(new PackedSymbolListFactory());
                
                if (modEnd>seqLength) {
                    int remaining = modLength;
                    int chunkSize = (seqLength-modStart)+1;
                    //   add modStart -> seqLength
                    symsf.addSymbols(
                            this.getAlphabet(),
                            (Symbol[])this.symList.subList(modStart,seqLength).toList().toArray(new Symbol[0]),
                            0,
                            chunkSize);
                    remaining -= chunkSize;
                    //   repeat add seqLength
                    while (remaining > seqLength) {
                        chunkSize = seqLength;
                        symsf.addSymbols(
                                this.getAlphabet(),
                                (Symbol[])this.symList.subList(1,seqLength).toList().toArray(new Symbol[0]),
                                0,
                                chunkSize);
                        remaining -= chunkSize;
                    }
                    //   add 0 -> remaining
                    chunkSize = remaining;
                    symsf.addSymbols(
                            this.getAlphabet(),
                            (Symbol[])this.symList.subList(1,chunkSize).toList().toArray(new Symbol[0]),
                            0,
                            chunkSize);
                } else {
                    //   add modStart->modEnd
                    symsf.addSymbols(
                            this.getAlphabet(),
                            (Symbol[])this.symList.subList(modStart,modEnd).toList().toArray(new Symbol[0]),
                            0,
                            modLength);
                }
                
                return symsf.makeSymbolList();
            } catch (IllegalAlphabetException e) {
                throw new RuntimeException("Don't understand our own alphabet?",e);
            }
        } else return this.symList.subList(start, end);
    }
    
    /**
     * {@inheritDoc}
     */
    public String seqString() { return this.symList.seqString(); }
    
    /**
     * {@inheritDoc}
     */
    public int length() { return this.symList.length(); }
    
    /**
     * {@inheritDoc}
     */
    public Iterator iterator() { return this.symList.iterator(); }
    
    /**
     * {@inheritDoc}
     */
    public Alphabet getAlphabet() { return this.symList.getAlphabet(); }
    
    // Hibernate requirement - not for public use.
    private String alphaname;
    
    // Hibernate requirement - not for public use.
    private void setAlphabetName(String alphaname) throws IllegalSymbolException, BioException {
        this.alphaname = alphaname;
        this.checkMakeSequence();
    }
    
    // Hibernate requirement - not for public use.
    private String getAlphabetName() { return (this.symList==SymbolList.EMPTY_LIST?null:this.symList.getAlphabet().getName()); }
    
    // Hibernate requirement - not for public use.
    private String seqstring;
    
    // Hibernate requirement - not for public use.
    private void setStringSequence(String seq) throws IllegalSymbolException, BioException {
        this.seqstring = seq;
        this.checkMakeSequence();
    }
    
    // Hibernate requirement - not for public use.
    private String getStringSequence() { return (this.symList==SymbolList.EMPTY_LIST?null:this.seqString()); }
    
    // Hibernate requirement - not for public use.
    private void checkMakeSequence() throws IllegalSymbolException, BioException {
        if (this.alphaname!=null && this.seqstring!=null) {
            // Make the symbol list and assign it.
            Alphabet a = AlphabetManager.alphabetForName(this.alphaname);
            this.symList = new SimpleSymbolList(a.getTokenization("token"), seqstring);
        }
    }
    
    // Hibernate requirement - not for public use.
    private void setSequenceLength(int length) {
        // ignore - it's calculated anyway.
    }
    
    // Hibernate requirement - not for public use.
    private int getSequenceLength() { return this.length(); }
    
    /**
     * {@inheritDoc}
     */
    public String getURN() { return this.getName(); }
    
    /**
     * {@inheritDoc}
     */
    public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
        SimpleFeatureHolder fh = new SimpleFeatureHolder();
        for (Iterator i = this.features.iterator(); i.hasNext(); ) {
            Feature f = (RichFeature)i.next();
            try {
                if (fc.accept(f)) fh.addFeature(f);
            } catch (ChangeVetoException e) {
                throw new RuntimeException("What? You don't like our features??");
            }
        }
        return fh;
    }
    
    /**
     * {@inheritDoc}
     */
    public Feature createFeature(Feature.Template ft) throws BioException, ChangeVetoException {
        Feature f;
        try {
            f = new SimpleRichFeature(this,ft);
        } catch (InvalidTermException e) {
            throw new ChangeVetoException("They don't like our term",e);
        }
        if(!this.hasListeners(RichSequence.FEATURES)) {
            this.features.add(f);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichSequence.FEATURES,
                    f,
                    null
                    );
            ChangeSupport cs = this.getChangeSupport(RichSequence.FEATURES);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.features.add(f);
                cs.firePostChangeEvent(ce);
            }
        }
        return f;
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeFeature(Feature f) throws ChangeVetoException, BioException {
        if (!(f instanceof RichFeature)) f = RichFeature.Tools.enrich(f);
        if(!this.hasListeners(RichSequence.FEATURES)) {
            this.features.remove(f);
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichSequence.FEATURES,
                    null,
                    f
                    );
            ChangeSupport cs = this.getChangeSupport(RichSequence.FEATURES);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.features.remove(f);
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean containsFeature(Feature f) {
        try {
            if (!(f instanceof RichFeature)) f = RichFeature.Tools.enrich(f);
        } catch (ChangeVetoException e) {
            // We just can't tell!
            return false;
        }
        return this.features.contains(f);
    }
    
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
    public Set getFeatureSet() { return this.features; } // must be original for Hibernate
    
    /**
     * {@inheritDoc}
     */
    public void setFeatureSet(Set features) throws ChangeVetoException { this.features = features; } // must be original for Hibernate
    
    /**
     * {@inheritDoc}
     */
    public FeatureFilter getSchema() { return FeatureFilter.top_level;}
    
    /**
     * {@inheritDoc}
     */
    public Iterator features() { return this.getFeatureSet().iterator(); }
    
    /**
     * {@inheritDoc}
     */
    public int countFeatures() { return this.features.size(); }
}
