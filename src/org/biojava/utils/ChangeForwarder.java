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
 * This is a ChangeListener that is designed to adapt events of one type from
 * one source to events of another type emitted by another source. For example,
 * you could adapt events made by edits in a database to being events fired by
 * a sequence implementation.
 *
 * @author Matthew Pocock
 * @since 1.1
 */
public class ChangeForwarder implements ChangeListener {
  private final Object source;
  private final transient ChangeSupport changeSupport;
  
  public ChangeForwarder(Object source, ChangeSupport changeSupport) {
    this.source = source;
    this.changeSupport = changeSupport;
  }
  
  /**
   * Retrieve the 'source' object for <code>ChangeEvent</code>s fired by this forwarder.
   *
   * @return the source Object
   */
  public Object getSource() { return source; }
  
  /**
   * Return the underlying <code>ChangeSupport</code> instance that can be used to
   * fire <code>ChangeEvent</code>s and mannage listeners.
   *
   * @return the ChangeSupport delegate
   */
  public ChangeSupport changeSupport() { return changeSupport; }
  
  /**
   * <p>
   * Return the new event to represent the originating event ce.
   * </p>
   *
   * <p>
   * The returned ChangeEvent is the event that will be fired, and should be
   * built from information in the original event. If it is null, then no event
   * will be fired.
   * </p>
   *
   * <p>
   * The default implementation just constructs a ChangeEvent of the same type
   * that chains back to ce.
   * </p>
   *
   * @param ce  the originating ChangeEvent
   * @return a new ChangeEvent to pass on, or null if no event should be sent
   * @throws ChangeVetoException if for any reason this event can't be handled
   */
  protected ChangeEvent generateEvent(ChangeEvent ce)
  throws ChangeVetoException {
    return new ChangeEvent(
      getSource(), ce.getType(),
      null, null,
      ce
    );
  }
    
  public void preChange(ChangeEvent ce)
  throws ChangeVetoException {
    ChangeEvent nce = generateEvent(ce);
    if(nce != null) {
      changeSupport.firePreChangeEvent(nce);
    }
  }
  
  public void postChange(ChangeEvent ce) {
    try {
      ChangeEvent nce = generateEvent(ce);
      if(nce != null) {
        changeSupport.firePostChangeEvent(nce);
      }
    } catch (ChangeVetoException cve) {
      throw new AssertionFailure(
        "Assertion Failure: Change was vetoed after it had been accepted by preChange",
        cve
      );
    }
  }
}
