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


package org.acedb.seq;

import java.util.*;

import org.acedb.*;
import org.biojava.bio.*;
/**
 * @author Matthew Pocock
 */

public class AceAnnotation implements Annotation {
  protected AceNode node;
  
  public Object getProperty(Object key) throws NoSuchElementException {
    if(!(key instanceof String))
      throw new ClassCastException("AceAnnotation objects only allow String keys");
      
    String name = (String) key;
    try {
      return node.retrieve(name);
    } catch (AceException ae) {
      throw new NoSuchElementException("Could not find object for key '" + name + ": " +
                                       ae.getMessage());
    }
  }
  
  public void setProperty(Object key, Object value) throws UnsupportedOperationException {
    throw new UnsupportedOperationException("AceAnnotation objects are immutable");
  }
  
  public Set keys() {
    return new AbstractSet() {
      public Iterator iterator() {
        return node.iterator();
      }
      
      public int size() {
        return node.size();
      }
      
      public boolean contains(Object obj) {
        if(obj instanceof String)
          return node.contains((String) obj);
        return super.contains(obj);
      }
    };
  }
  
  public Map asMap() {
    Map map = new HashMap();
    
    for(Iterator i = keys().iterator(); i.hasNext(); ) {
      Object key = i.next();
      map.put(key, getProperty(key));
    }
    
    return map;
  }
  
  public AceAnnotation(AceNode node) {
    this.node = node;
  }
}
    
