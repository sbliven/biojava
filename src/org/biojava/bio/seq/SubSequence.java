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
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.seq.projection.*;
import org.biojava.utils.*;

import java.util.*;

/**
 * View a sub-section of a given sequence object, including all the
 * features intersecting that region.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.2
 */

public class SubSequence implements Sequence {
    private Sequence parent;
    private SymbolList symbols;
    private String name;
    private String uri;
    private Annotation annotation;
    private int start;
    private int end;
    private transient FeatureHolder cachedFeatures;
    private final FeatureFilter projectff;
    private final boolean recurse;

    private transient ChangeSupport changeSupport;
    private transient ChangeListener seqListener;

    private void allocChangeSupport() {
	if (seqListener == null) {
	    installSeqListener();
	}
	changeSupport = new ChangeSupport();
    }

    private void installSeqListener() {
	seqListener = new ChangeListener() {
		public void preChange(ChangeEvent cev)
		    throws ChangeVetoException
		{
		    if (changeSupport != null) {
			changeSupport.firePreChangeEvent(makeChainedEvent(cev));
		    }
		}

		public void postChange(ChangeEvent cev) {
		    cachedFeatures = null;
		    if (changeSupport != null) {
			changeSupport.firePostChangeEvent(makeChainedEvent(cev));
		    }
		}

		private ChangeEvent makeChainedEvent(ChangeEvent cev) {
		    return new ChangeEvent(SubSequence.this,
					   FeatureHolder.FEATURES,
					   null, null,
					   cev);
		}
	    } ;
	parent.addChangeListener(seqListener, FeatureHolder.FEATURES);
    }

    /**
     * Construct a new SubSequence of the specified sequence.
     *
     * @param seq A sequence to view
     * @param start The start of the range to view
     * @param end The end of the range to view
     * @throws IndexOutOfBoundsException is the start or end position is illegal.
     */

    public SubSequence(Sequence seq,
		       final int start,
		       final int end) 
    {
        this(seq, start, end, null, false);
    }
    
    /**
     * Construct a new SubSequence of the specified sequence.
     *
     * @param seq A sequence to view
     * @param start The start of the range to view
     * @param end The end of the range to view
     * @param name Name for the subsequence
     * @throws IndexOutOfBoundsException is the start or end position is illegal.
     */
    public SubSequence(Sequence seq,
		       final int start,
		       final int end,
               final String name) 
    {
        this(seq, start, end, null, false, name);
    }

    /**
     * Construct a new SubSequence of the specified sequence.
     *
     * @param seq A sequence to view
     * @param start The start of the range to view
     * @param end The end of the range to view
     * @param ff A FeatureFilter to apply when cropping the sequence
     * @param recurse Recursion flag when filtering.
     * @throws IndexOutOfBoundsException is the start or end position is illegal.
     */

    public SubSequence(Sequence seq,
		       final int start,
		       final int end,
		       final FeatureFilter ff,
		       final boolean recurse) 
    {
        this(seq, start, end, ff, recurse, seq.getName() + " (" + start + " - " + end + ")");
    }
    
    /**
     * Construct a new SubSequence of the specified sequence with a new name.
     *
     * @param seq A sequence to view
     * @param start The start of the range to view
     * @param end The end of the range to view
     * @param ff A FeatureFilter to apply when cropping the sequence
     * @param recurse Recursion flag when filtering
     * @param name Name for the subsequence
     * @throws IndexOutOfBoundsException is the start or end position is illegal.
     */

    public SubSequence(Sequence seq,
		       final int start,
		       final int end,
		       final FeatureFilter ff,
		       final boolean recurse,
               final String name) 
    {
        this.parent = seq;
	this.start = start;
	this.end = end;

	symbols = seq.subList(start, end);
	this.name = name;
	uri = seq.getURN() + "?start=" + start + ";end=" + end;
	annotation = seq.getAnnotation();

	FeatureFilter locFilter = new FeatureFilter.OverlapsLocation(new RangeLocation(start, end));
	if (ff == null) {
	    this.projectff = locFilter;
	} else {
	    this.projectff = new FeatureFilter.And(ff, locFilter);
	}
	this.recurse = recurse;
    }

