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
 * Annotation that is optimized for memory usage.  Access time
 * is linear, so SmallAnnotations are not recommended when
 * the number of entries is large.  However, they are fine for
 * small numbers of keys.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.2
 */

public class SmallAnnotation extends AbstractAnnotation {
  private Map properties;
  
  protected final Map getProperties() {
    if(!propertiesAllocated()) {
      properties = new SmallMap();
    }
    return properties;
  }
  
  protected final boolean propertiesAllocated() {
    return properties != null;
  }

  public SmallAnnotation() {
    super();
  }
  
  public SmallAnnotation(Annotation ann)
  throws IllegalArgumentException {
    super(ann);
  }
  
  public SmallAnnotation(Map map) {
    super(map);
  }
}

