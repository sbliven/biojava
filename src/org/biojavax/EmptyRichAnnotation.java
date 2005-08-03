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

/*
 * EmptyRichAnnotation.java
 *
 * Created on August 3, 2005, 1:15 PM
 */

package org.biojavax;

import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.biojava.bio.Annotation;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.StaticMemberPlaceHolder;
import org.biojava.utils.Unchangeable;

/**
 * A place holder for a RichAnnotation that prevents null having to be used
 * @author Mark Schreiber
 */
class EmptyRichAnnotation extends Unchangeable implements RichAnnotation, Serializable{
  public Object getProperty(Object key) throws NoSuchElementException {
    throw new NoSuchElementException(
      "There are no keys in the Empty RichAnnotation object: " +
      key
    );
  }
  
  public Note getNote(Note note){
    throw new NoSuchElementException(
      "There are no notes in the Empty RichAnnotation object"
    );  
  }
  
  public void setProperty(Object key, Object value)
  throws ChangeVetoException {
    throw new ChangeVetoException(
      "You can not add properties to the Empty RichAnnotation object: " +
      key + " -> " + value
    );
  }
  
  public void setNoteSet(Set notes) throws ChangeVetoException{
      throw new ChangeVetoException(
      "You can not add Notes to the Empty RichAnnotation object");
  }

  public void addNote(Note note) throws ChangeVetoException{
      throw new ChangeVetoException(
      "You can not add Notes to the Empty RichAnnotation object");
  }
  
  public void clear() throws ChangeVetoException{
      throw new ChangeVetoException("You cannot clear the Empty RichAnnotation object");
  }
  
  public void removeProperty(Object key)
  throws ChangeVetoException 
  {
    throw new ChangeVetoException(
      "You cannot remove properties from the Empty RichAnnotation (!)"
    );
  }
  
  public void removeNote(Note note)
  throws ChangeVetoException 
  {
    throw new ChangeVetoException(
      "You cannot remove notes from the Empty RichAnnotation (!)"
    );
  }
  
  public boolean containsProperty(Object key) {
    return false;
  }
  
  public boolean contains(Note note){
      return false;
  }
  
  public Set keys() {
    return Collections.EMPTY_SET;
  }
  
  public Set getNoteSet(){
      return Collections.EMPTY_SET;
  }
  
  public Map asMap() {
    return new HashMap();
  }
  
  private Object writeReplace() throws ObjectStreamException {
    try {
      return new StaticMemberPlaceHolder(RichAnnotation.class.getField("EMPTY_ANNOTATION"));
    } catch (NoSuchFieldException nsfe) {
      throw new NotSerializableException(nsfe.getMessage());
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
