package org.Biocorba.Seqcore;

import java.lang.*;

public interface _SeqDB_Operations extends org.Biocorba.Seqcore._PrimarySeqDB_Operations {

  public org.Biocorba.Seqcore.Seq get_Seq(org.omg.CORBA.portable.ObjectImpl seqDB, java.lang.String primary_id) throws org.Biocorba.Seqcore.UnableToProcess;

  public java.lang.String[] get_primaryidList(org.omg.CORBA.portable.ObjectImpl seqDB);
}
