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
import org.biojava.bio.*;

/**
 * A database of sequences.
 * <P>
 * This may have several implementations with rich behaviour, but basicaly most
 * of the time you will just use the interface methods to do stuff. A sequence
 * database contains a finite number of sequences stoored under unique keys.
 *
 * @author Matthew Pocock
 * @author <A href="mailto:Gerald.Loeffler@vienna.at">Gerald Loeffler</A>
 */
public interface SequenceDB {
  /**
   * Get the name of this sequence database.
   *
   * @return the name of the sequence database, which may be null.
   */
  String getName();

  /**
   * Retrieve a single sequence by its id.
   *
   * @param the id to retrieve by
   * @return  the Sequence with that id
   * @throws BioException if for any reason the sequence could not be retrieved
   */
  Sequence getSequence(String id) throws BioException;
  
  /**
   * Get an imutable set of all of the IDs in the database. The ids are legal
   * arguments to getSequence.
   *
   * @return  a Set of ids - at the moment, strings
   */
  Set ids();
  
  /**
   * Returns a SequenceTterator over all sequences in the database. The order
   * of retrieval is undefined.
   *
   * @return  a SequenceIterator over all sequences
   */
  SequenceIterator sequenceIterator();
}
