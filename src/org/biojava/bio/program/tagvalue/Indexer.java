package org.biojava.bio.program.tagvalue;

import java.io.*;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.utils.io.*;
import org.biojava.bio.program.indexdb.*;

/**
 * <p>
 * Listens to tag-value events and passes on indexing events to an IndexStore.
 * </p>
 *
 * <p>
 * This class is provided to allow the indexing of arbitrary record-based text
 * files. Indexer objects are built for a single file and the indexes are
 * written to a single index store. To keep all of the reader offsets in sync
 * with one another, you will almost certainly wish to use the getReader()
 * method to retrieve a CountedBufferedReader instance if you want to read the
 * byte-offset between calls to Parser.read(). Below is an example of how to
 * index a file.
 * </p>
 *
 * <pre>
 * File fileToIndex; // get this from somewhere
 * BioStore store = bsf.createBioStore();
 * Indexer indexer = new Indexer(fileToIndex, store);
 * indexer.setPrimaryKeyName("foo");
 * indexer.addSecondaryKey("bar");
 * indexer.addSecondaryKey("baz");
 *
 * TagValueParser tvParser; // make this appropriate for your format
 * TagValueListener listener; // make this appropriate for your format
 *                            // and forward all events to changer
 * 
 * Parser parser = new Parser();
 * while(
 *   parser.read(indexer.getReader(), tvParser, listener)
 * ) {
 *   System.out.print(".");
 * }
 * </pre>
 *
 * @since 1.2
 * @author Matthew Pocock
 */
public class Indexer
implements TagValueListener {
  private final RAF file;
  private final CountedBufferedReader reader;
  private final IndexStore indexStore;
  private final Map seccondaryKeys;
  private String primaryKeyName;
  private String primaryKey;
  private Object tag;
  private long offset;
  
  /**
   * Build a new Indexer.
   *
   * @param file  the file to be processed
   * @param indexStore  the IndexStore to write to
   */
  public Indexer(File file, IndexStore indexStore)
  throws FileNotFoundException {
    this.file = new RAF(file, "r");
    this.reader = new CountedBufferedReader(new FileReader(file));
    this.indexStore = indexStore;
    this.seccondaryKeys = new SmallMap();
  }
  
  /**
   * Retrieve the reader that can be safely used to index this file.
   * 
   * @return the CountedBufferedReader that should be processed
   */
  public CountedBufferedReader getReader() {
    return reader;
  }
  
  /**
   * <p>
   * Set the tag to use as a primary key in the index.
   * </p>
   *
   * <p>
   * Whenever a value for the primary key tag is seen, this is passed to the
   * indexer as the primary key for indexing.
   * </p>
   *
   * <p>
   * Primary keys must be unique between entries, and each entry must provide
   * exactly one primary key value.
   * </p>
   *
   * @param primaryKeyName the tag to use as primary key
   */
  public void setPrimaryKeyName(String primaryKeyName) {
    this.primaryKeyName = primaryKeyName;
  }
  
  /**
   * Retrieve the tag currently used as primary key.
   *
   * @return a String representing the primary key name
   */
  public String getPrimaryKeyName() {
    return primaryKeyName;
  }
  
  /**
   * <p>
   * Add a secondary key.
   * </p>
   *
   * <p>
   * Secondary keys are potentialy non-unique properties of the entries being
   * indexed. Multiple records can use the same secondary key values, and a
   * single record can have multiple values for a secondary key.
   * </p>
   *
   * @param secKeyName  the name of the secondary key to add
   */
  public void addSecondaryKey(String secKeyName) {
    seccondaryKeys.put(secKeyName, new ArrayList());
  }
  
  /**
   * Remove a secondary key.
   *
   * @param secKeyName  the name of the secondary key to remove
   */
  public void removeSecondaryKey(String secKeyName) {
    seccondaryKeys.remove(secKeyName);
  }
  
  public void startRecord() {
    offset = reader.getFilePointer();
    primaryKey = null;
    for(Iterator i = seccondaryKeys.values().iterator(); i.hasNext(); ) {
      List list = (List) i.next();
      list.clear();
    }
  }
  
  public void startTag(Object tag) {
    this.tag = tag;
  }
  
  public void value(TagValueContext ctxt, Object value) {
    if(tag.equals(primaryKeyName)) {
      primaryKey = value.toString();
    }
    
    List l = (List) seccondaryKeys.get(tag);
    if(l != null) {
      l.add(value.toString());
    }
  }
  
  public void endTag() {}
  
  public void endRecord()
  throws ParserException
  {
    //try {
      if(primaryKey == null) {
        throw new NullPointerException("No primary key");
      }

      int length = (int) (reader.getFilePointer() - offset);
      indexStore.writeRecord(
        file,
        offset,
        length,
        primaryKey,
        seccondaryKeys
      );
    //} catch (NestedException ne) {
    //  throw new ParserException(ne);
    //}
  }
}

