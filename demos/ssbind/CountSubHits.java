package ssbind;

import org.biojava.bio.search.*;

/**
 * Prints out a count of the total number of sub hits in a report.
 *
 * <h2>Example</h2>
 * <pre>
 * java ProcessBlastReport blast.out ssbind.CountSubHits
 * </pre>
 *
 * @author Matthew Pocock
 * @author Rahul Karnik
 */
public class CountSubHits
extends SearchContentAdapter {
  private int subHits;

  public void startSearch() {
    subHits = 0;
  }

  public void endSearch() {
    System.out.println("Number of sub hits: " + subHits);
  }

  public void startSubHit() {
    subHits++;
  }
}

