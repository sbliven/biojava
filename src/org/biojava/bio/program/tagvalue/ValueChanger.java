package org.biojava.bio.program.tagValue;

import java.util.Map;
import java.util.List;
import java.util.Iterator;

import org.biojava.utils.ParserException;
import org.biojava.utils.SmallMap;

public class ValueChanger extends TagValueWrapper {
  private Map changers;
  private Map splitters;
  
  public ValueChanger(TagValueListener delegate) {
    super(delegate);
    this.changers = new SmallMap();
    this.splitters = new SmallMap();
  }
  
  public void setChanger(Object tag, Changer changer) {
    changers.put(tag, changer);
  }
  
  public void setSplitter(Object tag, Splitter splitter) {
    splitters.put(tag, splitter);
  }
  
  public Changer getChanger(Object tag) {
    return (Changer) changers.get(tag);
  }
  
  public Splitter getSplitter(Object tag) {
    return (Splitter) splitters.get(tag);
  }
  
  public void tagValue(Object tag, Object value)
  throws ParserException {
    Changer changer = getChanger(tag);
    Splitter splitter = getSplitter(tag);
    if(changer != null) {
      value = changer.change(value);
      super.tagValue(tag, value);
    } else if(splitter != null) {
      List values = splitter.split(value);
      for(Iterator i = values.iterator(); i.hasNext(); ) {
        Object v = i.next();
        super.tagValue(tag, v);
      }
    } else {
      super.tagValue(tag, value);
    }
  }
  
  public static interface Changer {
    public Object change(Object value);
  }
  
  public static interface Splitter {
    public List split(Object value);
  }
}


