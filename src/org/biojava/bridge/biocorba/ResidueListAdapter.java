package org.biojava.bridge.biocorba;

import java.util.Iterator;
import java.util.List;

import org.biocorba.Bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.tools.*;

public class ResidueListAdapter implements ResidueList {
  private PrimarySeq primarySeq;
  private ResidueList resList;
  
  public PrimarySeq getPrimarySeq() {
    return primarySeq;
  }
  
  public ResidueListAdapter(PrimarySeq primarySeq)
  throws IllegalAlphabetException, IllegalResidueException, SeqException {
    this.primarySeq = primarySeq;

    String alphaName = null;
    
    SeqType seqType = primarySeq.type();
    if(seqType == SeqType.DNA) {
      alphaName = "DNA";
    } else if(seqType == SeqType.RNA) {
      alphaName = "RNA";
    } else if(seqType == SeqType.PROTEIN) {
      alphaName = "PROTEIN";
    }
    
    if(alphaName == null) {
      throw new IllegalAlphabetException(
        "PrimarySeq type must be DNA, RNA or Protein"
      );
    } else {
      Alphabet alphabet = AlphabetManager.instance().alphabetForName(alphaName);
      ResidueParser resParser = alphabet.getParser("symbol");
      try {
        resList = resParser.parse(primarySeq.get_seq());
      } catch (RequestTooLarge rtl) {
        throw new SeqException(
          rtl, "Unable to get the sequence string from the corba object."
        );
      }
    }
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
  
  public ResidueList subList(int start, int end)
  throws IndexOutOfBoundsException {
    return resList.subList(start, end);
  }
  
  public List toList() {
    return resList.toList();
  }
  
  public String seqString() {
    return resList.seqString();
  }
  
  public String subStr(int start, int end)
  throws IndexOutOfBoundsException {
    return resList.subStr(start, end);
  }
}
