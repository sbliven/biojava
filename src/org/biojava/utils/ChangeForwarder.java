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
 */
public abstract class ChangeForwarder implements ChangeListener {
  private final Object source;
  private final transient ChangeSupport changeSupport;
  
  public ChangeForwarder(Object source, ChangeSupport changeSupport) {
    this.source = source;
    this.changeSupport = changeSupport;
  }
  
  public Object getSource() { return source; }
  public ChangeSupport changeSupport() { return changeSupport; }
  
  /**
   * Return the new event to represent the originating event ce.
   * <P>
   * The returned ChangeEvent is the event that will be fired, and should be
   * built from information in the original event. If it is null, then no event
   * will be fired.
   *
   * @param ce  the originating ChangeEvent
   * @return a new ChangeEvent to pass on, or null if no event should be sent
   */
  protected abstract ChangeEvent generateEvent(ChangeEvent ce);
    
  public void preChange(ChangeEvent ce)
  throws ChangeVetoException {
    ChangeEvent nce = generateEvent(ce);
    if(nce != null) {
      changeSupport.firePreChangeEvent(nce);
    }
  }
  
  public void postChange(ChangeEvent ce) {
    ChangeEvent nce = generateEvent(ce);
    if(nce != null) {
      changeSupport.firePostChangeEvent(nce);
    }
  }
}
