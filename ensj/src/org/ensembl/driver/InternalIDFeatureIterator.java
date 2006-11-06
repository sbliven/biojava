package org.ensembl.driver;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.ensembl.datamodel.Location;

/**
 * Produces zero or more Features from the adaptor that match the location
 * filter. Uses the IDProducer to create a stream of internalIDs to retrieve.
 */
class InternalIDFeatureIterator implements Iterator {

  /**
   * Produces zero or more internalIDs of features from the adaptor that match
   * the location filter. TODO add a buffer to enable batch id retrieval of
   * large numbers of ids currently nieve fetch all id implementation (this
   * will potentially require huge amounts of memory for things like snps).
   * 
   * @author <a href="mailto:craig@ebi.ac.uk">Craig Melsopp </a>
   */
  private class IDProducer {

    private int index = 0;

    private long[] ids;


    public IDProducer(long[] internalIDs) {
      this.ids = internalIDs;
    }
    

    public long nextID() {
      if (hasNextID())
        return ids[index++];
      else
        throw new NoSuchElementException();
    }

    public boolean hasNextID() {
      return index < ids.length;
    }
  }
  
  
  private int featureBufferSize;

  private boolean loadChildren;

  private long[] bufID;

  private List buf;

  private int pos = 0;

  private int size = 0;

  private FeatureAdaptor adaptor;

  // optional location used to construct iterator
  private Location location = null;

  private IDProducer idProducer;

  /**
   * @param adaptor2
   * @param featureBufferSize2
   * @param loadChildren2
   * @param internalIDs
   */
  InternalIDFeatureIterator(FeatureAdaptor adaptor, int featureBufferSize,
      boolean loadChildren, long[] internalIDs) {
    this.adaptor = adaptor;
    this.featureBufferSize = featureBufferSize;
    this.loadChildren = loadChildren;
    bufID = new long[featureBufferSize];
    this.idProducer = new IDProducer(internalIDs);
  }

  public boolean hasNext() {
    if (pos >= size)
      updateBuffer();
    return pos < size;
  }

  public Object next() {
    if (hasNext())
      return buf.get(pos++);
    else
      throw new NoSuchElementException();
  }

  private void updateBuffer() {

    Arrays.fill(bufID, 0);
    int c = 0;
    while (idProducer.hasNextID() && c < featureBufferSize)
      bufID[c++] = idProducer.nextID();

    if (c > 0) {

      long[] tmp = bufID;
      if (c < featureBufferSize) {
        tmp = new long[c];
        System.arraycopy(bufID, 0, tmp, 0, c);
      }
      try {
        buf = adaptor.fetch(tmp, loadChildren);
        if (location != null)
          adaptor.convertLocations(buf, location.getCoordinateSystem());
        size = buf.size();
      } catch (AdaptorException e) {
        throw new RuntimeException(e);
      }
    } else {
      size = 0;
    }
    pos = 0;

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

}