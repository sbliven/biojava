package org.biojava.bio.taxa;

import org.biojava.utils.*;

/**
 * Encapsulate the mapping between Taxa and stringified representations of Taxa.
 *
 * @author Matthew Pocock
 */
public interface TaxaParser {
  /**
   * Convert a stringified Taxa into a Taxa instance.
   *
   * @param taxaFactory  the TaxaFactory used to instantiate Taxa instances
   * @param taxaString  the String to parse
   * @return a Taxa instance created by the TaxaFactory from the taxaString
   */
  public Taxa parse(TaxaFactory taxaFactory, String taxaString)
  throws ChangeVetoException, CircularReferenceException;
  
  /**
   * Convert a Taxa into a stringified representation.
   *
   * @param taxa the Taxa to serialize
   * @return the stringified version of taxa
   */
  public String serialize(Taxa taxa);
}
