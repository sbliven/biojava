package org.biojava.bridge.biocorba;

import java.util.*;

import GNOME.*;
import Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

final public class PrimarySeqStreamImpl
extends UnknownImpl
implements _PrimarySeqStreamOperations {
  private SequenceIterator si;
  
  public PrimarySeqStreamImpl(SequenceIterator si) {
    this.si = si;
  }
  
  public Bio.PrimarySeq next()
  throws Bio.EndOfStream, Bio.UnableToProcess {
    try {
      Sequence seq = si.nextSequence();
      SeqImpl seqImpl = new SeqImpl(seq);
      _SeqTie seqTie = new _SeqTie(seqImpl);
      
      return seqTie;
    } catch (NoSuchElementException nsee) {
      throw new Bio.EndOfStream();
    } catch (SeqException se) {
      throw new Bio.UnableToProcess(se.getMessage());
    }
  }
  
  public boolean has_more() {
    return si.hasNext();
  }
}
