package org.Biocorba.Seqcore;

import java.lang.*;

public interface _SeqFeature_Operations extends GNOME._Unknown_Operations {

  public java.lang.String type(org.omg.CORBA.portable.ObjectImpl seqFeature);

  public java.lang.String source(org.omg.CORBA.portable.ObjectImpl seqFeature);

  public java.lang.String seq_primary_id(org.omg.CORBA.portable.ObjectImpl seqFeature);

  public int start(org.omg.CORBA.portable.ObjectImpl seqFeature);

  public int end(org.omg.CORBA.portable.ObjectImpl seqFeature);

  public short strand(org.omg.CORBA.portable.ObjectImpl seqFeature);

  public org.Biocorba.Seqcore.NameValueSet[] qualifiers(org.omg.CORBA.portable.ObjectImpl seqFeature);

  public boolean PrimarySeq_is_available(org.omg.CORBA.portable.ObjectImpl seqFeature);

  public org.Biocorba.Seqcore.PrimarySeq get_PrimarySeq(org.omg.CORBA.portable.ObjectImpl seqFeature) throws org.Biocorba.Seqcore.UnableToProcess;
}
