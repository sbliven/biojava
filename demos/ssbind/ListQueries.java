package ssbind;

import org.biojava.bio.search.*;

/**
 * <p>
 * Lists all query sequence names in the report.
 * </p>
 *
 * <h2>Example</h2>
 * <pre>
 * java ProcessBlastReport blast.out ssbind.ListQueries
 * </pre>
 *
 * @author Matthew Pocock
 */
public class ListQueries
extends SearchContentAdapter {
  public void setQuerySeq(String seqID) {
    System.out.println("Query: " + seqID);
  }
}
