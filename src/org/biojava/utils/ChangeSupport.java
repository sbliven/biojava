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
 */

package org.biojava.utils;

import java.io.Serializable;
import java.util.*;

/**
 * A utility class to provide management for informing ChangeListeners of
 * ChangeEvents.
 * <P>
 * This is loosly modeled after the standard PropertyChangeEvent objects.
 * <P>
 * For an object to correctly fire these events, they must follow a broad
 * outline like this:
 * <code><pre>
 * public void mutator(foo arg) throw ChangeVetoException {
 *   ChangeEvent cevt = new ChangeEvent(this, SOME_EVENT_TYPE, arg);
 *   synchronized(changeSupport) {
 *     changeSupport.firePreChangeEvent(cevt);
 *     // update our state using arg
 *     // ...
 *     changeSupport.firePostChangeEvent(cevt);
 *   }
 * }
 * </pre></code>
 * The methods that delegate adding and removing listeners to a ChangeSupport
 * must take responsibility for synchronizing on the delegate.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 * @since 1.1
 */
public class ChangeSupport {
  private int listenerCount;
  private int delta;
  private ChangeListener [] listeners;
  private ChangeType [] types;
  
  /**
   * Generate a new ChangeSupport instance.
   */
  public ChangeSupport() {
    this(5);
  }
  
  /**
   * Generate a new ChangeSupport instance which has room for initialSize
   * listeners before it needs to grow any resources.
   *
   * @param initialSize  the number of listeners that can be added before this
   *                     needs to grow for the first time
   */
  public ChangeSupport(int initialSize) {
    this(initialSize, 5);
  }
  
  /**
   * Generate a new ChangeSupport instance which has room for initialSize
   * listeners before it needs to grow any resources, and which will grow by
   * delta each time.
   *
   * @param initialSize  the number of listeners that can be added before this
   *                     needs to grow for the first time
   * @param delta  the number of listener slots that this will grow by each time
   *               it needs to
   */
  public ChangeSupport(int initialSize, int delta) {
    this.listenerCount = 0;
    this.listeners = new ChangeListener[initialSize];
    this.types = new ChangeType[initialSize];
    
    this.delta = delta;
  }

  /**
   * Add a listener that will be informed of all changes.
   *
   * @param cl  the ChangeListener to add
   */
  public void addChangeListener(ChangeListener cl) {
    addChangeListener(cl, null);
  }
  
  /**
   * Add a listener that will be informed of changes of a given type.
   *
   * @param cl  the ChangeListener
   * @param ct  the ChangeType it is to be informed of
   */
  public void addChangeListener(ChangeListener cl, ChangeType ct) {
    growIfNecisary();
    types[listenerCount] = ct;
    listeners[listenerCount] = cl;
    listenerCount++;
  }
  
  /**
   * Grows the internal resources if by adding one more listener they would be
   * full.
   */
  protected void growIfNecisary() {
    if(listenerCount == listeners.length) {
      int newLength = listenerCount + delta;
      ChangeListener[] newList = new ChangeListener[newLength];
      ChangeType[] newTypes = new ChangeType[newLength];
      
      System.arraycopy(listeners, 0, newList, 0, listenerCount);
      System.arraycopy(types, 0, newTypes, 0, listenerCount);
      
      listeners = newList;
      types = newTypes;
    }
  }
  
  /**
   * Remove a listener that was interested in all types of changes.
   *
   * @param cl  a ChangeListener to remove
   */
  public void removeChangeListener(ChangeListener cl) {
    removeChangeListener(cl, null);
  }
  
  /**
   * Remove a listener that was interested in a specific types of changes.
   *
   * @param cl  a ChangeListener to remove
   * @param ct  the ChangeType that it was interested in
   */
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
    for(int i = 0; i < listenerCount; i++) {
      if( (listeners[i] == cl) && (types[i] == ct) ) {
        listenerCount--;
        System.arraycopy(listeners, i+1, listeners, i, (listenerCount - i));
        System.arraycopy(types, i+1, types, i, (listenerCount - i));
        return;
      }
    }
  }
  
  /**
   * Inform the listeners that a change is about to take place using their
   * firePreChangeEvent methods.
   * <P>
   * Listeners will be informed if they were interested in all types of event,
   * or if ce.getType() is equal to the type they are registered for.
   *
   * @param ce  the ChangeEvent to pass on
   * @throws ChangeVetoException if any of the listeners veto this change
   */
  public void firePreChangeEvent(ChangeEvent ce)
  throws ChangeVetoException {
    ChangeType ct = ce.getType();
    for(int i = 0; i < listenerCount; i++) {
      ChangeType lt = types[i];
      if( (lt == null) || (lt == ct) ) {
        listeners[i].preChange(ce);
      }
    }
  }
  
  /**
   * Inform the listeners that a change has taken place using their
   * firePostChangeEvent methods.
   * <P>
   * Listeners will be informed if they were interested in all types of event,
   * or if ce.getType() is equal to the type they are registered for.
   *
   * @param ce  the ChangeEvent to pass on
   */
  public void firePostChangeEvent(ChangeEvent ce) {
    ChangeType ct = ce.getType();
    for(int i = 0; i < listenerCount; i++) {
      ChangeType lt = types[i];
      if( (lt == null) || (lt == ct) ) {
        listeners[i].postChange(ce);
      }
    }
  }
}
