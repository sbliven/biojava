package ssbind;

import org.biojava.bio.search.*;

/**
 * <p>
 * Echo the event stream to stdout with prety indenting.
 * </p>
 *
 * <p>
 * This class is most useful to check that your parsing is working. You can use
 * an Echoer as the last handler in a chain of filters, or attach it directly
 * to the parser. It is an instructive way to work out what events are being
 * fired when, and with what properties attached.
 * </p>
 *
 * @author Matthew Pocock
 */
public class Echoer
implements SearchContentHandler {
  private int indentDepth = 0;
  private String indentPrefix = "";
  private boolean moreSearches = false;
 
  public void indent() {
    indentDepth++;
    createPrefix();
  }
  
  public void outdent() {
    indentDepth--;
    createPrefix();
  }
  
  public String getPrefix() {
    return indentPrefix;
  }
  
  private void createPrefix() {
    indentPrefix = "";
    for(int i = 0; i < indentDepth; i++) {
      indentPrefix += "  ";
    }
  }
  
  public void addHitProperty(Object key, Object value) {
    System.out.println(getPrefix() + "hit property: " + key.getClass() + " " + key + " -> " + value.getClass() + " " + value);
  }
  public void addSearchProperty(Object key, Object value) {
    System.out.println(getPrefix() + "search property: " + key.getClass() + " " + key + " -> " + value.getClass() + " " + value);
  }

  public void addSubHitProperty(Object key, Object value) {
    System.out.println(getPrefix() + "subhit property: " + key.getClass() + " " + key + " -> " + value.getClass() + " " + value);
  }

  public void startHeader() {
    System.out.println(getPrefix() + "header:");
    indent();
  }

  public void endHeader() {
    outdent();
  }

  public void startHit() {
    System.out.println(getPrefix() + "hit:");
    indent();
  }

  public void endHit() {
    outdent();
  }

  public void startSearch() {
    System.out.println(getPrefix() + "search");
    indent();
  }

  public void endSearch() {
    outdent();
  }

  public void startSubHit() {
    System.out.println(getPrefix() + "sub hit:");
    indent();
  }

  public void endSubHit() {
    outdent();
  }

  public boolean getMoreSearches() {
    return moreSearches;
  }

  public void setMoreSearches(boolean val) {
    this.moreSearches = val;
  }

  public void setQuerySeq(String seqID) {
      setQueryID(seqID);
  }

  public void setSubjectDB(String dbID) {
      setDatabaseID(dbID);
  }

    public void setQueryID(String queryID)
    {
        System.out.println(getPrefix() + "query sequence: " + queryID);
    }

    public void setDatabaseID(String databaseID)
    {
        System.out.println(getPrefix() + "subject db: " + databaseID);
    }
}
