package ssearch;

import org.biojava.bio.search.*;

/**
 * Prints out a count of the total number of hits in a report.
 *
 * <h2>Example</h2>
 * <pre>
 * java ProcessBlastReport blast.out ssbind.CountHits
 * </pre>
 *
 * @author Matthew Pocock
 */
public class CountHits
extends SearchContentAdapter {
  private int hits;

  public void startSearch() {
    hits = 0;
  }

  public void endSearch() {
    System.out.println("Number of hits: " + hits);
  }

  public void startHit() {
    hits++;
  }
}
