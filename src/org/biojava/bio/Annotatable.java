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

/**
 * Flags an object as having associated annotation.
 * <P>
 * This interface was introduced in retrospect so that UI code could interrogate
 * an object to see if it was Annotatable, and if so pop up a suitable GUI for
 * browsing the annotation.
 *
 * @author  Matthew Pocock
 */
public interface Annotatable {
  /**
   * Should return the associated annotation object.
   *
   * @return	an Annotation object, never null
   */
  Annotation getAnnotation();
}
