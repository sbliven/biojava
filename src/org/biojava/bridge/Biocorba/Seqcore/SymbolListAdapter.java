package org.biojava.bridge.Biocorba.Seqcore;

import java.util.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

import org.Biocorba.Seqcore.*;

public class SymbolListAdapter implements SymbolList {
  private AnonymousSeq anonymousSeq;
  private SymbolList resList;
  
  public AnonymousSeq getAnonymousSeq() {
    return anonymousSeq;
  }
  
  public SymbolListAdapter(AnonymousSeq anonymousSeq)
  throws IllegalAlphabetException, IllegalSymbolException, BioException {
    AlphabetManager am = AlphabetManager.instance();
    Alphabet alpha = null;
    SeqType type = anonymousSeq.type();
    if(type == SeqType.DNA) {
      alpha = am.getGappedAlphabet(DNATools.getAmbiguity());
    } else if(type == SeqType.RNA) {
      alpha = am.getGappedAlphabet(am.alphabetForName("RNA"));
    } else if(type == SeqType.PROTEIN) {
      alpha = am.getGappedAlphabet(ProteinTools.getXAlphabet());
    } else {
      throw new IllegalAlphabetException("Could not find alphabet for " + type);
    }
    SymbolParser parser = alpha.getParser("token");

    try {
      resList = parser.parse(anonymousSeq.get_seq());
    } catch (RequestTooLarge rtl) {
      throw new BioException(rtl, "Unable to grap sequence string from CORBA object");
    }
    this.anonymousSeq = anonymousSeq;
  }
  
  public Alphabet alphabet() {
    return resList.alphabet();
  }
  
  public Iterator iterator() {
    return resList.iterator();
  }
  
  public int length() {
    return resList.length();
  }
  
  public Symbol symbolAt(int index)
  throws IndexOutOfBoundsException {
    return resList.symbolAt(index);
  }
  
  public String seqString() {
    return resList.seqString();
  }
  
  public SymbolList subList(int start, int end)
  throws IndexOutOfBoundsException {
    return resList.subList(start, end);
  }
  
  public String subStr(int start, int end)
  throws IndexOutOfBoundsException {
    return resList.subStr(start, end);
  }
  
  public List toList() {
    return resList.toList();
  }
}
