package ssbind;

import org.biojava.bio.search.*;

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
