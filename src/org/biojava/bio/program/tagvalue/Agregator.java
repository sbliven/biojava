package org.biojava.bio.program.srs;

import java.util.List;
import java.util.ArrayList;

import org.biojava.utils.ParserException;
import org.biojava.utils.NestedError;

public class Agregator extends TagValueWrapper {
  private List items;
  private Object item;
  private Object tag;
  
  public Agregator(TagValueListener delegate) {
    super(delegate);
  }
  
  public void tagValue(Object tag, Object value)
  throws ParserException {
    if(
      (this.tag == null) ||
      (this.tag.equals(tag))
    ) {
      if(item == null && items == null) {
        item = value;
      } else if(item == null && items != null) {
        items.add(value);
      } else if(item != null && items == null) {
        items = new ArrayList();
        items.add(item);
        items.add(value);
        item = null;
      } else if(item != null && items != null) {
        // defencive programming block
        throw new NestedError("Assertion failure: impossible parser state");
      }
      this.tag = tag;
    } else {
      fireEvent();
      
      this.tag = tag;
      this.item = value;
      this.items = null;
    }
  }
  
  public void startRecord()
  throws ParserException {
    this.tag = null;
    this.item = null;
    this.items = null;
    
    super.startRecord();
  }
  
  public void endRecord()
  throws ParserException {
    fireEvent();
    
    super.endRecord();
  }
  
  private void fireEvent()
  throws ParserException {
    if(items != null) {
      super.tagValue(this.tag, items);
    } else if(item != null) {
      super.tagValue(this.tag, item);
    } else {
      throw new NestedError("Assertion failure: impossible parser state");
    }
  }
}
