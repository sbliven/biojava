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

import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.genomic.*;
import junit.framework.TestCase;

/**
 * Tests for FeatureFilters.  Currently concentrating on
 * properSubset and disjunction.
 *
 * @author Thomas Down
 * @since 1.2
 */
public class FilterUtilsTest extends TestCase
{
    protected FeatureFilter tf1;
    protected FeatureFilter tf2;
    protected FeatureFilter tf3;

    protected FeatureFilter pf1;
    protected FeatureFilter pf2;
    protected FeatureFilter pf3;
   
    protected FeatureFilter cf_StrandedFeature;
    protected FeatureFilter cf_ComponentFeature;
    protected FeatureFilter cf_SpliceVariant;

    protected FeatureFilter ntf1;
    protected FeatureFilter ntf2;

    protected FeatureFilter olf1;
    protected FeatureFilter olf2;
    protected FeatureFilter olf3;
    protected FeatureFilter olf4;
    
    protected FeatureFilter clf1;
    protected FeatureFilter clf2;
    protected FeatureFilter clf3;
    protected FeatureFilter clf4;

    protected FeatureFilter tf1_or_tf2;
    protected FeatureFilter tf2_or_tf3;
    protected FeatureFilter tf1_or_tf3;
    protected FeatureFilter tf1_or_tf2_or_tf3;

    protected FeatureFilter pf1_and_pf2;
    protected FeatureFilter pf2_and_pf3;
    protected FeatureFilter pf1_and_pf3;
    protected FeatureFilter pf1_and_pf2_and_pf3;

    protected FeatureFilter pf1_and_tf1;
    protected FeatureFilter pf1_and_pf2_and_tf1;

    protected FeatureFilter pf1_AND_tf1_or_tf2;
    protected FeatureFilter pf1_and_pf2_OR_tf1;

    public FilterUtilsTest(String name) {
	super(name);
    }

    protected void setUp() throws Exception {
	//
	// Three type filters (opaque but mutually disjoint).
	//

	tf1 = new FeatureFilter.ByType("hello");
	tf2 = new FeatureFilter.ByType("goodbye");
	tf3 = new FeatureFilter.ByType("moo");

	//
	// Three annotation-property filters (opaque, non-disjoint)
	//

	pf1 = new FeatureFilter.HasAnnotation("foo");
	pf2 = new FeatureFilter.HasAnnotation("bar");
	pf3 = new FeatureFilter.HasAnnotation("baz");

	//
	// Class filters
	//
	
	cf_StrandedFeature = new FeatureFilter.ByClass(StrandedFeature.class);
	cf_ComponentFeature = new FeatureFilter.ByClass(ComponentFeature.class);
	cf_SpliceVariant = new FeatureFilter.ByClass(SpliceVariant.class);

	//
	// NOTed filters.
	//

	ntf1 = new FeatureFilter.Not(tf1);
	ntf2 = new FeatureFilter.Not(tf2);

	Location l1 = new RangeLocation(1000, 2000);
	Location l2 = new RangeLocation(10000, 11000);
	Location l3 = new PointLocation(10500);
	Location l4 = new RangeLocation(10700, 11700);

	olf1 = new FeatureFilter.OverlapsLocation(l1);
	olf2 = new FeatureFilter.OverlapsLocation(l2);
	olf3 = new FeatureFilter.OverlapsLocation(l3);
	olf4 = new FeatureFilter.OverlapsLocation(l4);

	clf1 = new FeatureFilter.ContainedByLocation(l1);
	clf2 = new FeatureFilter.ContainedByLocation(l2);
	clf3 = new FeatureFilter.ContainedByLocation(l3);
	clf4 = new FeatureFilter.ContainedByLocation(l4);

	tf1_or_tf2 = new FeatureFilter.Or(tf1, tf2);
	tf2_or_tf3 = new FeatureFilter.Or(tf2, tf3);
	tf1_or_tf3 = new FeatureFilter.Or(tf1, tf3);
	tf1_or_tf2_or_tf3 = new FeatureFilter.Or(tf1_or_tf2, tf3);

	pf1_and_pf2 = new FeatureFilter.And(pf1, pf2);
	pf2_and_pf3 = new FeatureFilter.And(pf2, pf3);
	pf1_and_pf3 = new FeatureFilter.And(pf1, pf3);
	pf1_and_pf2_and_pf3 = new FeatureFilter.And(pf1_and_pf2, pf3);
	pf1_and_tf1 = new FeatureFilter.And(pf1, tf1);
	pf1_and_pf2_and_tf1 = new FeatureFilter.And(pf1_and_pf2, tf1);

	pf1_AND_tf1_or_tf2 = new FeatureFilter.And(pf1, tf1_or_tf2);
	pf1_and_pf2_OR_tf1 = new FeatureFilter.Or(pf1_and_pf2, tf1);
    }

