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

import java.util.*;
import java.lang.ref.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * An abstract implementation of SequenceDB that provides the sequenceIterator
 * method.
 *
 * @author Matthew Pocock
 */
public abstract class AbstractSequenceDB implements SequenceDB {
  protected ChangeSupport changeSupport = null;

  protected void generateChangeSupport(ChangeType changeType) {
    if(changeSupport == null) {
      changeSupport = new ChangeSupport();
    }
  }

  public void addChangeListener(ChangeListener cl) {
    generateChangeSupport(null);
    changeSupport.addChangeListener(cl);
  }
  
  public void addChangeListener(ChangeListener cl, ChangeType ct) {
    generateChangeSupport(ct);
    changeSupport.addChangeListener(cl, ct);
  }
  
  public void removeChangeListener(ChangeListener cl) {
    changeSupport.removeChangeListener(cl);
  }
  
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
    changeSupport.removeChangeListener(cl, ct);
  }

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
  
  public void addSequence(Sequence seq)
  throws BioException, ChangeVetoException {
    throw new ChangeVetoException("AbstractSequenceDB is immutable");
  }
  
  public void removeSequence(String id)
  throws BioException, ChangeVetoException {
    throw new ChangeVetoException("AbstractSequenceDB is immutable");
  }
}
