package org.biojava.bio.program.ssaha;

/**
 * The interface used to inform interested parties that some sequence has
 * been searched and something found.
 * <p>
 * The callbacks will always be called in the order startSearch, hit,
 * endSearch, during which time there may be multiple hit calls. The seqID
 * of startSearch and endSearch will match. After this, a new startSearch
 * may begin. These events will usually originate from the search method of
 * DataStore.
 *
 * @author Matthew Pocock
 */
public interface SearchListener {
  /**
   * Indicates that a sequence is about to be searched against a DataStore.
   *
   * @param seqID  the id of the sequence to be searched
   */
  public void startSearch(String seqID);

  /**
   * Indicates that a sequence has been searched against a DataStore.
   *
   * @param seqID  the id of the sequence to be searched
   */
  public void endSearch(String seqID);
  
  /**
   * There has been a hit between the query sequence and a database
   * sequence.
   *
   * @param hitID  the number of the sequence hit; resolvable by
   *               String id = DataStore.seqNameForID(hitID)
   * @param queryOffset the offset into the query sequence
   * @param hitOffset the offset into the sequence hit in the database
   * @param hitLength the number of symbols hit
   */
  public void hit(
    int hitID,
    int queryOffset,
    int hitOffset,
    int hitLength
  );
}
