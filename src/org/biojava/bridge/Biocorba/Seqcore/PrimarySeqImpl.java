package org.biojava.bridge.Biocorba.Seqcore;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

import org.Biocorba.Seqcore.*;

/**
 * Implements a CORBA PrimarySeq object by adapting a biojava SymbolList.
 */
public class PrimarySeqImpl
extends AnonymousSeqImpl
implements _PrimarySeq_Operations {
  private String displayID;
  private String primaryID;
  private String accessionNumber;

  public PrimarySeqImpl(SymbolList symList,
                    String displayID, String primaryID, String accessionNumber)
  throws IllegalAlphabetException {
    super(symList);
    this.displayID = displayID;
    this.primaryID = primaryID;
    this.accessionNumber = accessionNumber;
  }
  
  public PrimarySeqImpl(SymbolList symList, String id)
  throws IllegalAlphabetException {
    this(symList, id, id, id);
  }
  
  public PrimarySeqImpl(SymbolList symList)
  throws IllegalAlphabetException {
    this(symList, symList.toString(), symList.toString(), symList.toString());
  }
  
  public String display_id(org.omg.CORBA.portable.ObjectImpl primarySeq) {
    return displayID;
  }
    
  public String primary_id(org.omg.CORBA.portable.ObjectImpl primarySeq) {
    return primaryID;
  }

  public String accession_number(org.omg.CORBA.portable.ObjectImpl primarySeq) {
    return accessionNumber;
  }

  public int version(org.omg.CORBA.portable.ObjectImpl primarySeq) {
    return 0;
  }
}
