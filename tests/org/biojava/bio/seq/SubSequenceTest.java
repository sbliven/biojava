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
 * @since 1.2
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
	assertEquals(subseq.countFeatures(), 1);
    }

    public void testFeatureProjection()
        throws Exception
    {
	Feature f = (Feature) subseq.features().next();
	Location fl = f.getLocation();
	assertEquals(fl.getMin(), 3);
	assertEquals(fl.getMax(), 5);
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
