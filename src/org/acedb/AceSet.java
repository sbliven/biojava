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


package org.acedb;

import java.util.*;

/**
 * A generalized set of named objects used within the ACeDB
 * system.
 *
 * @author Matthew Pocock
 * @author Thomas Down
 */
public interface AceSet {
  /**
   * The number of items in this AceSet.
   */
  int size();
  
  /**
   * An iterator over the names associated with each thing in the set.
   * Names are Strings.
   */
  Iterator nameIterator();
  
  /**
   * An iterator over every thing in this set.
   * Things are AceSet-derived objects.
   */
  Iterator iterator();

  /**
   * Returns whether an object is contained under a given name.
   */
  boolean contains(String name);
  
  /**
   * The logical parent of this object or null if there is no logical parent.
   */
  AceSet getParent();
  
  /**
   * A URL that can be used to retrieve this set (assuming that the server will
   * still be there when you try).
   *
   * @throws AceException if for any reason the URL can't be constructed
   */
  AceURL toURL() throws AceURLException;
  
  /**
   * Retrieve a memeber by name.
   */
  AceSet retrieve(String name) throws AceException;

  /**
   * Retrieve a sub-set of members by a filter string.
   */
  AceSet filter(String pattern) throws AceException;
}

