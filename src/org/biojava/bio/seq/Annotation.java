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


package org.biojava.bio.seq;

import java.util.*;

/**
 * Annotation for many, many biological objects.
 */
public interface Annotation {
  /**
   * Retrieve the value of a property by key.
   *
   * @param key  the key of the property to retrieve
   * @return  the object associated with that key
   * @throws NoSuchElementException if there is no property with the key
   */
  Object getProperty(Object key) throws NoSuchElementException;
  
  /**
   * Set the value of a property.
   * <P>
   * This method throws an exception if either properties can not be
   * added to this object, or that this particular property is immutable.
   *
   * @param key the key object
   * @param value the new value for this key
   * @throws IllegalArgumentException if the property <code>key</code> cannot
   *         be changed.
   * @throws UnsupportedOperationException if this annotation object is immutable.
   */
  void setProperty(Object key, Object value)
       throws IllegalArgumentException, UnsupportedOperationException;
       
  /**
   * Get a set of key objects.
   *
   * @return  a Set of key objects
   */
  Set keys();
  
  static final Annotation EMPTY_ANNOTATION = new EmptyAnnotation();
  
  class EmptyAnnotation implements Annotation {
    public Object getProperty(Object key) throws NoSuchElementException {
      throw new NoSuchElementException("There are no keys in the Empty Annotaion object: " + key);
    }
    
    public void setProperty(Object key, Object value) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("You can not add propertys to the Empy Annotaion object: " +
        key + " -> " + value);
    }
    
    public Set keys() {
      return Collections.EMPTY_SET;
    }
  }
}
