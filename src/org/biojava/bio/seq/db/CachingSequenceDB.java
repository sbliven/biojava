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

import java.io.Serializable;

import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.*;
import java.util.*;
import java.lang.ref.*;

/**
 * SequenceDB implementation that caches the results of another SequenceDB.
 * 
 * @author Matthew Pocock
 */

public class CachingSequenceDB implements SequenceDB, Serializable {
  private final SequenceDB parent;
  private final transient Map cache;
  
  {
    cache = new HashMap();
  }
  
  /**
   * Create a new CachingSequenceDB that caches the sequences in parent.
   *
   * @param parent the SequenceDB to cache
   */
  public CachingSequenceDB(SequenceDB parent) {
    this.parent = parent;
  }
  
  /**
   * Return the parent SequenceDB.
   *
   * @return the parent SequenceDB
   */
  public SequenceDB getParent() {
    return this.parent;
  }
  
  public String getName() {
    return parent.getName();
  }
  
  public Sequence getSequence(String id) throws BioException {
    SoftReference ref = (SoftReference) cache.get(id);
    Sequence seq;
    if(ref == null) {
      seq = parent.getSequence(id);
      cache.put(id, new SoftReference(seq));
    } else {
      seq = (Sequence) ref.get();
    }
    return seq;
  }
  
  public Set ids() {
    return parent.ids();
  }
  
  public SequenceIterator sequenceIterator() {
    return new SequenceIterator() {
      private Iterator pID = parent.ids().iterator();
      
      public boolean hasNext() {
        return pID.hasNext();
      }
      
      public Sequence nextSequence() throws BioException {
        return getSequence((String) pID.next());
      }
    };
  }
}
