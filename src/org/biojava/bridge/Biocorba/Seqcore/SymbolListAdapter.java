package org.biojava.bridge.Biocorba.Seqcore;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

import org.Biocorba.Seqcore.*;

public class SymbolListAdapter implements SymbolList {
  private AnonymousSeq anonymousSeq;
  private SymbolList symList;
  
  public AnonymousSeq getAnonymousSeq() {
    return anonymousSeq;
  }
  
  public SymbolListAdapter(AnonymousSeq anonymousSeq)
  throws IllegalAlphabetException, IllegalSymbolException, BioException {
    Alphabet alpha = null;
    SeqType type = anonymousSeq.type();
    if(type == SeqType.DNA) {
      alpha = DNATools.getDNA();
    } else if(type == SeqType.RNA) {
      alpha = AlphabetManager.alphabetForName("RNA");
    } else if(type == SeqType.PROTEIN) {
      alpha = ProteinTools.getAlphabet();
    } else {
      throw new IllegalAlphabetException("Could not find alphabet for " + type);
    }
    SymbolParser parser = alpha.getParser("token");

    try {
      symList = parser.parse(anonymousSeq.get_seq());
    } catch (RequestTooLarge rtl) {
      throw new BioException(rtl, "Unable to grap sequence string from CORBA object");
    }
    this.anonymousSeq = anonymousSeq;
  }
  
  public Alphabet getAlphabet() {
    return symList.getAlphabet();
  }
  
  public Iterator iterator() {
    return symList.iterator();
  }
  
  public int length() {
    return symList.length();
  }
  
  public Symbol symbolAt(int index)
  throws IndexOutOfBoundsException {
    return symList.symbolAt(index);
  }
  
  public String seqString() {
    return symList.seqString();
  }
  
  public SymbolList subList(int start, int end)
  throws IndexOutOfBoundsException {
    return symList.subList(start, end);
  }
  
  public String subStr(int start, int end)
  throws IndexOutOfBoundsException {
    return symList.subStr(start, end);
  }
  
  public List toList() {
    return symList.toList();
  }

  public void edit(Edit edit) throws ChangeVetoException {
    throw new ChangeVetoException("Can't modify SymbolList");
  }
  
  public void addChangeListener(ChangeListener cl) {}
  public void addChangeListener(ChangeListener cl, ChangeType ct) {}
  public void removeChangeListener(ChangeListener cl) {}
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
}
