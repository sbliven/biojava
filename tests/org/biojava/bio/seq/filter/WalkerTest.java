package org.biojava.bio.seq.filter;

import junit.framework.TestCase;

import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.seq.ComponentFeature;
import org.biojava.bio.symbol.RangeLocation;
import org.biojava.bio.BioException;

/**
 * Test some walkers for some viewers, ensuring that they get the events we
 * would expect.
 *
 * @author Matthew Pocock
 */
public class WalkerTest
extends TestCase {
  private FeatureFilter booring1;
  private FeatureFilter booring2;
  private FeatureFilter booring3;
  private FeatureFilter booring4;
  private FeatureFilter and;
  private FeatureFilter or;
  private FeatureFilter andOr;

  protected void setUp() {
    booring1 = new FeatureFilter.OverlapsLocation(new RangeLocation(20, 50));
    booring2 = new FeatureFilter.ByClass(StrandedFeature.class);
    booring3 = new FeatureFilter.BySequenceName("unrepeatable");
    booring4 = new FeatureFilter.ByClass(ComponentFeature.class);
    and = new FeatureFilter.And(booring1, booring2);
    or = new FeatureFilter.Or(booring3, booring4);
    andOr = new FeatureFilter.And(booring1,
                                  new FeatureFilter.Or(booring2, booring4));
  }

  public void testCountAll() {
    try {
      CountAll ca = new CountAll();
      Walker walker = WalkerFactory.getInstance().getWalker(ca);

      walker.walk(booring1, ca);
      assertEquals("One filter: " + booring1, 1, ca.count);

      ca.count = 0;
      walker.walk(and, ca);
      assertEquals("Three filters: " + and, 3, ca.count);

      ca.count = 0;
      walker.walk(andOr, ca);
      assertEquals("Five filters: " + andOr, 5, ca.count);
    } catch (BioException be) {
      throw (AssertionError) new AssertionError(
              "Could not instantiate visitor").initCause(be);
    }
  }

  public class CountAll implements Visitor {
    int count = 0;

    public void featureFilter(FeatureFilter filter) {
      System.err.println("Increasing counter");
      count++;
    }
  }
}
