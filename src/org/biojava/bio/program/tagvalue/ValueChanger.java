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
 * For a given tag, changers take prescendence over splitters, and explicitly
 * registered changers or splitters take prescendence over the default handlers.
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
  private final Map changers;
  private final Map splitters;
  private Changer defaultC;
  private Splitter defaultS;
  
  private Changer changer;
  private Splitter splitter;
  
  /** 
   * Create a new changer that will pass the modified event stream to a
   * delegate.
   *
   * @param delegate  the TagValueListener that will receive the events
   */
  public ValueChanger(TagValueListener delegate) {
    super(delegate);
    this.changers = new SmallMap();
    this.splitters = new SmallMap();
  }
  
  /**
   * The changer that will be applied to the values of tags not registered
   * explicitly to any changer or splitter instance.
   *
   * @param c  the default Changer
   */
  public void setDefaultChanger(Changer c) {
    this.defaultC = c;
  }
  
  /**
   * Get the changer that will be applied to values of tags with no specific
   * handler registered.
   *
   * @return  the default Changer, or null
   */
  public Changer getDefaultChanger() {
    return defaultC;
  }
  
  /**
   * The splitter that will be applied to the values of tags not registered
   * explicitly to any changer or splitter instance.
   *
   * @param c  the default Splitter
   */
  public void setDefaultSplitter(Splitter s) {
    this.defaultS = s;
  }

  /**
   * Get the splitter that will be applied to values of tags with no specific
   * handler registered.
   *
   * @return  the default Splitter, or null
   */
  public Splitter getDefaultSplitter() {
    return defaultS;
  }

  /**
   * Set the Changer to be used for all values of a particular tag.
   *
   * @param tag the tag Object which will have all values changed
   * @param changer the Changer used to change the values
   */
  public void setChanger(Object tag, Changer changer) {
    changers.put(tag, changer);
  }
  
  /**
   * Set the Splitter to be used for all values of a particular tag.
   *
   * @param tag the tag Object which will have all values split
   * @param changer the Splitter used to split the values
   */
  public void setSplitter(Object tag, Splitter splitter) {
    splitters.put(tag, splitter);
  }
  
  /**
   * Get the Changer currently registered to handle a tag.
   *
   * @param tag  the tag Object for which values would be changed
   * @return the associated Changer or null
   */
  public Changer getChanger(Object tag) {
    return (Changer) changers.get(tag);
  }
  
  /**
   * Get the Splitter currently registered to handle a tag.
   *
   * @param tag  the tag Object for which values would be split
   * @return the associated Splitter or null
   */
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
  
  /**
   * Callback used to produce a new value from an old one.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public static interface Changer {
    /**
     * <p>
     * Produce a moddified value from an old value.
     * </p>
     *
     * <p>
     * It is strongly recomended that this method is re-enterant and does not
     * modify the state of the Changer in a way that would affect future return
     * -values.
     * </p>
     *
     * @param value  the old value Object
     * @return  the new value Object
     */
    public Object change(Object value);
  }
  
  /**
   * Callback used to produce a list of values from a single old one.
   *
   * @author Matthew Pocock
   * @since 1.2
   */
  public static interface Splitter {
    /**
     * <p>
     * Produce a list of values from an old value.
     * </p>
     *
     * <p>
     * It is strongly recomended that this method is re-enterant and does not
     * modify the state of the Splitter in a way that would affect future return
     * -values.
     * </p>
     *
     * @param value  the old value Object
     * @return  a List of value Objects produced by splitting the old value
     *          Object
     */
    public List split(Object value);
  }
}


