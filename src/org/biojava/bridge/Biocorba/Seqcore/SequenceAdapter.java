package org.biojava.bridge.Biocorba.Seqcore;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

import org.Biocorba.Seqcore.*;

public class SequenceAdapter
extends SymbolListAdapter
implements Sequence {
  private FeatureHolder fHolder;
  
  public PrimarySeq getPrimarySeq() {
    return (PrimarySeq) getAnonymousSeq();
  }

  public Seq getSeq() {
    AnonymousSeq as = getAnonymousSeq();
    if(as instanceof Seq) {
      return (Seq) as;
    } else {
      return null;
    }
  }
  
  public SequenceAdapter(PrimarySeq primarySeq)
  throws IllegalAlphabetException, IllegalSymbolException, BioException {
    super(primarySeq);
    if(!(primarySeq instanceof Seq)) {
      fHolder = FeatureHolder.EMPTY_FEATURE_HOLDER;
    } else {
      fHolder = new SeqFeatureHolder();
    }
  }
  
  public Feature createFeature(FeatureHolder fh, Feature.Template template)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException("Can not add features to a CORBA sequence");
  }

  public Feature createFeature(Feature.Template template)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException("Can not add features to a CORBA sequence");
  }

    public void removeFeature(Feature f) {
	throw new UnsupportedOperationException("Cannot remove features from a CORBA sequence");
    }

  public String getName() {
    return getPrimarySeq().display_id();
  }
  
  public String getURN() {
    return getPrimarySeq().accession_number();
  }
  
  public int countFeatures() {
    return fHolder.countFeatures();
  }
  
  public Iterator features() {
    return fHolder.features();
  }
  
  public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
    return fHolder.filter(fc, recurse);
  }
  
  public Annotation getAnnotation() {
    return Annotation.EMPTY_ANNOTATION;
  }

  private class SeqFeatureHolder extends AbstractFeatureHolder {
    public int countFeatures() {
      return -1;
    }
        
    public Iterator features() {
      final SeqFeatureIterator sfi = getSeq().all_features_iterator();
      return new Iterator() {
        public boolean hasNext() {
          return sfi.has_more();
        }
            
        public Object next() throws NoSuchElementException {
          try {
            return new FeatureAdapter(SequenceAdapter.this, (SeqFeature) sfi.next());
          } catch (Exception e) {
            throw new NoSuchElementException(e.getMessage());
          }
        }
            
        public void remove()
        throws UnsupportedOperationException  {
          throw new UnsupportedOperationException(
            "Can't remove feature from CORBA object"
          );
        }
      };
    }
  }
}
