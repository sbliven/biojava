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
import org.biojava.bio.seq.impl.*;
import junit.framework.TestCase;

/**
 * Tests for ProjectedFeatureHolder
 *
 * @author Thomas Down
 * @since 1.3
 */

public class ProjectedFeatureHolderTest extends TestCase
{
    public ProjectedFeatureHolderTest(String name) {
        super(name);
    }

    public void testFeatureChangeEvent()
        throws Exception
    {
        Sequence seq = new SimpleSequence(
                DNATools.createDNA("gattaca"),
                "test",
                "test",
                Annotation.EMPTY_ANNOTATION
        );
        Feature.Template template = new Feature.Template();
        template.type = "test";
        template.source = "foo";
        template.location = new RangeLocation(2, 4);
        template.annotation = Annotation.EMPTY_ANNOTATION;
        Feature seqFeature = seq.createFeature(template);
        
        ProjectedFeatureHolder pfh = new ProjectedFeatureHolder(seq, seq, 7, true);
        Feature pfhFeature = (Feature) pfh.filter(new FeatureFilter.ByType("test")).features().next();
        
        pfhFeature.addChangeListener(ChangeListener.ALWAYS_VETO, ChangeType.UNKNOWN);
        boolean vetoed = false;
        try {
            seqFeature.setLocation(new RangeLocation(1, 3));
        } catch (ChangeVetoException cve) {
            vetoed = true;
        }
        assertTrue(vetoed);
        
        pfhFeature.removeChangeListener(ChangeListener.ALWAYS_VETO, ChangeType.UNKNOWN);
        seqFeature.setLocation(new RangeLocation(1, 3));
    }
}
