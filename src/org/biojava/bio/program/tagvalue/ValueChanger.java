package org.biojava.bio.program.tagvalue;

import java.util.Map;
import java.util.List;
import java.util.Iterator;

import org.biojava.utils.ParserException;
import org.biojava.utils.SmallMap;

public class ValueChanger
  extends
    TagValueWrapper
{
  private final Map changers;
  private final Map splitters;
  private Changer defaultC;
  private Splitter defaultS;
  
  private Changer changer;
  private Splitter splitter;
  
  public ValueChanger(TagValueListener delegate) {
    super(delegate);
    this.changers = new SmallMap();
    this.splitters = new SmallMap();
  }
  
  public void setDefaultChanger(Changer c) {
    this.defaultC = c;
  }
  
  public Changer getDefaultChanger() {
    return defaultC;
  }
  
  public void setDefaultSplitter(Splitter s) {
    this.defaultS = s;
  }

  public Splitter getDefaultSplitter() {
    return defaultS;
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
  
  public void startTag(Object tag)
  throws ParserException {
    this.changer = getChanger(tag);
    this.splitter = getSplitter(tag);
    
    if(this.changer == null) {
      this.changer = defaultC;
    }
    
    if(this.splitter == null) {
      this.splitter = defaultS;
    }
    
    super.startTag(tag);
  }
  
  public void value(TagValueContext ctxt, Object value)
  throws ParserException {
    if(this.changer != null) {
      value = changer.change(value);
      super.value(ctxt, value);
    } else if(this.splitter != null) {
      List values = splitter.split(value);
      for(Iterator i = values.iterator(); i.hasNext(); ) {
        Object v = i.next();
        super.value(ctxt, v);
      }
    } else {
      super.value(ctxt, value);
    }
  }
  
  public static interface Changer {
    public Object change(Object value);
  }
  
  public static interface Splitter {
    public List split(Object value);
  }
}


