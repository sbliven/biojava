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

package org.biojava.bio.seq;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

import java.util.*;
import java.lang.reflect.*;

/**
 * A view onto another Sequence object.  This class allows new
 * features and annotations to be overlayed onto an existing
 * Sequence without modifying it.
 *
 * <p>This currently uses feature realization code cut and
 * pasted from SimpleSequence.  We need to sort out feature
 * realization a bit better...</p>
 *
 * @author Thomas Down
 */

public class ViewSequence implements Sequence, MutableFeatureHolder {
    /**
     * Delegate Sequence.
     */
    
    private Sequence seqDelegate;

    /**
     * FeatureHolder support
     */

    private MergeFeatureHolder exposedFeatures;
    private MutableFeatureHolder addedFeatures;

    /**
     * IDs
     */

    private String name;
    private String urn;

    /**
     * Our annotation.
     */

    private Annotation anno;

    /**
     * The FeatureRealizer we use.
     */

    private FeatureRealizer featureRealizer;

    /**
     * Construct a view onto an existing sequence.
     */

    public ViewSequence(Sequence seq) {
	seqDelegate = seq;
	addedFeatures = new SimpleMutableFeatureHolder();
	exposedFeatures = new MergeFeatureHolder();
	exposedFeatures.addFeatureHolder(seqDelegate);
	exposedFeatures.addFeatureHolder(addedFeatures);

	name = seqDelegate.getName();  // Is this sensible?
	urn = seqDelegate.getURN();
	if (urn.indexOf('?') >= 0)
	    urn = urn + "&view=" + hashCode();
	else
	    urn = urn + "?view=" + hashCode();

	anno = new OverlayAnnotation(seqDelegate.getAnnotation());
	
	featureRealizer = SimpleFeatureRealizer.DEFAULT;
    }

    /**
     * Construct a view onto a sequence, using a specific FeatureRealizer
     */

    public ViewSequence(Sequence seq, FeatureRealizer fr) {
	this(seq);
	this.featureRealizer = fr;
    }

    //
    // We implement SymbolList by delegation
    //

    public Alphabet getAlphabet() {
	return seqDelegate.getAlphabet();
    }

    public Iterator iterator() {
	return seqDelegate.iterator();
    }

    public int length() {
	return seqDelegate.length();
    }

    public String seqString() {
	return seqDelegate.seqString();
    }

    public String subStr(int start, int end) {
	return seqDelegate.subStr(start, end);
    }

    public SymbolList subList(int start, int end) {
	return seqDelegate.subList(start, end);
    }

    public Symbol symbolAt(int indx) {
	return seqDelegate.symbolAt(indx);
    }

    public List toList() {
	return seqDelegate.toList();
    }

    //
    // ID methods -- we have our own.
    //

    public String getURN() {
	return urn;
    }

    public String getName() {
	return name;
    }

    //
    // Basic FeatureHolder methods -- delegate to exposedFeatures
    //

    public int countFeatures() {
	return exposedFeatures.countFeatures();
    }

    public Iterator features() {
	return exposedFeatures.features();
    }

    public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
	return exposedFeatures.filter(fc, recurse);
    }

    //
    // MutableFeatureHolder methods -- delegate to addedFeatures
    //

    public void addFeature(Feature f) {
	addedFeatures.addFeature(f);
    }

    /**
     * Remove a feature from this sequence.  <strong>NOTE:</strong> This
     * method will only succeed for features which were added to this
     * ViewSequence.  Trying to remove a Feature from the underlying
     * sequence will cause an IllegalArgumentException.  I think this
     * is the correct behaviour.
     */

    public void removeFeature(Feature f) {
	addedFeatures.removeFeature(f);
    }

    //
    // Get our annotation
    //

    public Annotation getAnnotation() {
	return anno;
    }

    //
    // Feature realization stuff, C+P from SimpleSequence.
    //

    public Feature createFeature(MutableFeatureHolder fh, Feature.Template template)
	throws BioException 
    {
	if (fh != this) {
	    if (! (fh instanceof Feature))
		throw new BioException("fh must be the ListSequence or one of its features.");
	    if (! (containsRecurse(addedFeatures, (Feature) fh)))
		throw new BioException("fh is not a child which has been added to this ListSequence");
	}
	Feature f = featureRealizer.realizeFeature(this, template);
	fh.addFeature(f);
	return f;
    }

    private static boolean containsRecurse(FeatureHolder fh, Feature f) {
	for (Iterator i = fh.features(); i.hasNext(); ) {
	    if (i.next() == f)
		return true;
	}
	
	for (Iterator i = fh.features(); i.hasNext(); ) {
	    if (containsRecurse((FeatureHolder) i.next(), f))
		return true;
	}
	return false;
    }
}
