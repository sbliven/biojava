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

import org.biojava.utils.*;

/**
 * Flags an object as having associated annotation.
 * <P>
 * This interface was introduced in retrospect so that UI code could interrogate
 * an object to see if it was Annotatable, and if so pop up a suitable GUI for
 * browsing the annotation.
 *
 * @author  Matthew Pocock
 */
public interface Annotatable extends Changeable {
  /**
   * Signals that the associated Annotation has altered in some way. The
   * chainedEvent property should refer back to the event fired by the
   * Annotation object.
   */
  public static final ChangeType ANNOTATION = new ChangeType(
    "the assicated annotation has changed",
    "org.biojava.bio.Annotatable",
    "ANNOTATION"
  );
    
  /**
   * Should return the associated annotation object.
   *
   * @return	an Annotation object, never null
   */
  Annotation getAnnotation();
  
  /**
   * A helper class so that you don't have to worry about forwarding events from
   * the Annotaion object to the Annotatable one.
   * <P>
   * Once a listener is added to your Annotatable that is interested in
   * ANNOTATION events, then instantiate one of these and add it as a listener
   * to the annotation object. It will forward the events to your listeners and
   * translate them accordingly.
   *
   * @author Matthew Pocock
   */
  static class AnnotationForwarder extends ChangeForwarder {
    public AnnotationForwarder(Object source, ChangeSupport cs) {
      super(source, cs);
    }
    
    protected ChangeEvent generateEvent(ChangeEvent ce) {
      ChangeType ct = ce.getType();
      if(ct == Annotation.PROPERTY) {
        return new ChangeEvent(
          getSource(),
          ANNOTATION,
          ct
        );
      }
      return null;
    }
  }
}
