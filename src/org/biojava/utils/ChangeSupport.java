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
import java.lang.ref.*;

/**
 * A utility class to provide management for informing ChangeListeners of
 * ChangeEvents.
 * <P>
 * This is loosely modelled after the standard PropertyChangeEvent objects.
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
 * @author Keith James (docs)
 * @since 1.1
 */

public class ChangeSupport {
  private int listenerCount;
  private int delta;
  private Reference[] listeners;
  private ChangeType[] types;

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
    this.listeners = new Reference[initialSize];
    this.types = new ChangeType[initialSize];

    this.delta = delta;
  }

  /**
   * Add a listener that will be informed of all changes.
   *
   * @param cl  the ChangeListener to add
   */
  public void addChangeListener(ChangeListener cl) {
    addChangeListener(cl, ChangeType.UNKNOWN);
  }

  /**
   * Add a listener that will be informed of changes of a given type (and it's subtypes)
   *
   * @param cl  the ChangeListener
   * @param ct  the ChangeType it is to be informed of
   */
  public synchronized void addChangeListener(ChangeListener cl, ChangeType ct) {
      // Needed to synchronize this method in case multiple threads attempt to add a change listener at the same time.
      // Richard J. Fox  05/30/2001
      if (ct == null) {
	  throw new NestedError("Since 1.2, listeners registered for the null changetype are not meaningful.  Please register a listener for ChangeType.UNKNOWN instead");
      }

      growIfNecessary();
      types[listenerCount] = ct;
      listeners[listenerCount] = new WeakReference(cl);
      listenerCount++;
  }

  /**
   * Grows the internal resources if by adding one more listener they would be
   * full.
   */
  protected void growIfNecessary() {
    if(listenerCount == listeners.length) {
      int newLength = listenerCount + delta;
      Reference[] newList = new Reference[newLength];
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
    removeChangeListener(cl, ChangeType.UNKNOWN);
  }

  /**
   * Remove a listener that was interested in a specific types of changes.
   *
   * @param cl  a ChangeListener to remove
   * @param ct  the ChangeType that it was interested in
   */
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
    for(int i = 0; i < listenerCount; i++) {
      if( (listeners[i].get() == cl) && (types[i] == ct) ) {
        listenerCount--;
        System.arraycopy(listeners, i+1, listeners, i, (listenerCount - i));
        System.arraycopy(types, i+1, types, i, (listenerCount - i));
        return;
      }
    }
  }

    /**
     * Remove all references to listeners which have been cleared by the
     * garbage collector.  This method should only be called when the
     * object is locked.
     */

    protected void reapGarbageListeners() {
	int pp = 0;
	for (int p = 0; p < listenerCount; ++p) {
	    Reference r = listeners[p];
	    if (r.get() != null) {
	        types[pp] = types[p];
		listeners[pp] = r;
		pp++;
	    }
	}
	listenerCount = pp;
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
      boolean needToReap = false;

    ChangeType ct = ce.getType();
    int listenerCount = this.listenerCount;
    ChangeType[] types = new ChangeType[listenerCount];
    System.arraycopy(this.types, 0, types, 0, listenerCount);

    Reference[] listeners = new Reference[listenerCount];
    System.arraycopy(this.listeners, 0, listeners, 0, listenerCount);

    for(int i = 0; i < listenerCount; i++) {
      ChangeType lt = types[i];
      if( ct.isMatchingType(lt)) {
        ChangeListener cl = (ChangeListener) listeners[i].get();
	if (cl != null) {
	    cl.preChange(ce);
	} else {
	    needToReap = true;
	}
      }
    }

    if (needToReap)
	reapGarbageListeners();
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
      boolean needToReap = false;

    ChangeType ct = ce.getType();
    int listenerCount = this.listenerCount;
    ChangeType[] types = new ChangeType[listenerCount];
    System.arraycopy(this.types, 0, types, 0, listenerCount);

    Reference[] listeners = new Reference[listenerCount];
    System.arraycopy(this.listeners, 0, listeners, 0, listenerCount);

    for(int i = 0; i < listenerCount; i++) {
      ChangeType lt = types[i];
      if( ct.isMatchingType(lt) ) {
        ChangeListener cl = (ChangeListener) listeners[i].get();
	if (cl != null) {
	    cl.postChange(ce);
	} else {
	    needToReap = true;
	}
      }
    }

    if (needToReap)
	reapGarbageListeners();
  }
}
