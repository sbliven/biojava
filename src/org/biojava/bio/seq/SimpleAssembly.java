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

public class SimpleAssembly extends AbstractSymbolList 
    implements Sequence, RealizingFeatureHolder 
{
    private int length = 0;
    private String name;
    private String uri;
    private Annotation annotation;
    private SimpleFeatureHolder features;

    private SortedMap components;
    private List componentList;

    private FeatureRealizer featureRealizer = org.biojava.bio.seq.impl.FeatureImpl.DEFAULT;

    private final static Symbol N;

    static {
	try {
	    N = DNATools.getDNA().getParser("token").parseToken("n");
	} catch (BioException ex) {
	    throw new BioError(ex);
	}
    }

    {
	features = new SimpleFeatureHolder();
	components = new TreeMap(Location.naturalOrder);
	componentList = new ArrayList();
    }

    private void putComponent(Location loc, SymbolList symbols) {
	components.put(loc, symbols);
	componentList.clear();
	componentList.addAll(components.keySet());
    }

    private void removeComponent(Location loc) {
	components.remove(loc);
	componentList.clear();
	componentList.addAll(components.keySet());
    }

    /**
     * Find the location containing p in a sorted list of non-overlapping contiguous
     * locations.
     */

    private Location locationOfPoint(int p) {
	int first = 0;
	int last = componentList.size() - 1;
	
	while (first <= last) {
	    int check = (first + last) / 2;
	    Location checkL = (Location) componentList.get(check);
	    if (checkL.contains(p))
		return checkL;

	    if (p < checkL.getMin())
		last = check - 1;
	    else
		first = check + 1;
	}
	
	return null;
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
	this.length = length;
	this.name = name;
	this.uri = uri;
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
	return DNATools.getDNA();
    }

    public int length() {
	return length;
    }

    public Symbol symbolAt(int pos) {
	Location l = locationOfPoint(pos);
	if (l != null) {
	    SymbolList syms = (SymbolList) components.get(l);
	    return syms.symbolAt(pos - l.getMin() + 1);
	}

	return N;
    }

    public SymbolList subList(int start, int end) {
	Location l = locationOfPoint(start);
	if (l.contains(end)) {
	    SymbolList symbols = (SymbolList) components.get(l);
	    int tstart = start - l.getMin() + 1;
	    int tend = end - l.getMin() + 1;
	    return symbols.subList(tstart, tend);
	}

	// All is lost.  Fall back onto `view' subList from AbstractSymbolList

	return super.subList(start, end);
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
	    for (Iterator i = components.keySet().iterator(); i.hasNext(); ) {
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
	    SymbolList cfsyms = cf.getSymbols();
	    putComponent(loc, cfsyms);
	    length = Math.max(length, loc.getMax());
	}
	return f;
    }

    public void removeFeature(Feature f)
    throws ChangeVetoException {
      if (f instanceof ComponentFeature) {
        removeComponent(f.getLocation());
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
}
