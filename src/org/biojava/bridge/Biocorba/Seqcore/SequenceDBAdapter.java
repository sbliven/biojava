package org.biojava.bridge.Biocorba.Seqcore;

import java.util.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;
import org.Biocorba.Seqcore.*;

public class SequenceDBAdapter implements SequenceDB {
  private PrimarySeqDB primarySeqDB;
  
  public PrimarySeqDB getPrimarySeqDB() {
    return primarySeqDB;
  }
  
  public SequenceDBAdapter(PrimarySeqDB primarySeqDB) {
    this.primarySeqDB = primarySeqDB;
  }
  
  public Sequence getSequence(String id)
  throws SeqException {
    try {
      PrimarySeq primarySeq = getPrimarySeqDB().get_PrimarySeq(id);
      return new SequenceAdapter(primarySeq);
    } catch (UnableToProcess utp) {
      throw new SeqException(
        utp,
        "Could not retrieve sequence from CORBA database " +
        getPrimarySeqDB().database_name()
      );
    } catch (IllegalAlphabetException iae) {
      throw new SeqException(iae, "Unable to create SequenceAdapter");
    } catch (IllegalResidueException ire) {
      throw new SeqException(ire, "Unable to create SequenceAdapter");
    } catch (SeqException se) {
      throw new SeqException(se, "Unable to create SequenceAdapter");
    }
  }
  
  public Set ids() {
    PrimarySeqDB psdb = getPrimarySeqDB();
    if(psdb instanceof SeqDB) {
      // turn array of strings into set of strings
      String [] ids = ((SeqDB) psdb).get_primaryidList();
      Set idSet = new HashSet();
      for(int i = 0; i < ids.length; i++) {
        idSet.add(ids[i]);
      }
      return idSet;
    } else {
      return Collections.EMPTY_SET;
    }
  }
  
  public SequenceIterator sequenceIterator() {
    PrimarySeqIterator psi = getPrimarySeqDB().make_PrimarySeqIterator();
    return new SequenceIteratorAdapter(psi);
  }
}
