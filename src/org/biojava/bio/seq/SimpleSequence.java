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

import java.lang.reflect.*;
import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;

/**
 * A basic implementation of the <code>Sequence</code> interface.
 * <p>
 * This class now implements all methods in the SymbolList
 * interface by delegating to another SymbolList object.  This
 * avoids unnecessary copying, but means that any changes in
 * the underlying SymbolList will be silently reflected in
 * the SimpleSequence.  In general, SimpleSequences should <em>only</em>
 * be constructed from SymbolLists which are known to be immutable.
 * <p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */
public class SimpleSequence implements Sequence, MutableFeatureHolder 
{
    //
    // This section is for the SymbolList implementation-by-delegation
    //

    /**
     * Delegate SymbolList.
     */

    private SymbolList symList;

    public Alphabet getAlphabet() {
	return symList.getAlphabet();
    }

    public Iterator iterator() {
	return symList.iterator();
    }

    public int length() {
	return symList.length();
    }

    public String seqString() {
	return symList.seqString();
    }

    public String subStr(int start, int end) {
	return symList.subStr(start, end);
    }

    public SymbolList subList(int start, int end) {
	return symList.subList(start, end);
    }

    public Symbol symbolAt(int indx) {
	return symList.symbolAt(indx);
    }

    public List toList() {
	return symList.toList();
    }

    //
    // Extra stuff which is unique to Sequences
    //
  
    private String urn;
    private String name;
    private Annotation annotation;
    private MutableFeatureHolder featureHolder;
    private FeatureRealizer featureRealizer;

    protected MutableFeatureHolder getFeatureHolder() {
	if(featureHolder == null)
	    featureHolder = new SimpleMutableFeatureHolder();
	return featureHolder;
    }
    
    protected boolean featureHolderAllocated() {
	return featureHolder != null;
    }

    public String getURN() {
	return urn;
    }

    public void setURN(String urn) {
	this.urn = urn;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public Annotation getAnnotation() {
	if(annotation == null)
	    annotation = new SimpleAnnotation();
	return annotation;
    }

    public int countFeatures() {
	if(featureHolderAllocated())
	    return getFeatureHolder().countFeatures();
	return 0;
    }

    public Iterator features() {
	if(featureHolderAllocated())
	    return getFeatureHolder().features();
	return Collections.EMPTY_LIST.iterator();
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	if(featureHolderAllocated())
	    return getFeatureHolder().filter(ff, recurse);
	return FeatureHolder.EMPTY_FEATURE_HOLDER;
    }

    public Feature createFeature(MutableFeatureHolder fh, Feature.Template template)
	throws BioException 
    {
	Feature f = featureRealizer.realizeFeature(this, template);
	if(fh == this) {
	    fh = this.getFeatureHolder();
	}
	fh.addFeature(f);
	return f;
    }

    public void addFeature(Feature f) {
	throw new UnsupportedOperationException();
    }

    public void removeFeature(Feature f) {
	featureHolder.removeFeature(f);
    }

    /**
     * Create a SimpleSequence with the symbols and alphabet of res, and the
     * sequence properties listed.
     *
     * @param res the SymbolList to wrap as a sequence
     * @param urn the URN
     * @param name the name - should be unique if practical
     * @param annotation the annotation object to use or null
     */
    public SimpleSequence(SymbolList res, String urn, String name, Annotation annotation) {
	symList = res;
	
	setURN(urn);
	setName(name);
	this.annotation = annotation;
	this.featureRealizer = SimpleFeatureRealizer.DEFAULT;
    }

    /**
     * Create a SimpleSequence using a specified FeatureRealizer.
     *
     * @param res the SymbolList to wrap as a sequence
     * @param urn the URN
     * @param name the name - should be unique if practical
     * @param annotation the annotation object to use or null
     * @param realizer the FeatureRealizer implemetation to use when adding features
     */
    public SimpleSequence(SymbolList res, 
			  String urn,
			  String name,
			  Annotation annotation,
			  FeatureRealizer realizer) 
    {
	symList = res;
	
	setURN(urn);
	setName(name);
	this.annotation = annotation;
	this.featureRealizer = realizer;
    }
}
