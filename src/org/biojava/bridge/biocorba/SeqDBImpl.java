package org.biojava.bridge.biocorba;

import org.biocorba.Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

final public class SeqDBImpl
extends PrimarySeqDBImpl
implements _PrimarySeqDB_Operations {
  public SeqDBImpl(SequenceDB db) {
    super(db);
  }
  
  public SeqDBImpl(SequenceDB db, String name) {
    super(db, name);
  }

  public Seq get_Seq(org.omg.CORBA.portable.ObjectImpl seqDB, String primary_id) throws UnableToProcess {
    try {
      Sequence seq = super.getDB().getSequence(primary_id);
      SeqImpl seqImpl = new SeqImpl(seq);
      _Seq_Tie seqTie = new _Seq_Tie(seqImpl);
      seqDB._orb().connect(seqTie);
      return seqTie;
    } catch (SeqException se) {
      throw new UnableToProcess(se.getMessage());
    }
  }
}
