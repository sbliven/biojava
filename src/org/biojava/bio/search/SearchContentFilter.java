package org.biojava.bio.search;

import org.biojava.bio.BioException;

/**
 * <p>
 * Filtering implementation of SearchContentHandler that by default passes
 * all messages on to the next delegate in the chain.
 * </p>
 *
 * <p>
 * In this handler, all info will be passed onto a delegate handler. You can
 * build up a chain of filters by using one filter as the delegate for
 * another. When you over-ride a method in a filter, you can modify any
 * state you wish. If you want that to propogate on, you should call
 * the method on yourself via super.(), if not, just return.
 * </p>
 *
 * <p>
 * It is your responsibility to ensure that the events emitted from your filter
 * are sensible. In particular, start/end messages must be paired even after
 * filtering.
 * </p>
 *
 * <h2>Example</h2>
 *
 * <pre>
 * // we have a handler from somewhere
 * SearchContentHandler handler = ...;
 *
 * // now we are going to mutate all "score" notifications to Double instances
 * // from strings
 * SearchContentHanlder filter = new SearchContentFilter() {
 *   public void addHitProperty(Object key, Object value) {
 *     if("score".equals(key)) {
 *       if(value instanceof String) {
 *         value = new Double(value);
 *       }
 *     }
 *     super(key, value);
 *   }
 * };
 * </pre>
 *
 * @author Matthew Pocock
 * @since 1.3
 */
public class SearchContentFilter
implements SearchContentHandler {
  private final SearchContentHandler delegate;

  public SearchContentFilter(SearchContentHandler delegate) {
    this.delegate = delegate;
  }

  public void addHitProperty(Object key, Object value) {
    delegate.addHitProperty(key, value);
  }

  public void addSearchProperty(Object key, Object value) {
    delegate.addSearchProperty(key, value);
  }

  public void addSubHitProperty(Object key, Object value) {
    delegate.addSubHitProperty(key, value);
  }

  public void startHeader() {
    delegate.startHeader();
  }

  public void endHeader() {
    delegate.endHeader();
  }

  public void startHit() {
    delegate.startHit();
  }

  public void endHit() {
    delegate.endHit();
  }

  public void startSearch() {
    delegate.startSearch();
  }

  public void endSearch() {
    delegate.endSearch();
  }

  public void startSubHit() {
    delegate.startSubHit();
  }

  public void endSubHit() {
    delegate.endSubHit();
  }

  public void setQuerySeq(String seqID)
  throws BioException {
    delegate.setQuerySeq(seqID);
  }

  public void setSubjectDB(String dbID)
  throws BioException {
    delegate.setSubjectDB(dbID);
  }

  public boolean getMoreSearches() {
    return delegate.getMoreSearches();
  }

  public void setMoreSearches(boolean val) {
    delegate.setMoreSearches(val);
  }
}
