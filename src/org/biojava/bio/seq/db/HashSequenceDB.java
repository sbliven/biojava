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
import java.io.*;

import org.biojava.utils.StaticMemberPlaceHolder;
import org.biojava.bio.*;
import org.biojava.bio.seq.*;

/**
 * An implementation of SequenceDB that uses an underlying HashMap to stoor the
 * sequence objects.
 *
 * @author Matthew Pocock
 * @author <A href="mailto:Gerald.Loeffler@vienna.at">Gerald Loeffler</A>
 */
public class HashSequenceDB implements SequenceDB, Serializable {
  /**
   * The sequence-by-id map.
   */
  final private Map sequenceByID;
  
  /**
   * An object to extract an ID for a sequence.
   */
  final private IDMaker idMaker;

  /** 
   * The name of this sequence database.
   */
  private String name;

  /**
   * Initialize sequenceByID.
   */
  {
    sequenceByID = new HashMap();
  }

  public String getName() {
    return name;
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
   * Generate a HashSequenceDB object that will use byName to generate ids for
   * sequences and have a null name.
   */
  public HashSequenceDB() {
    this(byName, null);
  }
  
  /**
   * Generate a HashSequenceDB object that will use idMaker to generate ids for
   * sequences and have a null name.
   *
   * @param idMaker the object that will work out the default id for a sequence
   */
  public HashSequenceDB(IDMaker idMaker) {
    this(idMaker, null);
  }
  
  /**
   * Generate a HashSequenceDB object that will use byName to generate ids and
   * will have the requested name.
   *
   * @param name the name for this database
   */
  public HashSequenceDB(String name) {
    this(byName, name);
  }
  
  /**
   * Generate a HashSequenceDB object that will use idMaker to generate ids for
   * sequences and have the requested name.
   *
   * @param idMaker the object that will work out the default id for a sequence
   * @param name the name for this database
   */
  public HashSequenceDB(IDMaker idMaker, String name) {
    this.idMaker = idMaker;
    this.name = name;
  }

  /**
   * Interface for objects that define how to make an ID for a sequence.
   * <P>
   * Nine times out of ten, you will use one of HashSequenceDB.byURN or
   * HashSequenceDB.byName, but once in a blue-moon, you will want some other
   * systematic way of retrieveing Sequences. This interface is here to allow
   * you to plug in this funcitonality if you need it.
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
  public final static IDMaker byURN = new SerializableIDMaker() {
    public String calcID(Sequence seq) {
      return seq.getURN();
    }
    public Object writeReplace() throws IOException {
      try {
        return new StaticMemberPlaceHolder(
          HashSequenceDB.class.getField("byURN")
        );
      } catch (NoSuchFieldException nsfe) {
        throw new BioError(
          nsfe,
          "Could not find field while serializing"
        );
      }
    }
  };
  
  /**
   * A simple implementation of IDMaker that hashes by sequence name.
   *
   * @author Matthew Pocock
   */
  public final static IDMaker byName = new SerializableIDMaker() {
    public String calcID(Sequence seq) {
      return seq.getName();
    }
    public Object writeReplace() throws IOException {
      try {
        return new StaticMemberPlaceHolder(
          HashSequenceDB.class.getField("byName")
        );
      } catch (NoSuchFieldException nsfe) {
        throw new BioError(
          nsfe,
          "Could not find field while serializing"
        );
      }
    }
  };
  
  private interface SerializableIDMaker extends IDMaker, Serializable {}
}
