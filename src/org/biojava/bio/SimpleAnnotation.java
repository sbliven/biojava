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

package org.biojava.bio;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;

/**
 * A no-frills implementation of Annotation that is just a wrapper around a Map.
 * <P>
 * It will allow you to set any property, but will throw exceptions if you try
 * to retrieve a property that is not set.
 * <P>
 * So that multiple values may be assigned to the same key, for example,
 * multiple "/note" fields on a GenBank feature, if a value would be
 * overwritten, a list will instead be composed of the old value and the new
 * one.  Therefore, use caution when adding a list as a value.
 *
 * @author Matthew Pocock
 * @atuhor Greg Cox
 */
public class SimpleAnnotation implements Annotation, Serializable {
  /**
   * The object to do the hard work of informing others of changes.
   */
  protected transient ChangeSupport changeSupport = null;

  /**
   * The properties map.
   * <P>
   * This may be null if no property values have yet been set.
   */
  private Map properties;

  /**
   * Retrieves properties, potentialy creating it if it was null.
   *
   * @return the properties Map
   */
  protected final Map getProperties() {
    if(!propertiesAllocated())
      properties = new HashMap();
    return properties;
  }

  /**
   * A convenience method to see if we have allocated the properties map yet.
   *
   * @returns true if properties is set and false otherwise
   */
  protected final boolean propertiesAllocated() {
    return properties != null;
  }

  /**
   * @param key The key whose property to retrieve.
   * @throws NoSuchElementException if the property 'key' does not exist
   */
  public Object getProperty(Object key) throws NoSuchElementException {
    if(propertiesAllocated()) {
      Map prop = getProperties();
      if(prop.containsKey(key)) {
        return prop.get(key);
      }
    }
    throw new NoSuchElementException("Property " + key + " unknown");
  }

  public void setProperty(Object key, Object value)
  throws ChangeVetoException {
	// This mess is to handle cases where there is already a value associated
	// with the key.
	Object newValue;
	Object oldValue = null;
	try
	{
		if(this.getProperties().containsKey(key))
		{
			oldValue = this.getProperty(key);
			// If the old value is already a list, append the new value to it.
			if (oldValue instanceof List)
			{
				newValue = oldValue;
				((List)newValue).add(value);
			}
			// Otherwise, construct a new list with the old and new values.
			else
			{
				newValue = new ArrayList();
				((List)newValue).add(oldValue);
				((List)newValue).add(value);
			}
		}
		else
		{
			newValue = value;
		}
	}
	// Catches ClassCast, Unsupported operation, and Illegal argument exceptions
	// from newValue.add.  If this operation fails, create a new list with the
	// new and old values.
	catch (RuntimeException re)
	{
		newValue = new ArrayList();
		((List)newValue).add(oldValue);
		((List)newValue).add(value);
	}

    if(changeSupport == null) {
      getProperties().put(key, newValue);
    } else {
      Map properties = getProperties();
      ChangeEvent ce = new ChangeEvent(
        this,
        Annotation.PROPERTY,
        new Object[] { key, newValue },
        new Object[] { key, properties.get(key)}
      );
      synchronized(changeSupport) {
        changeSupport.firePreChangeEvent(ce);
        properties.put(key, newValue);
        changeSupport.firePostChangeEvent(ce);
      }
    }
  }

  public boolean containsProperty(Object key) {  
    if(propertiesAllocated()) {
      return properties.containsKey(key);
    } else {
      return false;
    }
  }
 
  public Set keys() {
    if(propertiesAllocated()) {
      return properties.keySet();
    } else {
      return Collections.EMPTY_SET;
    }
  }

  public String toString() {
    StringBuffer sb = new StringBuffer("{");
    Map prop = getProperties();
    Iterator i = prop.keySet().iterator();
    if(i.hasNext()) {
      Object key = i.next();
      sb.append(key + "=" + prop.get(key));
    }
    while(i.hasNext()) {
      Object key = i.next();
      sb.append("," + key + "=" + prop.get(key));
    }
    sb.append("}");
    return sb.toString();
  }

  public Map asMap() {
    return new HashMap(getProperties());
  }

  public void addChangeListener(ChangeListener cl) {
    if(changeSupport == null) {
      changeSupport = new ChangeSupport();
    }

    synchronized(changeSupport) {
      changeSupport.addChangeListener(cl);
    }
  }

  public void addChangeListener(ChangeListener cl, ChangeType ct) {
    if(changeSupport == null) {
      changeSupport = new ChangeSupport();
    }

    synchronized(changeSupport) {
      changeSupport.addChangeListener(cl, ct);
    }
  }

  public void removeChangeListener(ChangeListener cl) {
    if(changeSupport != null) {
      synchronized(changeSupport) {
        changeSupport.removeChangeListener(cl);
      }
    }
  }

  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
    if(changeSupport != null) {
      synchronized(changeSupport) {
        changeSupport.removeChangeListener(cl, ct);
      }
    }
  }

  public SimpleAnnotation() {
  }

  public SimpleAnnotation(Annotation ann) throws IllegalArgumentException {
    if(ann == null) {
      throw new IllegalArgumentException(
        "Null annotation not allowed. Use Annotation.EMPTY_ANNOTATION instead."
      );
    }
    if(ann == Annotation.EMPTY_ANNOTATION) {
      return;
    }
    Map properties = getProperties();
    for(Iterator i = ann.keys().iterator(); i.hasNext(); ) {
      Object key = i.next();
      try {
        properties.put(key, ann.getProperty(key));
      } catch (IllegalArgumentException iae) {
        throw new BioError(
          iae,
          "Property was there and then dissapeared: " + key
        );
      }
    }
  }

  public SimpleAnnotation(Map annMap) {
    if(annMap == null) {
      throw new IllegalArgumentException(
        "Null annotation Map not allowed. Use an empy map instead."
      );
    }
    if(annMap.isEmpty()) {
      return;
    }

    Map properties = getProperties();
    for(Iterator i = annMap.keySet().iterator(); i.hasNext(); ) {
      Object key = i.next();
      properties.put(key, annMap.get(key));
    }
  }
}