    //
    // SymbolList stuff
    //

    public Symbol symbolAt(int pos) {
	return symbols.symbolAt(pos);
    }

    public Alphabet getAlphabet() {
	return symbols.getAlphabet();
    }

    public SymbolList subList(int start, int end) {
	return symbols.subList(start, end);
    }

    public String seqString() {
	return symbols.seqString();
    }

    public String subStr(int start, int end) {
	return symbols.subStr(start, end);
    }

    public List toList() {
	return symbols.toList();
    }

    public int length() {
	return symbols.length();
    }

    public Iterator iterator() {
	return symbols.iterator();
    }

    public void edit(Edit edit)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't edit SubSequences");
    }

    //
    // Implements featureholder
    //

    public int countFeatures() {
	return getFeatures().countFeatures();
    }

    public Iterator features() {
	return getFeatures().features();
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	return getFeatures().filter(ff, recurse);
    }

    public boolean containsFeature(Feature f) {
	return getFeatures().containsFeature(f);
    }

    public Feature createFeature(Feature.Template templ)
        throws BioException, ChangeVetoException
    {
	return createFeatureTranslated(parent, templ);
    }

    public void removeFeature(Feature f) 
        throws ChangeVetoException
    {
	removeProjectedFeature(parent, f);
    }

    protected FeatureHolder getFeatures() {
	if (seqListener == null) {
	    installSeqListener();
	}

	if (cachedFeatures == null) {
	    cachedFeatures = new CroppedFeatureSet(parent, this);
	}
	return cachedFeatures;
    }

    //
    // Identifiable
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

    public Sequence getParent() {
      return this.parent;
    }

    //
    // Guts
    //

    private Feature createFeatureTranslated(FeatureHolder creatrix, Feature.Template templ) 
        throws BioException, ChangeVetoException
    {
	Location oldLoc = templ.location;
	templ.location = templ.location.translate(start - 1);
	try {
	    return creatrix.createFeature(templ);
	} finally {
	    templ.location = oldLoc;
	}
    }

    private void removeProjectedFeature(FeatureHolder parentFH, Feature f)
        throws ChangeVetoException
    {
	if (! (f instanceof Projection)) {
	    throw new ChangeVetoException("Can't remove feature -- doesn't appear to be an appropriate projection");
	}

	parentFH.removeFeature(((Projection) f).getViewedFeature());
    }

    private class CroppedFeatureSet extends LazyFeatureHolder {
	private FeatureHolder features;
	private FeatureHolder featureParent;
	private ChangeListener flusher;

	private CroppedFeatureSet(FeatureHolder features, FeatureHolder featureParent) {
	    this.features = features;
	    this.featureParent = featureParent;
	    this.flusher = new ChangeListener() {
		    public void preChange(ChangeEvent cev)
		        throws ChangeVetoException
		    {
			if (CroppedFeatureSet.this.hasListeners()) {
			    CroppedFeatureSet.this.getChangeSupport(cev.getType()).firePreChangeEvent(cev);
			}
		    }

		    public void postChange(ChangeEvent cev) {
			flushFeatures();
			if (CroppedFeatureSet.this.hasListeners()) {
			    CroppedFeatureSet.this.getChangeSupport(cev.getType()).firePostChangeEvent(cev);
			}
		    }
		} ;
	    features.addChangeListener(flusher, FeatureHolder.FEATURES);
	}

	protected FeatureHolder createFeatureHolder() {
	    ProjectionContext pc = new ProjectionContext() {
		public FeatureHolder getParent(Feature f) {
		    return featureParent;
		}

		public Sequence getSequence(Feature f) {
		    return SubSequence.this;
		}

		public Location getLocation(Feature f) {
		    return f.getLocation().translate(1 - start);
		}

		public StrandedFeature.Strand getStrand(StrandedFeature f) {
		    return f.getStrand();
		}

		public Annotation getAnnotation(Feature f) {
		    return f.getAnnotation();
		}

		public FeatureHolder projectChildFeatures(Feature f, FeatureHolder parent) {
		    // return ProjectedFeatureHolder.projectFeatureHolder(f, parent, 1 - start, false);
		    return new CroppedFeatureSet(f, parent);
		}

		public Feature createFeature(Feature f, Feature.Template templ)
		    throws ChangeVetoException, BioException
		{
		    return createFeatureTranslated(f, templ);
		}
		    
		public void removeFeature(Feature f, Feature f2)
		    throws ChangeVetoException
		{
		    removeProjectedFeature(f, f2);
		}
	    } ;

	    try {
		SimpleFeatureHolder results = new SimpleFeatureHolder();
		FeatureHolder rawFeatures = features.filter(projectff, recurse);
		for (Iterator i = rawFeatures.features(); i.hasNext(); ) {
		    final Feature f = (Feature) i.next();

		    Location l = f.getLocation();
		    if (l.getMin() >= start && l.getMax() <= end) {
			results.addFeature(ProjectionEngine.DEFAULT.projectFeature(f, pc));
		    } else {
			RemoteFeature.Template rft = new RemoteFeature.Template();
			rft.type = f.getType();
			rft.source = f.getSource();
			rft.annotation = f.getAnnotation();
			rft.location = LocationTools.intersection(l.translate(1 - start), 
								  new RangeLocation(1, end - start + 1));
			if (f instanceof StrandedFeature) {
			    rft.strand = ((StrandedFeature) f).getStrand();
			} else {
			    rft.strand = StrandedFeature.UNKNOWN;
			}
			rft.resolver = new RemoteFeature.Resolver() {
				public Feature resolve(RemoteFeature rFeat) {
				    return f;
				}
			    } ;
			rft.regions = Collections.nCopies(1, new RemoteFeature.Region(f.getLocation(), f.getSequence().getName()));
			
			results.addFeature(new SSRemoteFeature(SubSequence.this, featureParent, rft, f));
		    }
		}
		
		return results;
	    } catch (ChangeVetoException cve) {
		throw new BioError("Assertion failure: can't modify newly created feature holder");
	    }
	}
    }

    //
    // Simple RemoteFeature implementation
    // (this could be usefully replaced by a more specific RemoteFeature)
    //

    private class SSRemoteFeature extends SimpleRemoteFeature implements RemoteFeature {
	private FeatureHolder childFeatures;
	
	private SSRemoteFeature(Sequence seq,
				FeatureHolder parent,
				RemoteFeature.Template templ,
				Feature f) {
	    super(seq, parent, templ);
	    childFeatures = new CroppedFeatureSet(f, this);
	}

	public int countFeatures() {
	    return getFeatures().countFeatures();
	}

	public Iterator features() {
	    return getFeatures().features();
	}

	public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	    return getFeatures().filter(ff, recurse);
	}

	public boolean containsFeature(Feature f) {
	    return getFeatures().containsFeature(f);
	}
	
	public Feature createFeature(Feature.Template templ)
	    throws BioException, ChangeVetoException
	{
	    throw new ChangeVetoException("Can't create features on SubSequence");
	}
	
	public void removeFeature(Feature f) 
	    throws ChangeVetoException
	{
	    throw new ChangeVetoException("Can't remove features from SubSequence");
	}
	
	protected FeatureHolder getFeatures() {
	    return childFeatures;
	}
    }
    
    public void addChangeListener(ChangeListener cl, ChangeType ct) {
	if (changeSupport == null) {
	    allocChangeSupport();
	}
	changeSupport.addChangeListener(cl, ct);
    }
    
    public void addChangeListener(ChangeListener cl) {
	addChangeListener(cl, ChangeType.UNKNOWN);
    }

    public void removeChangeListener(ChangeListener cl, ChangeType ct) {
	if (changeSupport != null) {
	    changeSupport.removeChangeListener(cl, ct);
	}
    }

    public void removeChangeListener(ChangeListener cl) {
	removeChangeListener(cl, ChangeType.UNKNOWN);
    }
    
    public boolean isUnchanging(ChangeType ct) {
      return false;
    }
}
