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
 * An abstract implementation of SequenceDB that provides the sequenceIterator
 * method.
 *
 * @author Matthew Pocock
 */
public abstract class AbstractSequenceDB implements SequenceDB {
  public SequenceIterator sequenceIterator() {
    return new SequenceIterator() {
      private Iterator pID = ids().iterator();
      
      public boolean hasNext() {
        return pID.hasNext();
      }
      
      public Sequence nextSequence() throws BioException {
        return getSequence((String) pID.next());
      }
    };
  }
}
