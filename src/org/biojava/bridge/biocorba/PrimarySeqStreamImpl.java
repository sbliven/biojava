package org.biojava.bridge.biocorba;

import java.util.*;

import org.biocorba.Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

final public class PrimarySeqStreamImpl
extends UnknownImpl
implements _PrimarySeqStream_Operations {
  private SequenceIterator si;
  
  public PrimarySeqStreamImpl(SequenceIterator si) {
    this.si = si;
  }
  
  public PrimarySeq next(org.omg.CORBA.portable.ObjectImpl primarySeqStream)
  throws EndOfStream, UnableToProcess {
    try {
      Sequence seq = si.nextSequence();
      SeqImpl seqImpl = new SeqImpl(seq);
      _Seq_Tie seqTie = new _Seq_Tie(seqImpl);
      primarySeqStream._orb().connect(seqTie);
      return seqTie;
    } catch (NoSuchElementException nsee) {
      throw new EndOfStream();
    } catch (SeqException se) {
      throw new UnableToProcess(se.getMessage());
    }
  }
  
  public boolean has_more(org.omg.CORBA.portable.ObjectImpl primarySeqStream) {
    return si.hasNext();
  }
}
