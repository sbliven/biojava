/*
 Copyright (C) 2001 EBI, GRL

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.ensembl.driver;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.ensembl.datamodel.Location;

/**
 * Provides an iterator over features from a FeatureAdaptor.
 * 
 * <p>
 * The features are retrieved in batches and buffered. This allows for a
 * compromise between runtime speed and memory usage efficiency. Increasing the
 * buffer size potentially reduces the number of database calls required and
 * potentially increases the memory requirement.
 * </p>
 * 
 * <p>
 * Example usage: Retrieve all genes in the specified location using batches of
 * upto 1mb.
 * </p>
 * 
 * <p>
 * <code>Iterator iter = new FeatureIterator(geneAdaptor,1000000,loc);</code>
 * </p>
 * 
 * This class acts as a proxy for InternalIDFeatureIterator and
 * LocationFeatureIterator and is maintained for backwards compatibility.
 * 
 * TODO chunk location into smaller regions TODO return results, ignoring
 * duplicates
 * 
 * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
 * 
 */
public class FeatureIterator implements Iterator {

  private Iterator delegateIterator;

  /**
   * Create an iterator over all features in the adaptor. The order is
   * unspecified.
   * 
   * @param adaptor
   *          adaptor to retrieve features from.
   * @param featureBufferSize
   *          number of items loaded into buffer.
   * @param loadChildren
   *          whether to preload child data.
   * @throws AdaptorException
   */
  public FeatureIterator(FeatureAdaptor adaptor, int featureBufferSize,
      boolean loadChildren) throws AdaptorException {
    this(adaptor, featureBufferSize, loadChildren, adaptor.fetchInternalIDs());
  }

  /**
   * Create an iterator over features with the specified internalIDs. The order
   * is unspecified.
   * 
   * @param adaptor
   *          adaptor to retrieve features from.
   * @param featureBufferSize
   *          number of items loaded into buffer.
   * @param loadChildren
   *          whether to preload child data.
   * @param internalIDs
   *          internal IDs of the features to retrieve
   * @throws AdaptorException
   */
  public FeatureIterator(FeatureAdaptor adaptor, int featureBufferSize,
      boolean loadChildren, long[] internalIDs) throws AdaptorException {

    this.delegateIterator = new InternalIDFeatureIterator(adaptor,
        featureBufferSize, loadChildren, internalIDs);
  }

  /**
   * Create an iterator over features in the adaptor that overlap the specified
   * location.
   * 
   * The order of returned items is the same as from adaptor.fetch(Location).
   * 
   * Location is broken down in "chunks" of the specified size and then features
   * are retrieved via adaptor.fetch(locationChunk).
   * 
   * @param adaptor
   *          adaptor to retrieve features from.
   * @param chunkSize
   *          size of the sub-location chunks.
   * @param loadChildren
   *          whether to preload child data.
   * @param location
   *          location filter.
   * @throws AdaptorException
   */
  public FeatureIterator(FeatureAdaptor adaptor, int chunkSize,
      boolean loadChildren, Location location,
      LocationConverter locationConverter) throws AdaptorException {
    this.delegateIterator = new LocationFeatureIterator(adaptor,
        chunkSize, loadChildren, location, locationConverter);
  }

  /**
   * Not supported.
   * 
   * @throws UnsupportedOperationException
   *           this method is not supported
   */
  public void remove() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();

  }

  /**
   * Returns whether there is another item to be returned via next().
   * 
   * @return true if the iterator has another feature.
   */
  public synchronized boolean hasNext() {
    return delegateIterator.hasNext();
  }

  /**
   * Returns the next element in the iteration.
   * 
   * @return the next Feature in the iteration.
   * @throws NoSuchElementException
   *           iteration has no more elements.
   * @throws RuntimeException
   *           problem occured reading the data from the database.
   */
  public synchronized Object next() {
    return delegateIterator.next();
  }

}
