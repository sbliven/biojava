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

package org.biojava.bio.seq.distributed;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;

/**
 * Sequence from the meta-DAS system.
 *
 * @author Thomas Down
 * @since 1.2
 */

class DistributedSequence implements Sequence{
    private DistributedSequenceDB db;
    private DistDataSource seqSource;
    private Set featureSources;
    private String id;

    private SymbolList symbols;
    private MergeFeatureHolder mfh;

    DistributedSequence(String id,
			DistributedSequenceDB db,
			DistDataSource seqSource,
			Set featureSources)
    {
	this.id = id;
	this.seqSource = seqSource;
	this.featureSources = featureSources;
	this.db = db;
    }

    DistributedSequenceDB getSequenceDB() {
	return db;
    }

    public String getName() {
	return id;
    }

    public String getURN() {
	return id;
    }

    public Annotation getAnnotation() {
	return Annotation.EMPTY_ANNOTATION;
    }
    

    public Alphabet getAlphabet() {
	return getSymbols().getAlphabet();
    }

    public int length() {
	return getSymbols().length();
    }

    public Symbol symbolAt(int i) {
	return getSymbols().symbolAt(i);
    }

    public SymbolList subList(int start, int end) {
	return getSymbols().subList(start, end);
    }

    public List toList() {
	return getSymbols().toList();
    }

    public Iterator iterator() {
	return getSymbols().iterator();
    }

    public String seqString() {
	return getSymbols().seqString();
    }

    public String subStr(int start, int end) {
	return getSymbols().subStr(start, end);
    }

    public void edit(Edit e) 
        throws ChangeVetoException 
    {
	throw new ChangeVetoException("Can't edit sequence in EGADS -- or at least not yet...");
    }

    public Iterator features() {
	return getFeatures().features();
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	return getFeatures().filter(ff, recurse);
    }

    public void removeFeature(Feature f) 
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't edit sequence in EGADS -- or at least not yet...");
    }

    public Feature createFeature(Feature.Template f)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't edit sequence in EGADS -- or at least not yet...");
    }

    public boolean containsFeature(Feature f) {
	return getFeatures().containsFeature(f);
    }

    public int countFeatures() {
	return getFeatures().countFeatures();
    }

    protected SymbolList getSymbols() {
	if (symbols == null) {
	    try {
		symbols = seqSource.getSequence(id);
	    } catch (BioException ex) {
		throw new BioRuntimeException(ex);
	    }
	}

	return symbols;
    }

    protected FeatureHolder getFeatures() {
	if (mfh == null) {
	    mfh = new MergeFeatureHolder();
	    for (Iterator i = featureSources.iterator(); i.hasNext(); ) {
		DistDataSource dds = (DistDataSource) i.next();
		try {
		    Annotation ann = new SmallAnnotation();
		    ann.setProperty("source", dds);
		    mfh.addFeatureHolder(new DistProjectedFeatureHolder(dds.getFeatures(id, FeatureFilter.all, false),
									this, ann));
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	    }
	}

	return mfh;
    }
	

    // 
    // Changeable stuff (which we'll cheat on for now...)
    //

    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
}
