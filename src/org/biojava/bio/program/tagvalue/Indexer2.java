/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.bio.program.tagvalue;

import java.io.*;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.utils.io.*;
import org.biojava.bio.program.indexdb.*;

/**
 * <p>
 * Listens to tag-value events and passes on indexing events to an IndexStore.
 * This is an update to Indexer that understands that indexed properties may
 * not be at the top level.
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
 *
 * // don't forget to register all the apropreate keys to the factory first.
 * BioIndexStore indexStore = bioIndxStrFact.createBioStore();
 *
 * Indexer indexer = new Indexer(fileToIndex, indexStore);
 * indexer.setPrimaryKeyName("foo", new String[] { "foo" });
 * indexer.addSecondaryKey("bar", new String[] { "x", "y", "bar"});
 * indexer.addSecondaryKey("baz", new String[] { "z" });
 *
 * TagValueParser tvParser; // make this appropriate for your format
 * TagValueListener listener; // make this appropriate for your format
 *                            // and forward all events to indexer
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
public class Indexer2
implements TagValueListener {
  private final String primaryKeyName;
  private final RAF file;
  private final CountedBufferedReader reader;
  private final IndexStore indexStore;
  private final Map keys;
  private final Map keyValues;
  private Object tag;
  private long offset;
  private int depth;
  private Stack stack;
  
  /**
   * Build a new Indexer.
   *
   * @param file  the file to be processed
   * @param indexStore  the IndexStore to write to
   */
  public Indexer2(File file, IndexStore indexStore, Index2Model model)
  throws FileNotFoundException {
    this.file = new RAF(file, "r");
    this.reader = new CountedBufferedReader(new FileReader(file));
    this.indexStore = indexStore;
    this.keyValues = new SmallMap();
    this.depth = 0;
    this.stack = new Stack();
    
    this.keys = new SmallMap();
    for(Iterator i = model.getKeys().iterator(); i.hasNext(); ) {
      String key = (String) i.next();
      Object val = model.getKeyPath(key);
      
      keys.put(val, key);
    }
    this.primaryKeyName = model.getPrimaryKeyName();
  }
  
  /**
   * Retrieve the reader that can be safely used to index this file.
   * 
   * @return the CountedBufferedReader that should be processed
   */
  public CountedBufferedReader getReader() {
    return reader;
  }
  
  public void startRecord() {
    if(depth == 0) {
      offset = reader.getFilePointer();
      
      Frame frame = new Frame();
      
      for(Iterator ki = keys.keySet().iterator(); ki.hasNext(); ) {
        Object[] keyPath = (Object[]) ki.next();
        if(keyPath.length == 1) {
          frame.addKey(keyPath);
        } else {
          frame.paths.add(keyPath);
        }
      }
      
      stack.push(frame);
    } else {
      Frame top = (Frame) stack.peek();
      Frame frame = new Frame();
      
      for(Iterator ki = top.paths.iterator(); ki.hasNext(); ) {
        Object[] keyPath = (Object[]) ki.next();
        if(keyPath[depth].equals(tag)) {
          if(keyPath.length == depth + 1) {
            frame.addKey(keyPath);
          } else {
            frame.paths.add(keyPath);
          }
        }
      }
      
      stack.push(frame);
    }
    
    depth++;
  }
  
  public void startTag(Object tag) {
    this.tag = tag;
  }
  
  public void value(TagValueContext ctxt, Object value) {
    Frame frame = (Frame) stack.peek();
    Object[] keyPath = (Object []) frame.getKeyPath(tag);
    
    if(keyPath != null) {
      KeyState ks = (KeyState) keyValues.get(keyPath);
      if(ks == null) {
        keyValues.put(tag, ks = new KeyState(keys.get(keyPath).toString()));
      }
      ks.values.add(value);
    }
  }
  
  public void endTag() {}
  
  public void endRecord()
  throws ParserException
  {
    depth--;
    if(depth == 0) {
      int length = (int) (reader.getFilePointer() - offset);

      String primaryKeyValue = null;
      Map secKeys = new SmallMap();
      for(Iterator i = keyValues.keySet().iterator(); i.hasNext(); ) {
        Object key = i.next();
        KeyState ks = (KeyState) keyValues.get(key);
        if(ks.keyName.equals(primaryKeyName)) {
          if(ks.values.size() != 1) {
            throw new ParserException(
              "There must be exactly one value for the primary key: " +
              primaryKeyName + " - " + ks.values
            );
          }
          primaryKeyValue = ks.values.iterator().next().toString();
        } else {
          secKeys.put(ks.keyName, ks.values);
        }
        
        ks.values.clear();
      }
      
      if(primaryKeyValue == null) {
        throw new NullPointerException("No primary key");
      }

      indexStore.writeRecord(
        file,
        offset,
        length,
        primaryKeyValue,
        secKeys
      );
      
      stack.clear();
    } else {
      stack.pop();
    }
  }
  
  private static class Frame {
    public final Map keys = new SmallMap();
    public final Set paths = new SmallSet();
    
    public void addKey(Object[] keyPath) {
      keys.put(keyPath[keyPath.length - 1], keyPath);
    }
    
    public Object[] getKeyPath(Object tag) {
      return (Object []) keys.get(tag);
    }
  }
  
  private static class KeyState {
    public final String keyName;
    public final Set values = new SmallSet();
    
    public KeyState(String keyName) {
      this.keyName = keyName;
    }
  }
}

