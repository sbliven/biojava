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

import java.util.*;
import java.io.*;

import org.biojava.utils.*;

/**
 * <p>A no-frills implementation of Annotation that is just a wrapper
 * around a Map.</p>
 *
 * <p>It will allow you to set any property, but will throw exceptions
 * if you try to retrieve a property that is not set.</p>
 *
 * @author Matthew Pocock
 * @author Greg Cox
 */
public class SimpleAnnotation extends AbstractAnnotation {
  /**
   * The properties map. This may be null if no property values have
   * yet been set.
   */
  private Map properties;

  protected final Map getProperties() {
    if(!propertiesAllocated()) {
      properties = new HashMap();
    }
    return properties;
  }

  protected final boolean propertiesAllocated() {
    return properties != null;
  }

  public SimpleAnnotation() {
    super();
  }
  
  public SimpleAnnotation(Annotation ann)
  throws IllegalArgumentException {
    super(ann);
  }
  
  public SimpleAnnotation(Map map) {
    super(map);
  }
}
