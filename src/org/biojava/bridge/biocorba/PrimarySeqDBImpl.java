package org.biojava.bridge.biocorba;

import org.biocorba.Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

public class PrimarySeqDBImpl
extends UnknownImpl
implements _PrimarySeqDB_Operations {
  private String name;
  private SequenceDB db;
  
  public String getName() {
    return name;
  }
  
  public SequenceDB getDB() {
    return db;
  }
  
  public PrimarySeqDBImpl(SequenceDB db) {
    this.db = db;
    this.name = db.toString();
  }
  
  public PrimarySeqDBImpl(SequenceDB db, String name) {
    this.db = db;
    this.name = name;
  }
  
  public String database_name(org.omg.CORBA.portable.ObjectImpl primarySeqDB) {
    return name;
  }
  
  public short database_version(org.omg.CORBA.portable.ObjectImpl primarySeqDB) {
    return 0;
  }
  
  public PrimarySeqStream make_stream(org.omg.CORBA.portable.ObjectImpl primarySeqDB) {
    SequenceIterator seqIt = db.sequenceIterator();
    PrimarySeqStreamImpl seqStreamImpl = new PrimarySeqStreamImpl(seqIt);
    _PrimarySeqStream_Tie seqStreamTie = new _PrimarySeqStream_Tie(seqStreamImpl);
    
    return seqStreamTie;
  }
  
  public PrimarySeq get_PrimarySeq(org.omg.CORBA.portable.ObjectImpl primarySeqDB, String primary_id)
  throws UnableToProcess {
    try {
      Sequence seq = db.getSequence(primary_id);
      SeqImpl seqImpl = new SeqImpl(seq);
      _Seq_Tie seqTie = new _Seq_Tie(seqImpl);
      primarySeqDB._orb().connect(seqTie);
      return seqTie;
    } catch (SeqException se) {
      throw new UnableToProcess(se.getMessage());
    }
  }
}
