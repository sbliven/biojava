package org.biojava.bridge.biocorba;

import java.util.*;

import org.biocorba.Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

public class SequenceDBAdapter implements SequenceDB {
  private SeqDB seqDB;
  
  public SeqDB getSeqDB() {
    return seqDB;
  }
  
  public SequenceDBAdapter(SeqDB seqDB) {
    this.seqDB = seqDB;
  }
  
  public Sequence getSequence(String id)
  throws SeqException {
    try {
      Seq seq = getSeqDB().get_Seq(id);
      return new SequenceAdapter(seq);
    } catch (UnableToProcess utp) {
      throw new SeqException(
        utp, "Could not retrieve sequence from CORBA database"
      );
    } catch (IllegalResidueException ire) {
      throw new SeqException(
        ire, "Unable to parse the sequence - contained foreign symbols"
      );
    } catch (IllegalAlphabetException iae) {
      throw new SeqException(
        iae, "Sequence was not DNA, RNA or Protein"
      );
    }
  }
  
  public Set ids() {
    return Collections.EMPTY_SET;
  }
  
  public SequenceIterator sequenceIterator() {
    final PrimarySeqStream si = getSeqDB().make_stream();
    return new SequenceIterator() {
      public boolean hasNext() {
        return si.has_more();
      }
      public Sequence nextSequence()
      throws SeqException, NoSuchElementException {
        try {
          Seq seq = (Seq) si.next();
          return new SequenceAdapter(seq);
        } catch (UnableToProcess utp) {
          throw new SeqException(utp, "Unable to retrieve next sequence");
        } catch (EndOfStream eos) {
          throw new NoSuchElementException("End of stream encountered");
        } catch (SeqException se) {
          throw new SeqException(se, "Unable to retrieve next sequence");
        } catch (IllegalAlphabetException iae) {
          throw new SeqException(iae, "Unable to retrieve next sequence");
        } catch (IllegalResidueException ire) {
          throw new SeqException(ire, "Unable to retrieve next sequence");
        }
      }
    };
  }
}
