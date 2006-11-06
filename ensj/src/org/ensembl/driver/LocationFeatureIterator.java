package org.ensembl.driver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.ensembl.datamodel.Feature;
import org.ensembl.datamodel.Location;
import org.ensembl.util.LongSet;

public class LocationFeatureIterator implements Iterator {

  private FeatureAdaptor adaptor;

  private boolean loadChildren;

  private Location[] locations;

  private int locationsIndex = 0;

  private List buffer;

  private int bufferIndex = -1;

  private LongSet ids = new LongSet();

  private Feature nextItem = null;

  public LocationFeatureIterator(FeatureAdaptor adaptor, int locationChunkSize,
      boolean loadChildren, Location location,
      LocationConverter locationConverter) {
    this.adaptor = adaptor;
    this.loadChildren = loadChildren;
    this.locations = locationChunks(locationConverter, location,
        locationChunkSize);
  }

  private Location[] locationChunks(LocationConverter locationConverter,
      Location location, int locationChunkSize) {

    // need a copy of location because going to change it during processing
    Location tmp = location.copy();
    if (!tmp.isComplete())
      try {
        tmp = locationConverter.fetchComplete(tmp);
      } catch (AdaptorException e) {
        throw new RuntimeAdaptorException(e);
      }

    List chunks = new ArrayList();
    for (Location node = tmp; node != null;) {

      if (node.getNodeLength() <= locationChunkSize) {
        chunks.add(node.copyNode());
        node = node.next();

      } else {
        Location l = node.copyNode();
        l.setEnd(l.getStart() + locationChunkSize - 1);
        chunks.add(l);

        node.setStart(l.getStart() + locationChunkSize);
      }
    }
    return (Location[]) chunks.toArray(new Location[chunks.size()]);
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

    // sets nextItem to the next valid item, or null if none available

    while (nextItem == null) {

      // try to move to next chunk
      if (bufferIndex == -1 || bufferIndex == buffer.size()) {

        if (locationsIndex < locations.length) {
          try {
            Location chunkLoc = locations[locationsIndex++];
            buffer = adaptor.fetch(chunkLoc, loadChildren);
            if (buffer.size() == 0) {
              bufferIndex = -1;
              continue;
            } else {
              bufferIndex = 0;
            }
          } catch (AdaptorException e) {
            throw new RuntimeAdaptorException(e);
          }
        } else {
          // no more chunks
          break;
        }
      }
      
      // skip previously seen features
      nextItem = (Feature) buffer.get(bufferIndex++);
      long id = nextItem.getInternalID();
      if (ids.contains(id)) 
        nextItem = null;
      else
        ids.add(id);

    }
    return nextItem != null;
  }

  // public synchronized boolean hasNext() {
  //
  // while (locationsIndex < locations.length) {
  //
  // if (buffer != null && buffer.hasNext())
  // return true;
  //
  // try {
  // Location chunkLoc = locations[locationsIndex++];
  // //System.out.println(chunkLoc);
  // buffer = adaptor.fetch(chunkLoc, loadChildren).iterator();
  // if (buffer.hasNext())
  // return true;
  //        
  // } catch (AdaptorException e) {
  // throw new RuntimeAdaptorException(e);
  // }
  //
  // }
  //
  // return false;
  // }

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
    if (hasNext()) {
      Object r = nextItem;
      nextItem = null;
      return r;
    } else
      throw new NoSuchElementException();
  }
  // public synchronized Object next() {
  // if (buffer.hasNext())
  // return buffer.next();
  // else
  // throw new NoSuchElementException();
  // }
}