    public void testTypes() throws Exception {
	assertTrue(! FilterUtils.areProperSubset(tf1, tf2));
	assertTrue(FilterUtils.areProperSubset(tf1, tf1));

	assertTrue(FilterUtils.areDisjoint(tf1, tf2));
	assertTrue(! FilterUtils.areDisjoint(tf1, tf1));
    }

    public void testByClass() throws Exception {
	assertTrue(FilterUtils.areProperSubset(cf_SpliceVariant, cf_StrandedFeature));
	assertTrue(FilterUtils.areProperSubset(cf_ComponentFeature, cf_StrandedFeature));
	assertTrue(FilterUtils.areDisjoint(cf_ComponentFeature, cf_SpliceVariant));
    }

    public void testLocation() throws Exception {
	// Simple comparison of locations.

	assertTrue(FilterUtils.areProperSubset(olf1, olf1));
	assertTrue(! FilterUtils.areProperSubset(olf1, olf2));
	assertTrue(! FilterUtils.areDisjoint(olf1, olf1));
	
	// Assymetry between containment and overlapping

	assertTrue(FilterUtils.areProperSubset(clf1, olf1));
	assertTrue(! FilterUtils.areProperSubset(olf1, clf1));
    }

    public void testNot() throws Exception {
	assertTrue(FilterUtils.areProperSubset(ntf1, ntf1));
	assertTrue(! FilterUtils.areDisjoint(ntf1, ntf1));

	assertTrue(FilterUtils.areDisjoint(tf1, ntf1));
	assertTrue(! FilterUtils.areProperSubset(tf1, ntf1));

	assertTrue(! FilterUtils.areDisjoint(tf2, ntf1));
	assertTrue(FilterUtils.areProperSubset(tf2, ntf1));
    }

    public void testOr() throws Exception {
	assertTrue(FilterUtils.areProperSubset(tf1, tf1_or_tf2));
	assertTrue(! FilterUtils.areDisjoint(tf1, tf1_or_tf2));
	assertTrue(FilterUtils.areDisjoint(tf3, tf1_or_tf2));
	
	assertTrue(! FilterUtils.areProperSubset(tf1_or_tf2,
						 tf2_or_tf3));
	assertTrue(! FilterUtils.areDisjoint(tf1_or_tf2,
					     tf2_or_tf3));

	assertTrue(FilterUtils.areProperSubset(tf1_or_tf2,
					       tf1_or_tf2_or_tf3));
	assertTrue(FilterUtils.areProperSubset(tf2_or_tf3,
					       tf1_or_tf2_or_tf3));
	assertTrue(FilterUtils.areProperSubset(tf1_or_tf3,
					       tf1_or_tf2_or_tf3));
    }

    public void testAnd() throws Exception {
	assertTrue(FilterUtils.areProperSubset(pf1_and_pf2, pf1));
	assertTrue(! FilterUtils.areDisjoint(pf1_and_pf2, pf1));
	assertTrue(FilterUtils.areProperSubset(pf1_and_pf2, pf2));
	assertTrue(! FilterUtils.areProperSubset(pf1_and_pf2, pf3));
	assertTrue(! FilterUtils.areProperSubset(pf2_and_pf3, pf1_and_pf2));
	assertTrue(FilterUtils.areProperSubset(pf1_and_pf2_and_pf3, pf1_and_pf2));
	assertTrue(FilterUtils.areProperSubset(pf1_and_pf2_and_pf3, pf2_and_pf3));

	assertTrue(FilterUtils.areProperSubset(pf1_and_pf2_and_tf1, pf1_and_pf2));
	assertTrue(FilterUtils.areProperSubset(pf1_and_pf2_and_tf1, pf1_and_tf1));

	assertTrue(FilterUtils.areDisjoint(pf1_and_tf1, tf2));
	assertTrue(FilterUtils.areDisjoint(pf1_and_pf2_and_tf1, tf2));	   
    }

    public void testAndOr() throws Exception {
	assertTrue(FilterUtils.areProperSubset(pf1_and_tf1, pf1_AND_tf1_or_tf2));
	assertTrue(FilterUtils.areProperSubset(pf1_and_pf2_and_tf1, pf1_and_pf2_OR_tf1));
    }
}
