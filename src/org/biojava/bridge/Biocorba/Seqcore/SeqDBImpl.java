package org.biojava.bridge.Biocorba.Seqcore;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

import org.Biocorba.Seqcore.*;

public class SeqDBImpl
extends PrimarySeqDBImpl
implements _SeqDB_Operations {
  public SeqDBImpl(SequenceDB sequenceDB, String name) {
    super(sequenceDB, name);
  }
  
  public Seq get_Seq(org.omg.CORBA.portable.ObjectImpl seqDB, String primary_id)
  throws UnableToProcess {
    try {
      Sequence seq = getSequenceDB().getSequence(primary_id);
      SeqImpl si = new SeqImpl(seq, getName() + ":" + primary_id);
      _Seq_Tie st = new _Seq_Tie(si);
      seqDB._orb().connect(st);
      return st;
    } catch (IllegalAlphabetException iae) {
      throw new UnableToProcess(iae.getMessage());
    } catch (BioException se) {
      throw new UnableToProcess(se.getMessage());
    }
  }

  public String[] get_primaryidList(org.omg.CORBA.portable.ObjectImpl seqDB) {
    return (String []) getSequenceDB().ids().toArray(new String[0]);
  }
}
