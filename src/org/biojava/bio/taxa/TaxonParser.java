package org.biojava.bio.taxa;

import org.biojava.utils.*;

/**
 * Encapsulate the mapping between Taxon and stringified
 * representations of taxa.
 *
 * @author Matthew Pocock
 */
public interface TaxonParser {
  /**
   * Convert a stringified Taxon into a Taxon instance.
   *
   * @param taxonFactory  the TaxonFactory used to instantiate taxa instances
   * @param taxonString  the String to parse
   * @return a Taxon instance created by the TaxonFactory from the taxonString
   */
  public Taxon parse(TaxonFactory taxonFactory, String taxonString)
  throws ChangeVetoException, CircularReferenceException;
  
  /**
   * Convert a Taxon into a stringified representation.
   *
   * @param taxon the Taxon to serialize
   * @return the stringified version of Taxon
   */
  public String serialize(Taxon taxon);
}
