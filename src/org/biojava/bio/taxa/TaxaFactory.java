package org.biojava.bio.taxa;

import java.util.*;
import org.biojava.utils.*;

/**
 * Factory for handling a particular implementation of a Taxa.
 *
 * @author Matthew Pocock
 */

public interface TaxaFactory {
  /**
   * Name for this TaxaFactory.
   *
   * @return the name of this TaxaFactory
   */
  public String getName();
  
  /**
   * Return the apropreate (possibly normalized) taxa object for a
   * fully-qualified taxa name.
   *
   * @param name  fully qualified taxa name
   * @return the Taxa representing this name
   * @throws ParserException if the name can't be parsed
   * @throws NoSuchElementException if the name refers to an unknown taxa and
   *         the factory is unable to generate a new taxa for it
   * @throws ChangeVetoException if parsing this Taxa would result in a Taxa
   *          being added that can't be added
   */
  public Taxa parseTaxa(String name)
  throws ParserException,
  NoSuchElementException,
  ChangeVetoException;
  
  /**
   * Return the fully qualified name of a taxa using this factory's notion of
   * naming.
   *
   * @param taxa the Taxa to name
   * @return a String containing the complete name
   */
  public String completeName(Taxa taxa);
  
  /**
   * Import a Taxa and all its children into the implementation provided by
   * this factory.
   *
   * <p>
   * The return value of this method should be .equals() and .hasCode()
   * compatable with the taxa parameter. It may not be implemented by the
   * same underlying implementation.
   * </p>
   *
   * @param source the Taxa to copy
   * @return a new Taxa
   */
  public Taxa importTaxa(Taxa source);
  
  /**
   * Retrive the root upon which all rooted Taxa that this factory knows about
   * are rooted.
   *
   * @return the 'root' Taxa
   */
  public Taxa getRoot();
  
  /**
   * Retreive a Taxa that matches some ID.
   *
   * <p>
   * This method is here out of desperation. It's nasty and should be replaced
   * by some propper querying API. Without having different methods for every
   * TaxaFactory I don't know what to do. All ideas apreciated.
   * </p>
   *
   * @param id  the Object identifying a Taxa
   * @return the Taxa matching the ID, or null if none match
   */
  public Taxa search(Object id);
  
  /**
   * Create a new orphan Taxa with a given scientific and common name.
   *
   * @param scientificName  the scientificName to give the taxa
   * @param commonName  the common name to give the taxa
   * @return a new Taxa with no parent and no children
   */
  public Taxa createTaxa(String scientificName, String commonName);
  
  
  /**
   * Add a taxa as a child to a parent.
   *
   * <P>
   * The TaxaFactory may chose to add the child directly, or make a new
   * object which is .equals() compatable with child. The actual Taxa instance
   * inserted into the child set is returned by the add method.
   * </P>
   *
   * @param parent the parent Taxa to add the child to
   * @param child  the Taxa to add as a child
   * @return the Taxa object actualy present as the child
   * @throws ChangeVetoException if for any reason the child can't be added
   * @throws CircularReferenceException if child is this taxa or any of its
   *          parents
   */
  public Taxa addChild(Taxa parent, Taxa child)
  throws ChangeVetoException,
  CircularReferenceException;
  
  /**
   * Remove a taxa as a child to this one.
   *
   * <P>
   * This Taxa should attempt to remove a child that is .equals() compatable
   * with child. If it is sucessful, it should return the Taxa instance that
   * was removed. If not, it should return null.
   * </P>
   *
   * @param parent the parent Taxa to remove the child from
   * @param child  the Taxa to remove as a child
   * @return the actual Taxa removed, or null if none were removed
   * @throws ChangeVetoException if for any reason the child can't be removed
   */
  public Taxa removeChild(Taxa parent, Taxa child)
  throws ChangeVetoException;

}
