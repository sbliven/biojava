package org.biojava.bio.program.tagvalue;

import java.util.Map;
import java.util.List;
import java.util.Iterator;

import org.biojava.utils.ParserException;
import org.biojava.utils.SmallMap;

/**
 * <p>
 * Intercept the values associated with some tags and change them
 * systematically.
 * </p>
 *
 * <p>
 * The two forms of changes that can be made are:
 * <ul>
 * <li>replace a single value with a new single value (e.g. changing the string
 * "1.87" into a Double object)</li>
 * <li>split a single value into multiple values and pass each one individualy
 * on to the delegate e.g. "a, b, c" becomes three values "a", "b", "c".</li>
 * </ul>
 * </p>
 *
 * <p>
 * For a given tag, changers take precendence over splitters, and explicitly
 * registered changers or splitters take precendence over the default handlers.
 * If there is not a specific handler for a tag and there is no default set,
 * then the value is passed on unchanged. 
 * </p>
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class ValueChanger
  extends
    TagValueWrapper
{
  private ChangeTable.Changer defaultC;
  private ChangeTable.Splitter defaultS;
  private ChangeTable changeTable;
  
  private ChangeTable.Changer changer;
  private ChangeTable.Splitter splitter;
  
  /** 
   * Create a new changer that will pass the modified event stream to a
   * delegate.
   *
   * @param delegate  the TagValueListener that will receive the events
   */
  public ValueChanger(TagValueListener delegate, ChangeTable changeTable) {
    super(delegate);
    this.changeTable = changeTable;
  }
  
  /**
   * The changer that will be applied to the values of tags not registered
   * explicitly to any changer or splitter instance.
   *
   * @param c  the default ChangeTable.Changer
   */
  public void setDefaultChanger(ChangeTable.Changer c) {
    this.defaultC = c;
  }
  
  /**
   * Get the changer that will be applied to values of tags with no specific
   * handler registered.
   *
   * @return  the default ChangeTable.Changer, or null
   */
  public ChangeTable.Changer getDefaultChanger() {
    return defaultC;
  }
  
  /**
   * The splitter that will be applied to the values of tags not registered
   * explicitly to any changer or splitter instance.
   *
   * @param c  the default ChangeTable.Splitter
   */
  public void setDefaultSplitter(ChangeTable.Splitter s) {
    this.defaultS = s;
  }

  /**
   * Get the splitter that will be applied to values of tags with no specific
   * handler registered.
   *
   * @return  the default ChangeTable.Splitter, or null
   */
  public ChangeTable.Splitter getDefaultSplitter() {
    return defaultS;
  }
  
  public void startTag(Object tag)
  throws ParserException {
    this.changer = changeTable.getChanger(tag);
    this.splitter = changeTable.getSplitter(tag);
    
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
}


