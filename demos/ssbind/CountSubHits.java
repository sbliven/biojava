package ssbind;

import org.biojava.bio.search.*;

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

