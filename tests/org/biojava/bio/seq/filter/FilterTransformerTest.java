package org.biojava.bio.seq.filter;

import junit.framework.TestCase;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FilterUtils;
import org.biojava.bio.BioException;

/**
 * Checks that FilterTransformer is not totaly nuts.
 *
 * @author Matthew Pocock
 */
public class FilterTransformerTest
extends TestCase {
  public void testIdentity() {
    try {
    FeatureFilter filt = FilterUtils.and(
            FilterUtils.byType("pigs"),
            FilterUtils.or(
                    FilterUtils.bySource("iran"),
                    FilterUtils.not(
                            FilterUtils.onlyChildren(
                                    FilterUtils.hasAnnotation("beer")
                            ))));

      FeatureFilter filt2 = (FeatureFilter) FilterUtils.visitFilter(
              filt,
              new FilterTransformer());
      assertTrue("Non-moidfying transformer gives equal results:\n\t" + filt + "\n\t" + filt2,
                   FilterUtils.areEqual(filt, filt2));
    } catch (BioException be) {
      throw (AssertionError) new AssertionError("Couldn't make walker").initCause(be);
    }
  }
}
