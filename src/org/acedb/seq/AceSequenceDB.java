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


package org.acedb.seq;

import java.util.*;

import org.acedb.*;
import org.biojava.bio.seq.*;

/**
 * @author Matthew Pocock
 */

public class AceSequenceDB implements SequenceDB {
  protected Database aceDB;
  protected AceSet seqSet;
  
  public Sequence getSequence(String id) throws SeqException {
    try {
      return new AceSequence(aceDB, id);
    } catch (AceException ae) {
      throw new SeqException(ae, "unable to retrieve sequence " + id);
    }
  }
  
  public Set ids() {
    return new AbstractSet() {
	public int size() {
	    return seqSet.size();
	}

	public Iterator iterator() {
	    return seqSet.nameIterator();
	}

	public boolean contains(Object o) {
	    if (! (o instanceof String))
		return false;
	    return seqSet.contains((String) o);
	}
    };
  }
  
  public SequenceIterator sequenceIterator() {
    return new SequenceIterator() {
      Iterator id = ids().iterator();
      
      public boolean hasNext() {
        return id.hasNext();
      }
      
      public Sequence nextSequence() throws SeqException {
        return getSequence((String) id.next());
      }
    };
  }
  
  public AceSequenceDB(Database aceDB, String pattern) throws AceException {
    this.aceDB = aceDB;
    this.seqSet = aceDB.select(AceType.getClassType(aceDB, "Sequence"), pattern);
  }

  public AceSequenceDB(Database aceDB) throws AceException {
    this(aceDB, "*");
  }
} 
