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

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.impl.*;

/**
 * A view onto another Sequence object.  This class allows new
 * features and annotations to be overlaid onto an existing
 * Sequence without modifying it.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 */

public class ViewSequence implements Sequence, RealizingFeatureHolder, Serializable {
    private static final long serialVersionUID = 9866447;
    /**
     * Delegate Sequence.
     */
    private Sequence seqDelegate;

    /**
     * FeatureHolder support
     */
    private ViewSeqMergeFeatureHolder exposedFeatures;
    /**
     * For serialization support, all methods delegated to super
     */
    private class ViewSeqMergeFeatureHolder extends MergeFeatureHolder implements Serializable {
        private static final long serialVersionUID = 68065743;
    }


    private ViewSeqSimpleFeatureHolder addedFeatures;
    /**
     * For serialization support, all methods delegated to super
     */
    private class ViewSeqSimpleFeatureHolder extends SimpleFeatureHolder implements Serializable {
	private static final long serialVersionUID = 38475932;
    }

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
    private transient FeatureRealizer featureRealizer;

    private void readObject(ObjectInputStream s)throws IOException, ClassNotFoundException{
	s.defaultReadObject();
	this.featureRealizer = FeatureImpl.DEFAULT;
    }

    /**
     * Construct a view onto an existing sequence and give it a new
     * name.
     */
    public ViewSequence(Sequence seq, String name) {
        this.name = name;

	seqDelegate = seq;
	addedFeatures = new ViewSeqSimpleFeatureHolder();
	exposedFeatures = new ViewSeqMergeFeatureHolder();
	try {
	    exposedFeatures.addFeatureHolder(new ProjectedFeatureHolder(seqDelegate, this, 0, false));
	    exposedFeatures.addFeatureHolder(addedFeatures);
	} catch (ChangeVetoException cve) {
	    throw new BioError(cve, "Modification of hidden featureholder vetoed!");
	}

	urn = seqDelegate.getURN();
	if (urn.indexOf('?') >= 0)
	    urn = urn + "&view=" + hashCode();
	else
	    urn = urn + "?view=" + hashCode();

	anno = new OverlayAnnotation(seqDelegate.getAnnotation());
	
	featureRealizer = FeatureImpl.DEFAULT;
    }

    /**
     * Construct a view onto an existing sequence which takes on that
     * sequence's name.
     */
    public ViewSequence(Sequence seq) {
        this(seq, seq.getName());
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

    /**
     * Remove a feature from this sequence.  <strong>NOTE:</strong> This
     * method will only succeed for features which were added to this
     * ViewSequence.  Trying to remove a Feature from the underlying
     * sequence will cause an IllegalArgumentException.  I think this
     * is the correct behaviour.
     */

    public void removeFeature(Feature f)
    throws ChangeVetoException {
      addedFeatures.removeFeature(f);
    }

    public boolean containsFeature(Feature f) {
      return exposedFeatures.containsFeature(f);
    }
    
    //
    // Get our annotation
    //

    public Annotation getAnnotation() {
	return anno;
    }

    //
    // Feature realization stuff
    //

    public Feature realizeFeature(FeatureHolder parent, Feature.Template template)
        throws BioException
    {
	return featureRealizer.realizeFeature(this, parent, template);
    }

    public Feature createFeature(Feature.Template template)
        throws BioException, ChangeVetoException
    {
      Location loc = template.location;
      if(loc.getMin() < 1 || loc.getMax() > this.length()) {
          throw new BioException("Failed to create a feature with a location "
                                 + loc
                                 + " outside the sequence: name '"
                                 + getName()
                                 + "', URN '"
                                 + getURN()
                                 + "' length "
                                 + length());
      }
      Feature f = realizeFeature(this, template);
      addedFeatures.addFeature(f);
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

    public FeatureHolder getAddedFeatures() {
	return addedFeatures;
    }
 
  public void edit(Edit edit) throws ChangeVetoException {
    throw new ChangeVetoException("ViewSequence is immutable");
  }
  
    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}    
}
