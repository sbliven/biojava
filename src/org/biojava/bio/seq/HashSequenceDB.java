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


package org.biojava.bio.seq;

import java.util.*;

import org.biojava.bio.seq.*;

public class HashSequenceDB implements SequenceDB {
  private Map sequenceByID;
  private IDMaker idMaker;
  
  {
    sequenceByID = new HashMap();
  }

  public Sequence getSequence(String id) {
    return (Sequence) sequenceByID.get(id);
  }

  public Set ids() {
    return sequenceByID.keySet();
  }

  public SequenceIterator sequenceIterator() {
    return new SequenceIterator() {
      Iterator seqI = sequenceByID.values().iterator();
      public boolean hasNext() { return seqI.hasNext(); }
      public Sequence nextSequence() { return (Sequence) seqI.next(); }
    };
  }

  public void addSequence(String id, Sequence seq) {
    sequenceByID.put(id, seq);
  }

  public void addSequence(Sequence seq) {
    sequenceByID.put(idMaker.calcID(seq), seq);
  }

  public HashSequenceDB(IDMaker idMaker) {
    this.idMaker = idMaker;
  }
  
  /**
   * Make a little one of these to define how to make an ID for a sequence.
   */
  public static interface IDMaker {
    /**
     * Calculate the id for a sequence.
     * <P>
     * Each unique sequence should return a unique ID.
     *
     * @param seq the sequence to ID
     * @return the id for the sequence
     */
    String calcID(Sequence seq);
  }
  
  /**
   * A simple implementation of IDMaker that hashes by URN.
   */
  public final static IDMaker byURN = new IDMaker() {
    public String calcID(Sequence seq) {
      return seq.getURN();
    }
  };
}
