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
 * Merged view onto a list of underlying Annotation objects.
 * Currently immutable (but reflects changes to underlying objects).
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @author Greg Cox
 * @since 1.2
 */

public class MergeAnnotation
  extends
    AbstractChangeable
  implements
    Annotation,
    Serializable
{
  private transient ChangeListener propertyForwarder = null;
  
  private List mergeSet;
  
  {
    mergeSet = new ArrayList();
  }
  
  public void addAnnotation(Annotation ann)
  throws ChangeVetoException
  {
    mergeSet.add(ann);
  }
  
  protected ChangeSupport getChangeSupport(ChangeType changeType) {
    ChangeSupport changeSupport = super.getChangeSupport(changeType);
    
    if(
      ((changeType == null) || (changeType.isMatchingType(Annotation.PROPERTY))
       &&
      (propertyForwarder == null))
    ) {
      propertyForwarder = new PropertyForwarder(
        MergeAnnotation.this,
        changeSupport
      );
      for (Iterator i = mergeSet.iterator(); i.hasNext(); ) {
        Annotation a = (Annotation) i.next();
        
        a.addChangeListener(propertyForwarder, Annotation.PROPERTY);
      }
    }
    
    return changeSupport;
  }
  
  public void setProperty(Object key, Object value) throws ChangeVetoException {
    throw new ChangeVetoException("MergeAnnotations don't allow property setting at the moment");
  }
  
  public void removeProperty(Object key) throws ChangeVetoException {
    throw new ChangeVetoException("MergeAnnotations don't allow property removal at the moment");
  }
  
  public Object getProperty(Object key) {
    for (Iterator i = mergeSet.iterator(); i.hasNext(); ) {
      Annotation a = (Annotation) i.next();
      if (a.containsProperty(key)) {
        return a.getProperty(key);
      }
    }
    throw new NoSuchElementException("Can't find property " + key);
  }
  
  public boolean containsProperty(Object key) {
    for (Iterator i = mergeSet.iterator(); i.hasNext(); ) {
      Annotation a = (Annotation) i.next();
      if (a.containsProperty(key)) {
        return true;
      }
    }
    
    return false;
  }
  
  private Object getPropertySilent(Object key) {
    try {
      return getProperty(key);
    } catch (NoSuchElementException ex) {
      return null;
    }
  }
  
  /**
  * Return a <code>Set</code> containing all key objects
  * visible in this annotation.  The <code>Set</code> is
  * unmodifiable, but will dynamically reflect changes made
  * to the annotation.
  */
  
  public Set keys() {
    Set s = new HashSet();
    for (Iterator i = mergeSet.iterator(); i.hasNext(); ) {
      Annotation a = (Annotation) i.next();
      s.add(a.keys());
    }
    return s;
  }
  
  /**
  * Return a <code>Map</code> view onto this annotation.
  * The returned <code>Map</code> is unmodifiable, but will
  * dynamically reflect any changes made to this annotation.
  */
  
  public Map asMap() {
    return new MAMap();
  }
  
  private class MAEntrySet extends AbstractSet {
    private MAEntrySet() {
      super();
    }
    
    public Iterator iterator() {
      return new Iterator() {
        Iterator ksi = MergeAnnotation.this.keys().iterator();
        
        public boolean hasNext() {
          return ksi.hasNext();
        }
        
        public Object next() {
          Object k = ksi.next();
          Object v = getProperty(k);
          return new MAMapEntry(k, v);
        }
        
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }
    
    public int size() {
      return MergeAnnotation.this.keys().size();
    }
  }
  
  private class MAMapEntry implements Map.Entry {
    private Object key;
    private Object value;
    
    private MAMapEntry(Object key, Object value) {
      this.key = key;
      this.value = value;
    }
    
    public Object getKey() {
      return key;
    }
    
    public Object getValue() {
      return value;
    }
    
    public Object setValue(Object v) {
      throw new UnsupportedOperationException();
    }
    
    public boolean equals(Object o) {
      if (! (o instanceof Map.Entry)) {
        return false;
      }
      
      Map.Entry mo = (Map.Entry) o;
      return ((key == null ? mo.getKey() == null : key.equals(mo.getKey())) &&
      (value == null ? mo.getValue() == null : value.equals(mo.getValue())));
    }
    
    public int hashCode() {
      return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
    }
  }
  
  private class MAMap extends AbstractMap {
    MAEntrySet es;
    
    private MAMap() {
      super();
      es = new MAEntrySet();
    }
    
    public Set entrySet() {
      return es;
    }
    
    public Set keySet() {
      return MergeAnnotation.this.keys();
    }
    
    public Object get(Object key) {
      try {
        return getProperty(key);
      } catch (NoSuchElementException ex) {
      }
      
      return null;
    }
  }
  
  protected class PropertyForwarder extends ChangeForwarder {
    public PropertyForwarder(Object source, ChangeSupport cs) {
      super(source, cs);
    }
    
    public ChangeEvent generateEvent(ChangeEvent ce) {
      ChangeType ct = ce.getType();
      if(ct == Annotation.PROPERTY) {
        Object curVal = ce.getChange();
        if(curVal instanceof Object[]) {
          Object[] cur = (Object []) curVal;
          if(cur.length == 2) {
            Object key = cur[0];
            Object value = cur[0];
            if(getProperty(key) != value) {
              return new ChangeEvent(
              getSource(),
              Annotation.PROPERTY,
              curVal,
              ce.getPrevious(),
              ce
              );
            }
          }
        }
      }
      return null;
    }
  }
}
