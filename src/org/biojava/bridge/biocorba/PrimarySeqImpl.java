package org.biojava.bridge.biocorba;

import GNOME.*;
import Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

public class PrimarySeqImpl
extends UnknownImpl
implements _PrimarySeqOperations {
  private static final SequenceFactory SEQUENCE_FACTORY;
  
  static {
    SEQUENCE_FACTORY = new SimpleSequenceFactory();
  }
  
  private Sequence seq;
  
  public Sequence getSequence() {
    return seq;
  }
  
  public PrimarySeqImpl(Sequence seq) {
    this.seq = seq;
  }
  
  public PrimarySeqImpl(ResidueList resList) {
    this.seq = SEQUENCE_FACTORY.createSequence(resList, null,
                                               resList.toString(), null);
  }
  
  /**
   * Returns a best guess at sequence type, or null.
   * <P>
   * This needs changing to check properly the alphabet name.
   */
  public Bio.SeqType type() {
    String alpha = seq.alphabet().getName();
    if(alpha.equals("DNA")) {
      return Bio.SeqType.DNA;
    } else if(alpha.equals("RNA")) {
      return Bio.SeqType.RNA;
    } else if(alpha.equals("PROTEIN")) {
      return Bio.SeqType.PROTEIN;
    } else {
      return null;
    }
  }
  
  public int length() {
    return seq.length();
  }
  
  public String get_seq() {
    return seq.seqString();
  }
  
  public String get_subseq(int start, int end) {
    return seq.subStr(start, end);
  }
  
  public String display_id() {
    return seq.getName();
  }
  
  public String primary_id() {
    return seq.getName();
  }
  
  public String accession_number() {
    return seq.getName();
  }
  
  public int version() {
    return 0;
  }
  
  public int max_request_length() {
    return Integer.MAX_VALUE;
  }
}
