package org.biojava.bridge.Biocorba.Seqcore;

import org.biojava.bio.seq.*;

import org.Biocorba.Seqcore.*;
import org.biojava.bridge.GNOME.*;

public class PrimarySeqDBImpl
extends UnknownImpl
implements _PrimarySeqDB_Operations {
  private SequenceDB sequenceDB;
  private String name;
  
  public SequenceDB getSequenceDB() {
    return sequenceDB;
  }
  
  public String getName() {
    return name;
  }
  
  public PrimarySeqDBImpl(SequenceDB sequenceDB, String name) {
    this.sequenceDB = sequenceDB;
    this.name = name;
  }
  
  public String database_name(org.omg.CORBA.portable.ObjectImpl primarySeqDB) {
    return name;
  }

  public short database_version(org.omg.CORBA.portable.ObjectImpl primarySeqDB) {
    return 0;
  }

  public PrimarySeqIterator make_PrimarySeqIterator(org.omg.CORBA.portable.ObjectImpl primarySeqDB) {
    SequenceIterator si = getSequenceDB().sequenceIterator();
    PrimarySeqIteratorImpl psii = new PrimarySeqIteratorImpl(si);
    _PrimarySeqIterator_Tie psit = new _PrimarySeqIterator_Tie(psii);
    primarySeqDB._orb().connect(psit);
    return psit;
  }

  public PrimarySeq get_PrimarySeq(org.omg.CORBA.portable.ObjectImpl primarySeqDB, String primary_id)
  throws UnableToProcess {
    try {
      Sequence seq = getSequenceDB().getSequence(primary_id);
      PrimarySeqImpl psi = new PrimarySeqImpl(seq, name + ":" + primary_id);
      _PrimarySeq_Tie pst = new _PrimarySeq_Tie(psi);
      primarySeqDB._orb().connect(pst);
      return pst;
    } catch (SeqException se) {
      throw new UnableToProcess(se.getMessage());
    } catch (IllegalAlphabetException iae) {
      throw new UnableToProcess(iae.getMessage());
    }
  }
}
