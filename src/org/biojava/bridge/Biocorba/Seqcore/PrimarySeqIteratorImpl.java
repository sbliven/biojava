package org.biojava.bridge.Biocorba.Seqcore;

import java.util.*;

import org.biojava.bio.seq.*;
import org.Biocorba.Seqcore.*;
import org.biojava.bridge.GNOME.*;

public class PrimarySeqIteratorImpl
extends UnknownImpl
implements _PrimarySeqIterator_Operations {
  private SequenceIterator si;
  
  public PrimarySeqIteratorImpl(SequenceIterator si) {
    this.si = si;
  }
  
  public PrimarySeq next(org.omg.CORBA.portable.ObjectImpl primarySeqIterator)
  throws org.Biocorba.Seqcore.EndOfStream, org.Biocorba.Seqcore.UnableToProcess {
    try {
      Sequence seq = si.nextSequence();
      PrimarySeqImpl psi = new PrimarySeqImpl(seq);
      _PrimarySeq_Tie pst = new _PrimarySeq_Tie(psi);
      primarySeqIterator._orb().connect(pst);
      return pst;
    } catch (NoSuchElementException e) {
      throw new EndOfStream();
    } catch (SeqException se) {
      throw new UnableToProcess(se.getMessage());
    } catch (IllegalAlphabetException iae) {
      throw new UnableToProcess(iae.getMessage());
    }
  }

  public boolean has_more(org.omg.CORBA.portable.ObjectImpl primarySeqIterator) {
    return si.hasNext();
  }
}

