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

package org.biojava.bio.seq.impl;

import java.lang.reflect.*;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * A basic implementation of the <code>Sequence</code> interface.
 * <p>
 * This class now implements all methods in the SymbolList
 * interface by delegating to another SymbolList object.  This
 * avoids unnecessary copying, but means that any changes in
 * the underlying SymbolList will be silently reflected in
 * the SimpleSequence.  In general, SimpleSequences should <em>only</em>
 * be constructed from SymbolLists which are known to be immutable.
 * </p>
 *
 * <p>By default, features attached to a SimpleSequence are 
 * realized as simple in-memory implementations using
 * <code>SimpleFeatureRealizer.DEFAULT</code>.  If you need
 * alternative feature realization behaviour, any
 * <code>FeatureRealizer</code> implementation may be
 * supplied at construction-time.</p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */
public class SimpleSequence implements Sequence, RealizingFeatureHolder
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

    public Symbol symbolAt(int index) {
	return symList.symbolAt(index);
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
    private SimpleFeatureHolder featureHolder;
    private FeatureRealizer featureRealizer;

    protected SimpleFeatureHolder getFeatureHolder() {
	if(featureHolder == null)
	    featureHolder = new SimpleFeatureHolder();
	return featureHolder;
    }
    
    protected boolean featureHolderAllocated() {
	return featureHolder != null;
    }

    public String getURN() {
	return urn;
    }
    
    /**
    *Provide the URN for this sequence
    */
    
    public void setURN(String urn) {
	this.urn = urn;
    }

    public String getName() {
	return name;
    }
    
    /**
    *Assign a name to this sequence
    */
    
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

    public Feature realizeFeature(FeatureHolder parent, Feature.Template template)
        throws BioException
    {
	return featureRealizer.realizeFeature(this, parent, template);
    }

    public Feature createFeature(Feature.Template template)
	throws BioException, ChangeVetoException 
    {
	Feature f = realizeFeature(this, template);
	SimpleFeatureHolder fh = this.getFeatureHolder();
	fh.addFeature(f);
	return f;
    }

    /**
     * Create a new feature in any FeatureHolder associated
     * with this sequence.
     *
     * @deprecated Please use new 1-arg createFeature instead.
     */

    public Feature createFeature(FeatureHolder fh, Feature.Template template)
	throws BioException, ChangeVetoException 
    {
	return fh.createFeature(template);
    }

    /**
     * Remove a feature attached to this sequence.
     */

    public void removeFeature(Feature f)
    throws ChangeVetoException {
      getFeatureHolder().removeFeature(f);
    }

    public void edit(Edit edit) throws ChangeVetoException {
      throw new ChangeVetoException("Can't edit the underlying SymbolList");
    }
   
    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}    
    
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
	this.featureRealizer = FeatureImpl.DEFAULT;
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
