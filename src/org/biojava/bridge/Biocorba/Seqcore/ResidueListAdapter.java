package org.biojava.bridge.Biocorba.Seqcore;

import java.util.*;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;
import org.Biocorba.Seqcore.*;

public class ResidueListAdapter implements ResidueList {
  private AnonymousSeq anonymousSeq;
  private ResidueList resList;
  
  public AnonymousSeq getAnonymousSeq() {
    return anonymousSeq;
  }
  
  public ResidueListAdapter(AnonymousSeq anonymousSeq)
  throws IllegalAlphabetException, IllegalResidueException, SeqException {
    AlphabetManager am = AlphabetManager.instance();
    Alphabet alpha = null;
    SeqType type = anonymousSeq.type();
    if(type == SeqType.DNA) {
      alpha = am.alphabetForName("DNA");
    } else if(type == SeqType.RNA) {
      alpha = am.alphabetForName("RNA");
    } else if(type == SeqType.PROTEIN) {
      alpha = am.alphabetForName("PROTEIN");
    } else {
      throw new IllegalAlphabetException("Could not find alphabet for " + type);
    }
    ResidueParser parser = alpha.getParser("symbol");

    try {
      resList = parser.parse(anonymousSeq.get_seq());
    } catch (RequestTooLarge rtl) {
      throw new SeqException(rtl, "Unable to grap sequence string from CORBA object");
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
  
  public Residue residueAt(int index)
  throws IndexOutOfBoundsException {
    return resList.residueAt(index);
  }
  
  public String seqString() {
    return resList.seqString();
  }
  
  public ResidueList subList(int start, int end)
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
