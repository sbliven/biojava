package org.Biocorba.Seqcore;

import java.lang.*;

public interface _PrimarySeqIterator_Operations extends GNOME._Unknown_Operations {

  public org.Biocorba.Seqcore.PrimarySeq next(org.omg.CORBA.portable.ObjectImpl primarySeqIterator) throws org.Biocorba.Seqcore.EndOfStream, org.Biocorba.Seqcore.UnableToProcess;

  public boolean has_more(org.omg.CORBA.portable.ObjectImpl primarySeqIterator);
}
