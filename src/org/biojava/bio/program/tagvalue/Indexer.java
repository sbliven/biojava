package org.biojava.bio.program.tagvalue;

import java.io.*;
import java.util.*;

import org.biojava.utils.*;
import org.biojava.utils.io.*;
import org.biojava.bio.program.indexdb.*;

public class Indexer
implements TagValueListener {
  private final File file;
  private final CountedBufferedReader reader;
  private final IndexStore indexStore;
  private final Map seccondaryKeys;
  private String primaryKeyName;
  private String primaryKey;
  private Object tag;
  private long offset;
  
  public Indexer(File file, IndexStore indexStore)
  throws FileNotFoundException {
    this.file = file;
    this.reader = new CountedBufferedReader(new FileReader(file));
    this.indexStore = indexStore;
    this.seccondaryKeys = new SmallMap();
  }
  
  public BufferedReader getReader() {
    return reader;
  }
  
  public void setPrimaryKeyName(String primaryKeyName) {
    this.primaryKeyName = primaryKeyName;
  }
  
  public String getPrimaryKeyName() {
    return primaryKeyName;
  }
  
  public void addSeccondaryKey(String secKeyName) {
    seccondaryKeys.put(secKeyName, new ArrayList());
  }
  
  public void removeSeccondaryKey(String secKeyName) {
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

