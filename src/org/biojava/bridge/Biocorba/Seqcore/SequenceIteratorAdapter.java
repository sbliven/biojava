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
  throws NoSuchElementException, SeqException {
    try {
      PrimarySeq ps = getPrimarySeqIterator().next();
      SequenceAdapter sa = new SequenceAdapter(ps);
      return sa;
    } catch (EndOfStream eos) {
      throw new NoSuchElementException(eos.getMessage());
    } catch (UnableToProcess utp) {
      throw new SeqException(
        utp, "Could not create SequenceAdapter for CORBA sequence"
      );
    } catch (IllegalAlphabetException iae) {
      throw new SeqException(
        iae, "Could not create SequenceAdapter for CORBA sequence"
      );
    } catch (IllegalResidueException ire) {
      throw new SeqException(
        ire, "Could not create SequenceAdapter for CORBA sequence"
      );
    }
  }
}
