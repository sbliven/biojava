package org.biojava.bio.search;

import org.biojava.bio.BioException;

/**
 * <p>
 * An adapter for SearchContentHandler.
 * </p>
 *
 * <p>
 * This adapter is in the same spirit as the event handler adapters in
 * java.awt.event, and is intended as a simple base-class for implementations
 * that only want to handle a small number of the possible call-backs. All
 * method implementations are empty except for getMoreSearches() and
 * setMoreSearches(). These two maintain a boolean state between calls.
 * If you over-ride one, you should override the other.
 * </p>
 *
 * <h2>Example</h2>
 * <pre>
 * // a very booring handler
 * SearchContentHanlder ignoreEverything = new SearchContentAdapter();
 *
 * // just respond to sub hit properties
 * SearchContentHander subHitsOnly = new SearchContentAdapter() {
 *   public void addSubHitProperth(Object key, Object value) {
 *     System.out.println(key + " -> " + value);
 *   }
 * };
 * </pre>
 * 
 * @author Matthew Pocock
 * @since 1.3
 */
public class SearchContentAdapter
implements SearchContentHandler {
  private boolean moreSearches = false;

  public void addHitProperty(Object key, Object value) {}
  public void addSearchProperty(Object key, Object value) {}
  public void addSubHitProperty(Object key, Object value) {}
  public void startHeader() {}
  public void endHeader() {}
  public void startHit() {}
  public void endHit() {}
  public void startSearch() {}
  public void endSearch() {}
  public void startSubHit() {}
  public void endSubHit() {}
  public void setQuerySeq(String seqID) throws BioException {}
  public void setSubjectDB(String dbID) throws BioException {}


  public boolean getMoreSearches() {
    return moreSearches;
  }

  public void setMoreSearches(boolean val) {
    this.moreSearches = val;
  }
}
