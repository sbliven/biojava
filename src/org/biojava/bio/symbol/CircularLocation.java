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

package org.biojava.bio.symbol;

import org.biojava.bio.*;
import org.biojava.utils.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.seq.io.*;

import java.util.*;

/**
 * Circular view onto an underlying Location instance.
 *
 * @author Matthew Pocock
 * @author Mark Schreiber
 * @since 1.2
 */
public class CircularLocation
extends AbstractDecorator {
  private final int length;
  
  public final int getLength() {
    return length;
  }

  public CircularLocation(Location wrapped, int length) {
    super(wrapped);
    this.length = length;
  }
  
  protected Location decorate(Location loc) {
    return new CircularLocation(loc, getLength());
  }
  
  public boolean contains(int p) {
    int pp = p % getLength() + (super.getMin() / getLength());
    
    return getWrapped().contains(pp);
  }
}
