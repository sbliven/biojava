package ssbind;

import org.biojava.bio.search.*;

public class ListQueries
extends SearchContentAdapter {
  public void setQuerySeq(String seqID) {
    System.out.println("Query: " + seqID);
  }
}
