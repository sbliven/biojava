package org.biojava.bio.seq.db;

import java.util.Set;

/**
 * A SequenceDBInstallation has the functionality of a factory for SequenceDB objects and additionally manages the
 * SequenceDB objects created by itself such that the minimum number of SequenceDB objects is created by a particular
 * SequenceDBInstallation object.
 * <p>
 * The idea behind this interface is that sequence databases are usually installed in groups. E.g., there might be a
 * directory which contains FASTA-formated sequence files for EMBL and SwissProt; or there might be an
 * SRS-installation that provides access to GenBank and SwissProt; or there might be a relational database that can be
 * queried for GenBank, PIR and SwissProt entries. These 3 cases would be represented through 3 distinct
 * SequenceDBInstallation objects. Each of these objects can be queried for the set of SequenceDB objects it supports,
 * or a particular SequenceDB object can be retrieved from a SequenceDBInstallation object through a string identifier.
 * All SequenceDB objects that belong to a particular SequenceDBInstallation share the same way of retrieving sequences
 * and will hence be constructed and configured in a very similar fashion - which is the primary reason for inventing
 * the SequenceDBInstallation object which can act as a factory for SequenceDB objects.
 * <p>
 * A SequenceDBInstallation object also manages the SequenceDB objects it has created so that requests for the same
 * database (say SwissProt) will always return the same SequenceDB object. This becomes particularly important when
 * SequenceDB objects allow the modification (create/update/delete of Sequence entries) of the underlying sequence
 * database and this sequence "database" is not transactional in itself (such as a FASTA file). Because in this case
 * the SequenceDB object must act as a transactional front-end to the sequence database and there should really be
 * only one SequenceDB object for each sequence database - which is ensured by SequenceDBInstallation.
 *
 * @author <A href="mailto:Gerald.Loeffler@vienna.at">Gerald Loeffler</A> for the 
 *         <A href="http://www.imp.univie.ac.at">IMP</A>
 */
public interface SequenceDBInstallation {
  /**
   * return all sequence dbs available in this sequence db installation. This is not just the set of sequence dbs
   * already returned by getSequenceDB() but the entire set of sequence dbs supported by this object.
   *
   * @return a set of SequenceDB objects which may be empty. An implementation may also return null if it is not at all
   *         possible to determine which sequence dbs are part of this installation.
   */
  Set getSequenceDBs();

  /**
   * return the SequenceDB for the given identifier. The identifier can (but need not) be the name of the sequence db.
   * An implementation may support any number of identifiers to (uniquely) identify a particular sequence db - but the
   * name of the sequence db (returned by SequenceDB.getName()) must always be among them.
   * <p>
   * If the sequence db identified by the given identifier has not been requested through this object, it will be
   * created and returned (hence this method is a factory method). If the sequence db identified by the given identifier
   * has already been requested, the same object is returned.
   *
   * @param identifier the string that identifies the sequence db. May not be null.
   * @return the SequenceDB object that matches the given identifier or null if no such SequenceDB object could be
   *         found. (It is the responsibility of the implementation to take care that all identifiers are unique so
   *         if it turns out that the given identifier identifies more than one sequence db, this method should throw
   *         a RuntimeException.)
   */
  SequenceDB getSequenceDB(String identifier);
}
