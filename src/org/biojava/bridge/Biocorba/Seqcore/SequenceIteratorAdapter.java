package org.biojava.bridge.Biocorba.Seqcore;

import java.util.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;
import org.Biocorba.Seqcore.*;

public class SequenceIteratorAdapter implements SequenceIterator {
  private PrimarySeqIterator primarySeqIterator;
  
  public PrimarySeqIterator getPrimarySeqIterator() {
    return primarySeqIterator;
  }
  
  public SequenceIteratorAdapter(PrimarySeqIterator primarySeqIterator) {
    this.primarySeqIterator = primarySeqIterator;
  }
  
  public boolean hasNext() {
    return getPrimarySeqIterator().has_more();
  }
  
  public Sequence nextSequence()
  throws NoSuchElementException, BioException {
    try {
      PrimarySeq ps = getPrimarySeqIterator().next();
      SequenceAdapter sa = new SequenceAdapter(ps);
      return sa;
    } catch (EndOfStream eos) {
      throw new NoSuchElementException(eos.getMessage());
    } catch (UnableToProcess utp) {
      throw new BioException(
        utp, "Could not create SequenceAdapter for CORBA sequence"
      );
    } catch (IllegalAlphabetException iae) {
      throw new BioException(
        iae, "Could not create SequenceAdapter for CORBA sequence"
      );
    } catch (IllegalSymbolException ire) {
      throw new BioException(
        ire, "Could not create SequenceAdapter for CORBA sequence"
      );
    }
  }
}
