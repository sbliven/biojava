package ssaha;

import org.biojava.bio.program.ssaha.*;

public class ResultPrinter implements SearchListener {
  String seqID;
  DataStore dataStore;
  
  public ResultPrinter(DataStore dataStore) {
    this.dataStore = dataStore;
  }

  public void startSearch(String seqID) {
    this.seqID = seqID;
  }

  public void endSearch(String seqID) {
  }

  public void hit(
    int hitID,
    int queryOffset,
    int hitOffset,
    int hitLength
  ) {
    String hitName = dataStore.seqNameForID(hitID);
    System.out.println(seqID + " " + hitName + " " + queryOffset + " " + hitOffset + " " + hitLength);
  }
}
