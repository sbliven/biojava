package org.biojava.bridge.Biocorba.Seqcore;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;

import org.Biocorba.Seqcore.*;
import org.biojava.bridge.GNOME.*;

public class PrimarySeqDBImpl
extends UnknownImpl
implements _PrimarySeqDB_Operations {
  private SequenceDB sequenceDB;
  
  public SequenceDB getSequenceDB() {
    return sequenceDB;
  }
  
  public PrimarySeqDBImpl(SequenceDB sequenceDB) {
    this.sequenceDB = sequenceDB;
  }
  
  public String database_name(org.omg.CORBA.portable.ObjectImpl primarySeqDB) {
    return getSequenceDB().getName();
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
      PrimarySeqImpl psi = new PrimarySeqImpl(seq, seq.getName(), seq.toString(), database_name(primarySeqDB) + ":" + primary_id);
      _PrimarySeq_Tie pst = new _PrimarySeq_Tie(psi);
      primarySeqDB._orb().connect(pst);
      return pst;
    } catch (IllegalAlphabetException iae) {
      throw new UnableToProcess(iae.getMessage());
    } catch (BioException se) {
      throw new UnableToProcess(se.getMessage());
    }
  }
}
