package org.biojava.bridge.biocorba;

import org.biocorba.Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

public class PrimarySeqImpl
extends UnknownImpl
implements _PrimarySeq_Operations {
  private ResidueList resList;
  private String name;
  
  public ResidueList getResidueList() {
    return resList;
  }

  public String getName() {
    return name;
  }
  
  public PrimarySeqImpl(ResidueList resList, String name) {
    this.resList = resList;
    this.name = name;
  }
  
  public PrimarySeqImpl(ResidueList resList) {
    this(resList, resList.toString());
  }
  
  /**
   * Returns a best guess at sequence type, or null.
   * <P>
   * This needs changing to check properly the alphabet name.
   */
  public SeqType type(org.omg.CORBA.portable.ObjectImpl primarySeq) {
    String alpha = resList.alphabet().getName();
    if(alpha.equals("DNA")) {
      return SeqType.DNA;
    } else if(alpha.equals("RNA")) {
      return SeqType.RNA;
    } else if(alpha.equals("PROTEIN")) {
      return SeqType.PROTEIN;
    } else {
      return null;
    }
  }
  
  public int length(org.omg.CORBA.portable.ObjectImpl primarySeq) {
    return resList.length();
  }
  
  public String get_seq(org.omg.CORBA.portable.ObjectImpl primarySeq) {
    return resList.seqString();
  }
  
  public String get_subseq(org.omg.CORBA.portable.ObjectImpl primarySeq, int start, int end) {
    return resList.subStr(start, end);
  }
  
  public String display_id(org.omg.CORBA.portable.ObjectImpl primarySeq) {
    return getName();
  }
  
  public String primary_id(org.omg.CORBA.portable.ObjectImpl primarySeq) {
    return getName();
  }
  
  public String accession_number(org.omg.CORBA.portable.ObjectImpl primarySeq) {
    return getName();
  }
  
  public int version(org.omg.CORBA.portable.ObjectImpl primarySeq) {
    return 0;
  }
  
  public int max_request_length(org.omg.CORBA.portable.ObjectImpl primarySeq) {
    return Integer.MAX_VALUE;
  }
}
