package org.biojava.bridge.biocorba;

import GNOME.*;
import Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

final public class SeqDBImpl
extends PrimarySeqDBImpl
implements _PrimarySeqDBOperations {
  public SeqDBImpl(SequenceDB db) {
    super(db);
  }
  
  public SeqDBImpl(SequenceDB db, String name) {
    super(db, name);
  }

  public Bio.Seq get_Seq(String primary_id) throws Bio.UnableToProcess {
    try {
      Sequence seq = super.getDB().getSequence(primary_id);
      SeqImpl seqImpl = new SeqImpl(seq);
      _SeqTie seqTie = new _SeqTie(seqImpl);
      
      return seqTie;
    } catch (SeqException se) {
      throw new Bio.UnableToProcess(se.getMessage());
    }
  }
}
