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
 * This is a flag interface that defines the common add/remove listener methods
 * for classes and interfaces that wish to indicate that they are sources of
 * ChangeEvents.
 *
 * @author Matthew Pocock
 */
public interface Changeable {
  /**
   * Add a listener that will be informed of all changes.
   *
   * @param cl  the ChangeListener to add
   */
  public void addChangeListener(ChangeListener cl);
  
  /**
   * Add a listener that will be informed of changes of a given type.
   *
   * @param cl  the ChangeListener
   * @param ct  the ChangeType it is to be informed of
   */
  public void addChangeListener(ChangeListener cl, ChangeType ct);
  
  /**
   * Remove a listener that was interested in all types of changes.
   *
   * @param cl  a ChangeListener to remove
   */
  public void removeChangeListener(ChangeListener cl);

  /**
   * Remove a listener that was interested in a specific types of changes.
   *
   * @param cl  a ChangeListener to remove
   * @param ct  the ChangeType that it was interested in
   */
  public void removeChangeListener(ChangeListener cl, ChangeType ct);
}
