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

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;

/**
 * A utility class to ease the problem of implementing an Annotation to that of
 * providing an apropreate implementation of Map.
 *
 * @author Matthew Pocock
 * @author Greg Cox
 *
 * @for.developer This class is only intended as a way to implement
 * Annotation. If you are not trying to do that, then don't read on. If you
 * are reading the documentation for an Annotation implementation that extends
 * this, then don't read on. There is nothing to see here.
 *
 * @for.developer If you are still reading this, then you must be trying to
 * implement Annotation. To do that, extend this class and implement
 * <code>getProperties()</code> and <code>propertiesAllocated()</code>.
 */
public abstract class AbstractAnnotation
  extends
    AbstractChangeable
  implements
    Annotation,
    Serializable
{
  /**
   * Implement this to return the Map delegate.
   *
   * From code in the 1.2 version of AbstractAnnotation
   * @for.developer This is required for the implementation of an Annotation that
   *            extends AbstractAnnotation
   */
  protected abstract Map getProperties();

  /**
   * A convenience method to see if we have allocated the properties
   * Map.
   *
   * @return true if the properties have been allocated, false otherwise
   * @for.developer This is required for the implementation of an Annotation that
   *            extends AbstractAnnotation
   */
  protected abstract boolean propertiesAllocated();


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
    if(!hasListeners()) {
      getProperties().put(key, value);
    } else {
      Map properties = getProperties();
      ChangeEvent ce = new ChangeEvent(
        this,
        Annotation.PROPERTY,
        new Object[] { key, value },
        new Object[] { key, properties.get(key)}
      );
      ChangeSupport cs = getChangeSupport(Annotation.PROPERTY);
      synchronized(cs) {
        cs.firePreChangeEvent(ce);
        properties.put(key, value);
        cs.firePostChangeEvent(ce);
      }
    }
  }

  public void removeProperty(Object key)
    throws ChangeVetoException, NoSuchElementException
  {
    if (!getProperties().containsKey(key)) {
        throw new NoSuchElementException("Can't remove key " + key.toString());
    }

    if(!hasListeners()) {
      getProperties().remove(key);
    } else {
      Map properties = getProperties();
      ChangeEvent ce = new ChangeEvent(
        this,
        Annotation.PROPERTY,
        new Object[] { key, null },
        new Object[] { key, properties.get(key)}
      );
      ChangeSupport cs = getChangeSupport(Annotation.PROPERTY);
      synchronized(cs) {
        cs.firePreChangeEvent(ce);
        properties.remove(key);
        cs.firePostChangeEvent(ce);
      }
    }
  }

  public boolean containsProperty(Object key) {
    if(propertiesAllocated()) {
      return getProperties().containsKey(key);
    } else {
      return false;
    }
  }

  public Set keys() {
    if(propertiesAllocated()) {
      return getProperties().keySet();
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
    return sb.substring(0);
  }

  public Map asMap() {
    return Collections.unmodifiableMap(getProperties());
  }

  protected AbstractAnnotation() {
  }

  protected AbstractAnnotation(Annotation ann) throws IllegalArgumentException {
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
          "Property was there and then disappeared: " + key, iae
        );
      }
    }
  }

  public AbstractAnnotation(Map annMap) {
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


  public int hashCode() {
    return asMap().hashCode();
  }

  public boolean equals(Object o) {
    if (! (o instanceof Annotation)) {
      return false;
    }

    return ((Annotation) o).asMap().equals(asMap());
  }
}
