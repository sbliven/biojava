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

/**
 * An implementation of SequenceDB that uses an underlying HashMap to stoor the
 * sequence objects.
 *
 * @author Matthew Pocock
 */
public class HashSequenceDB implements SequenceDB {
  /**
   * The sequence-by-id map.
   */
  private Map sequenceByID;
  
  /**
   * An object to extract an ID for a sequence.
   */
  private IDMaker idMaker;
  
  /**
   * Initialize sequenceByID.
   */
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

  /**
   * Add a sequence under a particular id.
   *
   * @param id  the id to use
   * @param seq the Sequence to add
   */
  public void addSequence(String id, Sequence seq) {
    sequenceByID.put(id, seq);
  }

  /**
   * Retrieve the IDMaker associated with this database.
   *
   * @return the current IDMaker object
   */
  public IDMaker getIDMaker() {
    return idMaker;
  }
  
  /**
   * Add a sequence under its default id.
   *
   * @param seq  the Sequence to add
   */
  public void addSequence(Sequence seq) {
    sequenceByID.put(idMaker.calcID(seq), seq);
  }

  /**
   * Generate a HashSequenceDB object that will use idMaker to generate ids for
   * sequences.
   *
   * @param idMaker the object that will work out the default id for a sequence
   */
  public HashSequenceDB(IDMaker idMaker) {
    this.idMaker = idMaker;
  }

  /**
   * No-args constructor for beany stuff.
   */
  protected HashSequenceDB() {}
  
  /**
   * You should make one of these to define how to make an ID for a sequence.
   * <P>
   * This gives you the freedom to re-map names from the fasta names to some
   * other representation if you need to.
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
   *
   * @author Matthew Pocock
   */
  public final static IDMaker byURN = new IDMaker() {
    public String calcID(Sequence seq) {
      return seq.getURN();
    }
  };
  
  /**
   * A simple implementation of IDMaker that hashes by sequence name.
   *
   * @author Matthew Pocock
   */
  public final static IDMaker byName = new IDMaker() {
    public String calcID(Sequence seq) {
      return seq.getName();
    }
  };
}
