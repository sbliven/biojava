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

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.symbol.*;

/**
 * A Sequence which is assembled from other sequences contained
 * in a set of ComponentFeature objects.
 *
 * <p>
 * There is still some potential for optimising SymbolList
 * operations on this class.
 * </p>
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.1
 */

public class SimpleAssembly
    implements Sequence, RealizingFeatureHolder 
{
    protected transient ChangeSupport changeSupport = null;
    private String name;
    private String uri;
    private Annotation annotation;
    private SimpleFeatureHolder features;
    private AssembledSymbolList assembly;

    private FeatureRealizer featureRealizer = org.biojava.bio.seq.impl.FeatureImpl.DEFAULT;

    {
	assembly = new AssembledSymbolList();
	features = new SimpleFeatureHolder();
    }

    /**
     * Construct a new SimpleAssembly using the DNA alphabet.
     * Initially, the sequence will just contain a list of `N's.
     * Sequence data can be added by adding one or more
     * ComponentFeatures.
     *
     * @param length The length of the sequence
     * @param name The name of the sequence (returned by getName())
     * @param uri The identifier of the sequence (returned by getURN());
     */

    public SimpleAssembly(int length, String name, String uri) {
	this.name = name;
	this.uri = uri;
	assembly.setLength(length);
    }

    /**
     * Construct a new SimpleAssembly using the DNA alphabet.
     * Initially, the sequence will just contain a list of `N's.
     * Sequence data can be added by adding one or more
     * ComponentFeatures.
     *
     * @param name The name of the sequence (returned by getName())
     * @param uri The identifier of the sequence (returned by getURN());
     */

    public SimpleAssembly(String name, String uri) {
	this.name = name;
	this.uri = uri;
    }

    //
    // SymbolList
    //

    public Alphabet getAlphabet() {
	return assembly.getAlphabet();
    }

    public int length() {
	return assembly.length();
    }

    public Symbol symbolAt(int pos) {
	return assembly.symbolAt(pos);
    }

    public SymbolList subList(int start, int end) {
	return assembly.subList(start, end);
    }

    public String seqString() {
	return assembly.seqString();
    }

    public String subStr(int start, int end) {
	return assembly.subStr(start, end);
    }

    public Iterator iterator() {
	return assembly.iterator();
    }

    public List toList() {
	return assembly.toList();
    }

    public void edit(Edit e) 
        throws IllegalAlphabetException, ChangeVetoException
    {
	assembly.edit(e);
    }

    //
    // Sequence identification
    //

    public String getName() {
	return name;
    }

    public String getURN() {
	return uri;
    }

    //
    // Annotatable
    //

    public Annotation getAnnotation() {
	return annotation;
    }

    //
    // FeatureHolder
    //

    public Iterator features() {
	return features.features();
    }

    public int countFeatures() {
	return features.countFeatures();
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	return features.filter(ff, recurse);
    }
    
    public boolean containsFeature(Feature f) {
      return features.containsFeature(f);
    }

    public Feature createFeature(Feature.Template temp) 
        throws BioException, ChangeVetoException
    {
	if (temp.location.getMin() < 1)
	    throw new BioException("Coordinates out of range");

	if (temp instanceof ComponentFeature.Template) {
	    for (Iterator i = assembly.getComponentLocationSet().iterator(); i.hasNext(); ) {
		Location l = (Location) i.next();
		if (l.overlaps(temp.location))
		    throw new BioError("Can't create overlapping ComponentFeature");
	    }
	}

	Feature f = realizeFeature(this, temp);
	features.addFeature(f);
	if (f instanceof ComponentFeature) {
	    ComponentFeature cf = (ComponentFeature) f;
	    Location loc = cf.getLocation();
	    if (loc.getMax() > assembly.length()) {
		assembly.setLength(loc.getMax());
	    }
	    assembly.putComponent( cf);
	}
	return f;
    }

    public void removeFeature(Feature f)
    throws ChangeVetoException {
      if (f instanceof ComponentFeature) {
        assembly.removeComponent(f.getLocation());
      }
      features.removeFeature(f);
    }

    //
    // Feature realization
    //

    public Feature realizeFeature(FeatureHolder fh, Feature.Template temp) 
        throws BioException
    {
	if (temp instanceof ComponentFeature.Template) {
	    if (fh != this) {
		throw new BioException("ComponentFeatures can only be attached directly to SimpleAssembly objects");
	    }
	    ComponentFeature.Template cft = (ComponentFeature.Template) temp;
	    return new SimpleComponentFeature(this, cft);
	} else {
	    FeatureHolder gopher = fh;
	    while (gopher instanceof Feature) {
		if (gopher instanceof ComponentFeature) {
		    throw new BioException("Cannot [currently] realize features on components of SimpleAssemblies");
		}
		gopher = ((Feature) gopher).getParent();
	    }
	    return featureRealizer.realizeFeature(this, fh, temp);
	}
    }

    //
    // Changeable
    //

    public void addChangeListener(ChangeListener cl) {
	if(changeSupport == null) {
	    changeSupport = new ChangeSupport();
	}

	synchronized(changeSupport) {
	    changeSupport.addChangeListener(cl);
	}
    }

    public void addChangeListener(ChangeListener cl, ChangeType ct) {
	if(changeSupport == null) {
	    changeSupport = new ChangeSupport();
	}

	synchronized(changeSupport) {
	    changeSupport.addChangeListener(cl, ct);
	}
    }

    public void removeChangeListener(ChangeListener cl) {
	if(changeSupport != null) {
	    synchronized(changeSupport) {
		changeSupport.removeChangeListener(cl);
	    }
	}
    }

    public void removeChangeListener(ChangeListener cl, ChangeType ct) {
	if(changeSupport != null) {
	    synchronized(changeSupport) {
		changeSupport.removeChangeListener(cl, ct);
	    }
	}
    }
}
