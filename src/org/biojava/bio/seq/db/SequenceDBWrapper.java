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

package org.biojava.bio.seq.db;

import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.*;
import java.util.*;
import java.lang.ref.*;

/**
 * An abstract implementation of SequenceDB that wraps up another database.
 *
 * @author Matthew Pocock
 */
public abstract class SequenceDBWrapper extends AbstractSequenceDB
implements java.io.Serializable{
  private final SequenceDB parent;
  
  /**
   * Return the parent SequenceDB.
   *
   * @return the parent SequenceDB
   */
  public SequenceDB getParent() {
    return this.parent;
  }

  public SequenceDBWrapper(SequenceDB parent) {
    this.parent = parent;
  }
}
