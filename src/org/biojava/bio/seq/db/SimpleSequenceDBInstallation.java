package org.biojava.bio.seq.db;

import org.biojava.utils.contract.Contract;
import org.biojava.utils.ObjectUtil;

import java.util.*;

/**
 * this class is an implementation of interface SequenceDBInstallation that manages a set of HashSequenceDB objects. The
 * set of HashSequenceDB objects is initially empty and can be expanded by the user through the addSequenceDB() method.
 * This SequenceDBInstallation is then able to serve the HashSequenceDB objects in this set.
 *
 * @author <A href="mailto:Gerald.Loeffler@vienna.at">Gerald Loeffler</A> for the 
 *         <A href="http://www.imp.univie.ac.at">IMP</A>
 */
public class SimpleSequenceDBInstallation implements SequenceDBInstallation {
  private Map sequenceDBByIdentifier = new HashMap();

  /**
   * create an initially empty SimpleSequenceDBInstallation
   */
  public SimpleSequenceDBInstallation() {}

  /**
   * this method creates a new (and empty) HashSequenceDB with the given name that will be accessible through this
   * sequence db installation through this name and all given other identifiers.
   * @param name the name of the SequenceDB to create. Not null. If this name is lready used by this sequence db
   *             installation, an IllegalArgumentException is thrown.
   * @param otherIdentifiers a set of String objects that also serve as identifiers for the newly created SequenceDB
   *                         object. This set should not contain the name of the SequenceDB - but if if does, it is just
   *                         ignored because the name is an identifier by definition. The parameter may be empty or the
   *                         empty set, in which case the name is the only identifier for the newly created SequenceDB.
   *                         If any of the given identifiers (including the name) is already used by this sequence db
   *                         installation, an IllegalArgumentException is thrown.
   */
  public synchronized void addSequenceDB(String name, Set otherIdentifiers) {
    Contract.pre(name != null, "name not null");
    // otherIdentifiers may only contain String objects - but this is checked later

    // create set of all identifiers for the to-be-created SequenceDB
    Set allIdentifiers = new HashSet();
    allIdentifiers.add(name);
    if (otherIdentifiers != null) allIdentifiers.addAll(otherIdentifiers);

    // none of the identifiers may already be in use
    Set currentIdentifiers = this.sequenceDBByIdentifier.keySet();
    for (Iterator i = allIdentifiers.iterator(); i.hasNext(); ) {
      Object o = i.next();
      Contract.pre(o instanceof String, "otherIdentifiers must be a set of String objects");
      Contract.pre(!(currentIdentifiers.contains(o)), "name and otherIdentifiers must not already be in use");
    }

    // create new HashSequenceDB and at it to the map under all its identifiers
    SequenceDB db = new HashSequenceDB(name);
    for (Iterator i = allIdentifiers.iterator(); i.hasNext(); ) {
      String identifier = (String) i.next();
      this.sequenceDBByIdentifier.put(identifier, db);
    }
  }
  
  /**
   * return a newly created set of the SequenceDB objects that were already created through method addSequenceDB(). This
   * set itself is not part of the state of this object (i.e. modifying the set does not modify this object) but the
   * SequenceDB objects contained in the set are the same objects managed by this object.
   */
  public synchronized Set getSequenceDBs() {
    Set allDBs = new HashSet();
    allDBs.addAll(this.sequenceDBByIdentifier.values());

    return allDBs;
  }

  /**
   * if the given identifier is known to this sequence db installation because it has been used in a call to
   * addSequenceDB(), then this method returns the SequenceDB associated with this identifier. Otherwise, null
   * is returned.
   */
  public synchronized SequenceDB getSequenceDB(String identifier) {
    Contract.pre(identifier != null, "identifier not null");

    return (SequenceDB) this.sequenceDBByIdentifier.get(identifier);
  }

  public String toString() {
    return "StupidSequenceDBInstallation";
  }
  
  public synchronized boolean equals(Object o) {
    if (o == this) return true;
    
    // if this class is a direct sub-class of Object:
    if (o == null) return false;
    if (!o.getClass().equals(this.getClass())) return false;
    
    SimpleSequenceDBInstallation that = (SimpleSequenceDBInstallation) o;
    
    // only compare fields of this class (not of super-classes):
    if (!ObjectUtil.equals(this.sequenceDBByIdentifier, that.sequenceDBByIdentifier)) return false;
    
    // this and that are identical if we made it 'til here
    return true;
  }
  
  public synchronized int hashCode() {
    // if this class is a direct sub-class of Object:
    int hc = 0;

    // only take into account fields of this class (not of super-class):
    hc = ObjectUtil.hashCode(hc, sequenceDBByIdentifier);

    return hc;
  }

  /**
   * test this class
   */
  public static void main(String[] args) {
    System.out.println("Create sequence db installation");
    SimpleSequenceDBInstallation dbInst = new SimpleSequenceDBInstallation();
    System.out.println("Sequence db installation serves " + dbInst.getSequenceDBs().size() + " sequence dbs");
    System.out.println("add swissprot (aka sprot, sp) and genbank (aka gb) do sequence db installation");
    Set swissprotIDs = new HashSet();
    swissprotIDs.add("sprot");
    swissprotIDs.add("sp");
    dbInst.addSequenceDB("swissprot", swissprotIDs);
    Set genbankIDs = new HashSet();
    genbankIDs.add("gb");
    genbankIDs.add("genbank"); // this is not correct but should be ignored
    dbInst.addSequenceDB("genbank", genbankIDs);
    System.out.println("Sequence db installation serves " + dbInst.getSequenceDBs().size() + " sequence dbs");
    System.out.println("Sequence db associated with identifier \"sprot\" is: " + dbInst.getSequenceDB("sprot"));
    System.out.println("Sequence db associated with identifier \"swissprot\" is: " + dbInst.getSequenceDB("swissprot"));
    System.out.println("Sequence db associated with identifier \"sp\" is: " + dbInst.getSequenceDB("sp"));
    System.out.println("Sequence db associated with identifier \"willi\" is: " + dbInst.getSequenceDB("willi"));
    System.out.println("Sequence db associated with identifier \"gb\" is: " + dbInst.getSequenceDB("gb"));
    System.out.println("Sequence db associated with identifier \"genbank\" is: " + dbInst.getSequenceDB("genbank"));
    System.out.println("Sequence db associated with identifier \"genebank\" is: " + dbInst.getSequenceDB("genebank"));
  }
}
