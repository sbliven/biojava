package org.Biocorba.Seqcore;

import java.lang.*;

public interface _AnonymousSeq_Operations extends GNOME._Unknown_Operations {

  public org.Biocorba.Seqcore.SeqType type(org.omg.CORBA.portable.ObjectImpl anonymousSeq);

  public int length(org.omg.CORBA.portable.ObjectImpl anonymousSeq);

  public java.lang.String get_seq(org.omg.CORBA.portable.ObjectImpl anonymousSeq) throws org.Biocorba.Seqcore.RequestTooLarge;

  public java.lang.String get_subseq(org.omg.CORBA.portable.ObjectImpl anonymousSeq, int start, int end) throws org.Biocorba.Seqcore.OutOfRange, org.Biocorba.Seqcore.RequestTooLarge;

  public int max_request_length(org.omg.CORBA.portable.ObjectImpl anonymousSeq);
}
