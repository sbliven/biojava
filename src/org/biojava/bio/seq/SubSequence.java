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
import org.biojava.utils.*;

import java.util.*;

/**
 * A subsection of a given sequence, with projected features.
 */

public class SubSequence implements Sequence {
    private SymbolList symbols;
    private FeatureHolder features;
    private String name;
    private String uri;
    private Annotation annotation;

    public SubSequence(Sequence seq, int start, int end) {
	symbols = seq.subList(start, end);
	FeatureFilter overlapping = new FeatureFilter.OverlapsLocation(new RangeLocation(start, end));
	features = new ProjectedFeatureHolder(seq,
					      overlapping,
					      this,
					      1 - start,
					      false);

	name = seq.getName() + " (" + start + " - " + end + ")";
	uri = seq.getURN() + "?start=" + start + ";end=" + end;
	annotation = seq.getAnnotation();
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
    // FeatureHolder stuff
    //

    public int countFeatures() {
	return features.countFeatures();
    }

    public Iterator features() {
	return features.features();
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
	return features.filter(ff, recurse);
    }

    public Feature createFeature(Feature.Template templ)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't add features to subsequences");
    }

    public void removeFeature(Feature f)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("Can't remove features from subsequences");
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

    //
    // Changeable
    //

    protected transient ChangeSupport changeSupport;
    private transient ChangeListener forwarder;

    protected void allocChangeSupport() {
	forwarder = new ChangeListener() {
		public void preChange(ChangeEvent ev)
		    throws ChangeVetoException
		{
		    if (changeSupport != null) {
			changeSupport.firePreChangeEvent(new ChangeEvent(this,
									 ev.getType(),
									 ev.getChange(),
									 ev.getPrevious(),
									 ev));
		    }
		}
		
		public void postChange(ChangeEvent ev) {
		    if (changeSupport != null) {
			changeSupport.firePostChangeEvent(new ChangeEvent(this,
									  ev.getType(),
									  ev.getChange(),
									  ev.getPrevious(),
									  ev));
		    }
		}
	    } ;
	symbols.addChangeListener(forwarder);
	features.addChangeListener(forwarder);
    }

    public void addChangeListener(ChangeListener cl) {
	if (changeSupport == null)
	    allocChangeSupport();
	changeSupport.addChangeListener(cl);
    }

    public void addChangeListener(ChangeListener cl, ChangeType ct) {
	if (changeSupport == null)
	    allocChangeSupport();
	changeSupport.addChangeListener(cl, ct);
    }

    public void removeChangeListener(ChangeListener cl) {
	if (changeSupport != null) {
	    changeSupport.removeChangeListener(cl);
	}
    }


    public void removeChangeListener(ChangeListener cl, ChangeType ct) {
	if (changeSupport != null) {
	    changeSupport.removeChangeListener(cl, ct);
	}
    }
    
}
