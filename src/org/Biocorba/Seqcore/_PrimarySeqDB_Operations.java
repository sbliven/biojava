package org.Biocorba.Seqcore;

import java.lang.*;

public interface _PrimarySeqDB_Operations extends GNOME._Unknown_Operations {

  public java.lang.String database_name(org.omg.CORBA.portable.ObjectImpl primarySeqDB);

  public short database_version(org.omg.CORBA.portable.ObjectImpl primarySeqDB);

  public org.Biocorba.Seqcore.PrimarySeqIterator make_PrimarySeqIterator(org.omg.CORBA.portable.ObjectImpl primarySeqDB);

  public org.Biocorba.Seqcore.PrimarySeq get_PrimarySeq(org.omg.CORBA.portable.ObjectImpl primarySeqDB, java.lang.String primary_id) throws org.Biocorba.Seqcore.UnableToProcess;
}
