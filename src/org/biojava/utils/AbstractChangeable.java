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

/**
 * Useful base-class for objects implementing Changeable
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */
 
public abstract class AbstractChangeable implements Changeable {
  private transient ChangeSupport changeSupport = null;

  protected boolean hasListeners() {
    return changeSupport != null && changeSupport.hasListeners();
  }

  /**
   * Called the first time a ChangeSupport object is needed.  Override this if
   * you want to set the Unchanging set on the ChangeSupport, or if you want to
   * install listeners on other objects when the change system is initialized.
   *
   * @since 1.3
   */
  
  protected ChangeSupport generateChangeSupport() {
      return new ChangeSupport();
  }
  
  /**
   * Called to retrieve the ChangeSupport for this object.
   *
   * <p>
   * Your implementation of this method should have the following structure:
   * <code><pre>
   * ChangeSupport cs = super.getChangeSupport(ct);
   *
   * if(someForwarder == null && ct.isMatching(SomeInterface.SomeChangeType)) {
   *   someForwarder = new ChangeForwarder(...
   *
   *   this.stateVariable.addChangeListener(someForwarder, VariableInterface.AChange);
   * }
   *
   * return cs;
   * </pre></code>
   *
   * It is usual for the forwarding listeners (someForwarder in this example) to
   * be transient and lazily instantiated. Be sure to register & unregister the
   * forwarder in the code that does the ChangeEvent handling in setter methods.
   */
  
  protected ChangeSupport getChangeSupport(ChangeType ct) {
    if(changeSupport != null) {
      return changeSupport;
    }
    
    synchronized(this) {
      if(changeSupport == null) {
        changeSupport = generateChangeSupport();
      }
    }
    
    return changeSupport;
  }

  public final void addChangeListener(ChangeListener cl) {
    addChangeListener(cl, ChangeType.UNKNOWN);
  }

  public final void addChangeListener(ChangeListener cl, ChangeType ct) {
    ChangeSupport cs = getChangeSupport(ct);
    cs.addChangeListener(cl, ct);
  }

  public final void removeChangeListener(ChangeListener cl) {
    removeChangeListener(cl, ChangeType.UNKNOWN);
  }

  public final void removeChangeListener(ChangeListener cl, ChangeType ct) {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(ct);
      cs.removeChangeListener(cl, ct);
    }
  }
  
  public final boolean isUnchanging(ChangeType ct) {
    ChangeSupport cs = getChangeSupport(ct);
    return cs.isUnchanging(ct);
  }
}
