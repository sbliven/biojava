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
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.impl.*;
import junit.framework.TestCase;

/**
 * Tests for SimpleAssembly.  By dependancy, this also
 * tests ProjectedFeatureHolder and SimpleAssembly.
 *
 * @author Thomas Down
 * @since 1.3
 */

public class SubSequenceTest extends TestCase
{
    protected Sequence seq;
    protected Sequence subseq;

    public SubSequenceTest(String name) {
	super(name);
    }

    protected void setUp() throws Exception {
	seq = new SimpleSequence(DNATools.createDNA("aacgtaggttccatgc"),
				       "fragment1",
				       "fragment1",
				       Annotation.EMPTY_ANNOTATION);
	
	Feature.Template sft = new Feature.Template();
	sft.type = "test";
	sft.source = "test";
	sft.annotation = Annotation.EMPTY_ANNOTATION;
	sft.location = new RangeLocation(1, 3);
	seq.createFeature(sft);

	sft.location = new RangeLocation(10, 12);
	seq.createFeature(sft);

	sft.location = new RangeLocation(5, 13);
	Feature choppedFeature = seq.createFeature(sft);

	sft.location = new RangeLocation(5,6);
	choppedFeature.createFeature(sft);
	
	sft.location = new RangeLocation(9,10);
	choppedFeature.createFeature(sft);

	subseq = new SubSequence(seq, 8, 14);
    }

    public void testSymbols()
	throws Exception
    {
	assertTrue(compareSymbolList(subseq,
				     DNATools.createDNA("gttccat")));
    }

    public void testFeatureClipping()
        throws Exception
    {
	assertEquals(subseq.countFeatures(), 2);
    }

    public void testFeatureProjection()
        throws Exception
    {
	Feature f = (Feature) subseq.filter(new FeatureFilter.Not(new FeatureFilter.ByClass(RemoteFeature.class)), false).features().next();
	Location fl = f.getLocation();
	assertEquals(fl.getMin(), 3);
	assertEquals(fl.getMax(), 5);
    }

    public void testRemoteFeature()
        throws Exception
    {
	RemoteFeature f = (RemoteFeature) subseq.filter(new FeatureFilter.ByClass(RemoteFeature.class), false).features().next();
	Location fl = f.getLocation();
	assertEquals(fl.getMin(), 1);
	assertEquals(fl.getMax(), 6);
	assertEquals(f.getRemoteFeature().getSequence().getName(), seq.getName());
    }

    public void testRemoteChildFeature()
        throws Exception
    {
	Feature f = (RemoteFeature) subseq.filter(new FeatureFilter.ByClass(RemoteFeature.class), false).features().next();
	assertEquals(f.countFeatures(), 1);
	
	Feature cf = (Feature) f.features().next();
	Location cfl = cf.getLocation();
	assertEquals(cfl.getMin(), 2);
	assertEquals(cfl.getMax(), 3);
    }

    public void testCreateOnSubsequence()
        throws Exception
    {
	Feature.Template templ = new Feature.Template();
	templ.type = "create_on_subsequence";
	templ.source = "test";
	templ.location = new RangeLocation(2, 3);
	templ.annotation = Annotation.EMPTY_ANNOTATION;
	subseq.createFeature(templ);
	
	Feature f = (Feature) seq.filter(new FeatureFilter.ByType("create_on_subsequence"), false).features().next();
	Location fl = f.getLocation();
	assertEquals(fl.getMin(), 9);
	assertEquals(fl.getMax(), 10);
    }

    public void testCreateOnSubsequenceFeature()
        throws Exception
    {
	Feature.Template templ = new Feature.Template();
	templ.type = "create_on_subsequence_feature";
	templ.source = "test";
	templ.location = new RangeLocation(3, 4);
	templ.annotation = Annotation.EMPTY_ANNOTATION;

	Feature subf = (Feature) subseq.filter(new FeatureFilter.Not(new FeatureFilter.ByClass(RemoteFeature.class)), false).features().next();
	subf.createFeature(templ);

	Feature f = (Feature) seq.filter(new FeatureFilter.ByType("create_on_subsequence_feature"), true).features().next();
	Location fl = f.getLocation();
	assertEquals(fl.getMin(), 10);
	assertEquals(fl.getMax(), 11);
    }

    public void testRemoveFeatureFromSubsequence()
        throws Exception
    {
	FeatureHolder fh = subseq.filter(new FeatureFilter.Not(new FeatureFilter.ByClass(RemoteFeature.class)), false);
	assertEquals(fh.countFeatures(), 1);
	Feature f = (Feature) fh.features().next();
	subseq.removeFeature(f);

	fh = subseq.filter(new FeatureFilter.Not(new FeatureFilter.ByClass(RemoteFeature.class)), false);
	assertEquals(fh.countFeatures(), 0);
    }

    private boolean compareSymbolList(SymbolList sl1, SymbolList sl2) {
	if (sl1.length() != sl2.length()) {
	    return false;
	}
	
	Iterator si1 = sl1.iterator();
	Iterator si2 = sl2.iterator();
	while (si1.hasNext()) {
	    if (! (si1.next() == si2.next())) {
		return false;
	    }
	}

	return true;
    }
}
