package org.biojava.bridge.Biocorba.Seqcore;

import org.biojava.bio.seq.*;

import org.Biocorba.Seqcore.*;
import org.biojava.bridge.GNOME.*;

public class AnonymousSeqImpl
extends UnknownImpl
implements _AnonymousSeq_Operations {
  private SymbolList resList;
  
  public SymbolList getSymbolList() {
    return resList;
  }
  
  private SeqType seqType;
  
  public AnonymousSeqImpl(SymbolList resList) throws IllegalAlphabetException {
    String alphaName = resList.alphabet().getName();
    seqType = null;
    if(alphaName.equals("DNA")) {
      seqType = SeqType.DNA;
    } else if(alphaName.equals("RNA")) {
      seqType = SeqType.RNA;
    } else if(alphaName.equals("PROTEIN")) {
      seqType = SeqType.PROTEIN;
    } else {
      throw new IllegalAlphabetException("Can only serve DNA, RNA or PROTEIN");
    }
    
    this.resList = resList;
  }
  
  public SeqType type(org.omg.CORBA.portable.ObjectImpl anonymousSeq) {
    return seqType;
  }

  public int length(org.omg.CORBA.portable.ObjectImpl anonymousSeq) {
    return getSymbolList().length();
  }

  public String get_seq(org.omg.CORBA.portable.ObjectImpl anonymousSeq)
  throws RequestTooLarge {
    return getSymbolList().seqString();
  }

  public String get_subseq(org.omg.CORBA.portable.ObjectImpl anonymousSeq, int start, int end)
  throws OutOfRange, RequestTooLarge {
    try {
      return getSymbolList().subStr(start, end);
    } catch (IndexOutOfBoundsException e) {
      throw new OutOfRange(e.toString());
    }
  }

  public int max_request_length(org.omg.CORBA.portable.ObjectImpl anonymousSeq) {
    return Integer.MAX_VALUE;
  }
}
