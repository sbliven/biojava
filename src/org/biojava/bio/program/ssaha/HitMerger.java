package org.biojava.bio.program.ssaha;

import java.util.*;

/**
 * A listener that merges overlapping hits and culls all hits under a given
 * length.
 *
 * @author Matthew Pocock
 */
public class HitMerger implements SearchListener {
  private List hitList;
  private int minLength;
  private SearchListener delegate;
  
  /**
   * Build a new HitMerger that will pass events on to a delegate.
   *
   * @param delegate  the SearchListener to inform of all merged and
   *                  filtered hits
   * @param minLength the minimum length a hit must reach to be passed on
   */
  public HitMerger(SearchListener delegate, int minLength) {
    this.hitList = new ArrayList();
    this.delegate = delegate;
    this.minLength = minLength;
  }
  
  public void startSearch(String seqID) {
    hitList.clear();
    delegate.startSearch(seqID);
  }
  
  public void hit(
    int hitID,
    int queryOffset,
    int hitOffset,
    int hitLength
  ) {
    hitList.add(new Record(hitID, queryOffset, hitOffset, hitLength));
  }
  
  public void endSearch(String seqID) {
    Collections.sort(hitList);
    
    Iterator i = hitList.iterator();
    Record last = (Record) i.next();
    while(i.hasNext()) {
      Record current = (Record) i.next();
      if(
        (current.hitID == last.hitID)
       &&
        (current.queryOffset <= last.queryOffset + last.hitLength)
       &&
        (current.hitOffset <= last.hitOffset + last.hitLength)
       &&
        ((current.hitOffset - last.hitOffset) == (current.queryOffset - last.queryOffset))
      ) {
        last.hitLength = current.hitOffset + current.hitLength - last.hitOffset;
      } else {
        if(last.hitLength >= minLength) {
          delegate.hit(last.hitID, last.queryOffset, last.hitOffset, last.hitLength);
        }
        last = current;
      }
    }
    if(last.hitLength >= minLength) {
      delegate.hit(last.hitID, last.queryOffset, last.hitOffset, last.hitLength);
    }
    delegate.endSearch(seqID);
  }
  
  private static class Record
  implements Comparable {
    public int hitID;
    public int queryOffset;
    public int hitOffset;
    public int hitLength;
    
    public Record(
      int hitID,
      int queryOffset,
      int hitOffset,
      int hitLength
    ) {
      this.hitID = hitID;
      this.queryOffset = queryOffset;
      this.hitOffset = hitOffset;
      this.hitLength = hitLength;
    }
    
    public boolean equals(Object o) {
      if(o instanceof Record) {
        Record r = (Record) o;
        return
          hitID == r.hitID &&
          queryOffset == r.queryOffset &&
          hitOffset == r.hitOffset &&
          hitLength == r.hitLength;
      }
      return false;
    }
    
    public int compareTo(Object o) {
      Record r = (Record) o;
      
      int relDist = queryOffset + hitOffset - (r.queryOffset + r.hitOffset);
      
      if(hitID > r.hitID) {
        return 1;
      } else if(hitID < r.hitID) {
        return -1;
      } else if(relDist > 0) {
        return 1;
      } else if(relDist < 0) {
        return -1;
      } else if(hitOffset > r.hitOffset) {
        return 1;
      } else if(hitOffset < r.hitOffset) {
        return -1;
      } else if(hitLength > r.hitLength) {
        return 1;
      } else if(hitLength < r.hitLength) {
        return -1;
      } else {
        return 0;
      }
    }
    
    public String toString() {
      return hitID + " " + queryOffset + " " + hitOffset + " " + hitLength;
    }
  }
}
