package org.biojava.bridge.Biocorba.Seqcore;

import org.biojava.bio.symbol.*;

import org.Biocorba.Seqcore.*;
import org.biojava.bridge.GNOME.*;

public class AnonymousSeqImpl
extends UnknownImpl
implements _AnonymousSeq_Operations {
  private SymbolList symList;
  
  public SymbolList getSymbolList() {
    return symList;
  }
  
  private SeqType seqType;
  
  public AnonymousSeqImpl(SymbolList symList) throws IllegalAlphabetException {
    String alphaName = symList.getAlphabet().getName();
    seqType = null;
    if(alphaName.startsWith("DNA")) {
      seqType = SeqType.DNA;
    } else if(alphaName.startsWith("RNA")) {
      seqType = SeqType.RNA;
    } else if(alphaName.startsWith("PROTEIN")) {
      seqType = SeqType.PROTEIN;
    } else {
      throw new IllegalAlphabetException("Can only serve DNA, RNA or PROTEIN");
    }
    
    this.symList = symList;
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
