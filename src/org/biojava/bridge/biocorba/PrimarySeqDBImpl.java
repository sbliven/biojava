package org.biojava.bridge.biocorba;

import GNOME.*;
import Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

public class PrimarySeqDBImpl
extends UnknownImpl
implements _PrimarySeqDBOperations {
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
  
  public String database_name() {
    return name;
  }
  
  public short database_version() {
    return 0;
  }
  
  public Bio.PrimarySeqStream make_stream() {
    SequenceIterator seqIt = db.sequenceIterator();
    PrimarySeqStreamImpl seqStreamImpl = new PrimarySeqStreamImpl(seqIt);
    _PrimarySeqStreamTie seqStreamTie = new _PrimarySeqStreamTie(seqStreamImpl);
    
    return seqStreamTie;
  }
  
  public Bio.PrimarySeq get_PrimarySeq(String primary_id)
  throws Bio.UnableToProcess {
    try {
      Sequence seq = db.getSequence(primary_id);
      SeqImpl seqImpl = new SeqImpl(seq);
      _SeqTie seqTie = new _SeqTie(seqImpl);
      
      return seqTie;
    } catch (SeqException se) {
      throw new Bio.UnableToProcess(se.getMessage());
    }
  }
}
